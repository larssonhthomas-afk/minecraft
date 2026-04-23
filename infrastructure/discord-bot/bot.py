"""
Discord bot for Minecraft mod management via Claude Code.

Commands (all respond in threads, not the main channel):
  !create <description>  — generate, test, and deploy a new Fabric mod
  !list                  — show active and disabled mods
  !remove <modname>      — disable a mod (moves jar to mods-disabled/)
  !activate <modname>    — re-enable a disabled mod
  !restore <hash>        — git-reset the repo to a specific commit
  !history               — last 10 git commits
  !status                — check if the Minecraft server is running
  !help                  — show this list
"""

import asyncio
import logging
import os

import discord
from discord.ext import commands, tasks
from dotenv import load_dotenv

from claude_runner import (
    analyze_mod_request,
    build_mod,
    list_mods,
    remove_mod,
    activate_mod,
    restore_commit,
    get_history,
    get_server_status,
    restart_server,
    reset_health,
)

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
log = logging.getLogger("mc-bot")

DISCORD_TOKEN: str = os.environ["DISCORD_TOKEN"]
CHANNEL_ID: int = int(os.environ["CHANNEL_ID"])

intents = discord.Intents.default()
intents.message_content = True

bot = commands.Bot(command_prefix="!", intents=intents, help_command=None)

# ── State machine for !create sessions ──────────────────────────────────────
# Key: thread.id  Value: session dict
#   state         : "PENDING_SIMILAR_CONFIRM" | "PENDING_CLARIFICATIONS" | "BUILDING"
#   description   : original user string
#   discord_user  : str(ctx.author)
#   questions     : list[str]
#   answers       : list[str]  (grows as user replies)
active_sessions: dict[int, dict] = {}
_server_was_online: bool | None = None  # None = unknown (first check not done yet)

HELP_TEXT = """\
**Minecraft Mod Bot — Commands**

`!create <description>` — Skapa en ny mod. Claude granskar, ställer följdfrågor, bygger, testar och deployer.
`!list` — Visa aktiva och inaktiverade mods.
`!remove <modnamn>` — Inaktivera en mod (flyttar jar till mods-disabled/).
`!activate <modnamn>` — Aktivera en inaktiverad mod.
`!restore <commit-hash>` — Återställ repot till en specifik Git-version.
`!history` — Visa de 10 senaste Git-commits med hash.
`!server` — Visa serverstatus, RAM, disk och aktiva spelare.
`!restart` — Starta om Minecraft-servern.
`!reset` — Återställ alla spelares HP till standard 20 HP (10 hjärtan).
`!help` — Visa denna lista.

Alla svar visas i trådar.
"""


# ── Discord helpers ──────────────────────────────────────────────────────────

async def get_or_create_thread(message: discord.Message, name: str) -> discord.Thread | discord.TextChannel:
    """Return the current thread if already in one; otherwise create a new thread from the message."""
    if isinstance(message.channel, discord.Thread):
        return message.channel
    try:
        return await message.create_thread(name=name[:100], auto_archive_duration=1440)
    except discord.HTTPException as e:
        log.warning("Could not create thread: %s", e)
        return message.channel  # fall back to channel


# ── Server watchdog ──────────────────────────────────────────────────────────

@tasks.loop(seconds=60)
async def server_watchdog():
    global _server_was_online
    channel = bot.get_channel(CHANNEL_ID)
    if channel is None:
        return

    is_online = "ONLINE" in await get_server_status()

    if _server_was_online is None:
        # First check — just record state, no notification
        _server_was_online = is_online
        return

    if _server_was_online and not is_online:
        log.warning("Server went offline — notifying Discord")
        await channel.send("🔴 **Minecraft-servern är nere!** Kör `!restart` för att starta om.")
    elif not _server_was_online and is_online:
        log.info("Server came back online — notifying Discord")
        await channel.send("🟢 **Minecraft-servern är tillbaka online!**")

    _server_was_online = is_online


