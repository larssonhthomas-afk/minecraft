# Unbroken Chain Ability

**Mod-ID:** `unbroken_chain_ability`  
**Version:** 1.0.0  
**Typ:** Server-side Fabric mod (MC 1.21.4)  
**Ersätter:** `combat_enchant_custom`

## Spelmekanik

### Ability

Unbroken Chain är en permanent spellar-bunden ability. När en spelare har den visas `[⛓]` (guldigt, fetstil) bredvid spelarens namn i tab-list och i nametag ovanför huvudet.

### Träff-tracking

- **3 konsekutiva svärdslag** mot **samma motståndare** aktiverar bonusen.
- Från och med det 4:e slaget ger varje träff **+3% extra skada** (stackas), max **+30%** (efter 10 bonusslag).
- Ett högt **kedje-brutande ljud** (`BLOCK_CHAIN_BREAK`, volym 2.5) spelas vid varje bonusslag.

### Kedjebrott

Kedjan bryts och räknarens nollställs om:
- Spelaren tar skada (av någon källa).
- Det gått mer än **7 sekunder** mellan slagen.
- Spelaren byter måltavla.

### Crafting — Unbroken Chain-bok

```
[ ]  [Dragon Egg]  [ ]
[ ]  [Player Head] [ ]
[ ]  [ ]           [ ]
```

Slot 2 (topp-center): Dragon Egg  
Slot 5 (center): Player Head med spelprofil (t.ex. en huvud-item med spel-UUID)

Resultatet är en **Unbroken Chain**-bok med guldiga bokstäver.

### Applicering

Högerklicka boken för att aktivera abilityn permanent. Boken förbrukas.

### PvP Kill-transfer

Om en spelare **utan** Unbroken Chain dödar en spelare **med** Unbroken Chain:
- Mördaren **får** abilityn.
- Offret **förlorar** abilityn.
- Servern broadcastar ett meddelande.

## Persistens

Ability-data sparas i `<world>/unbroken_chain_ability.json`.

## Tab-display

Spelare med abilityn visas med `[⛓]` (guld, fetstilt) efter sitt namn i tab-list och nametag.
