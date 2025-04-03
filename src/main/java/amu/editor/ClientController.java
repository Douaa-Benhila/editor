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
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    public void initialize() {
        try {
            socket = new Socket(HOST, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Failed to connect to server in initialize: " + e.getMessage());
            e.printStackTrace();
        }

        handleRefresh();

        listView.getSelectionModel().selectedItemProperty().addListener((observableValue, string, newValue) -> {
            deleteLineMenuItem.setDisable(newValue == null);
            if (newValue != null) {
                textField.setText(newValue);
            }
        });
    }

    private List<String> sendCommand(String command) {
        List<String> response = new ArrayList<>();
        System.out.println("Sending command: " + command); // Debug

        try {
            out.println(command);
            System.out.println("Command sent: " + command); // Debug
            String line;

            while ((line = in.readLine()) != null) {
                response.add(line);
                if (line.equals("OK")) break;
            }

            System.out.println("Response from server: " + response); // Debug
        } catch (IOException e) {
            System.err.println("Communication error: " + e.getMessage());
            e.printStackTrace();
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
                listView.getItems().add(parts[2]);
            }
        }
    }
}

