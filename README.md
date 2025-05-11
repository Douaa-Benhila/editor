#  Éditeur Collaboratif Distribué

Bienvenue dans notre éditeur de texte collaboratif, développé en Java avec une architecture distribuée. Ce projet permet à plusieurs clients de modifier simultanément un même document partagé, en se connectant à un ou plusieurs serveurs. Il comprend des mécanismes de communication, de synchronisation, de tolérance aux pannes, et de test de performance.

##  Objectif

Permettre à plusieurs utilisateurs de travailler en collaboration sur un document texte, via :
- Un **serveur centralisé** ou en **mode push**
- Une **fédération de serveurs** (simple ou avec un serveur maître)
- Une **architecture totalement décentralisée tolérante aux pannes**
- Des **tests automatisés de performance** et de **résilience**

---

##  Structure du Code

Le code est organisé en **plusieurs classes**, chaque version du serveur/client ayant sa propre classe pour bien distinguer les évolutions.

### Côté Serveur :
- `ServerCentral1`, `ServerCentralPush1`, `ServerCentral4`, `ServerCentral5`, `ServerFinal`
- Chaque classe de serveur correspond à une architecture différente (pull, push, fédérée, tolérante aux pannes)

### Côté Client :
- `ClientController`, `ClientControllerPush`, `ClientControllerFinal`
- Un seul client GUI, à ajuster selon le serveur utilisé

### Configuration :
- `peers.cfg` : Fichier de configuration pour déterminer le rôle (maître ou pair)
- `PeerConfig.java` : Parse ce fichier au démarrage

---

##  Comment Tester

1. **Assurez-vous que le serveur et le client sont de la même tâche.**
2. Dans `clientView.fxml`, modifiez cette ligne selon la tâche :
   ```xml
   fx:controller="amu.editor.ClientControllerPush"
   ```
   Remplacez `ClientControllerPush` par le nom du contrôleur correspondant à la tâche en cours.
3. Positionnez-vous dans le bon dossier :
   ```bash
   cd IdeaProjects/editor
   ```
4. Pour exécuter les tests automatiques :
   ```bash
   bash test_push.sh
   bash test_federation_master.sh
   ```

---
##  Tolérance aux Pannes

- En cas de panne du serveur maître, les autres serveurs redondants continuent
- Les messages sont identifiés par un `UUID` pour éviter les doublons
- L’algorithme garantit la cohérence même après la perte d’un nœud

---

##  Remarques

- Tous les contrôleurs clients sont dans `clientView.fxml`. Vérifiez que le `fx:controller` est bien celui que vous voulez utiliser.
- L’interface graphique JavaFX permet de visualiser en direct la collaboration.
- Les scripts shell (`test_push.sh`, `test_federation_master.sh`) facilitent les tests automatisés.

---

## ✅ Conclusion

Ce projet démontre comment construire un éditeur collaboratif tolérant aux pannes, basé sur différentes architectures de communication. Il met en œuvre des concepts avancés de **réseau**, de **concurrence**, de **résilience**, et de **décentralisation**.

Pour toute exécution correcte, assurez-vous d’avoir la **bonne configuration client/serveur**, et utilisez les **scripts fournis** pour les tests.
