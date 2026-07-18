#!/bin/bash

# Définition des variables
APP_NAME="framework"
SRC_DIR="src/main/java"
BUILD_DIR="build"
LIB_DIR="lib"
JAR_DIR="jar"

# Nettoyage et création du répertoire de build
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR
mkdir -p $JAR_DIR

# Classpath — tous les JARs du dossier lib
CLASSPATH=$(find "$LIB_DIR" -name "*.jar" | tr '\n' ':')

# Compilation
find "$SRC_DIR" -name "*.java" > sources.txt

echo "--- $APP_NAME : Démarrage de la compilation ---"
javac -cp "$CLASSPATH" -d $BUILD_DIR @sources.txt

if [ $? -eq 0 ]; then
    echo "Succès : Fichiers .class générés."
else
    echo "Erreur : Échec de la compilation."
    rm sources.txt
    exit 1
fi
rm sources.txt

# Génération du fichier framework.jar
echo "--- Génération du fichier $APP_NAME.jar ---"
cd $BUILD_DIR || exit
jar -cvf ../$JAR_DIR/$APP_NAME.jar .
cd ..

# Copie des jars Spring dans jar/ SAUF servlet-api.jar
for jar in $LIB_DIR/*.jar; do
    if [ "$(basename $jar)" != "servlet-api.jar" ]; then
        cp $jar $JAR_DIR/
    fi
done

echo "--- Copie des jars Spring dans $JAR_DIR/ ---"
# Nettoyage
rm -rf $BUILD_DIR

echo ""
echo "Build terminé : jars disponibles dans $JAR_DIR/"