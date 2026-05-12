# Heavenly — Översikt

Heavenly är en permanent spellarförmåga som ger spelaren ett automatiskt skydd mot ett dödligt slag, med 20 minuters cooldown.

## Spelmekanik

### Skaffa förmågan
1. En admin kör `/give customitem heavenly` — spelaren får en guldig bok med texten **Heavenly**.
2. Spelaren högerklickar med boken i handen. Boken förbrukas och förmågan appliceras permanent.

### Heavenly-skyddet
När spelaren med Heavenly tar ett dödligt slag (och cooldown INTE är aktiv):
- Döden avbryts.
- Totem-animation och -ljud spelas upp för alla i närheten.
- Spelaren återfår 1 HP och får statuseffekterna:
  - **Brandresistans** (45 s)
  - **Absorption II** (5 s)
  - **Regenerering II** (45 s)
- Hjälmen tar **40% av sin max durability** i skada (kan gå sönder).
- Cooldown på **20 minuter** aktiveras och visas i ActionBar.

### Cooldown
Under de 20 minuterna visas en räknare i ActionBar (`[Heavenly] Cooldown: MM:SS`).  
Om spelaren tar ett dödligt slag under cooldown aktiveras INTE skyddet — spelaren dör och Heavenly överförs till mördaren (se PvP-transfer).

### PvP kill-transfer
- Om spelaren med Heavenly dödas av en annan spelare (t.ex. under cooldown) → Heavenly flyttas till mördaren.
- Om spelaren dör av icke-PvP-skada → Heavenly tas bort utan transfer.

### Ta bort förmågan
`/give customitem heavenly remove <spelarnamn>` — kräver op-nivå 2.

## Namnvisning
Spelaren med Heavenly visas som `Spelarnamn §6Heavenly` i:
- Tab-listan
- Chatten (via `getDisplayName()`)
- Namnlappen ovanför karaktären
