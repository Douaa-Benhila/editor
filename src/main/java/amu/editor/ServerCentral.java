package amu.editor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


public class ServerCentral {
    private static final int port = 12345; // je definis mon port
    private final List<String> document = new ArrayList<>();//mon document partagé je le modelise avec  liste
    private final ReentrantLock lock = new ReentrantLock();//assurer que l'accès à la ressource partagée se fait de manière sécurisée

    public static void main(String[] args) {
        new ServerCentral().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur démarré sur le port " + port);
            while (true) { // // j'attend la connexion du client
                Socket clientSocket = serverSocket.accept();//j'ai créer un serveur qui est capable d'accepter un client a n'importe quelle moment donnée
                // chaque fois un client est connecté je crée pour lui son propre thread avec ClientHandler une classe que je vais créer ulterieurment
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

