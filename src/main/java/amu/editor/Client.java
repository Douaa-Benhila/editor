package amu.editor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Client {

    // Adresse et ports des serveurs à tester
    private static final String[] SERVERS = {"127.0.0.1:10101", "127.0.0.1:00000"};
    private static final int CLIENTS_PER_SERVER = 20; //le nombre de clients par serveur
    private static final int OPERATIONS = 5; // Nombre d'opérations par client

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(SERVERS.length * CLIENTS_PER_SERVER);

        for (String address : SERVERS) {
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            for (int i = 0; i < CLIENTS_PER_SERVER; i++) {
                int clientId = i;
                executor.submit(() -> runBenchmarkClient(host, port, clientId));
            }
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
        System.out.println("Tous les benchmarks sont terminés ✅");
    }

    private static void runBenchmarkClient(String host, int port, int clientId) {
        List<Long> latences = new ArrayList<>();
        long startGlobal = System.nanoTime();

        try {
            Socket socket = new Socket(host, port); // Connexion au serveur
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            for (int i = 0; i < OPERATIONS; i++) {
                long start = System.nanoTime(); // Début chrono
                out.println("ADDL 0 Client" + clientId + " Ligne" + i); // Ajout d'une ligne
                out.println("GETD"); // Lecture du document

                String line;
                while ((line = in.readLine()) != null && !line.equals("DONE")) {
                    // Attente de la réponse complète
                }
                long end = System.nanoTime(); // Fin chrono
                latences.add((end - start) / 1_000_000); // Calcul en millisecondes
                Thread.sleep(10); // Pause pour simuler une activité réelle
            }

            long endGlobal = System.nanoTime();
            long totalTimeMs = (endGlobal - startGlobal) / 1_000_000;
            double avgLatency = latences.stream().mapToLong(Long::longValue).average().orElse(0);
            double throughput = (OPERATIONS * 1000.0) / totalTimeMs;

            System.out.println("[BENCHMARK] " + host + ":" + port +
                    " | Client " + clientId +
                    " | Latence moy. = " + avgLatency + " ms | Débit = " + throughput + " op/s");

            // Sauvegarde des résultats
            synchronized (Client.class) {
                try (FileWriter fw = new FileWriter("resultat.csv", true)) {
                    fw.write(host + "," + port + "," + clientId + "," + OPERATIONS + "," + avgLatency + "," + throughput + "\n");
                }
            }
            socket.close();
        } catch (Exception e) {
            System.err.println("Erreur client " + clientId + " vers " + host + ":" + port);
            e.printStackTrace();
        }
    }
}

