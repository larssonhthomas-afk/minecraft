# Discord-bot för mod-hantering

Discord-boten låter spelare skapa och hantera Minecraft-mods direkt via Discord, utan att behöva SSH-tillgång till servern. Boten kommunicerar med Claude Code på servern och svarar alltid i trådar.

## Kommandon

| Kommando | Beskrivning |
|---|---|
| `!create <beskrivning>` | Skapar en ny mod — Claude granskar, ställer följdfrågor, bygger, testar och deployer |
| `!list` | Visar aktiva och inaktiverade mods |
| `!remove <modnamn>` | Inaktiverar en mod (flyttar jar till `mods-disabled/`) |
| `!activate <modnamn>` | Aktiverar en inaktiverad mod |
| `!restore <commit-hash>` | Återställer repot till en specifik Git-version |
| `!history` | Visar de 10 senaste Git-commits med hash |
| `!status` | Visar om Minecraft-servern är igång |
| `!restart` | Startar om Minecraft-servern via Crafty Controller |
| `!help` | Visar alla kommandon |

## Hur !create fungerar

1. **Granskning** — Claude kontrollerar om en liknande mod redan finns. Om ja, frågas användaren i tråden.
2. **Följdfrågor** — Om beskrivningen är oklar ställs max 3 följdfrågor i tråden. Claude väntar på svar.
3. **Bygge** — Claude genererar Java-kod baserat på lifesteal-arkitekturen (separerad logik och mixins).
4. **Test** — `./gradlew test` körs alltid. Aldrig deploy om tester misslyckas.
5. **Fixa** — Vid testfel rättar Claude och försöker igen, max 5 gånger. Därefter förklaras vad som gick fel.
6. **Checkpoint** — Git-commit skapas: `"Checkpoint: <modnamn> created by <discord-användare>"`.
7. **Deploy** — Jar kopieras till serverns `mods/`-mapp. Servern behöver startas om.

## Säkerhet

- **Ingenting raderas** — `!remove` inaktiverar (flyttar jar), `!activate` återaktiverar.
- **Git-checkpoint** skapas automatiskt innan varje ändring.
- **Tokens och lösenord** lagras enbart i `/opt/minecraft-dev/minecraft/infrastructure/discord-bot/.env` (ej i repot).

## Teknisk arkitektur

```
infrastructure/discord-bot/
  bot.py           — Discord-kommandon och tillståndmaskin för !create-konversationer
  claude_runner.py — Anropar Claude Code CLI, hanterar Crafty API och filoperationer
  mod_template/    — Minimal Fabric-mod som Claude använder som referens
  tests/           — 28 enhetstester (kommandoparsning, mod-namnsgenerering)
```

Boten körs som systemd-tjänst (`discord-bot.service`) under `deploy`-användaren och startar automatiskt vid omstart.

## Installation och drift

Se `infrastructure/discord-bot/README.md` för fullständiga installationsinstruktioner.

### Loggar

```bash
journalctl -u discord-bot -f
```

### Starta om boten

```bash
systemctl restart discord-bot
```
