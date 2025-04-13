## Collaborative editor

Projet réseau 2024/2025 L3-info/AMU. 

Edition collaborative de documents texte 
avec une architecture client/serveur

- Binôme 1: *BENHILA* *Douaa* 
- Binôme 2: *MOTAIM* *Badr* 

Nous vous laissons ici un petit guide afin de tester le projet :

Vous pouvez constater la présence de plusieurs classes, car pour chaque tâche, nous avons créé une classe dédiée afin de pouvoir visualiser les changements d'une tâche à l'autre.

La plupart des modifications ont été effectuées côté serveur, c'est pourquoi les clients restent les mêmes pour toutes les tâches. Pour les exécuter, il suffit simplement de vérifier que le serveur et le client correspondent à la même tâche.

Enfin, pour vous assurer que vous lancez le bon client, rendez-vous dans le fichier clientView.fxml et modifiez cette ligne :
                     
                         fx:controller="amu.editor.ClientControllerPush">

en remplaçant "ClientControllerPush" par le nom correct du contrôleur client.

Dans mon code il y a aussi des scripts,

Pour les exécuter on doit bien vérifier quand ce positionne dans le bon path : IdeaProjects/editor

Et pour exécuter le fichier test_push.sh voila la commande : bash test_push.sh 

Et pour exécuter le fichier test_federation_master.sh voila la commande : bash test_federation_master.sh
