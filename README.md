# Puzzle AI - Projet L1

## Description du projet

Puzzle AI est une application Java qui permet de rechercher automatiquement des pieces de puzzle dans une image en utilisant des techniques de vision par ordinateur avec la bibliotheque OpenCV. L'utilisateur selectionne une zone dans une image, et l'application recherche toutes les occurrences similaires dans l'image principale.

### Fonctionnalites principales

Chargement d'image : Importez n'importe quelle image aux formats JPG, PNG, BMP ou GIF. Une image de demonstration nommee image-demo.jpg est fournie pour tester directement l'application.

Selection intuitive : Selectionnez une zone par glisser-deposer pour definir la piece a rechercher.

Trois methodes de recherche : Pattern Matching pour la correspondance directe des pixels, Shape Matching pour la recherche basee sur les contours, et Combined Search combinant les deux methodes.

Parametres ajustables : Reglage du seuil de confiance de 0 a 100 pourcent, limitation du nombre de matches de 1 a 10, et mode Best match only.

Visualisation des resultats : Affichage des rectangles colores selon le niveau de confiance avec les scores directement sur l'image, et garantie d'absence de chevauchement entre les matches.

## Technologies utilisees

Java version 8 ou superieure
OpenCV version 4.12.0 pour la vision par ordinateur
Swing pour l'interface graphique

## Structure du projet
PuzzleIA
├── puzzle
│ ├── Main.java
│ ├── MainFrame.java
│ ├── PuzzlePieceFrame.java
│ ├── PuzzleSearch.java
│ └── MatchedRegion.java
├── opencv
│ └── build
│ └── java
│   ├── opencv-4120.jar
│   └── x64
│       └── opencv_java4120.dll
├── image-demo.jpg
├── Puzzle_IA_interface.png
└── README.md


## Installation

### Prerequis

Java JDK 8 ou superieur
OpenCV 4.12.0

### Installation d'OpenCV

Telechargez OpenCV 4.12.0 et extrayez l'archive. Placez le dossier opencv dans votre dossier PuzzleIA en respectant la structure indiquee ci-dessus. Les fichiers importants sont opencv-4120.jar dans le dossier build\java et opencv_java4120.dll dans le dossier build\java\x64.

## Compilation et execution

### Compilation

Ouvrez une invite de commandes dans le dossier contenant le dossier PuzzleIA et executez :

javac -cp "PuzzleIA\opencv\build\java\opencv-4120.jar;PuzzleIA" PuzzleIA\puzzle\*.java

### Execution

Apres compilation, lancez l'application avec :

java -cp "PuzzleIA\opencv\build\java\opencv-4120.jar;PuzzleIA" "-Djava.library.path=PuzzleIA\opencv\build\java\x64" puzzle.Main
Test rapide avec l'image de demonstration
Une image nommee image-demo.jpg est fournie avec le projet. Vous pouvez l'utiliser pour tester rapidement l'application sans avoir a chercher d'autres images.

## Guide d'utilisation

### Lancer l'application
Executez la commande d'execution.

### Charger une image
Cliquez sur le bouton Load Image. Pour un test rapide, selectionnez l'image image-demo.jpg fournie dans le dossier.

### Selectionner une piece
Effectuez un clic-glisser sur l'image pour definir la zone a rechercher, puis relachez pour valider la selection. Une nouvelle fenetre s'ouvre avec les details de la piece selectionnee.

### Configurer la recherche
Dans la fenetre d'analyse, vous pouvez ajuster plusieurs parametres. La methode de recherche peut etre Pattern Matching, Shape Matching ou Combined Search. Le seuil de confiance determine la strictesse de la recherche. Le nombre maximum de matches limite le nombre de resultats entre 1 et 10. Le mode Best match only affiche uniquement le meilleur resultat.

### Lancer la recherche
Cliquez sur le bouton Lancer la recherche pour demarrer l'analyse. Les resultats apparaissent dans la zone de texte et sont visualises sur l'image principale.

### Interpreter les resultats
La confiance est exprimee en pourcentage de similarite et n'atteint jamais exactement 100 pourcent. La position donne les coordonnees x et y dans l'image. Les rectangles sont de couleur rouge pour une confiance superieure a 80 pourcent, orange pour une confiance entre 60 et 80 pourcent, jaune pour une confiance entre 40 et 60 pourcent, et gris pour une confiance inferieure a 40 pourcent.

### Parametres avancés
Le seuil de confiance
Un seuil entre 90 et 100 pourcent correspond a une recherche tres stricte avec des correspondances presque parfaites. Un seuil entre 70 et 80 pourcent represente l'equilibre recommande. Un seuil entre 40 et 60 pourcent donne une recherche permissive avec plus de resultats mais des risques de faux positifs.

## Methodes de recherche
La methode Pattern Matching compare directement les pixels et est precise pour des pieces identiques mais sensible aux variations de couleur et d'echelle. La methode Shape Matching compare les contours et est robuste aux variations de couleur mais moins precise sur les details fins. La methode Combined Search fusionne les deux methodes pour un meilleur compromis mais est plus lente a l'execution.

### Aucun match trouve
Baissez le seuil de confiance, changez de methode de recherche, ou selectionnez une zone plus caracteristique dans l'image.

## Structure du code
Le dossier puzzle contient cinq fichiers sources. Main.java est le point d'entree de l'application. MainFrame.java gere la fenetre principale avec la selection. PuzzlePieceFrame.java est la fenetre d'analyse de la piece. PuzzleSearch.java contient les algorithmes de recherche OpenCV. MatchedRegion.java definit la classe representant une correspondance.

# Licence
Ce projet est libre d'utilisation pour un usage educatif et personnel.

## Auteurs
Projet realise par Matheo Brugnon pour un stage de fin d'année pour le cursus CMII (Cursus Master en Ingénierie Informatique) de l'URCA.