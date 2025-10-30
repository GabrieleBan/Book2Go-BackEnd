#!/bin/bash
set -e
echo "Avvio Neo4j..."
neo4j start &
echo "Attendo Neo4j..."
until cypher-shell -u neo4j -p "$NEO4J_AUTH_PASSWORD" "RETURN 1;" >/dev/null 2>&1; do
  sleep 1
done
DB_NAME=${DB_NAME:-b2g_graph}
GUEST_USER=${GUEST_USER:-guest}
GUEST_PASSWORD=${GUEST_PASSWORD:-guest}
echo "Creazione database $DB_NAME..."
cypher-shell -u neo4j -p "$NEO4J_AUTH_PASSWORD" "CREATE DATABASE $DB_NAME IF NOT EXISTS;"
echo "Creazione utente $GUEST_USER..."
cypher-shell -u neo4j -p "$NEO4J_AUTH_PASSWORD" <<EOF
CREATE USER $GUEST_USER SET PASSWORD '$GUEST_PASSWORD' CHANGE NOT REQUIRED IF NOT EXISTS;
GRANT ROLE admin TO $GUEST_USER;
GRANT ALL ON DATABASE $DB_NAME TO $GUEST_USER;
EOF
echo "Esecuzione script iniziali..."
for f in /docker-entrypoint-initdb.d/*.cypher; do
  if [ -f "$f" ]; then
    echo "Eseguo $f..."
    cypher-shell -u neo4j -p "$NEO4J_AUTH_PASSWORD" -d "$DB_NAME" < "$f"
  fi
done
echo " Setup completato ✅ — Neo4j in esecuzione..."
exec neo4j console