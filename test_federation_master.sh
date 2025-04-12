#!/bin/bash

mkdir -p bin

# Compilation uniquement des classes nécessaires
javac -d bin \
src/main/java/amu/editor/ServerCentral5.java \
src/main/java/amu/editor/ServerCentralPush5.java \
src/main/java/amu/editor/ServerReplica5.java \
src/main/java/amu/editor/PeerConfig.java \
src/main/java/amu/editor/ClientAutoMaster.java \
src/main/java/amu/editor/ClientAutoPush.java \
src/main/java/amu/editor/ClientAutoReplica.java || exit 1

# Lancer les serveurs (dans l’ordre)
java -cp bin amu.editor.ServerCentral5 &
sleep 1
java -cp bin amu.editor.ServerCentralPush5 &
sleep 1
java -cp bin amu.editor.ServerReplica5 &
sleep 2

# Lancer les clients automatiques
java -cp bin amu.editor.ClientAutoMaster &
java -cp bin amu.editor.ClientAutoPush &
java -cp bin amu.editor.ClientAutoReplica &

# Attendre un peu pour voir les résultats
sleep 5

# commande: bash test_federation_master.sh