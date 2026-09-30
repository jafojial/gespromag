# GesProMag

Application web de gestion du catalogue produits et du stock d'un magasin : produits, catégories, mouvements de stock (entrées/sorties/ajustements), alertes de réapprovisionnement et tableau de bord.

**Stack :** Java 21 · Spring Boot · Spring MVC · Thymeleaf · Spring Data JPA · Spring Security · PostgreSQL · Flyway · Maven · Docker

---

## Sommaire

- [Prérequis](#prérequis)
- [Configuration](#configuration)
- [Lancement avec Docker Compose (recommandé)](#lancement-avec-docker-compose-recommandé)
- [Lancement en local (sans Docker)](#lancement-en-local-sans-docker)
- [Base de données PostgreSQL](#base-de-données-postgresql)
- [Premier utilisateur (administrateur)](#premier-utilisateur-administrateur)
- [Exécution des tests](#exécution-des-tests)
- [Structure du projet](#structure-du-projet)
- [Rôles et fonctionnalités](#rôles-et-fonctionnalités)

---

## Prérequis

| Mode de lancement | Outils necessaires |
|---|---|
| Docker (recommandé) | Docker et Docker Compose |
| Local (sans Docker) | Java 21 (JDK), Maven 3.9+, une instance PostgreSQL 16 accessible |

## Configuration

La configuration se fait entièrement par variables d'environnement. Copier le fichier d'exemple puis l'adapter :

```bash
cp .env.example .env
```

| Variable | Description | Valeur par défaut |
|---|---|---|
| `DB_HOST` | Hôte PostgreSQL | `localhost` (`postgres` dans Docker Compose) |
| `DB_PORT` | Port PostgreSQL | `5432` |
| `DB_NAME` | Nom de la base | `gespromag` |
| `DB_USER` | Utilisateur PostgreSQL | `gespromag` |
| `DB_PASSWORD` | Mot de passe PostgreSQL | — (à définir) |
| `APP_PORT` | Port exposé par l'application | `8080` |
| `THYMELEAF_CACHE` | Cache des templates (mettre `false` en développement) | `true` |
| `ADMIN_USERNAME` | Nom d'utilisateur de l'administrateur créé au premier démarrage | `admin` |
| `ADMIN_EMAIL` | Email de cet administrateur | `admin@gespromag.local` |
| `ADMIN_PASSWORD` | Mot de passe de cet administrateur | — (à définir) |

Ne jamais committer le fichier `.env` (déjà exclu via `.gitignore`) ni des identifiants réels dans le code.

## Lancement avec Docker Compose (recommandé)

Une seule commande suffit, une fois `.env` renseigné :

```bash
docker compose up -d
```

Cela démarre deux conteneurs :

- `postgres` : base de données PostgreSQL 16, avec un volume nommé (`postgres_data`) pour persister les données entre les redémarrages ;
- `app` : l'application Spring Boot, construite depuis le `Dockerfile` (build multi-étapes Maven), exposée sur `http://localhost:8080` (ou le port défini par `APP_PORT`).

Au premier démarrage, les migrations Flyway créent le schéma et un compte administrateur est automatiquement créé (voir [Premier utilisateur](#premier-utilisateur-administrateur)).

Pour arrêter l'application :

```bash
docker compose down
```

Pour arrêter l'application **et** supprimer les données PostgreSQL :

```bash
docker compose down -v
```

## Lancement en local (sans Docker)

1. Démarrer une instance PostgreSQL 16 accessible (par exemple via `docker compose up -d postgres`, en publiant le port 5432 si nécessaire) et créer la base/l'utilisateur correspondant aux variables `DB_*`.
2. Exporter les variables d'environnement nécessaires (voir [Configuration](#configuration)) :

   ```bash
   export DB_HOST=localhost DB_PORT=5432 DB_NAME=gespromag DB_USER=gespromag DB_PASSWORD=change-me
   export ADMIN_USERNAME=admin ADMIN_EMAIL=admin@gespromag.local ADMIN_PASSWORD=change-me
   ```

3. Lancer l'application :

   ```bash
   ./mvnw spring-boot:run
   ```

   ou, avec un Maven installé localement :

   ```bash
   mvn spring-boot:run
   ```

4. Ouvrir `http://localhost:8080`.

## Base de données PostgreSQL

Le schéma est géré exclusivement par les migrations Flyway (`src/main/resources/db/migration`) ; `ddl-auto` est en mode `validate`, aucune modification de schéma n'est effectuée en dehors des migrations. Au démarrage, Flyway applique automatiquement les migrations manquantes.

## Premier utilisateur (administrateur)

Au tout premier démarrage (table `users` vide), un compte **ADMINISTRATEUR** est créé automatiquement à partir des variables `ADMIN_USERNAME` / `ADMIN_EMAIL` / `ADMIN_PASSWORD`. Se connecter avec ces identifiants sur `/login`, puis créer les catégories, produits et autres utilisateurs depuis l'interface.

Si la table `users` n'est pas vide, ce compte n'est pas recréé : modifiez les variables `ADMIN_*` avant le tout premier démarrage pour choisir vos propres identifiants.

## Exécution des tests

```bash
mvn test
```

Les tests unitaires couvrent la logique métier critique : unicité du SKU, validation de la catégorie, génération du mouvement de stock initial (`ProductService`), calcul des quantités et refus d'un stock négatif (`StockMovementService`), et blocage de la désactivation d'une catégorie contenant des produits actifs (`CategoryService`).

## Structure du projet

```text
gespromag/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .env.example
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/gespromag/store/
    │   │   ├── config/        # Sécurité, authentification, initialisation du compte admin
    │   │   ├── controller/    # Contrôleurs MVC (une route -> une action -> un service)
    │   │   ├── dto/           # Objets de formulaire (création/édition), validés côté serveur
    │   │   ├── entity/        # Entités JPA (Product, Category, StockMovement, User) et enums
    │   │   ├── repository/    # Repositories Spring Data JPA
    │   │   ├── service/       # Logique métier (règles de gestion, transactions)
    │   │   └── StoreApplication.java
    │   └── resources/
    │       ├── db/migration/  # Migrations Flyway (schéma PostgreSQL)
    │       ├── static/        # CSS, JS, polices (charte graphique et gabarit d'interface)
    │       └── templates/     # Vues Thymeleaf (auth, dashboard, products, categories, stock, users, fragments)
    └── test/
        └── java/com/gespromag/store/service/   # Tests unitaires des services
```

## Rôles et fonctionnalités

| Fonctionnalité | Administrateur | Gestionnaire |
|---|---|---|
| Tableau de bord | Oui | Oui |
| Produits (consulter, créer, modifier) | Oui | Oui |
| Produits (désactiver) | Oui | Non |
| Catégories (consulter, créer, modifier, désactiver) | Oui | Non |
| Stock (mouvements, alertes) | Oui | Oui |
| Utilisateurs (créer, modifier, rôles) | Oui | Non |

Règles de gestion notables :

- le SKU d'un produit est unique ;
- la quantité en stock d'un produit ne se modifie que via un mouvement de stock (entrée, sortie, ajustement) — jamais directement depuis le formulaire de modification du produit — afin que chaque variation reste tracée ;
- un mouvement qui ferait passer le stock sous zéro est refusé ;
- une catégorie contenant encore des produits actifs ne peut pas être désactivée ;
- la suppression d'un produit est une désactivation logique (le produit et son historique de mouvements sont conservés).
