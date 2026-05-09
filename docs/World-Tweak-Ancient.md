# World Tweak Ancient

Fabric-mod för Minecraft 1.21.4 som tweakar Nether-relaterat innehåll: gör Ancient Debris ovanligare, tar bort Drowned-trident-spawns, blockerar valda entiteter, filtrerar items ur loot tables och hindrar Gold Blocks från att placeras under världsgenerering.

## Vad gör modden?

| Funktion | Beskrivning |
|---|---|
| Ancient Debris rarity | Ore-frekvensen styrs av `rarity_filter` i datapacken — lavare sannolikhet än vanilla |
| Drowned utan trident | Drowned spawnar aldrig med trident som main-hand-vapen |
| Entitets-blocklista | Valda entitets-ID:n hindras helt från att spawnas |
| Global loot-ban | Valda items filtreras ur **alla** loot tables |
| Bastion loot-ban | Valda items filtreras ur Bastion chest loot tables |
| Gold Block-blockering | `setBlockState` i ChunkRegion avvisas för block på Nether-banlistans |

## Arkitektur

Samma separationsmönster som resten av projektet:

| Lager | Klass | Ansvar |
|---|---|---|
| Entry point | `WorldTweakAncientMod` | Laddar listor, exponerar dem som statiska accessorer |
| Logik | `logic/AncientDebrisRarity` | Sannolikhetsberäkning, inga MC-imports |
| Logik | `logic/BastionLootBanList` | Bastionens item-bannlista |
| Logik | `logic/DrownedEquipmentRules` | Regel: trident alltid bannlyst |
| Logik | `logic/EntitySpawnBlockList` | Entitets-ID:n som ska blockeras |
| Logik | `logic/GlobalLootBanList` | Global item-bannlista |
| Logik | `logic/NetherWorldGenBlockBanList` | Block-ID:n bannlysta under Nether-världsgen |
| Mixin | `mixin/ChunkRegionGoldBlockMixin` | Injectar i `setBlockState` för att blockera bannlysta block |
| Mixin | `mixin/DrownedEntityMixin` | Injectar i `initEquipment` för att ta bort trident |
| Mixin | `mixin/LootTableGlobalBanMixin` | Injectar i `generateLoot(LootWorldContext)` för att filtrera items |
| Mixin | `mixin/ServerWorldSpawnEntityMixin` | Injectar i `spawnEntity` för att blockera entitets-ID:n |

## Bygge och installation

```bash
cd mods/world_tweak_ancient
./gradlew test   # Kör JUnit 5-tester
./gradlew build  # Bygger world_tweak_ancient-<version>.jar
cp build/libs/world_tweak_ancient-<version>.jar server/mods/
```

## Tester

JUnit 5-tester i `src/test/java/com/worldtweakancient/logic/`:

| Testklass | Vad som testas |
|---|---|
| `AncientDebrisRarityTest` | Sannolikhetsvärde inom [0.0, 1.0] |
| `BastionLootBanListTest` | Korrekt item matchas/utesluts |
| `DrownedEquipmentRulesTest` | Trident alltid bannlyst |
| `EntitySpawnBlockListTest` | Bannlysta entitets-ID:n blockeras |
| `GlobalLootBanListTest` | Global item-bannlista matchar korrekt |
| `NetherWorldGenBlockBanListTest` | Gold Block och andra block blockeras |

## Kända buggar och historik

### v1.0.3 (2026-05-09) — Bugfix: `LootTableGlobalBanMixin` kraschade servern vid start

`LootTableGlobalBanMixin` använde `@Inject(method = "generateLoot", ...)` utan full metoddescriptor. Mixin matchade fel overload — `generateLoot(LootWorldContext, long, Consumer<ItemStack>)` (void) — istället för `generateLoot(LootWorldContext)` (returnerar `ObjectArrayList<ItemStack>`). Det ledde till `InvalidInjectionException: Invalid descriptor` och servern startade inte.

**Fix:** Descriptor specificeras explicit i `@Inject`-annotationen:
```java
@Inject(
    method = "generateLoot(Lnet/minecraft/loot/context/LootWorldContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
    at = @At("RETURN")
)
```
Buggen fanns sedan v1.0.0 och triggade varje serverstart med modden aktiv.
