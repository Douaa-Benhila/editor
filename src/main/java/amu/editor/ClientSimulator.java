package amu.editor;



import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ClientSimulator {

    private static final List<String[]> SERVERS = Arrays.asList(
            new String[]{"127.0.0.1", "12345"},
            new String[]{"127.0.0.1", "12346"},
            new String[]{"127.0.0.1", "12347"}
    );

    private static final Random random = new Random();

    // je crée un thread pour chaque client
    public static void main(String[] args) {
        int clientCount = 8;
        for (int i = 0; i < clientCount; i++) {
            int clientId = i;
            new Thread(() -> simulateClient(clientId)).start();
        }
    }

    // je choisis un port parhasard a chaque client
    private static void simulateClient(int clientId) {
        String[] server = SERVERS.get(random.nextInt(SERVERS.size()));
        String host = server[0];
        int port = Integer.parseInt(server[1]);

        // crée une connexions avec le serveur
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // out envoie les commandes
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) { //in lit les réponses

            System.out.println("Client " + clientId + " connecté à " + host + ":" + port);

            // Demander le document
            out.println("GETD");
            String line;
            while ((line = in.readLine()) != null && !line.equals("DONE")) {
                // Juste lire les lignes
            }

            // Envoyer les trois actions aléatoires
            for (int i = 0; i < 3; i++) {
                int index = random.nextInt(5);
                String command;
                switch (random.nextInt(3)) {
                    case 0:
                        command = "ADDL " + index + " (Auto Line " + index + ")";
                        break;
                    case 1:
                        command = "MDFL " + index + " (Edited Line " + index + ")";
                        break;
                    default:
                        command = "RMVL " + index;
                        break;
                }

                out.println(command);
                String response;
                while ((response = in.readLine()) != null) {
                    if (response.equals("OK") || response.equals("DONE")) break;
                    if (response.startsWith("ERRL")) {
                        System.out.println("[Client " + clientId + "] Erreur : " + response);
                        break;
                    }
                }

                Thread.sleep(300 + random.nextInt(500)); // petite pause entre les actions
            }

        } catch (Exception e) {
            System.err.println("[Client " + clientId + "] Erreur : " + e.getMessage());
        }
    }
}

