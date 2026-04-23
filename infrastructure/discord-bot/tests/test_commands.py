"""
Unit tests for command parsing and mod-name generation logic.
Run with:  python3 -m pytest tests/ -v
"""

import sys
import os
import re

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from claude_runner import generate_mod_names, _STOP_WORDS


# ---------------------------------------------------------------------------
# generate_mod_names
# ---------------------------------------------------------------------------

class TestGenerateModNames:
    def test_basic_two_word(self):
        r = generate_mod_names("speed boost")
        assert r["mod_id"] == "speed_boost"
        assert r["package"] == "speedboost"
        assert r["class_prefix"] == "SpeedBoost"

    def test_filters_stop_words(self):
        r = generate_mod_names("a mod that adds speed")
        parts = r["mod_id"].split("_")
        # "a", "mod", "that" are all stop-words — none should appear as their own token
        assert "a" not in parts
        assert "mod" not in parts
        assert "that" not in parts
        assert "adds" in parts or "speed" in parts

    def test_max_three_words(self):
        r = generate_mod_names("health regeneration system bonus extra")
        parts = r["mod_id"].split("_")
        assert len(parts) <= 3

    def test_empty_description_fallback(self):
        r = generate_mod_names("")
        assert r["mod_id"] == "custom"
        assert r["package"] == "custom"
        assert r["class_prefix"] == "Custom"

    def test_mod_id_is_snake_case(self):
        r = generate_mod_names("frost damage effect")
        assert " " not in r["mod_id"]
        assert r["mod_id"] == r["mod_id"].lower()

    def test_package_has_no_underscores(self):
        r = generate_mod_names("loot drop bonus")
        assert "_" not in r["package"]
        assert r["package"] == r["package"].lower()

    def test_class_prefix_is_camel_case(self):
        r = generate_mod_names("lightning strike ability")
        assert r["class_prefix"][0].isupper()
        # Each component should be capitalised
        assert r["class_prefix"] == "".join(
            w.capitalize() for w in r["mod_id"].split("_")
        )

    def test_single_word(self):
        r = generate_mod_names("teleport")
        assert r["mod_id"] == "teleport"
        assert r["package"] == "teleport"
        assert r["class_prefix"] == "Teleport"

    def test_only_stop_words_falls_back_to_first_word(self):
        r = generate_mod_names("a the and")
        # All three are stop-words; fallback takes first raw word
        assert len(r["mod_id"]) > 0

    def test_numbers_excluded(self):
        # Numbers are stripped by the regex; result still valid
        r = generate_mod_names("player 2x speed")
        assert r["mod_id"]  # not empty


# ---------------------------------------------------------------------------
# Command string parsing (pure string logic, no Discord objects)
# ---------------------------------------------------------------------------

def parse_command(content: str) -> tuple[str, str]:
    """Extract (command_name, args) from a '!cmd args' message."""
    parts = content.strip().split(None, 1)
    if not parts or not parts[0].startswith("!"):
        return ("", "")
    cmd = parts[0][1:].lower()
    args = parts[1].strip() if len(parts) > 1 else ""
    return (cmd, args)


class TestParseCommand:
    def test_create_with_description(self):
        cmd, args = parse_command("!create a mod that adds speed to players")
        assert cmd == "create"
        assert args == "a mod that adds speed to players"

    def test_list(self):
        cmd, args = parse_command("!list")
        assert cmd == "list"
        assert args == ""

    def test_remove_with_name(self):
        cmd, args = parse_command("!remove lifesteal")
        assert cmd == "remove"
        assert args == "lifesteal"

    def test_activate_with_name(self):
        cmd, args = parse_command("!activate speed_boost")
        assert cmd == "activate"
        assert args == "speed_boost"

    def test_restore_with_hash(self):
        cmd, args = parse_command("!restore abc1234")
        assert cmd == "restore"
        assert args == "abc1234"

    def test_history(self):
        cmd, args = parse_command("!history")
        assert cmd == "history"
        assert args == ""

    def test_status(self):
        cmd, args = parse_command("!status")
        assert cmd == "status"

    def test_help(self):
        cmd, args = parse_command("!help")
        assert cmd == "help"

    def test_case_insensitive(self):
        cmd, _ = parse_command("!CREATE something")
        assert cmd == "create"

    def test_extra_whitespace(self):
        cmd, args = parse_command("  !remove   mymod  ")
        assert cmd == "remove"
        assert args == "mymod"

    def test_no_prefix_returns_empty(self):
        cmd, args = parse_command("just a regular message")
        assert cmd == ""

    def test_empty_string(self):
        cmd, args = parse_command("")
        assert cmd == ""


# ---------------------------------------------------------------------------
# Commit hash validation (mirrors the regex in claude_runner.restore_commit)
# ---------------------------------------------------------------------------

_HASH_RE = re.compile(r"^[0-9a-fA-F]{4,40}$")


class TestCommitHashValidation:
    def _is_valid(self, h: str) -> bool:
        return bool(_HASH_RE.fullmatch(h))

    def test_short_hash_valid(self):
        assert self._is_valid("abc1234")

    def test_full_hash_valid(self):
        assert self._is_valid("a" * 40)

    def test_too_short_invalid(self):
        assert not self._is_valid("ab")

    def test_non_hex_invalid(self):
        assert not self._is_valid("xyz1234")

    def test_empty_invalid(self):
        assert not self._is_valid("")

    def test_with_spaces_invalid(self):
        assert not self._is_valid("abc 1234")
