# Monorepo: Minecraft Mods & Server Components

Detta repo innehåller alla komponenter för vår privata Minecraft-server: mods, konfiguration, deploy-skript och dokumentation.

## Struktur

- `lifesteal/` — LifeSteal Fabric-mod (permanent hjärtöverföring vid PvP)
- `docs/` — Markdown-dokumentation, synkas automatiskt till GitHub Wiki
- `.github/workflows/` — CI/CD (bygge, test, deploy, wiki-sync)

Fler mods och komponenter läggs till som egna undermappar över tid.

## Servermiljö

- Host: Hetzner CX23 (Ubuntu 22.04)
- Minecraft: 1.21.4 med Fabric Loader 0.19.2
- Java: 21 (OpenJDK)
- Utveckling sker på servern via Claude Code

## Kodprinciper

1. **Separera ren logik från Minecraft-integration.** All affärslogik i `.logic`-paket utan Minecraft-imports. Mixins är tunna och delegerar till logik-klasser.
2. **Tester är obligatoriska.** JUnit 5 för all ren logik. Ingen kod committas utan gröna tester.
3. **Språk:** Kod och commit-meddelanden på engelska. Kommentarer, dokumentation och README:s på svenska.
4. **Små commits.** En logisk ändring per commit.

## Git-flöde

- Huvudbranch: `main` — alltid deploybar
- Feature-branches: `feature/<beskrivning>` eller `fix/<beskrivning>`
- PR-granskning innan merge till `main` (när juniorer är ombord)

## Dokumentation

Dokumentation skrivs i `docs/` som vanliga `.md`-filer. GitHub Wiki speglas automatiskt från denna mapp via workflow.

## Säkerhet

- Inga secrets i repot. Använd GitHub Secrets och environment variables.
- SSH-nycklar och API-tokens nämns aldrig i kod eller commit-meddelanden.

## Arbetsregler för Claude Code

- Sök på webben för aktuella versioner av Fabric-beroenden innan du antar.
- Följ `CLAUDE.md` i respektive undermapp för mod-specifika instruktioner.
- Skapa aldrig filer utanför den mapp du är grundad i, utom när en specifik instruktion tillåter det (t.ex. dokumentation till `docs/`).
- Kör alltid `./gradlew test` efter kodändringar. Commita inte röda tester.
- **Wiki-regel:** När en ny komponent, mod eller infrastrukturändring läggs till ska motsvarande dokumentation skapas eller uppdateras i `docs/`. Committa alltid doc-filen i samma commit som koden.
- **Issue-regel:** Stäng GitHub-issues med en kommentar (vilken commit/PR som löste det) när funktionalitet är klar och deployad. Öppna nya issues för buggar eller förbättringar som upptäcks under arbetet.

## Fabric Mixin — kända fallgropar (MC 1.21.4)

### `PlayerEntity.attack()` anropar `sidedDamage`, inte `damage`

I MC 1.21.4 delegerar `PlayerEntity.attack()` skadan via `Entity.sidedDamage(DamageSource, float)` — **inte** `Entity.damage()`. Korrekt Mixin-target:

```
Lnet/minecraft/entity/Entity;sidedDamage(Lnet/minecraft/entity/damage/DamageSource;F)Z
```

Intermediary: `class_1297.method_64420(class_1282, F)Z`

När en *server-spelare tar emot* skada används däremot `ServerPlayerEntity.damage(ServerWorld, DamageSource, float)` — intermediary `method_64397`.

### Diagnos: "0 target(s) scanned"

Gröna tester ≠ fungerande Mixin. Injektionsfel syns bara när servern startar. När Mixin rapporterar "0 target(s) scanned":

1. Läs refmappen (`build/classes/java/main/<modid>-refmap.json`) — den visar vad Loom faktiskt kopplade annotationen till i intermediary.
2. Dekompilera den Yarn-mappade MC-jarn för att se vilka metoder som faktiskt anropas:
   ```bash
   MC_JAR=$(find <mod>/.gradle/loom-cache -name "minecraft-merged-d1fae903ae-*.jar" \
     ! -name "*sources*" ! -name "*.backup" | head -1)
   cd /tmp && jar xf "$MC_JAR" "net/minecraft/entity/player/PlayerEntity.class"
   javap -c -p PlayerEntity.class | grep -A 300 "public void attack" | grep "invokevirtual"
   ```

### Föredra `@ModifyArg` framför `@Redirect`

`@Redirect` kräver att du anropar målmetoden igen i handleren — om metoden inte är direkt åtkomlig i Yarn-Java uppstår kompilatorfel. `@ModifyArg` ändrar bara ett argument och låter Mixin sköta anropet. Välj `@ModifyArg` när du bara vill justera ett värde (t.ex. ett damage-float).
