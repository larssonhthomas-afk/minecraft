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
SERVER_DIR = "/opt/minecraft-dev/minecraft/server"
SERVER_MODS_DIR = f"{SERVER_DIR}/mods"
SERVER_MODS_DISABLED_DIR = f"{SERVER_DIR}/mods-disabled"
LIFESTEAL_JSON = f"{SERVER_DIR}/world/lifesteal.json"
VANILLA_MAX_HEALTH = 20.0
LIFESTEAL_TEMPLATE = f"{MODS_SOURCE_DIR}/lifesteal"


# ---------------------------------------------------------------------------
# Low-level helpers
# ---------------------------------------------------------------------------

def _read_source_files(mod_dir: Path) -> str:
    """Read all Java source files and return them as a formatted string."""
    parts = []
    for f in sorted(mod_dir.rglob("*.java")):
        rel = f.relative_to(mod_dir)
        parts.append(f"### {rel}\n```java\n{f.read_text(errors='replace')}\n```")
    return "\n\n".join(parts)

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
# Mod directory lookup
# ---------------------------------------------------------------------------

def _find_mod_dir(modname: str) -> Path | None:
    """Find a mod source directory by exact or partial name match."""
    # Strip .jar extension and version suffix (e.g. "ranked_smp_rank-1.0.0.jar" → "ranked_smp_rank")
    if modname.lower().endswith(".jar"):
        modname = modname[:-4]
    mods_path = Path(MODS_SOURCE_DIR)
    # Exact match first
    exact = mods_path / modname
    if exact.is_dir() and (exact / "src").exists():
        return exact
    # Partial match (case-insensitive, both directions)
    needle = modname.lower()
    for d in sorted(mods_path.iterdir()):
        if d.is_dir() and (d / "src").exists():
            dn = d.name.lower()
            if needle in dn or dn in needle:
                return d
    return None


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
3. Generate a brief implementation plan (2-5 bullet points) summarising what the mod will do.
4. Add up to 2 clarifying questions IF anything is ambiguous or has multiple valid interpretations.
5. ALWAYS append "build?" as the very last question so the user can confirm the plan before building starts.

Output ONLY valid JSON — no markdown, no prose, no code fences:
{{
  "has_similar": false,
  "similar_mod_name": null,
  "questions": ["Plan:\n• Bullet 1\n• Bullet 2\n• Bullet 3\nNågot att ändra?", "build?"]
}}

