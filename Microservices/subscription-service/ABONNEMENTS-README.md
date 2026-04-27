# Abonnements – Base de données

## Base MySQL

Le service utilise une seule base **MySQL** configurée dans `application.properties` :

- **Base** : `pedia_nephro_subscription` (créée automatiquement si besoin)
- **Tables** : `subscription`, `subscription_plan`, et table des fonctionnalités des plans (plus de table `association`)

Si vous aviez déjà une base avec la table `association` et la colonne `association_id` dans `subscription`, vous pouvez les supprimer manuellement en SQL : `ALTER TABLE subscription DROP COLUMN association_id;` puis `DROP TABLE IF EXISTS association;`

## Démarrer le service

```bash
cd "Gestion des abonnements/subscription-service"
mvnw.cmd spring-boot:run
```

Les plans (Essential, Professional, Customized) sont insérés au premier démarrage.

## Plan et formulaire frontend

- **Plans** : chargés depuis `GET /api/subscriptions/plans`.
- Si « Impossible de charger les plans » : vérifier que le backend est démarré (port 8081 ou 8080 si gateway) et que l’URL d’API dans Angular pointe vers le bon port.
