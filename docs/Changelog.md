# Changelog

Alla noterbara ändringar i det här repot dokumenteras här.

Format inspirerat av [Keep a Changelog](https://keepachangelog.com/). Datum är i ISO-format. Projektet har ännu ingen semantisk versionering — entries grupperas per dag.

## 2026-04-17

### Nytt

- **LifeSteal-modden** är implementerad och körs på produktionsservern. Flyttar 1 hjärta (2 HP max health) från offer till mördare vid PvP-död. Defaultvärden: minHealth 4.0 HP, maxHealth 40.0 HP, 1 hjärta per kill.
- **Admin-kommandon** under `/lifesteal` (op-nivå 2) — `status <player>`, `simulate-kill <victim>`, `reset <player>`. ([PR #12](https://github.com/larssonhthomas-afk/minecraft/pull/12), stänger #6)
- **Konfigurerbara hjärt-parametrar** via `config/lifesteal.json` i serverns rot. Skapas automatiskt vid första uppstart med defaults. Ogiltiga värden faller tillbaka till defaults med loggad varning. ([PR #13](https://github.com/larssonhthomas-afk/minecraft/pull/13), stänger #7)
- **Wiki-sync** från `docs/` till GitHub Wiki vid push till `main`. Home, sidebar och flatta filnamn. ([PR #9](https://github.com/larssonhthomas-afk/minecraft/pull/9), [PR #10](https://github.com/larssonhthomas-afk/minecraft/pull/10), [PR #11](https://github.com/larssonhthomas-afk/minecraft/pull/11))
- **CLAUDE.md** med projektkontext, kodprinciper och git-flöde på repo-rot, plus undermappsspecifik fil under `lifesteal/`.
- **Fabric/Gradle-skelett** för LifeSteal: Loom 1.10.5, Yarn 1.21.4+build.8, Fabric API 0.119.4+1.21.4, JDK 21.
- **LifeSteal-dokumentation:** översikt, arkitektur, testning, deployment.

### Infrastruktur

- **Minecraft-server uppsatt** på Hetzner CX23 (Ubuntu 22.04, hostname `clawbot`, hel1-dc2). Fabric Loader 0.19.2 + Fabric API 0.119.4+1.21.4, körs som `systemd`-unit `minecraft.service` under användaren `minecraft`.
- **LifeSteal-jar deployad** till `/opt/minecraft/server/mods/` och verifierad live (`/lifesteal status @s`, `/lifesteal simulate-kill @s`, elimination → spectator, `/lifesteal reset @s`, auto-skapad config-fil).
- **Op-rättigheter:** `MasterOlley` (UUID `9dd7ffcd-…`) har nivå 4 i `ops.json`.

### Tester

- 28 JUnit 5-tester (21 `HeartManager` + 7 `LifeStealConfig`), alla gröna. Tester kör på < 200 ms eftersom logik-lagret saknar Minecraft-beroenden.

### Kända begränsningar

- Ingen GitHub Actions CI ännu (issue [#1](https://github.com/larssonhthomas-afk/minecraft/issues/1)).
- Ingen auto-deploy (issue [#2](https://github.com/larssonhthomas-afk/minecraft/issues/2)) — manuell kopiering + `systemctl restart` gäller.
- Ingen rollback-automatik (issue [#3](https://github.com/larssonhthomas-afk/minecraft/issues/3)).
- Ingen Discord-bot eller övervakning (issues [#5](https://github.com/larssonhthomas-afk/minecraft/issues/5), [#8](https://github.com/larssonhthomas-afk/minecraft/issues/8)).
