# Deploy Services Guide

## 1. Costruisci le immagini Docker per ogni servizio

```bash
cd auth-service
docker build -t auth-service .
cd ../catalog-service
docker build -t catalog-service .
# Ripeti per gli altri servizi
```

## 2. Avviare i container dei database e delle code di messaggi (Docker Compose)

```bash
docker compose -f docker-compose.dev.db.yaml up -d
```

## 3. Avviare i container dei servizi

```bash
docker compose -f docker-compose.dev.services.yaml up -d
```

## 4. Controllare lo stato dei container

```bash
docker ps
```

## 5. Testare le connessioni ai servizi e ai database

* Verifica log dei container: `docker logs <container_name>`
* Assicurati che i servizi possano connettersi ai database usando i nomi host dei container dei database.

## 6. Creare la rete Docker condivisa (se necessario)

```bash
docker network create b2g-net
```

## 7. Pulizia dei container e della rete

```bash
docker compose -f docker-compose.services.yaml down
# opzionale: rimuovere rete
# docker network rm b2g-net
```

## 8. Deploy su Kubernetes

```bash
kubectl apply -f auth-db-secret.yaml
kubectl apply -f auth-configmap.yaml
kubectl apply -f auth-deployment.yaml
kubectl apply -f auth-service.yaml
# Ripeti per gli altri servizi
```

## 9. Controllare lo stato dei pod e servizi su Kubernetes

```bash
kubectl get pods
kubectl get svc
```
