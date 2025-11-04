#!/bin/bash
set -e

DB_LOG="/var/lib/neo4j/logs/neo4j.log"
SEC_LOG="/var/lib/neo4j/logs/security.log"

# Leggi variabili d’ambiente
NEO4J_PASSWORD=${NEO4JAUTHPASSWORD:-guest}
DB_NAME=${DB_NAME-neo4j}

echo "Configurazione:"
echo "NEO4J_PASSWORD: $NEO4J_PASSWORD"
echo ""

echo "Avvio Neo4j in background..."
neo4j console > "$DB_LOG" 2>&1 &
NEO4J_PID=$!

echo "Attendo che Neo4j sia pronto (leggendo il log)..."
( tail -f "$DB_LOG" & ) | while read -r line; do
    echo "$line"
    if [[ "$line" == *"Started."* ]]; then
        echo "✅ Server pronto!"
        break
    fi
done

# Aggiungi un piccolo margine per sicurezza
sleep 5
#echo "Cambio password predefinita (se ancora 'neo4j')..."
if cypher-shell -a bolt://localhost:7687 -u neo4j -p neo4j --change-password "$NEO4J_PASSWORD"; then
    echo "✅ Password cambiata con successo!"
else
    echo "⚠️  Tentativo di cambio password fallito — probabilmente già aggiornata."
    tail -n 50 /var/lib/neo4j/logs/neo4j.log
    tail -n 50 /var/lib/neo4j/logs/security.log
fi
echo "showdb"
cypher-shell -a bolt://localhost:7687 -u neo4j -p guest "SHOW DATABASES;"

echo "▶️ Eseguo test-node.sh — creazione nodo di test..."

cypher-shell -u neo4j -p "$NEO4JAUTHPASSWORD" <<EOF
CREATE (:TestNode {name: "hello", createdAt: datetime()});
EOF

echo "✅ Nodo di test creato!"
# Esegui tutti gli script nella cartella /init-neo4j
INIT_DIR="/init-neo4j"
if [ -d "$INIT_DIR" ]; then
    echo "Eseguo script in $INIT_DIR..."
    find "$INIT_DIR" -type f | sort | while read -r f; do
        echo "➡️  Eseguo $f..."
        case "$f" in
            *.cypher)
                cypher-shell -u neo4j -p "$NEO4J_PASSWORD" < "$f" || echo "❌ Errore in $f"
                ;;
            *.sh)
                bash "$f" || echo "❌ Errore in $f"
                ;;
            *)
                echo "⚠️  Ignorato $f (estensione non riconosciuta)"
                ;;
        esac
    done
else
    echo "ℹ️  Nessuna directory /init-neo4j trovata, skip."
fi

echo "✅ Setup completato, Neo4j in esecuzione."
wait $NEO4J_PID