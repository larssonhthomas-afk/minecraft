#!/bin/bash
# Körs efter varje gång nya filer kopieras till servermappen
# Fixar behörigheter så Crafty (crafty-användaren) kan läsa/skriva

SERVER_DIR="/var/opt/minecraft/crafty/crafty-4/servers/97dc0db9-50ed-4ecf-a28b-b4f7d2fe0908"

echo "Fixar behörigheter..."
chown -R crafty:crafty "$SERVER_DIR"
echo "Klart!"