# ── Event: on_ready ──────────────────────────────────────────────────────────

@bot.event
async def on_ready():
    log.info("Logged in as %s (id=%s)", bot.user, bot.user.id)
    server_watchdog.start()


# ── Event: on_message ────────────────────────────────────────────────────────

@bot.event
async def on_message(message: discord.Message):
    if message.author.bot:
        return

    channel = message.channel
    parent_id = channel.parent_id if isinstance(channel, discord.Thread) else channel.id
    if parent_id != CHANNEL_ID:
        return

    # Route plain (non-command) replies inside active !create threads.
    if (
        isinstance(channel, discord.Thread)
        and channel.id in active_sessions
        and not message.content.startswith("!")
    ):
        await _handle_session_reply(message)
        return

    await bot.process_commands(message)


# ── Session state machine ─────────────────────────────────────────────────────

async def _handle_session_reply(message: discord.Message):
    thread = message.channel
    session = active_sessions.get(thread.id)
    if session is None:
        return

    state = session["state"]

    if state == "PENDING_SIMILAR_CONFIRM":
        reply = message.content.strip().lower()
        if reply in ("yes", "y", "ja"):
            if session["questions"]:
                await _ask_next_question(thread, session)
            else:
                await _start_build(thread, session)
        elif reply in ("no", "n", "nej"):
            await thread.send("Understood — mod creation cancelled.")
            active_sessions.pop(thread.id, None)
        else:
            await thread.send("Please reply **yes** or **no**.")

    elif state == "PENDING_CLARIFICATIONS":
        session["answers"].append(message.content.strip())
        remaining = session["questions"][len(session["answers"]):]
        if remaining:
            idx = len(session["answers"])
            total = len(session["questions"])
            await thread.send(f"**Question {idx + 1}/{total}:** {remaining[0]}")
        else:
            await _start_build(thread, session)


async def _ask_next_question(thread: discord.Thread, session: dict):
    session["answers"] = []
    session["state"] = "PENDING_CLARIFICATIONS"
    total = len(session["questions"])
    await thread.send(
        f"Before I start building I have {total} clarifying question(s).\n"
        f"**Question 1/{total}:** {session['questions'][0]}"
    )


async def _start_build(thread: discord.Thread, session: dict):
    session["state"] = "BUILDING"
    await thread.send("Starting the build process — I'll post updates every 30 s.")
    asyncio.create_task(_run_build_task(thread, session))


async def _run_build_task(thread: discord.Thread, session: dict):
    """Detached task: run build_mod with periodic progress pings."""

    progress_messages: list[str] = []

    async def progress_cb(msg: str):
        progress_messages.append(msg)
        await thread.send(msg[:2000])

    build_coro = build_mod(
        description=session["description"],
        discord_user=session["discord_user"],
        questions=session["questions"],
        answers=session["answers"],
        progress_cb=progress_cb,
    )
    build_task = asyncio.create_task(build_coro)

    try:
        elapsed = 0
        while not build_task.done():
            await asyncio.sleep(30)
            elapsed += 30
            if not build_task.done():
                await thread.send(f"Still working... ({elapsed}s elapsed)")

        result = await build_task
        await thread.send(result[:2000])
    except Exception as exc:
        log.exception("Build task raised an unexpected exception")
        await thread.send(f"Unexpected error during build: {exc}")
    finally:
        active_sessions.pop(thread.id, None)


# ── Commands ──────────────────────────────────────────────────────────────────

