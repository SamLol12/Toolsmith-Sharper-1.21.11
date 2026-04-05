# 🛠️ Toolsmith Sharper

**Toolsmith Sharper** est un mod Fabric qui transforme l'entretien de votre équipement en une mécanique stratégique et immersive. Ne vous contentez plus de simples enchantements : apprenez à aiguiser vos lames, à polir vos outils et à gérer l'usure de vos revêtements en fonction de votre environnement.

> *"Un bon artisan n'est rien sans un outil bien affûté, mais un sage artisan sait quelle huile choisir pour sa lame."*

---

## ✨ Fonctionnalités Principales

### 🪨 L'Art de l'Aiguisage
Améliorez les performances de base de votre équipement (Dégâts pour les armes, Vitesse pour les outils).
* **Meule (Grindstone) :** Accroupissez-vous (`Sneak`) et faites un clic droit sur une meule avec l'objet en main principale et un composant (Silex ou Huile) en main secondaire.
* **Pierre à Aiguiser (Whetstone) :** Un nouvel objet artisanal portable indispensable pour appliquer des bonus en plein voyage.
* **Entretien préventif :** L'aiguisage **répare 10%** (configurable) de la durabilité de l'objet.
* **Conditions :** Nécessite **1 niveau d'XP** et un outil ayant au moins **20% de sa durabilité**.

### 🧪 Huiles & Spécialisation (Équilibrage)
Toutes les huiles ne sont pas adaptées à tous les usages. Le mod impose désormais une distinction stricte entre le combat et la récolte :

| Élément | Effet (Armes) | Effet (Outils) |
| :--- | :--- | :--- |
| **Silex (Simple)** | + Dégâts | + Vitesse de minage |
| 🔥 **Fire Oil** | Enflamme les ennemis | ❌ *Incompatible* |
| 🧪 **Poison Oil** | Empoisonne la cible | ❌ *Incompatible* |
| 🧛 **Vampire Oil** | Vol de vie (Heal) | ❌ *Incompatible* |
| ❄️ **Frost Oil** | Ralentit l'ennemi | ❌ *Incompatible* |
| 🍀 **Luck Oil** | **Butin (Looting)** | **Fortune** |

*Les **Armes** incluent : Épées, Haches et Lances. Les **Outils** incluent : Pioches, Pelles et Houes.*

---

## ☁️ Système de Dégradation Dynamique
La durée de vie de vos bonus dépend de l'environnement. Soyez attentif aux éléments :

* **🌊 L'Eau & la Pluie :** Éteignent les huiles de **Feu** et lavent celles de **Poison** 2x plus vite.
* **☀️ La Chaleur Extrême :** L'huile de **Givre (Frost)** fond 2x plus vite dans les Déserts, le Nether et les Savanes, ou si vous touchez de la **Lave** ou du **Feu**.
* **🌿 L'Écrin Biologique :** Le **Poison** est plus stable dans les Marais et Jungles. Le **Givre** dure plus longtemps dans les biomes froids.
* **🌙 Le Cycle Circadien :** Les huiles de **Vampire** et de **Chance** s'usent moins vite durant la **nuit**.

---

## 🔮 Prochaines Étapes & Améliorations Futures
Le développement continue ! Voici les fonctionnalités envisagées pour les prochaines versions :

* **📈 Niveaux de Pierres à Aiguiser :** Introduction de variantes en Fer, Diamant et Netherite avec une durabilité accrue et une vitesse d'application plus rapide.
* **🏹 Support des Armes à Distance :** Possibilité d'enduire les arcs et les arbalètes pour appliquer des effets aux flèches tirées.
* **🧪 Nouvelles Huiles
* **⚡ Intégration Thunder :** Un bonus massif pour l'huile de Givre ou de Chance lors des orages.
* **🛡️ Polissage d'Armure :** Extension du système pour "polir" l'armure et obtenir des bonus de résistance ou de recul temporaires.

---

## ⚙️ Configuration & Commandes
Vous pouvez personnaliser l'intégralité du mod de trois façons différentes :

1.  **Interface Visuelle :** Installez **ModMenu** pour accéder à une interface de configuration simplifiée.
2.  **Commandes In-Game :** Utilisez `/toolsmithsharper set...` pour ajuster les valeurs (Dégâts, Vitesse, Coût XP, Réparation).
3.  **Fichier de Config :** Modifiez le fichier `config/toolsmithsharper.properties`.

---

## 📦 Installation
1.  Téléchargez et installez **Fabric Loader**.
2.  Ajoutez le **Fabric API** dans votre dossier `mods`.
3.  Placez le fichier `.jar` de **Toolsmith Sharper** dans votre dossier `mods`.

---

## 📜 Licence
Ce mod est distribué sous licence **MIT**.

**Auteur :** *samlol12* **Version Minecraft :** *1.21.1* (Fabric)
