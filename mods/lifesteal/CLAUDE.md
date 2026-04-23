# LifeSteal Mod

Fabric-mod för Minecraft 1.21.4. När en spelare dödar en annan spelare flyttas ett hjärta (2 max HP) permanent från offer till mördare.

## Målversioner (måste matcha exakt)

- Minecraft: **1.21.4**
- Fabric Loader: **0.19.2**
- Fabric API: **0.119.4+1.21.4**
- Java: **21**
- Yarn mappings: senaste för 1.21.4 (kolla https://fabricmc.net/develop)
- fabric-loom: välj version kompatibel med 1.21.4

## Arkitektur

### `com.lifesteal.logic.HeartManager` — Ren Java, noll Minecraft-imports

- Konstruktor: `HeartManager(float minHealth, float maxHealth, float heartsToSteal)`
- Standardvärden: min=4.0 (2 hjärtan), max=40.0 (20 hjärtan), steal=1.0
- Konstant: `HEALTH_PER_HEART = 2.0f`
- Metoder:
  - `TransferResult transferHearts(float killerCurrentMax, float victimCurrentMax)`
  - `boolean isAtMaximumHealth(float currentMax)`
  - `boolean isEliminated(float currentMax)` — true om under minHealth
- Validering i konstruktor (IllegalArgumentException vid ogiltiga värden)

### `com.lifesteal.LifeStealMod` — ModInitializer

- Håller singleton HeartManager
- Registrerar ServerLifecycleEvents för ladda/spara persistens
- Loggar konfig vid uppstart

### `com.lifesteal.mixin.PlayerDeathMixin` — Mixin på ServerPlayerEntity.onDeath

- Detekterar PvP-kill (DamageSource från annan ServerPlayerEntity)
- Anropar `HeartManager.transferHearts`
- Applicerar nya max-HP via `EntityAttributes.GENERIC_MAX_HEALTH`
- Broadcastar "[Mördare] stal ett hjärta från [Offer]"
- Eliminerat offer → spectator-läge

## Persistens

- Fil: `lifesteal.json` i världens root (`WorldSavePath.ROOT`)
- Format: `Map<UUID, Float>` för max-HP per spelare
- Ladda vid server start, spara vid varje överföring

## Tester (JUnit 5)

Minst 15 tester på `HeartManager`:

- Konstruktor-validering (ogiltiga värden kastar exception)
- `transferHearts` normal PvP-kill
- `transferHearts` när offret är på minimum
- `transferHearts` när mördaren är på maximum
- `isAtMaximumHealth` med olika värden
- `isEliminated` detektion
- Anpassade konfigurationer (2 hjärtan per kill, lägre max, etc.)

**Regel:** Tester får inte importera något från `net.minecraft.*` eller `net.fabricmc.*`. Om det känns nödvändigt — flytta logiken ur mixin till HeartManager.

## Dokumentation

När du skapar eller ändrar kod, uppdatera också motsvarande fil i `../docs/lifesteal/`:

- `overview.md` — Vad modden gör, spelmekanik
- `architecture.md` — Design-beslut, varför separation logik/mixin
- `testing.md` — Hur testerna körs, vad de täcker
- `deployment.md` — Bygge och installation

Max 1-3 sidor per fil. Svenska. Konkreta exempel före teori.

## Bygge

```bash
./gradlew build   # Bygger .jar i build/libs/
./gradlew test    # Kör JUnit-tester
```

Resulterande `.jar` installeras i `/opt/minecraft/server/mods/` på produktionsservern.
