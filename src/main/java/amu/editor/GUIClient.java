package amu.editor;


import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
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

            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();

        }

        Scene scene = new Scene(root);
        primaryStage.setTitle("collaborative editor");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}



