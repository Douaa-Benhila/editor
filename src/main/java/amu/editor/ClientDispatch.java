package amu.editor;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ClientDispatch{

    private static final int NUM_CLIENTS = 6; // Nombre de clients à simuler
    private static final String DISPATCH_HOST = "127.0.0.1"; // Adresse du serveur Dispatch
    private static final int DISPATCH_PORT = 13000; // Port du serveur Dispatch

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);

        for (int i = 0; i < NUM_CLIENTS; i++) {
            int clientId = i;
            executor.submit(() -> runClient(clientId));
        }

        executor.shutdown();
    }

    private static void runClient(int clientId) {
        try {
            // Étape 1 : Le client contacte le serveur Dispatch pour obtenir une redirection
            Socket dispatchSocket = new Socket(DISPATCH_HOST, DISPATCH_PORT);
            BufferedReader dispatchIn = new BufferedReader(new InputStreamReader(dispatchSocket.getInputStream()));

            // Lecture de l'adresse redirigée
            String redirectAddress = dispatchIn.readLine(); // exemple: "127.0.0.1:12345"
            dispatchSocket.close();

            // Extraction de l'hôte et du port
            String[] parts = redirectAddress.split(":");
            String serverHost = parts[0];
            int serverPort = Integer.parseInt(parts[1]);

            // Étape 2 : Connexion au serveur désigné par le dispatch
            Socket serverSocket = new Socket(serverHost, serverPort);
            PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            System.out.println("Client " + clientId + " connecté à " + serverHost + ":" + serverPort);

            // Le client envoie quelques commandes aléatoires (par exemple, ajouter une ligne)
            out.println("ADDL 0 Client" + clientId + " ajoute une ligne");
            out.println("GETD"); // Demande le document après modification

            // Affiche la réponse du serveur
            String line;
            while ((line = in.readLine()) != null && !line.equals("DONE")) {
                System.out.println("[Client " + clientId + "] Reçu: " + line);
            }

            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Client " + clientId + " erreur : " + e.getMessage());
        }
    }
}

