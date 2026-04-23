"""
Wrapper that invokes Claude Code CLI for mod management operations.
All file I/O for the Minecraft server and repo happens here.
"""

import asyncio
import json
import os
import re
import shutil
from pathlib import Path

REPO_DIR = "/opt/minecraft-dev/minecraft"
MODS_SOURCE_DIR = f"{REPO_DIR}/mods"
SERVER_DIR = "/var/opt/minecraft/crafty/crafty-4/servers/97dc0db9-50ed-4ecf-a28b-b4f7d2fe0908"
SERVER_MODS_DIR = f"{SERVER_DIR}/mods"
SERVER_MODS_DISABLED_DIR = f"{SERVER_DIR}/mods-disabled"
LIFESTEAL_TEMPLATE = f"{MODS_SOURCE_DIR}/lifesteal"


# ---------------------------------------------------------------------------
# Low-level helpers
# ---------------------------------------------------------------------------

async def _run_claude(prompt: str, cwd: str = REPO_DIR, timeout: int = 1200) -> tuple[str, str, int]:
    """Run Claude Code in non-interactive print mode."""
    env = {**os.environ}
    proc = await asyncio.create_subprocess_exec(
        "claude", "--dangerously-skip-permissions", "-p", prompt,
        cwd=cwd,
        stdout=asyncio.subprocess.PIPE,
        stderr=asyncio.subprocess.PIPE,
        env=env,
    )
    try:
        stdout, stderr = await asyncio.wait_for(proc.communicate(), timeout=timeout)
    except asyncio.TimeoutError:
        proc.kill()
        await proc.communicate()
        raise TimeoutError(f"Claude Code timed out after {timeout}s")
    return stdout.decode(errors="replace"), stderr.decode(errors="replace"), proc.returncode


async def _run_shell(cmd: str, cwd: str = REPO_DIR, timeout: int = 120) -> tuple[str, str, int]:
    """Run a shell command and return (stdout, stderr, returncode)."""
    proc = await asyncio.create_subprocess_shell(
        cmd,
        cwd=cwd,
        stdout=asyncio.subprocess.PIPE,
        stderr=asyncio.subprocess.PIPE,
    )
    try:
        stdout, stderr = await asyncio.wait_for(proc.communicate(), timeout=timeout)
    except asyncio.TimeoutError:
        proc.kill()
        await proc.communicate()
        raise TimeoutError(f"Shell command timed out: {cmd}")
    return stdout.decode(errors="replace"), stderr.decode(errors="replace"), proc.returncode


# ---------------------------------------------------------------------------
# Mod name helpers
# ---------------------------------------------------------------------------

_STOP_WORDS = {
    "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for",
    "of", "with", "by", "that", "this", "mod", "minecraft", "when", "player",
    "players", "server", "make", "create", "add", "new", "which", "is", "are",
    "each", "every", "other",
}


def generate_mod_names(description: str) -> dict:
    """
    Derive consistent identifier variants from a free-text description.

    Returns dict with keys:
      mod_id       — snake_case directory/archive name  (e.g. speed_boost)
      package      — lowercase no-separator Java package (e.g. speedboost)
      class_prefix — CamelCase prefix for Java classes  (e.g. SpeedBoost)
    """
    words = re.findall(r"[a-zA-Z]+", description.lower())
    meaningful = [w for w in words if w not in _STOP_WORDS][:3]
    if not meaningful:
        meaningful = (words[:2] if words else ["custom"])

    mod_id = "_".join(meaningful)
    package = "".join(meaningful)
    class_prefix = "".join(w.capitalize() for w in meaningful)
    return {"mod_id": mod_id, "package": package, "class_prefix": class_prefix}


# ---------------------------------------------------------------------------
# !create — two-phase: analysis then build
# ---------------------------------------------------------------------------

