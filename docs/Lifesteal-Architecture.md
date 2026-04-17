# LifeSteal — arkitektur

Modden är medvetet uppdelad i **tre lager** så att själva spelregeln går att testa utan att starta Minecraft.

```
┌─────────────────────────────────────────────────────────────┐
│  PlayerDeathMixin (ren Minecraft-integration)               │
│    ↓ läser maxHealth-attributet, skickar chat, byter gamemode │
├─────────────────────────────────────────────────────────────┤
│  LifeStealMod + HeartDataStore (Fabric-glue + IO)           │
│    ↓ ModInitializer, ServerLifecycleEvents, JSON-persistens │
├─────────────────────────────────────────────────────────────┤
│  HeartManager (ren Java, noll Minecraft-importer)           │
│    ↓ transferHearts, isEliminated, isAtMaximumHealth        │
└─────────────────────────────────────────────────────────────┘
```

## Lager 1 — `com.lifesteal.logic.HeartManager`

Hela hjärt-matematiken bor här. Klassen har **noll** importer från `net.minecraft.*` eller `net.fabricmc.*`. Det är den kritiska invariansen — så fort man importerar något från Minecraft måste man starta dev-klienten för att köra en test, och cykeln dör.

### API

```java
HeartManager hm = new HeartManager(4.0f, 40.0f, 1.0f);

TransferResult r = hm.transferHearts(
    /* killerCurrentMax */ 20.0f,
    /* victimCurrentMax */ 20.0f);

r.newKillerMax()      // 22.0
r.newVictimMax()      // 18.0
r.transferOccurred()  // true
r.victimEliminated()  // false
```

### Designval

- **Record för TransferResult** — värdena som återgår tillhör ett beslut (en kill), inte ett objekt med livslängd. Ingen mutationsrisk.
- **Clamp på mördarens sida** — om mördaren har plats för bara 0,5 hjärtan får hen 0,5, inte noll. Mindre abrupt för spelarupplevelsen.
- **Ingen statefull singleton i logiken** — `HeartManager` vet inte om "aktiva spelare". State ligger i `HeartDataStore`. Gör att en instans av `HeartManager` räcker för hela serverns livstid och kan delas fritt mellan trådar.

### Validering

Konstruktorn kastar `IllegalArgumentException` vid ogiltig konfig (negativ min, max ≤ min, heartsToSteal ≤ 0, NaN/Infinity). Orimlig input ska inte överleva instantieringen — annars skulle buggar gömma sig i persistens-filen långt senare.

## Lager 2 — `com.lifesteal.LifeStealMod` + `com.lifesteal.persistence.HeartDataStore`

Detta är Fabric-limret: `@Override onInitialize`, registrering av `ServerLifecycleEvents` och `ServerPlayConnectionEvents`, samt läs/skriv av `lifesteal.json`.

### Livscykel

| Händelse | Vad sker |
| --- | --- |
| `SERVER_STARTED` | Läs `lifesteal.json` → `HeartDataStore` |
| `PLAY_CONNECTION_JOIN` | Applicera lagrad max-HP på spelarens `MAX_HEALTH`-attribut. Byt till spectator om eliminerad. |
| PvP-kill *(via mixin)* | `transferHearts` → applicera max på båda → `store.save()` → broadcast |
| `SERVER_STOPPING` | En sista `save()` som säkerhet |

### Atomic write

`HeartDataStore.save()` skriver till `lifesteal.json.tmp` först och gör sedan `Files.move(..., ATOMIC_MOVE, REPLACE_EXISTING)`. Det undviker en korrupt halv fil om JVM dör mitt i skrivningen.

## Lager 3 — `com.lifesteal.mixin.PlayerDeathMixin`

Minsta möjliga klassen som går utanför lagren ovan.

```java
@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDeathMixin {
    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V",
            at = @At("HEAD"))
    private void lifesteal$onDeath(DamageSource source, CallbackInfo ci) { … }
}
```

### Varför `@Inject(HEAD)` och inte `@Redirect` eller `ServerLivingEntityEvents`

- Fabric API har `ServerLivingEntityEvents.ALLOW_DEATH` och `AFTER_DEATH`, vilka kunde funka. Men `onDeath` är den kanoniska Minecraft-metoden och ger oss exakt rätt tid — efter att dödsorsaken är verifierad, innan dödsmeddelande. Det är tydligare för läsaren.
- `@Inject(HEAD)` inverkar inte på originalflödet om vi inte cancellar. Vi gör aldrig det — LifeSteal lägger till en sidoeffekt, inte en ersättare.

### Vad mixinen *inte* gör

- Inget hjärt-räknande — det gör `HeartManager.transferHearts`.
- Ingen JSON-serialisering — det gör `HeartDataStore`.
- Ingen Fabric-event-registrering — det gör `LifeStealMod.onInitialize`.

Det är avsiktligt. En mixin är en Minecraft-specifik teknik; allt som kan flyttas ut borde flyttas ut, för testbarhetens skull.

## Varför det är testbart

`HeartManagerTest` har 21 unit-tester som körs på **< 100 ms** och kräver **inget** av Minecraft: inga mappings, ingen klient, ingen Gradle-loom. Det är därför arkitekturen är uppdelad så här — att komma åt en grafrenderare för att verifiera att 20 - 2 = 18 är ett orimligt fotavtryck.

Se `testing.md` för körinstruktioner.
