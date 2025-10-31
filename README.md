# Backend - Billetterie Théâtre

API Spring Boot pour gérer des spectacles, réservations et statistiques.

## Démarrage rapide

1. Lancer Postgres via Docker (optionnel si vous avez déjà un Postgres local):

```cmd
docker compose up -d
```

2. Lancer l'application:

```cmd
mvn spring-boot:run
```

La config par défaut pointe sur `jdbc:postgresql://localhost:5432/archiapp` (user: postgres / password: postgres). Possible de surcharger via variables d'env: `DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD`.

## Endpoints principaux

- GET /api/spectacles
- GET /api/spectacles/{id}
- POST /api/spectacles
- PUT /api/spectacles/{id}
- DELETE /api/spectacles/{id}

- GET /api/reservations/user/{userId}
- POST /api/reservations { userId, spectacleId, quantity }
- GET /api/reservations/stats/total-sales
- GET /api/reservations/stats/by-spectacle

CORS autorise http://localhost:3000 et http://localhost:5173.
