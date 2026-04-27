# Tests unitaires — Microservice Réclamation

Ce document résume les tests ajoutés, comment les lancer, et quoi présenter à l’évaluation.

## 1) Fichiers de tests (backend)

| Fichier | Rôle |
|--------|------|
| `src/test/java/esprit/reclamation/services/ReclamationServiceImplTest.java` | Service métier : création, stub utilisateur / inactif, changement de statut (avec / sans historique), statistiques agrégées, réponse admin |
| `src/test/java/esprit/reclamation/services/CsatServiceTest.java` | CSAT : soumission OK, refus si statut non éligible, doublon, moyenne globale |
| `src/test/java/esprit/reclamation/services/CategorisationServiceTest.java` | Catégorisation / SLA (facturation, technique, urgent, défaut) |
| `src/test/java/esprit/reclamation/controller/ReclamationControllerTest.java` | `@WebMvcTest` : POST créer, GET liste, PUT répondre, GET par user |
| `src/test/java/esprit/reclamation/ReclamationApplicationTests.java` | Test de contexte Spring (`@SpringBootTest`, H2) |

**Technologies :** JUnit 5, Mockito, AssertJ, Spring `MockMvc` (controller).

**Résultat type :** `Tests run: 21`, `Failures: 0`, `BUILD SUCCESS` (nombre exact selon Surefire).

### Rappel utile pour la soutenance

- **JUnit** : structure des tests (`@Test`, exécution, rapport).
- **Mockito** : `@Mock`, `when`, `verify` — isoler dépôts et clients Feign.
- **Test unitaire** : une classe testée, dépendances simulées, rapide.
- **Test de contexte** : `ReclamationApplicationTests` démarre Spring + JPA (H2), plus lent.

---

## 2) Commandes (Windows, chemins du dépôt actuel)

Tous les tests du microservice Réclamation :

```bat
cd C:\Users\USER\Desktop\finalle\integration\pi\BackEnd\Microservices\Reclamation
mvnw.cmd test
```

Unitaires **sans** le test de contexte Spring :

```bat
cd C:\Users\USER\Desktop\finalle\integration\pi\BackEnd\Microservices\Reclamation
mvnw.cmd "-Dtest=ReclamationServiceImplTest,CsatServiceTest,CategorisationServiceTest,ReclamationControllerTest" test
```

---

## 3) Frontend Angular (pedia-nephro)

| Fichier | Contenu |
|---------|---------|
| `FrontEnd/pedia-nephro/src/app/core/services/reclamation.service.spec.ts` | `TestBed` + `HttpClientTestingModule` : GET/POST/PUT, `getStatistiques`, erreur `creer` sans userId, plateforme `server` (pas de HTTP) |

**Inclusion :** le fichier est référencé dans `tsconfig.spec.json` et `angular.json` → `test.options.include`.

Lancer toute la suite de tests du projet :

```bat
cd C:\Users\USER\Desktop\finalle\integration\pi\FrontEnd\pedia-nephro
npm run test -- --watch=false
```

Cibler uniquement le service réclamation (si toujours listé dans `tsconfig.spec.json`) :

```bat
cd C:\Users\USER\Desktop\finalle\integration\pi\FrontEnd\pedia-nephro
npm run test -- --watch=false --include "**/reclamation.service.spec.ts"
```

**Stack :** Vitest (via `@angular/build:unit-test`), API de style Jasmine (`describe` / `it` / `expect`), `HttpTestingController` pour les appels HTTP.

---

## 4) Phrases types pour l’oral

1. « J’ai ajouté des tests unitaires avec JUnit 5 et Mockito sur le service et le CSAT, plus des tests `@WebMvcTest` sur les endpoints critiques. »
2. « Les cas nominaux, les erreurs métier (CSAT non éligible, doublon) et l’agrégation des statistiques sont couverts. »
3. « Côté Angular, les tests du `ReclamationService` vérifient les URLs, les méthodes HTTP et le comportement sans utilisateur connecté. »
4. « `mvnw.cmd test` et `npm run test -- --watch=false` passent sur ma machine. »

---

## 5) Pistes d’amélioration

- Tests d’intégration HTTP bout-en-bout (Testcontainers MySQL ou profil dédié).
- Composants `admin-reclamation` / `patient-reclamation` : tests avec composants mockés.
