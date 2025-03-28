package amu.editor;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

public class GUIClient extends javafx.application.Application{
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    @Override
    public void start(Stage primaryStage) {


        URL url = getClass().getResource("clientView.fxml");
        System.out.println("URL: " + url);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("clientView.fxml"));
        Parent root = null;
        try {
            connectToServer();  // Connexion au serveur

            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();

        }

        Scene scene = new Scene(root);
        primaryStage.setTitle("collaborative editor");
        primaryStage.setScene(scene);
        primaryStage.show();
        new Thread(this::updateDocumentLoop).start();
    }
    private void connectToServer() throws IOException {
        socket = new Socket("localhost", 12345);  // Adapter si le serveur est sur une autre adresse
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Connecté au serveur !");
    }

    private void sendRequest(String request) {
        out.println(request);
    }

    private void updateDocumentLoop() {
        try {
            while (true) {
                sendRequest("GETD");  // Demander la version actuelle du document
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals("DONE")) break;
                    System.out.println("Serveur: " + line);
                    // Mettre à jour l'interface graphique avec les nouvelles données ici
                }
                Thread.sleep(2000); // Rafraîchir toutes les 2 secondes
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}



