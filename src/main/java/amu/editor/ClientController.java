package amu.editor;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ClientController {

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField textField;

    @FXML
    private MenuItem deleteLineMenuItem;

    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    private Socket socket;
    private PrintWriter out; // pour envoyer les commandes
    private BufferedReader in;// pour lire les reponses

    @FXML
    public void initialize() {
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("vous avez pas reussi la connexion au serveur  " + e.getMessage());
            e.printStackTrace();
        }

        handleRefresh();//Récupère et affiche les lignes du serveur au démarrage

        listView.getSelectionModel().selectedItemProperty().addListener((observableValue, string, newValue) -> {
            deleteLineMenuItem.setDisable(newValue == null);
            if (newValue != null) {
                textField.setText(newValue);
            }
        });
    }

    private List<String> sendCommand(String command) {
        List<String> response = new ArrayList<>();
        String line;

        try {
            out.println(command); // Envoie la commande
            while ((line = in.readLine()) != null) {
                response.add(line); // Ajoute chaque ligne à la réponse
                if (line.equals("OK") || line.equals("DONE")) break; // Fin de réponse
                if (line.startsWith("ERRL")) {
                    System.err.println("Erreur du serveur : " + line);
                    break;
                }

            }
        } catch (IOException e) {
            System.err.println("Erreur de communication avec le serveur : " + e.getMessage());
            e.printStackTrace(); // Affiche le détail de l’erreur
            response.add("ERRL Erreur de lecture depuis le serveur.");
        }

        return response;
    }



    @FXML
    private void handleAddLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        int insertIndex = selectedIndex == -1 ? listView.getItems().size() : selectedIndex + 1;
        sendCommand("ADDL " + insertIndex + " (New Line)");
        handleRefresh();
    }

    @FXML
    private void handleDeleteLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            sendCommand("RMVL " + selectedIndex);
            handleRefresh();
        }
    }

    @FXML
    private void handleTextFieldUpdate() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            String newText = textField.getText();
            sendCommand("MDFL " + selectedIndex + " " + newText);
            handleRefresh();
        }
    }

    @FXML
    private void handleRefresh() {
        List<String> response = sendCommand("GETD");
        listView.getItems().clear();
        for (String line : response) {
            if (line.startsWith("LINE")) {
                String[] parts = line.split(" ", 3);
                if (parts.length >= 3) {
                    listView.getItems().add(parts[2]);
                }
            }
        }
    }

    @FXML
    private void handleGetLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            List<String> response = sendCommand("GETL " + selectedIndex);
            for (String line : response) {
                if (line.startsWith("LINE")) {
                    String[] parts = line.split(" ", 3);
                    textField.setText(parts[2]); // Affiche juste la ligne dans le champ texte
                }
            }
        }
    }

}

