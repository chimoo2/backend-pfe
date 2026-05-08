<div align="center">

# 🛠️ Backend — Technologies & Bibliothèques
### Projet CapTalent · Talent Intelligence Platform

</div>

---

## 🗣️ Langage

| Langage | Version | Rôle |
|:---|:---:|:---|
| **Java** | `17` | Langage principal du backend |

---

## ⚙️ Framework principal

| Bibliothèque | Version | Rôle |
|:---|:---:|:---|
| **Spring Boot** | `3.2.5` | Socle applicatif — démarrage, auto-configuration, DI |
| **Spring Boot Starter Web** | `3.2.5` | Exposition d'une API REST (controllers, JSON) |
| **Spring Boot Starter WebFlux** | `3.2.5` | Client HTTP réactif (`WebClient`) pour appeler les microservices Python |

---

## 🗄️ Base de données & Persistance

| Bibliothèque | Version | Rôle |
|:---|:---:|:---|
| **Spring Boot Starter Data JPA** | `3.2.5` | Couche ORM — accès BDD via repositories |
| **Hibernate** | *(inclus JPA)* | Implémentation JPA — mapping objet-relationnel |
| **PostgreSQL Driver** | *(runtime)* | Connecteur JDBC pour la base de données PostgreSQL |
| **PostgreSQL** | `15+` | Base de données relationnelle principale |

> **Schéma :** `career_platform` · Port `5432` · Schéma public

---

## 🔐 Sécurité & Authentification

| Bibliothèque | Version | Rôle |
|:---|:---:|:---|
| **Spring Boot Starter Security** | `3.2.5` | Filtres de sécurité HTTP, gestion des rôles |
| **Auth0 Java JWT** | `4.4.0` | Génération et validation des tokens JWT (HS256) |
| **BCryptPasswordEncoder** | *(Spring Security)* | Hachage des mots de passe |

> Rôles gérés : **ADMIN**, **MANAGER**, **EMPLOYE**

---

## ✉️ Email

| Bibliothèque | Version | Rôle |
|:---|:---:|:---|
| **Spring Boot Starter Mail** | `3.2.5` | Envoi d'emails via SMTP (JavaMailSender) |
| **Gmail SMTP** | *(externe)* | Serveur d'envoi — réinitialisation de mot de passe |

---

## ✅ Validation

| Bibliothèque | Version | Rôle |
|:---|:---:|:---|
| **Spring Boot Starter Validation** | `3.2.5` | Validation des DTOs via annotations (`@Valid`, `@NotNull`, etc.) |

---

## 🧰 Outillage & Utilitaires

| Bibliothèque | Version | Rôle |
|:---|:---:|:---|
| **Lombok** | *(dernière)* | Réduction du boilerplate : `@Getter`, `@Setter`, `@Slf4j`, `@Builder` |
| **Maven** | `3+` | Gestionnaire de dépendances et outil de build |
| **Spring Boot Maven Plugin** | `3.2.5` | Packaging en JAR exécutable |
| **Maven Compiler Plugin** | `3+` | Compilation Java 17 source/target |

---

## 🧪 Tests

| Bibliothèque | Version | Rôle |
|:---|:---:|:---|
| **Spring Boot Starter Test** | `3.2.5` | JUnit 5, Mockito, AssertJ, Spring Test Context |
| **JUnit 5** | *(inclus)* | Framework de tests unitaires et d'intégration |
| **Mockito** | *(inclus)* | Mock des dépendances dans les tests unitaires |

> Tests existants : `CareerApplicationTests`, `ProjectControllerTest`, `ProjectServiceUnitTest`

---

## 📁 Upload de fichiers

| Élément | Valeur | Rôle |
|:---|:---:|:---|
| **Spring Multipart** | `10 MB max` | Réception et stockage des CVs uploadés |
| **Répertoire de stockage** | `uploads/cvs/` | Stockage local des fichiers PDF/CV |
| **Formats acceptés** | PDF, DOC, DOCX… | Documents envoyés au microservice IA |

---

## 🤖 Intégration Microservices IA

| Élément | Valeur | Rôle |
|:---|:---:|:---|
| **WebClient (WebFlux)** | réactif | Appels HTTP non-bloquants vers les microservices Python |
| **AI Extraction Service** | `http://localhost:8001` | Extraction de compétences depuis un CV (endpoint `/extract`) |
| **Matching Microservice** | `http://localhost:8001` | Matching employés ↔ projets par compétences |

---

## 🏗️ Architecture & Patterns

| Pattern | Description |
|:---|:---|
| **Architecture en couches** | `Controller → Service → Repository → Entity` |
| **REST API** | Endpoints JSON exposés sur le port `8081` |
| **DTO Pattern** | Séparation entités JPA ↔ objets de transfert (DTOs) |
| **Mapper** | `ProjectMapper`, `UserMapper` — conversion DTO ↔ Entity |
| **Stateless (JWT)** | Aucune session serveur — authentification par token |
| **CORS configuré** | Autorise les requêtes depuis le frontend (`localhost:5173`) |
| **Global Exception Handler** | Gestion centralisée des erreurs HTTP |
| **Data Initializer** | Création de l'admin par défaut au démarrage |
| **SPA Redirect** | Redirection des routes frontend vers `index.html` |

---

## 📦 Structure des packages

```
com.example.career/
├── config/        → SecurityConfig, WebConfig, CORS, DataInitializer, GlobalExceptionHandler
├── controller/    → AuthController, AdminController, ProjectController, SkillController, ProfileController, DocumentController
├── dto/           → LoginRequest, RegisterRequest, ProjectDto, UserDto, DocumentDTO…
├── mapper/        → ProjectMapper, UserMapper
├── model/         → User, Project, Role, Document, TeamMember, RequiredSkill, SkillTaxonomy…
├── repository/    → UserRepository, ProjectRepository, DocumentRepository…
├── security/      → JwtService, JwtAuthenticationFilter
└── service/       → AuthService, UserService, ProjectService, CvService, AiExtractionService, EmailService
```

---

<div align="center">
  <sub>CapTalent · 2026</sub>
</div>
