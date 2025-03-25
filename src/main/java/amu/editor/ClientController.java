package amu.editor;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.System.in;
import static java.lang.System.out;

public class ClientController {

    @FXML
    private ListView<String> listView;

    @FXML
    private TextField textField;

    @FXML
    private MenuItem deleteLineMenuItem;


    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    public void initialize() {
        connectToServer();//comme demendader
        handleRefresh(); // get last version of the document

        // Activate "Delete Line" option when a line is selected
        listView.getSelectionModel().selectedItemProperty().addListener((observableValue, string, newValue) -> {
            deleteLineMenuItem.setDisable(newValue == null);

            // For editing selected line
            if (newValue != null) {
                textField.setText(newValue);
            }
        });
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 1234);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(String request) {
        out.println(request);
    }

    private String receiveResponse() throws IOException {
        return in.readLine();
    }
    @FXML
    private void handleAddLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) {
            listView.getItems().add("(New Line)");
        } else {
            // new line added below selected line
            listView.getItems().add(selectedIndex+1, "(New Line)");
        }
        //TODO request server to add a new line
        sendRequest("ADDL " + selectedIndex + " (New Line)");
        handleRefresh();
    }

    @FXML
    private void handleDeleteLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            listView.getItems().remove(selectedIndex);
            sendRequest("RMVL " + (selectedIndex + 1));
            handleRefresh();
        }
        // TODO request server to remove line
    }

    @FXML
    private void handleTextFieldUpdate() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            listView.getItems().set(selectedIndex, textField.getText());
            sendRequest("MDFL " + (selectedIndex + 1) + " " + textField.getText());
            handleRefresh();
        }
        // TODO request server to modify line
    }


    /*private void  handleRefresh() {
        // TODO request server last version of the document
        String[] textSample = { "FIRST WITCH  When shall we three meet again?\n",
                "   In thunder, lightning, or in rain?\n",
                "SECOND WITCH  When the hurly-burly’s done\n",
                "   When the battle’s lost and won.\n",
                "THIRD WITCH  That will be ere the set of sun\n"};
        listView.getItems().clear();
        for(String line : textSample){
            listView.getItems().add(line);
        }
    }*/
    @FXML
    private void handleRefresh() {
        listView.getItems().clear(); // Vider la liste avant de charger les nouvelles données

        sendRequest("GETD"); // Envoyer la requête au serveur pour obtenir la dernière version du document

        try {
            String response;
            while (!(response = receiveResponse()).equals("DONE")) {
                if (response.startsWith("LINE ")) {
                    listView.getItems().add(response.substring(5)); // Ajouter chaque ligne reçue à la liste
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Afficher l'erreur en cas de problème avec le serveur
        }
    }


}

