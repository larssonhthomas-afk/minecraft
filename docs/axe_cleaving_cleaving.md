# Axe Cleaving — Mod-dokumentation

## Spelmekanik

**Cleaving** är en permanent förmåga som kan appliceras på vilken yxa som helst.

- **Shield-disable bonus:** När en spelare med en Cleaving-yxa disablar en motståndares sköld tar motståndaren automatiskt **3 extra HP (1,5 hjärtan)** i samma slag.
- **Containerblockering:** En Cleaving-yxa kan inte placeras i chest, trapped chest eller shulker box.

## Ge ut boken

```
/give customitem Cleaving           → ger boken till den som kör kommandot
/give customitem Cleaving <spelare> → ger boken till angiven spelare
```

Kräver operatörsbehörighet (nivå 2).

## Applicera förmågan

1. Ha en yxa i ena handen och boken i andra handen.
2. Högerklicka med boken.
3. Boken förbrukas och yxan märks permanent med Cleaving.

## Arkitektur

| Klass | Roll |
|---|---|
| `CleavingLogic` | Ren logik utan MC-imports: axe-ID-lista, NBT-nyckelkonstanter, extra damage |
| `AxeCleavingCleavingMod` | Entrypoint: kommandoregistrering, `UseItemCallback` för boksapplikation |
| `ShieldDisableMixin` | Mixin på `ServerPlayerEntity.damage()`: detekterar shield-disable och applicerar extradamage |
| `ContainerBlockMixin` | Mixin på `ScreenHandler.onSlotClick()`: blockerar Cleaving-yxan i chest/shulker |

## NBT-märkning

Cleaving-yxan identifieras via `DataComponentTypes.CUSTOM_DATA` med nyckeln `axe_cleaving_cleaving = true`.
Boken identifieras med nyckeln `axe_cleaving_cleaving_book = true`.

## Bygge

```bash
cd mods/axe_cleaving_cleaving
./gradlew test    # kör JUnit 5-tester
./gradlew build   # bygger axe_cleaving_cleaving-1.0.0.jar
```

Resulterande JAR kopieras till `server/mods/`.
