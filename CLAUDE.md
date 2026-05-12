# Monorepo: Minecraft Mods & Server Components

Detta repo innehåller alla komponenter för vår privata Minecraft-server: mods, konfiguration, deploy-skript och dokumentation.

## MODS

Varje rad: **mod-id** — `mods/<dir>/` — kort beskrivning av spelmekaniken.
Uppdatera denna lista varje gång en mod skapas eller ändras.

- **lifesteal** — `mods/lifesteal/` — vid PvP-kill: ett hjärta (2 HP) förs permanent från offer till mördare
- **combat_enchant_custom** — `mods/combat_enchant_custom/` — Unbroken Chain-svärdenchantment: +3% skada per träff efter 3 i rad (max +30%), kedjebrott vid skada (**ersatt av unbroken_chain_ability**)
- **unbroken_chain_ability** — `mods/unbroken_chain_ability/` — permanent spellarability (Unbroken Chain): craftad bok (Dragon Egg slot 2 + Player Head slot 5), högerklick applicerar; 3 svärdslag mot samma mål → +3%/slag (max +30%), kedjeljud, bryts vid skada/7s timeout, PvP kill-transfer, tab-display
- **combat_tweak_pearl** — `mods/combat_tweak_pearl/` — 15s cooldown på ender pearl och wind charge; mace-skada nerfad -40% med max 14 HP (7 hjärtan) per slag
- **combat_tweak_combat** — `mods/combat_tweak_combat/` — 15s cooldown på ender pearl och wind charge; mace-skada nerfad -40% med max 14 HP (7 hjärtan) per slag (separat mod, identisk funktionalitet)
- **armor_enchant_tre** — `mods/armor_enchant_tre/` — rustningsenchantment (tre)
- **drop_n_r** — `mods/drop_n_r/` — drop & respawn-tweak
- **world_tweak_ancient** — `mods/world_tweak_ancient/` — världstweak för ancient mechanics
- **special_death_message** — `mods/special_death_message/` — anpassade dödsmeddelanden
- **ranked_smp_rank** — `mods/ranked_smp_rank/` — 9-nivåers rangsystem med PvP-swap, hälsa/potion/XP-buffs och extra inventory via /bag
- **admin_tool_operatorer** — `mods/admin_tool_operatorer/` — admin-kommandon: /check inv/enderchest/bag, /rank set, /give customitem
- **admin_tool_check** — `mods/admin_tool_check/` — op-verktyg för att inspektera spelares inventory/enderchest/bag live
- **gold_guld_spawn** — `mods/gold_guld_spawn/` — 1.5× guld ore spawn i overworld (+2/chunk) och nether (+10/chunk) via Fabric BiomeModifications
- **axe_cleaving_cleaving** — `mods/axe_cleaving_cleaving/` — Cleaving-förmåga på yxor: /give customitem Cleaving ger bok, högerklick applicerar på yxa i andra handen; shield-disable → +3 HP (1.5 hjärtan) extra skada; Cleaving-yxa blockad från chest/trapped chest/shulker box
- **heavenly_n_r** — `mods/heavenly_n_r/` — permanent spellarability (Heavenly): /give customitem heavenly ger bok, högerklick applicerar; absorberar ett dödligt slag (totem-effekt, 40% hjälm-durability-förlust), 20 min cooldown i ActionBar; PvP kill-transfer; /give customitem heavenly remove <spelare> tar bort förmågan; "Heavenly" i guldtext i tab, chat och namnlapp

## Struktur

- `mods/` — alla Fabric-mods (se ##MODS ovan)
- `docs/` — Markdown-dokumentation, synkas automatiskt till GitHub Wiki
- `.github/workflows/` — CI/CD (bygge, test, deploy, wiki-sync)

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

## Serverkonfiguration — låsta värden

Följande inställningar i `server/server.properties` är permanenta och får **aldrig** ändras:

| Inställning | Värde | Motivering |
|---|---|---|
| `difficulty` | `hard` | Servern ska alltid köras på Hard — inget undantag |

Ändra inte dessa värden vid `!restore server`, konfigurationsjusteringar eller andra operationer. Om en restore skulle skriva över ett låst värde ska det skrivas tillbaka direkt efteråt.

## Arbetsregler för Claude Code

- Sök på webben för aktuella versioner av Fabric-beroenden innan du antar.
- Följ `CLAUDE.md` i respektive undermapp för mod-specifika instruktioner.
- Skapa aldrig filer utanför den mapp du är grundad i, utom när en specifik instruktion tillåter det (t.ex. dokumentation till `docs/`).
- Kör alltid `./gradlew test` efter kodändringar. Commita inte röda tester.
- **Wiki-regel:** När en ny komponent, mod eller infrastrukturändring läggs till ska motsvarande dokumentation skapas eller uppdateras i `docs/`. Committa alltid doc-filen i samma commit som koden.
- **CLAUDE.md-regel:** När en ny Mixin-fallgrop, MC API-quirk eller process-regel upptäcks under utveckling ska den dokumenteras i `CLAUDE.md` (rätt sektion, eller ny sektion vid behov) i samma commit som bugfixen eller koden som triggade insikten.
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

### Ange alltid full descriptor när en klass har överlagrade metoder

Utan descriptor väljer Mixin själv bland matchande metoder — vilket kan vara fel overload. Resultatet är `InvalidInjectionException: Invalid descriptor` vid serverstart, inte vid kompilering eller test.

**Regel:** Skriv alltid full descriptor i `method`-attributet om målklassen har mer än en metod med samma namn.

```java
// FEL — Mixin kan välja fel overload (t.ex. void-varianten)
@Inject(method = "generateLoot", at = @At("RETURN"))

// RÄTT — pekar ut exakt den overload som returnerar ObjectArrayList
@Inject(
    method = "generateLoot(Lnet/minecraft/loot/context/LootWorldContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
    at = @At("RETURN")
)
```

Descriptorn använder Yarn-mappade MC-klassnamn (Loom remapar dem till intermediary vid bygget). Kontrollera tillgängliga overloads med `javap` på den Yarn-mappade MC-jarn:

```bash
MCJAR=$(find ~/.gradle/caches/fabric-loom -name "minecraft-merged-*.jar" ! -name "*sources*" ! -name "*.backup" | head -1)
jar xf "$MCJAR" net/minecraft/loot/LootTable.class && javap -p LootTable.class | grep generateLoot && rm -rf net/
```

Verkligt fall: `LootTableGlobalBanMixin` i `world_tweak_ancient` v1.0.0–1.0.2 matchade `generateLoot(LootWorldContext, long, Consumer)` (void) istället för `generateLoot(LootWorldContext)` → `ObjectArrayList`. Servern kraschade vid varje start tills descriptorn sattes explicit i v1.0.3.

### Föredra `@ModifyArg` framför `@Redirect`

`@Redirect` kräver att du anropar målmetoden igen i handleren — om metoden inte är direkt åtkomlig i Yarn-Java uppstår kompilatorfel. `@ModifyArg` ändrar bara ett argument och låter Mixin sköta anropet. Välj `@ModifyArg` när du bara vill justera ett värde (t.ex. ett damage-float).
