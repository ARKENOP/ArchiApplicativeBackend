# 🎭 API Réservation Théâtre - Backend

API REST pour la gestion des réservations de spectacles de théâtre.

## 📋 Table des matières

- [Fonctionnalités](#-fonctionnalités)
- [Technologies](#-technologies)
- [Améliorations implémentées](#-fonctionnalités-implémentées)
- [Démarrage rapide](#-démarrage-rapide)
- [Documentation](#-documentation)
- [Architecture](#-architecture)
- [Endpoints](#-endpoints-principaux)

## ✨ Fonctionnalités

### Côté Utilisateur
- 📋 Consulter la liste des spectacles (paginée, triée, mise en cache)
- 🎫 Réserver des billets avec vérification automatique de disponibilité
- 📱 Consulter ses réservations
- ❌ Annuler une réservation (avec remise en disponibilité des billets)

### Côté Administrateur
- ➕ Créer de nouveaux spectacles
- ✏️ Modifier les spectacles existants
- 🗑️ Supprimer des spectacles
- 📊 Consulter les statistiques de ventes détaillées

## 🛠 Technologies

- **Spring Boot 3.5.7** - Framework Java moderne
- **Spring Security + OAuth2** - Sécurité via Keycloak (JWT)
- **Spring Data JPA** - ORM et gestion de la persistance
- **PostgreSQL** - Base de données relationnelle
- **Caffeine** - Cache haute performance
- **SpringDoc OpenAPI 3** - Documentation Swagger automatique
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances
- **Java 21** - Dernière version LTS

## 🎯 Fonctionnalités implémentées

### 1. **Documentation Swagger/OpenAPI complète** ✅
- Interface Swagger UI interactive : http://localhost:8080/swagger-ui.html
- Documentation détaillée de tous les endpoints
- Exemples de requêtes et réponses
- Support de l'authentification JWT
- Groupement par tags métier

### 2. **Gestion d'erreurs** ✅
- Exceptions personnalisées (`ResourceNotFoundException`, `InsufficientTicketsException`, `UnauthorizedAccessException`)
- `GlobalExceptionHandler` centralisé
- Messages d'erreur en français
- Logging structuré de toutes les erreurs
- Format de réponse JSON standardisé

### 3. **Architecture DTOs et Mappers** ✅
- Séparation claire entre entités JPA et DTOs
- `SpectacleRequest` / `SpectacleResponse`
- Validation Jakarta Bean Validation sur les DTOs
- Mapper dédié pour les conversions
- Messages de validation en français

### 4. **Auditing JPA automatique** ✅
- Traçabilité : `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
- Configuration avec `@EnableJpaAuditing`
- Extraction automatique du user depuis le contexte de sécurité
- Historique complet des modifications

### 5. **Cache Caffeine** ✅
- Cache des spectacles (5 minutes, max 100 entrées)
- Cache des statistiques
- Invalidation automatique lors des modifications
- Configuration avec métriques

### 6. **Pagination et tri** ✅
- Support de la pagination sur toutes les listes
- Tri personnalisable (date, titre, prix, etc.)
- Paramètres : `page`, `size`, `sort`
- Requêtes optimisées

### 7. **Logging professionnel** ✅
- Logs différenciés par niveau (DEBUG, INFO, WARN, ERROR)
- Logging des opérations métier importantes
- Configuration par profil d'environnement
- Format de log clair et lisible

### 8. **Profils d'environnement** ✅
- **dev** : Logs détaillés, SQL visible
- **prod** : Logs minimaux, sécurité renforcée, stacktraces masquées
- Configuration externalisée

### 9. **Sécurité renforcée** ✅
- Vérification que le spectacle est dans le futur
- Impossible d'annuler une réservation passée
- Versioning optimiste avec `@Version`
- Contrôle d'accès par rôles (USER, ADMIN)
- Protection CSRF, CORS configuré

### 10. **Validation robuste** ✅
- Contraintes métier sur tous les champs
- Messages d'erreur explicites en français
- Validation des dates futures
- Limites de prix et quantités

### 12. **Optimisations base de données** ✅
- Index sur les colonnes fréquemment recherchées
- JOIN FETCH pour éviter N+1
- HikariCP optimisé
- Requêtes natives pour les statistiques
- Batch insert/update activé

### 13. **Endpoints utilitaires** ✅
- `/api/info` - Informations sur l'API
- `/api/health` - Health check simple
- `/api/test/hello` - Test d'authentification
- Tous accessibles via Swagger

## 🚀 Démarrage rapide

### Prérequis
- Java 21
- Maven 3.8+
- PostgreSQL 14+
- Keycloak configuré

### Installation

1. **Cloner le projet**
```bash
git clone <repository-url>
cd Backend
```

2. **Configurer la base de données**
```bash
# Avec Docker Compose
docker-compose up -d

# Ou créer manuellement
createdb archiapp
```

3. **Configurer les variables d'environnement** (optionnel)
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=archiapp
export DB_USER=postgres
export DB_PASSWORD=postgres
```

4. **Compiler et démarrer**
```bash
# Développement
mvn spring-boot:run

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

L'application démarre sur **http://localhost:8080**

## 📚 Documentation

### Swagger UI (Recommandé) 🎯
**http://localhost:8080/swagger-ui.html**

Interface interactive pour tester tous les endpoints avec :
- Description détaillée de chaque endpoint
- Exemples de requêtes/réponses
- Possibilité de tester directement avec authentification JWT

### OpenAPI JSON
http://localhost:8080/v3/api-docs

## 🏗 Architecture

```
src/main/java/epsi/archiapp/backend/
├── config/              # Configuration Spring
│   ├── CacheConfig.java
│   ├── JpaAuditingConfig.java
│   ├── OpenAPIConfig.java
│   └── SecurityConfig.java
├── controller/          # Contrôleurs REST
│   ├── AdminController.java
│   ├── ReservationController.java
│   ├── SpectacleController.java
│   └── TestController.java
├── dto/                 # Data Transfer Objects
│   ├── ApiInfoResponse.java
│   ├── ReservationRequest.java
│   ├── ReservationResponse.java
│   ├── SpectacleRequest.java
│   ├── SpectacleResponse.java
│   └── StatsResponse.java
├── exception/           # Exceptions personnalisées
│   ├── GlobalExceptionHandler.java
│   ├── InsufficientTicketsException.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedAccessException.java
├── mapper/              # Mappers DTO <-> Entity
│   └── SpectacleMapper.java
├── model/               # Entités JPA
│   ├── Reservation.java
│   └── Spectacle.java
├── repository/          # Repositories Spring Data
│   ├── ReservationRepository.java
│   └── SpectacleRepository.java
├── service/             # Logique métier
│   ├── ReservationService.java
│   └── SpectacleService.java
└── util/                # Utilitaires
    └── JwtUtils.java
```

## 📍 Endpoints principaux

### Publics (sans authentification)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/info` | Informations sur l'API |
| GET | `/api/health` | Health check |
| GET | `/api/spectacles` | Liste des spectacles (paginée) |
| GET | `/api/spectacles/{id}` | Détails d'un spectacle |

### Authentifiés (token JWT requis)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/reservations` | Créer une réservation |
| GET | `/api/reservations` | Mes réservations |
| GET | `/api/reservations/{id}` | Détails d'une réservation |
| DELETE | `/api/reservations/{id}` | Annuler une réservation |

### Administrateurs (rôle ADMIN requis)
| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/spectacles` | Créer un spectacle |
| PUT | `/api/spectacles/{id}` | Modifier un spectacle |
| DELETE | `/api/spectacles/{id}` | Supprimer un spectacle |
| GET | `/api/admin/stats` | Statistiques de ventes |

## 🔐 Authentification

L'API utilise OAuth2/JWT via Keycloak.

**Headers requis pour les endpoints authentifiés :**
```
Authorization: Bearer {votre-token-jwt}
```
## 🧪 Tests

```bash
# Exécuter tous les tests
mvn test

# Exécuter les tests avec coverage
mvn clean test jacoco:report
```

## 📦 Build

```bash
# Compiler le projet
mvn clean package

# Le JAR sera créé dans target/Backend-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker

```bash
# Démarrer avec Docker Compose
docker-compose up -d
```

## 🤝 Contribution

Ce projet a été développé selon les bonnes pratiques Spring Boot pour un cours d'Architecture Applicative à l'EPSI.

## 📝 Licence

MIT

---

**Auteurs** : I1 Dev2 EPSI Nantes
**Version** : 1.0.0
**Date** : 2025
