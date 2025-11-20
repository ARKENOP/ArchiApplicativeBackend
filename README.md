# Backend - Billetterie Théâtre

API Spring Boot pour gérer des spectacles, réservations et statistiques avec authentification Keycloak.

## 🎭 Description

Application web de réservation de billets de théâtre avec :
- 🔐 **Authentification Keycloak** (OAuth2/JWT)
- 🎫 **Gestion des spectacles** (CRUD admin)
- 📝 **Système de réservations** (utilisateurs authentifiés)
- 📊 **Statistiques de ventes** (dashboard admin)

## 🚀 Démarrage rapide

### 1. Base de données

Lancer PostgreSQL via Docker :

```bash
docker compose up -d
```

Ou utiliser une instance PostgreSQL locale sur `localhost:5432`.

### 2. Lancer l'application

```bash
./mvnw spring-boot:run
```

L'API sera disponible sur **http://localhost:8080**

## 🛠️ Technologies

- **Java 21**
- **Spring Boot 3.5.7**
- **Spring Security** avec OAuth2 Resource Server
- **Spring Data JPA**
- **PostgreSQL**
- **Keycloak** (authentification)
- **Lombok**
- **Maven**

## 📡 Routes API principales

### Spectacles (publiques en lecture)
- `GET /api/spectacles` - Liste tous les spectacles
- `GET /api/spectacles/{id}` - Détails d'un spectacle
- `POST /api/spectacles` - Créer (ADMIN)
- `PUT /api/spectacles/{id}` - Modifier (ADMIN)
- `DELETE /api/spectacles/{id}` - Supprimer (ADMIN)

### Réservations (authentification requise)
- `POST /api/reservations` - Créer une réservation
- `GET /api/reservations` - Mes réservations
- `GET /api/reservations/{id}` - Une réservation
- `DELETE /api/reservations/{id}` - Annuler une réservation

### Administration (rôle ADMIN)
- `GET /api/admin/stats` - Statistiques de ventes

## 🔑 Authentification

L'API utilise Keycloak avec JWT. Toutes les requêtes protégées nécessitent un header :

```
Authorization: Bearer {votre_token_jwt}
```

### Configuration par défaut

```properties
# Keycloak
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://key.serveralin.work/realms/archiapp

# Base de données
spring.datasource.url=jdbc:postgresql://localhost:5432/archiapp
spring.datasource.username=postgres
spring.datasource.password=postgres

# CORS
Origines autorisées: http://localhost:3000, http://localhost:5173
```

## 🧪 Tests

Utilisez les fichiers HTTP dans `src/test/resources/http/` :
- `spectacle.http` - Tests des spectacles
- `reservation.http` - Tests des réservations

## 🏗️ Architecture

```
src/main/java/epsi/archiapp/backend/
├── config/              # Configuration Spring Security, CORS, init data
├── controller/          # REST Controllers
├── dto/                 # Data Transfer Objects
├── exception/           # Gestion globale des exceptions
├── model/               # Entités JPA (Spectacle, Reservation)
├── repository/          # Repositories Spring Data
└── service/             # Logique métier
```

## 🔧 Variables d'environnement

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=archiapp
DB_USER=postgres
DB_PASSWORD=postgres
```

## 🎯 Fonctionnalités

### Utilisateurs
- ✅ Consulter les spectacles disponibles
- ✅ Réserver des billets
- ✅ Consulter ses réservations
- ✅ Annuler une réservation

### Administrateurs
- ✅ Gérer le catalogue de spectacles (CRUD)
- ✅ Consulter les statistiques de ventes
- ✅ Voir le nombre de billets vendus par spectacle
- ✅ Voir le revenu total

## 📝 Notes importantes

1. **Sécurité** : Chaque utilisateur ne peut voir que ses propres réservations
2. **Concurrence** : Le champ `version` dans Spectacle gère les conflits
3. **Validation** : Les DTOs incluent des validations Jakarta
4. **Gestion d'erreurs** : Messages d'erreur clairs en français
5. **CORS** : Configuré pour React (ports 3000 et 5173)