async def analyze_mod_request(description: str) -> dict:
    """
    Phase 1 of !create: check for similar mods and surface clarifying questions.

    Returns dict:
      has_similar      — bool
      similar_mod_name — str | None
      questions        — list[str]  (max 3)
    """
    prompt = f"""You are analyzing a request to create a new Minecraft Fabric mod.

Repository: {REPO_DIR}
Existing mods directory: {MODS_SOURCE_DIR}

User's mod request: {description}

Tasks:
1. List all subdirectories in {MODS_SOURCE_DIR}/ (ignore the 'external' folder).
2. Check if any existing mod is functionally similar to what the user described.
3. Generate up to 3 clarifying questions ONLY IF the mod's core game mechanic is ambiguous
   (e.g. unclear trigger events, unclear scope, mutually exclusive interpretations).
   Do NOT ask if the description is already clear enough to implement.

Output ONLY valid JSON — no markdown, no prose, no code fences:
{{
  "has_similar": true,
  "similar_mod_name": "lifesteal",
  "questions": ["Question 1?", "Question 2?"]
}}

Rules:
- "has_similar" is true only when an existing mod implements the SAME core mechanic.
- "similar_mod_name" is null when has_similar is false.
- "questions" is [] when no clarification is needed.
"""
    stdout, _stderr, _rc = await _run_claude(prompt, timeout=120)

    json_match = re.search(r"\{.*\}", stdout, re.DOTALL)
    if json_match:
        try:
            data = json.loads(json_match.group())
            return {
                "has_similar": bool(data.get("has_similar", False)),
                "similar_mod_name": data.get("similar_mod_name"),
                "questions": list(data.get("questions", []))[:3],
            }
        except (json.JSONDecodeError, KeyError):
            pass

    # Safe fallback — treat as no duplicate, no questions needed.
    return {"has_similar": False, "similar_mod_name": None, "questions": []}


async def build_mod(
    description: str,
    discord_user: str,
    questions: list[str],
    answers: list[str],
    progress_cb=None,
) -> str:
    """
    Phase 2 of !create: build, test, fix (up to 5 tries), git-checkpoint, deploy.

    progress_cb — optional async callable(str) for streaming progress messages.
    Returns a human-readable result string.
    """
    names = generate_mod_names(description)
    mod_id = names["mod_id"]
    package = names["package"]
    class_prefix = names["class_prefix"]
    mod_dir = f"{MODS_SOURCE_DIR}/{mod_id}"

    async def progress(msg: str):
        if progress_cb:
            await progress_cb(msg)

    await progress(f"Building mod `{mod_id}` — this takes a few minutes.")

    qa_section = ""
    if questions and answers:
        pairs = [f"Q: {q}\nA: {a}" for q, a in zip(questions, answers)]
        qa_section = "\n\n## Clarifications from the user\n" + "\n\n".join(pairs)

    prompt = f"""You are implementing a new Minecraft Fabric 1.21.4 server-side mod.

## Mod specification
{description}{qa_section}

## Identifiers to use everywhere (do not invent different ones)
- mod_id / directory name : {mod_id}
- Java package             : com.{package}
- Class name prefix        : {class_prefix}   (e.g. main class is {class_prefix}Mod)

## File locations
- Create mod at            : {mod_dir}/
- Template reference       : {LIFESTEAL_TEMPLATE}/   (study its structure; do NOT copy lifesteal logic)
- Deploy jar to            : {SERVER_MODS_DIR}/

## Gradle versions (copy exactly from lifesteal gradle.properties)
minecraft_version=1.21.4
yarn_mappings=1.21.4+build.8
loader_version=0.19.2
loom_version=1.10.5
fabric_version=0.119.4+1.21.4
mod_version=1.0.0
maven_group=com.{package}
archives_base_name={mod_id}
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true

## Architecture requirements (same pattern as lifesteal)
1. Pure game logic with NO Minecraft imports → placed in com.{package}.logic.*
2. Thin Mixin wrappers in com.{package}.mixin.*
3. Entry point: com.{package}.{class_prefix}Mod  (implements ModInitializer)
4. JUnit 5 unit tests for ALL logic classes (no Minecraft mocks needed)

## Build & test procedure
1. Create all files under {mod_dir}/
2. Run: cd {mod_dir} && ./gradlew test
3. If tests FAIL: read the error output, fix the code, run again. Repeat up to 5 times total.
4. If tests still fail after 5 attempts: stop and output the FAILURE line below.
5. If tests PASS: run ./gradlew build
6. Find the built jar: ls {mod_dir}/build/libs/*.jar
7. Copy it: cp {mod_dir}/build/libs/{mod_id}-*.jar {SERVER_MODS_DIR}/

## Final output (REQUIRED — last line of your response, nothing after it)
Output EXACTLY one of these lines as the very last line:
  SUCCESS: {mod_id}
  FAILURE: <clear explanation of what failed and what the user should change in their description>
"""

    await progress("Claude is generating the mod code and running tests...")
    stdout, stderr, _rc = await _run_claude(prompt, cwd=REPO_DIR, timeout=1200)

    # Parse result
    last_lines = stdout.strip().split("\n")[-5:]
    status_line = next(
        (l for l in reversed(last_lines) if l.startswith("SUCCESS:") or l.startswith("FAILURE:")),
        None,
    )

    if status_line and status_line.startswith("SUCCESS:"):
        await progress("Tests passed — creating git checkpoint...")
        git_msg = f"Checkpoint: {mod_id} created by {discord_user}"
        _out, git_err, git_rc = await _run_shell(
            f'git add -A && git commit -m "{git_msg}"',
            cwd=REPO_DIR,
        )
        if git_rc != 0:
            await progress(f"Warning: git commit failed — {git_err.strip()[:200]}")
        return (
            f"Mod **{mod_id}** deployed successfully!\n"
            f"The Minecraft server needs a restart to load it.\n"
            f"Git checkpoint created: _{git_msg}_"
        )

    if status_line and status_line.startswith("FAILURE:"):
        reason = status_line[len("FAILURE:"):].strip()
        return f"Could not build **{mod_id}** after up to 5 attempts.\n\n**Reason:** {reason}"

    # No recognizable status line — surface raw tail for debugging
    tail = "\n".join(last_lines)
    return (
        f"Build of **{mod_id}** finished with an unexpected result.\n"
        f"Last output:\n```\n{tail}\n```"
    )


