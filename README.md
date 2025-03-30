🛒 Gestion des Ventes - API Laravel & Client Java Swing

Ce projet est une application de gestion des ventes composée de deux parties principales :

    Backend : API REST développée en Laravel pour gérer les produits, les ventes et les statistiques.

    Frontend : Interface utilisateur développée en Java Swing, permettant aux clients de consulter, ajouter, modifier et supprimer des produits.

📌 Fonctionnalités principales
🔹 Backend (Laravel API)

✅ CRUD des produits (Créer, Lire, Mettre à jour, Supprimer)
✅ Gestion des ventes et des stocks
✅ API REST avec authentification
✅ Génération de statistiques sur les ventes
🔹 Frontend (Java Swing)

✅ Interface graphique intuitive avec un tableau affichant les produits
✅ Boutons Modifier et Supprimer intégrés directement dans le tableau
✅ Affichage des statistiques de vente avec un graphique interactif
✅ Communication avec l'API Laravel pour synchroniser les données en temps réel
🚀 Technologies utilisées

    Backend : Laravel, MySQL, JWT pour l'authentification

    Frontend : Java Swing, JTable, REST API (via HttpClient)

📂 Installation & Exécution
1️⃣ Cloner le projet

git clone https://github.com/votre-utilisateur/gestion-ventes.git
cd gestion-ventes

2️⃣ Backend - Installation (Laravel)

cd backend
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --seed
php artisan serve

3️⃣ Frontend - Exécution (Java Swing)

-Ouvrir frontend/ dans IntelliJ IDEA ou Eclipse et exécuter MainFrame.java.
ou
-si vous Utilise Vs code + Maven :
Ouvrir le dossier frontend/ dans VS Code et s'assurer que Maven est bien installé.
Puis, exécuter la commande suivante dans le terminal pour lancer l'application :
" mvn clean compile exec:java "