Rules:
- "has_similar" is true only when an existing mod implements the SAME core mechanic.
- "similar_mod_name" is null when has_similar is false.
- The first question is ALWAYS the plan summary — format it as "Plan:\n• ...\nNågot att ändra?".
- "build?" is ALWAYS the last question, never omitted.
- Max 3 questions total (plan + up to 1 clarifier + build?).
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
        else:
            await progress("Pushing to GitHub...")
            _out, push_err, push_rc = await _run_shell("git push", cwd=REPO_DIR)
            if push_rc != 0:
                await progress(f"Warning: git push failed — {push_err.strip()[:200]}")
        return (
            f"Mod **{mod_id}** deployed successfully!\n"
            f"The Minecraft server needs a restart to load it.\n"
            f"Git checkpoint created and pushed: _{git_msg}_"
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
# !change — two-phase: analysis then modify
# ---------------------------------------------------------------------------

async def analyze_change_request(modname: str, description: str) -> dict:
    """
    Phase 1 of !change: find the mod and plan the changes.

    Returns dict:
      found    — bool
      mod_dir  — str | None
      questions — list[str]  (plan summary + optional clarifier + "build?")
    """
    mod_dir = _find_mod_dir(modname)
    if mod_dir is None:
        return {"found": False, "mod_dir": None, "questions": []}

    source_files = _read_source_files(mod_dir)

    prompt = f"""You are analyzing a request to modify an existing Minecraft Fabric mod.

Mod directory: {mod_dir}
Change request: {description}

## Existing source files
{source_files}

Tasks:
1. Create a brief plan (2-5 bullet points) describing exactly what will change.
2. Add up to 1 clarifying question if something is ambiguous.
3. ALWAYS append "build?" as the very last question.

Output ONLY valid JSON — no markdown, no prose, no code fences:
{{
  "questions": ["Plan:\n• Bullet 1\n• Bullet 2\nNågot att ändra?", "build?"]
}}

Rules:
- The first question is ALWAYS the plan summary.
- "build?" is ALWAYS the last question, never omitted.
- Max 3 questions total (plan + up to 1 clarifier + build?).
"""
    stdout, _stderr, _rc = await _run_claude(prompt, timeout=600)
    json_match = re.search(r"\{.*\}", stdout, re.DOTALL)
    if json_match:
        try:
            data = json.loads(json_match.group())
            return {
                "found": True,
                "mod_dir": str(mod_dir),
                "questions": list(data.get("questions", []))[:3],
            }
        except (json.JSONDecodeError, KeyError):
            pass
    return {"found": True, "mod_dir": str(mod_dir), "questions": ["build?"]}


def _ask_claude_for_edits(source_files: str, description: str, qa_section: str, error_output: str = "") -> str:
    """
    Ask claude -p for search/replace instructions (not full file contents).
    Small output → fast generation.
    """
    error_section = (
        f"\n\n## Test failure output — fix these errors\n```\n{error_output}\n```"
        if error_output else ""
    )
    return f"""You are a Minecraft Fabric 1.21.4 mod developer.

## Existing source files
{source_files}

## Change request
{description}{qa_section}{error_section}

## Task
Implement the change request. Output ONLY the minimal edits needed as JSON.
Preserve the existing architecture:
- Pure logic stays in logic/* (no Minecraft imports)
- Thin Mixin wrappers stay in mixin/*
- Update or add JUnit 5 tests for any changed logic

For EXISTING files: use "replacements" — list of exact search/replace pairs.
  Each "old" string must be unique within the file (include enough context lines).
For NEW files only: use "content" with the full file content.

Output ONLY valid JSON, no markdown, no code fences:
{{
  "files": [
    {{
      "path": "src/main/java/com/example/Foo.java",
      "replacements": [
        {{"old": "exact string to find", "new": "replacement string"}}
      ]
    }},
    {{
      "path": "src/main/java/com/example/NewFile.java",
      "content": "package com.example;\\n..."
    }}
  ]
}}
"""


async def change_mod(
    modname: str,
    mod_dir: str,
    description: str,
    discord_user: str,
    questions: list[str],
    answers: list[str],
    progress_cb=None,
) -> str:
    """
    Phase 2 of !change: apply changes, rebuild, redeploy.

    Claude outputs file contents as JSON (no tool calls → fast).
    Python writes the files, runs tests/build, and deploys.
    """
    async def progress(msg: str):
        if progress_cb:
            await progress_cb(msg)

    await progress(f"Modifying mod `{modname}` — this takes a few minutes.")

    qa_section = ""
    if questions and answers:
        pairs = [f"Q: {q}\nA: {a}" for q, a in zip(questions, answers)]
        qa_section = "\n\n## Clarifications from the user\n" + "\n\n".join(pairs)

    def apply_edits(stdout: str, base: Path) -> int:
        """Parse JSON edits from stdout and apply to disk. Returns number of files touched."""
        match = re.search(r"\{.*\}", stdout, re.DOTALL)
        if not match:
            return 0
        try:
            data = json.loads(match.group())
        except json.JSONDecodeError:
            return 0
        count = 0
        for entry in data.get("files", []):
            path = base / entry["path"]
            path.parent.mkdir(parents=True, exist_ok=True)
            if "content" in entry:
                path.write_text(entry["content"], encoding="utf-8")
                count += 1
            elif "replacements" in entry and path.exists():
                text = path.read_text(encoding="utf-8")
                for rep in entry["replacements"]:
                    text = text.replace(rep["old"], rep["new"], 1)
                path.write_text(text, encoding="utf-8")
                count += 1
        return count

    # Step 1: Ask Claude for edits (text output only — no subprocess tool calls)
    await progress("Claude is planning and writing changes...")
    source = _read_source_files(Path(mod_dir))
    prompt = _ask_claude_for_edits(source, description, qa_section)
    stdout, _stderr, _rc = await _run_claude(prompt, cwd=REPO_DIR, timeout=120)
    written = apply_edits(stdout, Path(mod_dir))
    if written == 0:
        return f"Claude returnerade inga filändringar.\nOutput:\n```\n{stdout[-500:]}\n```"

    # Step 2: Test/fix loop — Python runs tests, Claude only fixes on failure
    MAX_ATTEMPTS = 5
    for attempt in range(1, MAX_ATTEMPTS + 1):
        await progress(f"Running tests (attempt {attempt}/{MAX_ATTEMPTS})...")
        test_out, test_err, test_rc = await _run_shell(
            "./gradlew test 2>&1", cwd=mod_dir, timeout=120
        )
        combined = (test_out + test_err).strip()
        if test_rc == 0:
            break
        if attempt == MAX_ATTEMPTS:
            return (
                f"Kunde inte ändra **{modname}** — tester misslyckas efter {MAX_ATTEMPTS} försök.\n\n"
                f"**Sista fel:**\n```\n{combined[-800:]}\n```"
            )
        await progress(f"Tests failed — asking Claude to fix (attempt {attempt})...")
        source = _read_source_files(Path(mod_dir))
        fix_prompt = _ask_claude_for_edits(source, description, qa_section, combined[-2000:])
        stdout, _stderr, _rc = await _run_claude(fix_prompt, cwd=REPO_DIR, timeout=120)
        apply_edits(stdout, Path(mod_dir))

    # Step 3: Build
    await progress("Tests passed — building jar...")
    build_out, build_err, build_rc = await _run_shell(
        f"cd {mod_dir} && ./gradlew build 2>&1", cwd=mod_dir, timeout=120
    )
    if build_rc != 0:
        combined = (build_out + build_err).strip()
        return f"Build misslyckades.\n```\n{combined[-800:]}\n```"

    # Step 4: Deploy jar
    jars = list(Path(mod_dir).glob("build/libs/*.jar"))
    jars = [j for j in jars if "sources" not in j.name]
    if not jars:
        return "Build lyckades men ingen jar hittades i build/libs/."
    jar = max(jars, key=lambda j: j.stat().st_mtime)
    dest = Path(SERVER_MODS_DIR) / jar.name
    shutil.copy2(str(jar), str(dest))

    # Step 5: Git checkpoint
    await progress("Deploying and creating git checkpoint...")
    git_msg = f"Checkpoint: {modname} changed by {discord_user}"
    _out, git_err, git_rc = await _run_shell(
        f'git add -A && git commit -m "{git_msg}"', cwd=REPO_DIR
    )
    if git_rc != 0:
        await progress(f"Warning: git commit failed — {git_err.strip()[:200]}")
    else:
        _out, push_err, push_rc = await _run_shell("git push", cwd=REPO_DIR)
        if push_rc != 0:
            await progress(f"Warning: git push failed — {push_err.strip()[:200]}")

    return (
        f"Mod **{modname}** uppdaterad!\n"
        f"Servern behöver startas om för att ladda ändringarna.\n"
        f"Git checkpoint skapad och pushad: _{git_msg}_"
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

async def _checkout_path(commit_hash: str, path: str) -> tuple[str, str, int]:
    """Restore a specific path from a commit without touching the rest of the repo."""
    return await _run_shell(
        f"git checkout {commit_hash} -- {path}", cwd=REPO_DIR
    )


async def _build_and_deploy_mod(mod_dir: Path) -> str:
    """Build a mod from source and deploy its jar. Returns status line."""
    build_out, build_err, build_rc = await _run_shell(
        "./gradlew build -x test", cwd=str(mod_dir), timeout=300
    )
    if build_rc != 0:
        return f"❌ {mod_dir.name}: build misslyckades"
    jars = [j for j in (mod_dir / "build" / "libs").glob("*.jar")
            if "sources" not in j.name and "dev" not in j.name]
    if not jars:
        return f"⚠️ {mod_dir.name}: ingen jar hittades"
    for old in Path(SERVER_MODS_DIR).glob(f"{mod_dir.name}*.jar"):
        old.unlink(missing_ok=True)
    shutil.copy(str(jars[0]), SERVER_MODS_DIR)
    return f"✅ {mod_dir.name}"


async def _sync_server_configs() -> None:
    """Copy tracked server configs from repo to live server dir."""
    for cfg in ["server.properties", "ops.json", "whitelist.json", "eula.txt"]:
        src = Path(REPO_DIR) / "server" / cfg
        dst = Path(SERVER_DIR) / cfg
        if src.exists():
            shutil.copy(str(src), str(dst))


async def restore_commit(commit_hash: str, target: str = "all") -> str:
    """
    Restore to a specific commit.

    target:
      "all"      — full repo reset + rebuild all mods + sync configs + restart
      "server"   — restore only server/ configs + restart
      <modname>  — restore only that mod's source + rebuild + restart
    """
    if not re.fullmatch(r"[0-9a-fA-F]{4,40}", commit_hash):
        return f"Ogiltigt commit-hash: `{commit_hash}`"

    # Always checkpoint current state first
    await _run_shell(
        f'git add -A && git commit -m "Checkpoint: before restore {target} to {commit_hash}" --allow-empty',
        cwd=REPO_DIR,
    )

    if target == "all":
        stdout, stderr, rc = await _run_shell(
            f"git reset --hard {commit_hash}", cwd=REPO_DIR
        )
        if rc != 0:
            return f"Restore misslyckades:\n```\n{stderr[:500]}\n```"
        results = []
        for mod_dir in sorted(Path(MODS_SOURCE_DIR).iterdir()):
            if mod_dir.is_dir() and mod_dir.name != "external" and (mod_dir / "build.gradle").exists():
                results.append(await _build_and_deploy_mod(mod_dir))
        await _sync_server_configs()
        await _run_shell("sudo systemctl restart minecraft")
        return (
            f"Allt återställt till `{commit_hash}`.\n"
            f"Mods: {', '.join(results) or 'inga'}\n"
            f"Server omstartad — uppe om ~30 s."
        )

    elif target == "server":
        _, stderr, rc = await _checkout_path(commit_hash, "server/")
        if rc != 0:
            return f"Restore server misslyckades:\n```\n{stderr[:400]}\n```"
        await _sync_server_configs()
        await _run_shell("sudo systemctl restart minecraft")
        return (
            f"Server-configs återställda till `{commit_hash}`.\n"
            f"Server omstartad — uppe om ~30 s."
        )

    else:
        # Specific mod
        mod_dir = _find_mod_dir(target)
        if mod_dir is None:
            return f"Ingen mod hittades med namnet `{target}`."
        _, stderr, rc = await _checkout_path(commit_hash, f"mods/{mod_dir.name}/")
        if rc != 0:
            return f"Restore av `{mod_dir.name}` misslyckades:\n```\n{stderr[:400]}\n```"
        result = await _build_and_deploy_mod(mod_dir)
        await _run_shell("sudo systemctl restart minecraft")
        return (
            f"Mod `{mod_dir.name}` återställd till `{commit_hash}`.\n"
            f"{result}\n"
            f"Server omstartad — uppe om ~30 s."
        )


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
# !server
# ---------------------------------------------------------------------------

async def get_server_status() -> str:
    """Return a rich server status: online/offline, players, RAM, disk, uptime."""
    import socket

    # Online check
    s = socket.socket()
    s.settimeout(2)
    online = s.connect_ex(("localhost", 25565)) == 0
    s.close()
    status_line = "🟢 **ONLINE**" if online else "🔴 **OFFLINE**"

    lines = [f"**Minecraft Server — {status_line}**", ""]

    # Uptime via systemctl
    uptime_out, _, rc = await _run_shell(
        "systemctl show minecraft --property=ActiveEnterTimestamp --value 2>/dev/null"
    )
    if rc == 0 and uptime_out.strip():
        lines.append(f"⏱ Uppe sedan: `{uptime_out.strip()}`")

    # Players via log (last "joined/left" counts)
    players_out, _, _ = await _run_shell(
        "journalctl -u minecraft -n 500 --no-pager -o cat 2>/dev/null"
        " | grep -E 'joined the game|left the game|lost connection'"
        " | tail -50"
    )
    if players_out:
        joined = set()
        for line in players_out.strip().splitlines():
            if "joined the game" in line:
                name = line.split("]:")[-1].replace("joined the game", "").strip()
                joined.add(name)
            elif "left the game" in line or "lost connection" in line:
                name = line.split("]:")[-1].split("lost connection")[0].replace("left the game", "").strip()
                joined.discard(name)
        if joined:
            lines.append(f"👥 Online: {', '.join(sorted(joined))}")
        else:
            lines.append("👥 Inga spelare online")

    # RAM
    mem_out, _, _ = await _run_shell(
        "free -m | awk '/^Mem:/ {printf \"%s MB used / %s MB total\", $3, $2}'"
    )
    if mem_out:
        lines.append(f"🧠 RAM: `{mem_out.strip()}`")

    # Disk
    disk_out, _, _ = await _run_shell(
        "df -h / | awk 'NR==2 {printf \"%s used / %s total (%s)\", $3, $2, $5}'"
    )
    if disk_out:
        lines.append(f"💾 Disk: `{disk_out.strip()}`")

    # Active mods count
    mods = list(Path(SERVER_MODS_DIR).glob("*.jar"))
    lines.append(f"🧩 Aktiva mods: {len(mods)}")

    return "\n".join(lines)


# ---------------------------------------------------------------------------
# !restart
# ---------------------------------------------------------------------------

async def restart_server() -> str:
    """Restart the Minecraft server via systemctl, wait until port 25565 is up."""
    import socket as _socket
    import asyncio as _asyncio

    stdout, stderr, rc = await _run_shell("sudo systemctl restart minecraft")
    if rc != 0:
        return f"Failed to restart server:\n```\n{stderr[:400]}\n```"

    # Wait up to 90 s for the server to accept connections
    for _ in range(45):
        await _asyncio.sleep(2)
        s = _socket.socket()
        s.settimeout(1)
        up = s.connect_ex(("localhost", 25565)) == 0
        s.close()
        if up:
            return "🟢 **Servern är uppe igen!**"

    return "⚠️ Restart skickad men servern svarar inte på port 25565 efter 90 s — kolla loggar med `journalctl -u minecraft -f`."


# ---------------------------------------------------------------------------
# !reset
# ---------------------------------------------------------------------------

async def reset_health() -> str:
    """
    Reset all players' max health to 20 HP.
    Must stop server first — otherwise it overwrites the file on shutdown.
    Sequence: systemctl stop → edit lifesteal.json → systemctl start.
    """
    path = Path(LIFESTEAL_JSON)
    if not path.exists():
        return "❌ `lifesteal.json` hittades inte — har servern startats någon gång?"

    # Stop server
    _, _, rc = await _run_shell("sudo systemctl stop minecraft", timeout=60)
    if rc != 0:
        return "❌ Kunde inte stoppa minecraft.service."

    # Wait for process to fully exit
    await asyncio.sleep(5)

    # Edit lifesteal.json
    with path.open() as f:
        data = json.load(f)

    count = len(data)
    reset_data = {uuid: VANILLA_MAX_HEALTH for uuid in data}
    with path.open("w") as f:
        json.dump(reset_data, f, indent=2)

    # Start server
    await _run_shell("sudo systemctl start minecraft", timeout=30)

    return (
        f"✅ **{count} spelare** återställda till 20 HP (10 hjärtan).\n"
        f"Servern startar nu — spelarna kan logga in om ~30 sekunder."
    )
