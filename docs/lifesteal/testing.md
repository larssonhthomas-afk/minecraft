# LifeSteal — testning

## Snabbstart

```bash
cd lifesteal
./gradlew test
```

Förväntat resultat: **21 tester, 0 failures, 0 errors**. Körtiden är sub-sekund när Gradle-daemonen är varm.

Rapport hittas i `build/reports/tests/test/index.html` efter körning.

## Vad testerna täcker

Alla tester ligger i `src/test/java/com/lifesteal/logic/HeartManagerTest.java`. De är grupperade enligt:

### Konstruktor-validering (7 tester)

- Negativ `minHealth` → `IllegalArgumentException`
- `maxHealth == minHealth` → `IllegalArgumentException`
- `maxHealth < minHealth` → `IllegalArgumentException`
- `heartsToSteal == 0` → `IllegalArgumentException`
- Negativ `heartsToSteal` → `IllegalArgumentException`
- `NaN` → `IllegalArgumentException`
- `POSITIVE_INFINITY` → `IllegalArgumentException`

### Default + konstanter (3 tester)

- `createDefault()` ger min=4, max=40, steal=1
- `HEALTH_PER_HEART == 2.0`
- `clampToRange` klämmer värden till `[0, max]`

### `transferHearts` — huvudlogik (5 tester)

- Normal PvP-kill: mördare +2, offer −2, `transferOccurred=true`
- Mördare på max: ingen överföring, `transferOccurred=false`
- Mördare nära max (39 → 40): partiell överföring (1 HP)
- Offer på min (4 HP): hjärtat tas, offret går till 2 HP, `victimEliminated=true`
- Offer på 0: ingen överföring, `victimEliminated=true`

### `isAtMaximumHealth` + `isEliminated` (4 tester)

- `isAtMaximumHealth` är `true` vid max, annars `false`
- `isEliminated` är `true` under minHealth (inkl. 0), annars `false`

### Anpassade konfigurationer (2 tester)

- 2 hjärtan per kill → 4 HP transfereras
- Lägre tak (20 HP) → partiell clamp fungerar

Totalt: **21 tester**.

## Varför unit-tester räcker för kärnlogiken

Spelregeln *"flytta ett hjärta från offret till mördaren"* är i grunden en aritmetisk operation: två flyttal in, två flyttal ut, några gränsfall. Att starta en full Minecraft-server, dra igång en dev-klient, koppla upp en andra spelare och verifiera i bläddrarverktyg att maxHealth-attributet gick från 20 till 18 — det är 30 minuter per iteration.

Separationen i `HeartManager` gör att den hela loopen "ändra regel → verifiera" tar **under en sekund**. Om man ändrar balansen (t.ex. 1,5 hjärtan per kill) räcker det oftast att skriva ett nytt test, se det bli rött, ändra defaulten och se det gå grönt.

### Vad som **inte** kan unit-testas

- **Mixinens injection-point** — att `onDeath(DamageSource)` faktiskt finns och har rätt signatur i 1.21.4. Detta fångas vid `./gradlew build` (remap-steget validerar mot mappings).
- **`DamageSource.getAttacker()`** returnerar rätt entitet i alla PvP-scenarion — kräver en riktig server.
- **`player.changeGameMode(SPECTATOR)`** har de sidoeffekter vi tror (tappar items, visar åskådarkamera) — kräver en riktig klient.

Dessa punkter ska verifieras manuellt innan release, men är **inte** regressionsrisker när man ändrar balansparametrar — vilket är 90 % av det underhåll som faktiskt händer.

## Köra enbart ett test

```bash
./gradlew test --tests "com.lifesteal.logic.HeartManagerTest.transfer_normalKill_movesOneHeart"
```

## Manuell in-game-verifiering

Se `deployment.md` för hur jar:en kommer upp på testservern. Minimal smoke-test:

1. Två testspelare joinar — båda ska få 10 hjärtan.
2. A dödar B — kollen chat: "A stal ett hjärta från B". A har 11, B har 9.
3. Upprepa tills B har 2 hjärtan och dör — B ska gå till spectator.
4. Starta om servern — bägges hjärtvärden ska ligga kvar från `lifesteal.json`.
