#!/bin/bash

# Définition des variables
APP_NAME="framework"
SRC_DIR="src/main/java"
BUILD_DIR="build"
LIB_DIR="lib"
JAR_DIR="jar"
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"

# Nettoyage et création du répertoire de build
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR
mkdir -p $JAR_DIR

# Classpath — JARs du dossier lib (ex: servlet-api.jar)

# Compilation
find "$SRC_DIR" -name "*.java" > sources.txt

echo "--- $APP_NAME : Démarrage de la compilation ---"
javac -cp "$SERVLET_API_JAR" -d $BUILD_DIR @sources.txt

if [ $? -eq 0 ]; then
    echo "Succès : Fichiers .class générés."
else
    echo "Erreur : Échec de la compilation."
    rm sources.txt
    exit 1
fi
rm sources.txt

# Génération du fichier .jar
echo "--- Génération du fichier $APP_NAME.jar ---"
cd $BUILD_DIR || exit
jar -cvf ../$JAR_DIR/$APP_NAME.jar .
cd ..

# Nettoyage
rm -rf $BUILD_DIR

echo ""
echo "Build terminé : $APP_NAME.jar disponible dans $JAR_DIR/"