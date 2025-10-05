🗂️ Table des matières
Aperçu
Fonctionnalités
Règles Métier
Scénario de Validation
Prérequis Techniques
Démarrage
Structure du Projet
Documentation API
Licence
🔍 Aperçu
Cette application permet de :

Créer, lire, modifier des transactions de paiement
Gérer différents moyens de paiement (carte bancaire, carte cadeau, PayPal)
Appliquer des règles métier strictes sur les statuts de transaction
Manipuler des commandes associées à chaque transaction
✅ Fonctionnalités
Création de transaction avec détails de commande
Mise à jour de statut : passage de NEW → AUTHORIZED → CAPTURED
Validation métier des transitions de statut
Connexion à MongoDB pour stockage des données
API REST réactive (WebFlux) avec gestion des erreurs
📋 Règles Métier
Une nouvelle transaction commence toujours avec le statut NEW.
Il est interdit de passer à CAPTURED si la transaction n’est pas d’abord AUTHORIZED.
Une transaction CAPTURED ne peut plus être modifiée.
Les lignes de commande ne peuvent pas être modifiées après création de la transaction.
🧪 Scénario de Validation
Pour valider fonctionnellement l’API, le scénario suivant est testé :

Création d’une transaction de 54,80 EUR avec une carte bancaire comprenant :
4 paires de gants de ski à 10 EUR pièce
1 bonnet en laine à 14,80 EUR
Passage du statut à AUTHORIZED
Passage du statut à CAPTURED
Création d’une transaction de 208 EUR via PayPal comprenant :
1 vélo à 208 EUR
Récupération de toutes les commandes
🧱 Prérequis Techniques
Java : 17
Spring Boot : Reactive (WebFlux)
MongoDB : base de données NoSQL
Maven : 3.8+
Lombok : pour générer le code boilerplate
AssertJ / Reactor Test : pour les tests unitaires
🚀 Démarrage
1. Cloner le dépôt
