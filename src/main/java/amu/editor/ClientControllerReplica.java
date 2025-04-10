package amu.editor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientControllerReplica {

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField textField;

    @FXML
    private MenuItem deleteLineMenuItem;

    private static final String HOST = "localhost";
    private static final int PORT = 12346;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    public void initialize() {
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Démarre le thread qui écoute les messages du serveur
            new Thread(this::listenToServer).start();

            // Ajoute un listener pour gérer les clics sur la liste
            listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                deleteLineMenuItem.setDisable(newVal == null);
                if (newVal != null) {
                    textField.setText(newVal);
                }
            });

        } catch (IOException e) {
            System.err.println("Erreur de connexion au serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // traite les commandes recu par le serveur
    private void listenToServer() {
        String line;
        try {
            while ((line = in.readLine()) != null) {
                System.out.println("Reçu du serveur : " + line);

                if (line.startsWith("LINE")) {
                    String[] parts = line.split(" ", 3);
                    int index = Integer.parseInt(parts[1]);
                    String content = parts[2];
                    System.out.println("[CLIENT] Reçu LINE : index=" + index + ", content=" + content);


                    Platform.runLater(() -> {
                        if (index < listView.getItems().size()) {
                            listView.getItems().set(index, content);
                        } else if (index == listView.getItems().size()) {
                            listView.getItems().add(content);
                        }
                    });
                } else if (line.startsWith("ADDL")) {
                    String[] parts = line.split(" ", 3);
                    int index = Integer.parseInt(parts[1]);
                    String content = parts[2];
                    System.out.println("[CLIENT] Reçu ADDL : ajout de \"" + content + "\" à l'index " + index);

                    Platform.runLater(() -> {
                        listView.getItems().add(index, content);
                    });

                } else if (line.startsWith("RMVL")) {
                    int index = Integer.parseInt(line.split(" ")[1]);
                    System.out.println("[CLIENT] Reçu RMVL : suppression de la ligne " + index);
                    Platform.runLater(() -> {
                        if (index < listView.getItems().size()) {
                            listView.getItems().remove(index);
                        }
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de lecture serveur : " + e.getMessage());
        }
    }

    private void sendCommand(String command) {
        if (out != null) {
            System.out.println("[CLIENT] Envoi de commande au serveur : " + command);
            out.println(command);
        }
    }

    @FXML
    private void handleAddLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        int insertIndex = selectedIndex == -1 ? listView.getItems().size() : selectedIndex + 1;
        sendCommand("ADDL " + insertIndex + " (New Line)");
    }

    @FXML
    private void handleDeleteLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            sendCommand("RMVL " + selectedIndex);
        }
    }

    @FXML
    private void handleTextFieldUpdate() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            String newText = textField.getText();
            sendCommand("MDFL " + selectedIndex + " " + newText);
        }
    }
    //obligatoire de limplementer pour raison FXML
    @FXML
    private void handleRefresh() {
        // Méthode présente uniquement pour éviter les erreurs de chargement FXML
        System.out.println("cette methode est ignorée en mode PUSH");
    }

}