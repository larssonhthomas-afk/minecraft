# Discord-bot för mod-hantering

Discord-boten låter spelare skapa och hantera Minecraft-mods direkt via Discord, utan att behöva SSH-tillgång till servern. Boten kommunicerar med Claude Code på servern och svarar alltid i trådar.

## Kommandon

| Kommando | Beskrivning |
|---|---|
| `!create <beskrivning>` | Skapar en ny mod — plan, följdfrågor, bygge, tester och deploy |
| `!change <modnamn> <beskrivning>` | Ändrar en befintlig mod — samma flöde som `!create` |
| `!list` | Visar aktiva och inaktiverade mods |
| `!remove <modnamn>` | Inaktiverar en mod (flyttar jar till `mods-disabled/`) |
| `!activate <modnamn>` | Aktiverar en inaktiverad mod |
| `!restore <hash>` | Återställer allt (repo + mods + server-configs) till en Git-commit |
| `!restore server <hash>` | Återställer bara server-configs till en Git-commit |
| `!restore <modnamn> <hash>` | Återställer och bygger om en specifik mod från en Git-commit |
| `!history` | Visar de 10 senaste Git-commits med hash |
| `!server` | Visar serverstatus: online/offline, RAM, disk, uptime, spelare, mods |
| `!restart` | Startar om Minecraft-servern och väntar tills den svarar |
| `!reset` | Återställer alla spelares HP till 20 (10 hjärtan) |
| `!help` | Visar alla kommandon |

## Hur !create fungerar

1. **Plan** — Claude genererar en plan och eventuella följdfrågor (max 3).
2. **Liknande mod?** — Om en liknande mod redan finns frågas användaren om de vill fortsätta.
3. **Följdfrågor** — Ställs en i taget i tråden. Claude väntar på svar.
4. **Bekräftelse** — Sista steget är alltid `"build?"`. Bygget startar inte utan ja.
5. **Bygge** — Claude genererar Java-kod baserat på lifesteal-arkitekturen.
6. **Test** — `./gradlew test` körs alltid. Aldrig deploy om tester misslyckas.
7. **Fixa** — Vid testfel rättar Claude och försöker igen, max 5 gånger.
8. **Checkpoint** — Git-commit: `"Checkpoint: <modnamn> created by <discord-användare>"`.
9. **Deploy** — Jar kopieras till serverns `mods/`-mapp. Servern behöver startas om.

## Hur !change fungerar

Samma flöde som `!create` men hittar modens befintliga källkod och modifierar den. Skapar checkpoint-commit med `"modified by <discord-användare>"`.

## Hur !restore fungerar

`!restore` har tre lägen beroende på argument:

```
!restore abc1234          # Återställer allt: repo + alla mods + server-configs + omstart
!restore server abc1234   # Återställer bara server-configs (server.properties, ops, whitelist, eula)
!restore lifesteal abc1234 # Återställer bara en mod, bygger om och deployer
```

Skapar alltid en checkpoint-commit av nuläget *innan* återställning.

## Säkerhet

- **Ingenting raderas** — `!remove` inaktiverar (flyttar jar), `!activate` återaktiverar.
- **Git-checkpoint** skapas automatiskt innan varje ändring eller återställning.
- **Tokens och lösenord** lagras enbart i `/opt/minecraft-dev/minecraft/infrastructure/discord-bot/.env` (ej i repot).
- **Ingen webbpanel** — Crafty Controller är borttaget. Servern hanteras enbart via systemd.

## Teknisk arkitektur

```
infrastructure/discord-bot/
  bot.py           — Discord-kommandon och tillståndmaskin för !create/!change-konversationer
  claude_runner.py — Anropar Claude Code CLI, hanterar systemd och filoperationer
  mod_template/    — Minimal Fabric-mod som Claude använder som referens
  tests/           — 28 enhetstester (kommandoparsning, mod-namnsgenerering)
```

Boten körs som systemd-tjänst (`discord-bot.service`) under `deploy`-användaren och startar automatiskt vid omstart.

Minecraft-servern körs som `minecraft.service` under `crafty`-användaren från `/opt/minecraft-dev/minecraft/server/`.

## Installation och drift

Se `infrastructure/discord-bot/README.md` för fullständiga installationsinstruktioner.

### Loggar

```bash
# Bot-loggar
journalctl -u discord-bot -f

# Minecraft-serverloggar
journalctl -u minecraft -f
```

### Starta om boten

```bash
systemctl restart discord-bot
```

### Starta om Minecraft-servern

```bash
sudo systemctl restart minecraft
# eller via Discord:
!restart
```
