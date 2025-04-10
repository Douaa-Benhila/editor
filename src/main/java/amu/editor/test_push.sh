#!/bin/bash


CLASSPATH="build/classes/java/main"

# commande avec lequel je lance mon script
xterm -hold -e "java -cp $CLASSPATH amu.editor.ServerCentralPush" &

# j'attends que le serveur demare
sleep 2

# modification de mes clients
xterm -hold -e "java -cp $CLASSPATH amu.editor.AutoClientPush Client1 \"ADDL 1 Ligne A\" \"MDFL 0 Modif A\"" &
xterm -hold -e "java -cp $CLASSPATH amu.editor.AutoClientPush Client2 \"ADDL 2 Ligne B\" \"RMVL 3\"" &
xterm -hold -e "java -cp $CLASSPATH amu.editor.AutoClientPush Client3 \"MDFL 1 Final B\"" &

# j'attends que tous les clients termines la modifications
wait