# LifeSteal — översikt

LifeSteal är en Fabric-mod för Minecraft **1.21.4** som ändrar PvP-dynamiken på vår server. Varje gång en spelare dödar en annan spelare flyttas ett hjärta (2 HP max health) permanent från offret till mördaren.

## Spelmekanik

- **Startläge:** Alla spelare börjar på 10 hjärtan (20 HP, vanilla Minecraft).
- **Takvärde:** En spelare kan maximalt nå 20 hjärtan (40 HP).
- **Golv:** När en spelare går under 2 hjärtan (4 HP) räknas de som *eliminerade* och sätts automatiskt till spectator-läge.
- **Endast PvP räknas.** Dödas man av zombier, fall, lava eller /kill påverkas inga hjärtan.
- **Självskador/självmord** räknas inte heller — killer måste vara en annan spelare.
- **Mördaren i full hälsa:** Om mördaren redan är på 20 hjärtan sker ingen överföring. Hjärtat "tappas" snarare än att dubbelräknas.
- **Offret på gränsen:** Om offret har exakt 2 hjärtan och dör flyttas det sista hjärtat till mördaren, offret hamnar under golvet → eliminerad.

## Exempel

| Mördare före | Offer före | Mördare efter | Offer efter | Notering |
| --- | --- | --- | --- | --- |
| 10 hjärtan | 10 hjärtan | 11 hjärtan | 9 hjärtan | Vanlig kill |
| 20 hjärtan | 10 hjärtan | 20 hjärtan | 10 hjärtan | Mördaren i tak — inget händer |
| 19,5 hjärtan | 10 hjärtan | 20 hjärtan | 9,5 hjärtan | Partiell överföring (0,5 hjärtan) |
| 10 hjärtan | 2 hjärtan | 11 hjärtan | 1 hjärta → **eliminerad** | Sista hjärtat tas |

## Broadcast

När en överföring sker får alla på servern ett chatmeddelande:

```
<Mördare> stal ett hjärta från <Offer>
```

Om offret blir eliminerat följer ett extra meddelande:

```
<Offer> har eliminerats och är nu åskådare.
```

## Konfiguration

Standardvärdena är hårdkodade i `HeartManager.createDefault()`:

| Parameter | Värde | Betydelse |
| --- | --- | --- |
| `minHealth` | 4.0 HP (2 hjärtan) | Under detta → eliminerad |
| `maxHealth` | 40.0 HP (20 hjärtan) | Takvärdet |
| `heartsToSteal` | 1.0 | Antal hjärtan per kill |

Att ändra balansen kräver i nuläget en kodändring i `LifeStealMod.onInitialize()`. Exempel — hårdare server med 2 hjärtan per kill och lägre tak:

```java
heartManager = new HeartManager(4.0f, 30.0f, 2.0f);
```

En extern konfig-fil är medvetet utelämnad i första versionen — mindre yta att underhålla, lätt att lägga till senare.

## Persistens

Spelarnas nuvarande max-HP sparas i världens `lifesteal.json`:

```json
{
  "11111111-1111-1111-1111-111111111111": 22.0,
  "22222222-2222-2222-2222-222222222222": 4.0
}
```

Filen skrivs om efter varje PvP-kill (atomic rename via `.tmp`) och laddas när servern startar. Det innebär att en serverkrasch i värsta fall tappar noll händelser — all state är redan på disk så fort hjärtat flyttats.

## Vad modden **inte** gör

- Ingen återanvändning av hjärtan via crafting (kan läggas till senare).
- Ingen "revive"-mekanik för eliminerade spelare.
- Ingen GUI/HUD — klienten visar bara fler/färre hjärtan via vanilla-attributet.
- Ingen klientkomponent. Modden körs server-only och kräver inte att spelare installerar något.