# ---------------------------------------------------------------------------
# !list
# ---------------------------------------------------------------------------

async def list_mods() -> str:
    """Return a formatted string listing active and disabled mods."""
    os.makedirs(SERVER_MODS_DISABLED_DIR, exist_ok=True)

    active = sorted(Path(SERVER_MODS_DIR).glob("*.jar"), key=lambda p: p.name)
    disabled = sorted(Path(SERVER_MODS_DISABLED_DIR).glob("*.jar"), key=lambda p: p.name)

    lines = ["**Active mods:**"]
    lines += [f"  ✅ {p.name}" for p in active] or ["  (none)"]
    lines += ["", "**Disabled mods:**"]
    lines += [f"  ❌ {p.name}" for p in disabled] or ["  (none)"]
    return "\n".join(lines)


# ---------------------------------------------------------------------------
# !remove
# ---------------------------------------------------------------------------

async def remove_mod(modname: str) -> str:
    """Move a matching jar from mods/ to mods-disabled/."""
    os.makedirs(SERVER_MODS_DISABLED_DIR, exist_ok=True)

    matches = list(Path(SERVER_MODS_DIR).glob(f"*{modname}*.jar"))
    if not matches:
        return f"No active mod matching **{modname}** found."

    jar = matches[0]
    dest = Path(SERVER_MODS_DISABLED_DIR) / jar.name
    shutil.move(str(jar), str(dest))
    return (
        f"Mod **{jar.name}** disabled — moved to `mods-disabled/`.\n"
        f"The server needs a restart. Use `!activate {modname}` to re-enable."
    )


# ---------------------------------------------------------------------------
# !activate
# ---------------------------------------------------------------------------

