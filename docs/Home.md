# Minecraft Monorepo

Welcome till wikin för vår privata Minecraft-server. Allt innehåll speglas automatiskt från `docs/` i [huvudrepot](https://github.com/larssonhthomas-afk/minecraft) vid push till `main`.

## Mods

### LifeSteal

Fabric-mod som permanent överför hjärtan mellan spelare vid PvP-död.

- [Översikt](lifesteal/overview)
- [Arkitektur](lifesteal/architecture)
- [Testning](lifesteal/testing)
- [Deployment](lifesteal/deployment)

## Infrastruktur

- Host: Hetzner CX23 (Ubuntu 22.04)
- Minecraft: 1.21.4 med Fabric Loader 0.19.2
- Java: 21 (OpenJDK)
