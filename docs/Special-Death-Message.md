# Special Death Message

Fabric-mod för Minecraft 1.21.4 som ersätter standarddödsskärmsmeddelandet med "du doooggg".

## Vad gör modden?

När en spelare dör skickas normalt ett meddelande till alla spelare, t.ex. "du dog av..." eller "du dödades av...". Denna mod ersätter det meddelandet med texten **"du doooggg"** oavsett dödsorsak.

## Arkitektur

Samma separationsmönster som lifesteal-modden:

| Lager | Klass | Ansvar |
|---|---|---|
| Logik | `com.specialdeathmessage.logic.DeathMessageLogic` | Ren Java, håller konstanten `"du doooggg"`, inga Minecraft-imports |
| Mixin | `com.specialdeathmessage.mixin.PlayerDeathMixin` | Omdirigerar `DamageTracker.getDeathMessage()` i `ServerPlayerEntity.onDeath` |
| Entry point | `com.specialdeathmessage.SpecialDeathMessageMod` | Startar modden, loggar initiering |

## Bygge och installation

```bash
cd mods/special_death_message
./gradlew test   # Kör JUnit 5-tester
./gradlew build  # Bygger special_death_message-1.0.0.jar
cp build/libs/special_death_message-1.0.0.jar /var/opt/minecraft/crafty/crafty-4/servers/.../mods/
```

## Tester

4 JUnit 5-tester på `DeathMessageLogic` (ingen Minecraft-import):

- Meddelandet är exakt `"du doooggg"`
- Meddelandet är inte null
- Meddelandet är inte tomt
- Konstanten matchar metodens returvärde
