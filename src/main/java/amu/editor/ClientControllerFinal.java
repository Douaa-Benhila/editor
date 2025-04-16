package amu.editor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;

public class ClientControllerFinal {
    @FXML
    private ListView<String> listView;
    @FXML
    private TextField textField;
    @FXML
    private MenuItem deleteLineMenuItem;
    private PrintWriter out;
    private BufferedReader in;

    @FXML
    public void initialize() {
        connectToServer("localhost", 11111);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            deleteLineMenuItem.setDisable(newVal == null);
            if (newVal != null) {
                textField.setText(newVal);
            }
        });
    }

    private void connectToServer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.equals("DONE")) continue;

                        if (line.startsWith("LINE")) {
                            String[] tokens = line.split(" ", 3);
                            int index = Integer.parseInt(tokens[1]);
                            String text = tokens.length > 2 ? tokens[2] : "";
                            Platform.runLater(() -> {
                                while (listView.getItems().size() <= index) {
                                    listView.getItems().add("");
                                }
                                listView.getItems().set(index, text);
                            });
                        } else if (line.startsWith("MSG")) {
                            System.out.println("[CLIENT] Reçu MSG : " + line);
                            String[] tokens = line.split(" ", 4);
                            if (tokens.length < 4) continue;

                            String[] parts = tokens[3].split(" ", 2);
                            String command = tokens[2];
                            int index = Integer.parseInt(parts[0]);
                            String content = (parts.length > 1) ? parts[1] : "";

                            Platform.runLater(() -> {
                                switch (command) {
                                    case "ADDL":
                                        if (index >= 0 && index <= listView.getItems().size()) {
                                            listView.getItems().add(index, content);
                                        }
                                        break;
                                    case "MDFL":
                                        if (index >= 0 && index < listView.getItems().size()) {
                                            listView.getItems().set(index, content);
                                        }
                                        break;
                                    case "RMVL":
                                        if (index >= 0 && index < listView.getItems().size()) {
                                            try {
                                                listView.getItems().remove(index);
                                                System.out.println("[CLIENT] Ligne supprimée à l'index : " + index);
                                            } catch (Exception e) {
                                                System.out.println("[CLIENT] Échec suppression index " + index + ": " + e.getMessage());
                                            }
                                        } else {
                                            System.out.println("[CLIENT] Index " + index + " invalide pour suppression.");
                                            sendCommand("GETD"); // optionnel : resynchronisation
                                        }
                                        break;

                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            out.println("GETD");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddLine() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        int insertIndex = (selectedIndex == -1) ? listView.getItems().size() : selectedIndex + 1;
        String text = "(New Line)";
        sendCommand("ADDL " + insertIndex + " " + text);
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

    @FXML
    private void handleRefresh() {
        listView.getItems().clear();
        sendCommand("GETD");
    }

    private void sendCommand(String cmd) {
        if (out != null) {
            System.out.println("[CLIENT] Commande envoyée : " + cmd);
            out.println(cmd);
        }
    }
}