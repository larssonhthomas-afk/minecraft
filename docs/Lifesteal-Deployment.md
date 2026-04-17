# LifeSteal — deployment

## Förutsättningar

- JDK 21 (Gradle-wrappern hämtar allt annat)
- Minecraft-server 1.21.4 med Fabric Loader ≥ 0.16
- Fabric API `0.119.4+1.21.4` på serverns `mods/`-mapp
- Serverns `mods/` måste också innehålla en `gson`-lib — kommer med Minecraft själv, men nämner det ifall någon kör en ultra-skalad "server-api-only"-bygge.

## Bygga jar:en

```bash
cd lifesteal
./gradlew build
```

Resultat:

```
build/libs/
├── lifesteal-1.0.0.jar           ← detta ska upp på servern
└── lifesteal-1.0.0-sources.jar   ← debug/IDE-stöd, behövs inte på servern
```

`lifesteal-1.0.0.jar` är redan remappad från yarn till intermediary — det är formen Fabric Loader laddar i produktion.

## Snabb verifiering av jar:en

```bash
jar tf build/libs/lifesteal-1.0.0.jar
```

Ska innehålla:

- `com/lifesteal/LifeStealMod.class`
- `com/lifesteal/logic/HeartManager.class`
- `com/lifesteal/mixin/PlayerDeathMixin.class`
- `com/lifesteal/persistence/HeartDataStore.class`
- `fabric.mod.json`
- `lifesteal.mixins.json`
- `lifesteal-refmap.json`  ← kritisk, mappar mixin-targets till intermediary

Saknas `lifesteal-refmap.json` kommer mixinen att *tyst inte göra något* i produktion. Se `architecture.md` för varför.

## Installation på servern

1. Stoppa servern.
2. Kopiera `lifesteal-1.0.0.jar` till `<server>/mods/`.
3. Bekräfta att `fabric-api-0.119.4+1.21.4.jar` också ligger i `mods/`.
4. Starta servern.
5. Granska loggen för raden:
   ```
   [LifeSteal] LifeSteal initierad: minHealth=4.0, maxHealth=40.0, heartsToSteal=1.0
   ```
   Följt av:
   ```
   [LifeSteal] LifeSteal persistens laddad från <world>/lifesteal.json (N spelare)
   ```

## Uppdatera till ny version

```bash
cd lifesteal
git pull
./gradlew clean build
```

Ersätt `mods/lifesteal-*.jar` med nya jar:en under ett servernedslag. `lifesteal.json` är framåtkompatibel — inga migrations behövs så länge formatet "UUID → float" är oförändrat.

## Backup

Vilket mod-tillstånd som helst kan återställas genom att:

1. Ersätta `lifesteal.json` i världens root med en tidigare kopia.
2. Starta om servern.

Filen är tillräckligt liten för att versioneras i världens dagliga backup utan eftertanke.

## Avinstallation

1. Stoppa servern.
2. Ta bort jar:en från `mods/`.
3. Starta servern.

Spelarnas max-HP blir då **kvar på de värden de hade senast** — Minecraft persisterar `MAX_HEALTH`-attributet i `playerdata/*.dat`, inte bara LifeSteal själv. Om du vill återställa alla till vanilla 20 HP: använd `/attribute @a minecraft:generic.max_health base set 20` efter uppstart utan modden.

## Felsökning

| Symptom | Trolig orsak |
| --- | --- |
| Inga hjärtmeddelanden i chat | Modden inte laddad — kolla `mods/`-mappen och att Fabric Loader är ≥ 0.16 |
| `ClassNotFoundException: ServerPlayerEntity` | Fel MC-version — bygget är hårdkodat för 1.21.4 |
| `MixinApplyError: onDeath` | Fel Yarn-mapping eller att MC-versionen inte matchar — bygg om mot rätt `minecraft_version` |
| `lifesteal.json` töms vid omstart | Skrivbehörighet på världsmappen — kör `chown -R minecraft:minecraft <world>/` |
