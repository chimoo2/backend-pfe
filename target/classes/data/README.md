# Matching Dataset - Documentation

## 📋 Overview

Ce dossier contient un dataset complet pour démontrer et tester le système de matching des compétences.

- **employees.json** : 8 profils d'employés avec leurs compétences et niveaux
- **projects.json** : 6 projets avec leurs exigences en compétences
- **MATCHING_EXAMPLES.txt** : Exemples détaillés de matching avec scoring

---

## 👥 Employees Dataset

### Structure d'un employé

```json
{
  "id": 1,
  "name": "Alice Martin",
  "email": "alice.martin@company.com",
  "department": "Backend",
  "skills": [
    {
      "name": "Java",
      "level": "Senior",
      "yearsExperience": 8
    }
  ]
}
```

### Niveaux disponibles
- **Junior** : 0-2 ans d'expérience
- **Mid** : 3-5 ans d'expérience
- **Senior** : 6+ ans d'expérience

### Profils inclus

| ID | Nom | Département | Spécialité |
|----|-----|-------------|-----------|
| 1  | Alice Martin | Backend | Java, Spring Boot, PostgreSQL |
| 2  | Bob Chen | Full-Stack | React, Node.js, MongoDB |
| 3  | Carol Dubois | Frontend | React, Vue.js, CSS, Material-UI |
| 4  | David Engels | Backend | Python, Django, ML |
| 5  | Eve Laurent | DevOps | Kubernetes, Docker, AWS |
| 6  | Frank Müller | Full-Stack | Java/React full-stack |
| 7  | Grace Okoro | QA | Selenium, API Testing |
| 8  | Henry Sato | Backend | Node.js, Express.js, MongoDB |

---

## 🎯 Projects Dataset

### Structure d'un projet

```json
{
  "id": 101,
  "name": "E-Commerce Platform Redesign",
  "description": "Modernize our e-commerce platform",
  "manager": "John Smith",
  "status": "To Do",
  "startDate": "2026-03-15",
  "endDate": "2026-09-15",
  "requiredSkills": [
    {
      "skillName": "React",
      "level": "Senior",
      "count": 2
    }
  ]
}
```

### Projets inclus

| ID | Nom | Manager | Statut | Besoins principaux |
|----|-----|---------|--------|-------------------|
| 101 | E-Commerce Platform | John Smith | To Do | React (x2), Node.js (x2) |
| 102 | Microservices Migration | Jane Doe | In Progress | Java (x3), Spring Boot (x3) |
| 103 | DevOps Infrastructure | Robert Johnson | To Do | Kubernetes (x2), Docker (x2), AWS (x2) |
| 104 | Data Analytics Platform | Lisa Wang | To Do | Python (x2), ML (x1) |
| 105 | Mobile App Frontend | Mike Brown | To Do | React (x2), TypeScript (x1) |
| 106 | API Testing Suite | Sarah Davis | In Progress | API Testing (x2), Selenium (x1) |

---

## 🧮 Algorithme de Scoring

Chaque match est évalué selon cette formule :

```
Pour chaque compétence requise :
  - Si employé possède la compétence :
    • Cas 1: niveau >= requis : +15 points
    • Cas 2: niveau < requis : +10 points
    • Cas 3: absent : -2 points

Score final = (points / (requis × 15)) × 100
```

### Exemples de scoring

**Projet 103: DevOps Infrastructure Upgrade**
- Eve Laurent (DevOps): **98/100** ✓ MATCH PARFAIT
  - Kubernetes Senior ✓
  - Docker Senior ✓
  - AWS Senior ✓
  - Terraform Mid ✓
  - CI/CD Senior ✓

- Alice Martin (Backend): **52/100** ✗ NON CONVENABLE
  - Kubernetes Junior (requis Senior) → +10
  - Docker Mid (requis Senior) → +10
  - Machine Learning absent → -2
  - AWS absent → -2
  - CI/CD absent → -2

---

## 📊 Cas d'usage pour un PFE

### 1. **Test simple** : Faire un matching sur le projet 105
```
Input: Project 105 (Mobile App Frontend)
Expected output: Carol Dubois (95/100), Bob Chen (88/100), Frank Müller (72/100)
```

### 2. **Test modéré** : Projet 102 avec 5 employés
```
Input: Project 102 (Microservices)
Expected: Classer Alice, Frank, David, Eve, Henry
Résultat: Alice > Frank > Henry > Grace
```

### 3. **Test avancé** : Tous les projets avec tous les employés
- Créer un tableau de scoring complet (6 × 8 = 48 scores).
- Analyser les patterns de matching.
- Identifier les compétences manquantes (ex: aucun expert AWS senior).

---

## 🚀 Comment intégrer dans ton backend

### Option 1 : Charger depuis JSON (au démarrage)

```java
// DataInitializer.java
@Component
public class DataInitializer {
    @Autowired private EmployeeRepository empRepo;
    @Autowired private ProjectRepository projRepo;
    
    @PostConstruct
    public void initialize() {
        // Charger employees.json et projects.json
        // Sauvegarder en DB
    }
}
```

### Option 2 : API de chargement

```java
@PostMapping("/api/admin/load-dataset")
public ResponseEntity<String> loadDataset() {
    // Charger depuis src/main/resources/data/
    return ResponseEntity.ok("Dataset loaded");
}
```

### Option 3 : Utiliser dans les tests

```java
@Test
public void testMatchingWithRealData() {
    Project project102 = projectRepo.findById(102L);
    List<EmployeeDto> results = matchingService.findMatches(project102);
    
    assertThat(results).isNotEmpty();
    assertThat(results.get(0).getName()).isEqualTo("Alice Martin");
}
```

---

## 📝 Notes importantes

### ✓ Points forts du dataset
- Couverture diverse (8 profils × 5 départements)
- Projets réalistes avec exigences variées
- Exemples de perfect match et de mismatches
- Scorings documentés et vérifiables

### ⚠️ Limitations (à mentionner dans le PFE)
- Dataset synthétique, pas réel
- Ne couvre pas soft skills (leadership, communication)
- Pas de données historiques de succès
- Pas de gestion des disponibilités/calendrier
- Pas de constraints géographiques

### 🔮 Évolutions futures
- Intégrer avec LDAP pour sync employés réels
- Ajouter feedback utilisateur pour améliorer l'algo
- Support du matching multi-critères (budget, localisation)
- Version ML si données historiques disponibles

---

## 📞 Support

Pour charger ce dataset dans ton application :

1. Crée un endpoint `/api/admin/load-demo-data`
2. Charge les deux fichiers JSON
3. Insère dans DB
4. Teste avec l'UI (clic sur "Voir matching")

Les résultats doivent correspondre à ceux de `MATCHING_EXAMPLES.txt`.
