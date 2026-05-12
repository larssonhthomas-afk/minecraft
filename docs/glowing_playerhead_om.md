# glowing_playerhead_om

Mod som låter spelare använda spelarhuvuden för att ge Glowing-effekt till alla spelare i närheten.

## Funktion

1. Håll i ett **spelarhuvud** (med spelarprofil-data, t.ex. ett huvud som droppats vid PvP-kill).
2. **Högerklicka** — en bekräftelsemeny visas i chatten.
3. Klicka **[Ja]** → huvudet konsumeras, alla spelare inom 50 block från dig (i samma dimension) får **Glowing** i **10 minuter**.
4. Klicka **[Nej]** → avbryt, inget händer.

## Detaljer

- Radien (50 block) rör sig med spelaren som aktiverade effekten — inte en låst punkt.
- Om en spelare lämnar 50-block-radien försvinner Glowing-effekten omedelbart (kontrolleras varje sekund).
- Om spelaren som aktiverade effekten loggar ut rensas alla aktiva Glowing-effekter.
- Bekräftelsemenyn går ut efter 60 sekunder utan svar.
- Fungerar med alla spelarhuvuden som har PlayerProfile-data (vanliga `PLAYER_HEAD`-items utan profil triggar inte menyn).

## Kommandon

Interna kommandon (används via clickbara chattknappar, inte manuellt):
- `/glowing_phead_confirm yes` — bekräftar användning
- `/glowing_phead_confirm no` — avbryter

## Teknisk översikt

| Komponent | Beskrivning |
|---|---|
| `GlowingSession` | Ren logik: session per aktiverare, expiry, radius-check |
| `GlowingLogic` | Hanterar aktiva sessioner (ingen MC-import) |
| `GlowingPlayerheadOmMod` | Fabric-integration: events, kommandon, effekthantering |

Tick-kontroll sker var 20:e tick (1 sekund) via `ServerTickEvents.END_SERVER_TICK`.
