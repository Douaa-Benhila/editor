#!/bin/bash

mkdir -p bin

# Compilation uniquement des fichiers nécessaires
javac -d bin \
src/main/java/amu/editor/ServerCentralPush1.java \
src/main/java/amu/editor/ClientAuto1.java \
src/main/java/amu/editor/ClientAuto2.java \
src/main/java/amu/editor/ClientAuto3.java || exit 1

# Lancer le serveur en arrière-plan
echo "Lancement du serveur..."
java -cp bin amu.editor.ServerCentralPush1 &
SERVER_PID=$!
sleep 2

# Lancer les clients
echo "Lancement des clients..."
java -cp bin amu.editor.ClientAuto1 &
java -cp bin amu.editor.ClientAuto2 &
java -cp bin amu.editor.ClientAuto3 &

# Attendre que le serveur se termine (ctrl+C pour arrêter)
wait $SERVER_PID


#commande pour tester le script bash test_push.sh