@bot.command(name="create")
async def cmd_create(ctx: commands.Context, *, description: str = ""):
    if not description.strip():
        thread = await get_or_create_thread(ctx.message, "!create — usage")
        await thread.send("Usage: `!create <description of the mod you want>`")
        return

    thread = await get_or_create_thread(ctx.message, f"create: {description[:60]}")
    await thread.send(f"Analyzing your request: *{description}*")

    try:
        analysis = await analyze_mod_request(description)
    except Exception as exc:
        await thread.send(f"Analysis failed: {exc}")
        return

    session: dict = {
        "description": description,
        "discord_user": str(ctx.author),
        "questions": analysis.get("questions", [])[:3],
        "answers": [],
        "state": "",
    }
    active_sessions[thread.id] = session

    if analysis.get("has_similar"):
        session["state"] = "PENDING_SIMILAR_CONFIRM"
        await thread.send(
            f"A similar mod already exists: **{analysis['similar_mod_name']}**.\n"
            "Do you still want to create a new one? Reply **yes** or **no**."
        )
    elif session["questions"]:
        await _ask_next_question(thread, session)
    else:
        session["state"] = "BUILDING"
        await _start_build(thread, session)


@bot.command(name="list")
async def cmd_list(ctx: commands.Context):
    thread = await get_or_create_thread(ctx.message, "mod list")
    try:
        await thread.send(await list_mods())
    except Exception as exc:
        await thread.send(f"Error listing mods: {exc}")


@bot.command(name="remove")
async def cmd_remove(ctx: commands.Context, modname: str = ""):
    thread = await get_or_create_thread(ctx.message, f"remove: {modname}")
    if not modname.strip():
        await thread.send("Usage: `!remove <modname>`")
        return
    try:
        await thread.send(await remove_mod(modname))
    except Exception as exc:
        await thread.send(f"Error: {exc}")


@bot.command(name="activate")
async def cmd_activate(ctx: commands.Context, modname: str = ""):
    thread = await get_or_create_thread(ctx.message, f"activate: {modname}")
    if not modname.strip():
        await thread.send("Usage: `!activate <modname>`")
        return
    try:
        await thread.send(await activate_mod(modname))
    except Exception as exc:
        await thread.send(f"Error: {exc}")


@bot.command(name="restore")
async def cmd_restore(ctx: commands.Context, commit_hash: str = ""):
    thread = await get_or_create_thread(ctx.message, f"restore: {commit_hash[:8]}")
    if not commit_hash.strip():
        await thread.send("Usage: `!restore <commit-hash>`  (get hashes with `!history`)")
        return
    await thread.send(f"Restoring repo to `{commit_hash}`…")
    try:
        await thread.send(await restore_commit(commit_hash))
    except Exception as exc:
        await thread.send(f"Error: {exc}")


@bot.command(name="history")
async def cmd_history(ctx: commands.Context):
    thread = await get_or_create_thread(ctx.message, "git history")
    try:
        await thread.send(await get_history())
    except Exception as exc:
        await thread.send(f"Error: {exc}")


@bot.command(name="server")
async def cmd_server(ctx: commands.Context):
    thread = await get_or_create_thread(ctx.message, "server status")
    try:
        await thread.send(await get_server_status())
    except Exception as exc:
        await thread.send(f"Error: {exc}")


@bot.command(name="restart")
async def cmd_restart(ctx: commands.Context):
    thread = await get_or_create_thread(ctx.message, "server restart")
    await thread.send("Startar om Minecraft-servern...")
    try:
        await thread.send(await restart_server())
    except Exception as exc:
        await thread.send(f"Error: {exc}")


@bot.command(name="reset")
async def cmd_reset(ctx: commands.Context):
    thread = await get_or_create_thread(ctx.message, "reset health")
    await thread.send("Återställer alla spelares HP till 20...")
    try:
        await thread.send(await reset_health())
    except Exception as exc:
        await thread.send(f"Error: {exc}")


@bot.command(name="help")
async def cmd_help(ctx: commands.Context):
    thread = await get_or_create_thread(ctx.message, "help")
    await thread.send(HELP_TEXT)


# ── Entry point ───────────────────────────────────────────────────────────────

if __name__ == "__main__":
    bot.run(DISCORD_TOKEN)
