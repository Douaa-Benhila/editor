package amu.editor;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerDispatch {

    private static final List<String> serverList = Arrays.asList(
            "127.0.0.1:11111",
            "127.0.0.1:22222",
            "127.0.0.1:33333"
    );

    private static int nextServerIndex = 0; // Index pour choisir le prochain serveur
    private static final int DISPATCH_PORT = 13000; // Port d'écoute du serveur dispatch

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(DISPATCH_PORT)) {
            System.out.println("Serveur Dispatch en écoute sur le port " + DISPATCH_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Attend un client
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());

                // Traite le client dans un thread séparé
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            // Choix du serveur à attribuer
            String selectedServer = serverList.get(nextServerIndex);
            nextServerIndex = (nextServerIndex + 1) % serverList.size(); // Incrémente pour le prochain client

            // Envoie l'adresse du serveur sélectionné au client
            out.println(selectedServer);
            System.out.println("Client redirigé vers " + selectedServer);

        } catch (IOException e) {
            System.err.println("Erreur avec le client : " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // Ferme la connexion avec le client
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
