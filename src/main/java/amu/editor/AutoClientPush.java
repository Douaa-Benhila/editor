package amu.editor;

import java.io.*;
import java.net.*;

public class AutoClientPush {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java AutoClientPush <NomClient> <Commande1> [Commande2] ...");
            return;
        }

        String clientName = args[0];
        String host = "localhost";
        int port = 12345;

        // créer une connexion avec mon serveurPush
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            //Récupère le document initial
            String line;
            while ((line = in.readLine()) != null && !line.equals("DONE")) {
                System.out.println("[" + clientName + "] Reçu: " + line);
            }

            // Envoie les commandes à exécuter
            for (int i = 1; i < args.length; i++) {
                String cmd = args[i];
                System.out.println("[" + clientName + "] Envoi: " + cmd);
                out.println(cmd);
            }

            // Attend pour voir les modifications reçues
            Thread.sleep(2000);

        } catch (Exception e) {
            System.err.println("[" + clientName + "] Erreur : " + e.getMessage());
        }
    }
}