async def activate_mod(modname: str) -> str:
    """Move a matching jar from mods-disabled/ back to mods/."""
    os.makedirs(SERVER_MODS_DISABLED_DIR, exist_ok=True)

    matches = list(Path(SERVER_MODS_DISABLED_DIR).glob(f"*{modname}*.jar"))
    if not matches:
        return f"No disabled mod matching **{modname}** found."

    jar = matches[0]
    dest = Path(SERVER_MODS_DIR) / jar.name
    shutil.move(str(jar), str(dest))
    return (
        f"Mod **{jar.name}** re-enabled — moved back to `mods/`.\n"
        f"The server needs a restart to load it."
    )


# ---------------------------------------------------------------------------
# !restore
# ---------------------------------------------------------------------------

async def restore_commit(commit_hash: str) -> str:
    """Hard-reset the repo to a specific git commit (saves a checkpoint first)."""
    if not re.fullmatch(r"[0-9a-fA-F]{4,40}", commit_hash):
        return f"Invalid commit hash: `{commit_hash}`"

    # Save current state so nothing is lost.
    await _run_shell(
        f'git add -A && git commit -m "Checkpoint: before restore to {commit_hash}" --allow-empty',
        cwd=REPO_DIR,
    )

    stdout, stderr, rc = await _run_shell(
        f"git reset --hard {commit_hash}", cwd=REPO_DIR
    )
    if rc != 0:
        return f"Restore failed:\n```\n{stderr[:500]}\n```"

    return f"Repo restored to `{commit_hash}`.\n```\n{stdout.strip()}\n```"


# ---------------------------------------------------------------------------
# !history
# ---------------------------------------------------------------------------

async def get_history() -> str:
    """Return the last 10 git commits as a formatted string."""
    stdout, stderr, rc = await _run_shell(
        "git log --oneline -10 --pretty=format:\"`%h` — %s\"",
        cwd=REPO_DIR,
    )
    if rc != 0:
        return f"Error fetching history:\n```\n{stderr[:300]}\n```"
    return f"**Recent commits:**\n{stdout.strip()}"


# ---------------------------------------------------------------------------
# !status
# ---------------------------------------------------------------------------

async def get_server_status() -> str:
    """Check whether the Minecraft server is listening on port 25565."""
    stdout, _stderr, rc = await _run_shell(
        "python3 -c \""
        "import socket, sys; s=socket.socket(); s.settimeout(2); "
        "sys.exit(0 if s.connect_ex(('localhost', 25565)) == 0 else 1)"
        "\""
    )
    if rc == 0:
        return "Minecraft server is **online** (port 25565 is reachable)."
    return "Minecraft server is **offline** (port 25565 is not reachable)."


# ---------------------------------------------------------------------------
# !restart
# ---------------------------------------------------------------------------

async def restart_server() -> str:
    """Restart the Minecraft server via Crafty Controller REST API."""
    import ssl
    import urllib.request

    crafty_url = os.environ.get("CRAFTY_URL", "https://localhost:8443")
    username = os.environ["CRAFTY_USERNAME"]
    password = os.environ["CRAFTY_PASSWORD"]
    server_id = os.environ.get("CRAFTY_SERVER_ID", "97dc0db9-50ed-4ecf-a28b-b4f7d2fe0908")

    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE

    loop = asyncio.get_event_loop()

    def _api_call():
        # 1 — Login
        login_data = json.dumps({"username": username, "password": password}).encode()
        req = urllib.request.Request(
            f"{crafty_url}/api/v2/auth/login",
            data=login_data,
            headers={"Content-Type": "application/json"},
            method="POST",
        )
        with urllib.request.urlopen(req, context=ctx, timeout=10) as resp:
            token = json.loads(resp.read())["data"]["token"]

        # 2 — Restart
        req2 = urllib.request.Request(
            f"{crafty_url}/api/v2/servers/{server_id}/action/restart",
            data=b"",
            headers={"Authorization": f"Bearer {token}"},
            method="POST",
        )
        with urllib.request.urlopen(req2, context=ctx, timeout=10) as resp2:
            return json.loads(resp2.read())

    try:
        result = await loop.run_in_executor(None, _api_call)
        return "Server restart initiated. It will be back online in ~30 seconds."
    except Exception as exc:
        return f"Failed to restart server: {exc}"
