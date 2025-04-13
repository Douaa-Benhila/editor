package amu.editor;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerCentral5 {
    private static final int PORT = 11111;
    private static final List<String> document = Collections.synchronizedList(new ArrayList<>(Arrays.asList(
            "FIRST WITCH  When shall we three meet again?",
            "   In thunder, lightning, or in rain?",
            "SECOND WITCH  When the hurly-burly’s done",
            "   When the battle’s lost and won.",
            "THIRD WITCH  That will be ere the set of sun"
    )));

    private static final List<PrintWriter> clients = Collections.synchronizedList(new ArrayList<>());
    private static final List<Socket> peerSockets = Collections.synchronizedList(new ArrayList<>());
    private static final List<PrintWriter> peerWriters = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        try {
            PeerConfig config = PeerConfig.load("peers.cfg", PORT);

            for (PeerConfig.Peer peer : config.getPeers()) {
                if (peer.port() != PORT) {
                    try {
                        Socket peerSocket = new Socket(peer.host(), peer.port());
                        peerSockets.add(peerSocket);
                        peerWriters.add(new PrintWriter(peerSocket.getOutputStream(), true));
                        System.out.println("Connecté au pair : " + peer.host() + ":" + peer.port());
                    } catch (IOException e) {
                        System.err.println("Erreur de connexion au pair : " + peer.host() + ":" + peer.port());
                    }
                }
            }

            // Arrêt automatique après 2 minutes
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("[INFO] ServeurCentral5 s'arrête après 2 minutes.");
                    System.exit(0);
                }
            }, 120000);

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Serveur Central démarré sur le port  " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nouveau client connecté  " + clientSocket.getInetAddress());
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur au démarrage du serveur central : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            clients.add(out);

            String line;
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split(" ", 3);
                String command = tokens[0];
                switch (command) {
                    case "GETD":
                        handleGetDocument(out);
                        break;
                    case "MDFL":
                        handleModifyLine(tokens, out);
                        break;
                    case "ADDL":
                        handleAddLine(tokens, out);
                        break;
                    case "RMVL":
                        handleRemoveLine(tokens, out);
                        break;
                    default:
                        out.println("ERRL commande inconnue: " + command);
                        out.println("DONE");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client est déconnecté .");
        }
    }

    private static void handleGetDocument(PrintWriter out) {
        synchronized (document) {
            for (int i = 0; i < document.size(); i++) {
                out.println("LINE " + i + " " + document.get(i));
            }
            out.println("DONE");
        }
    }

    private static void handleModifyLine(String[] tokens, PrintWriter out) {
        if (tokens.length < 3) return;
        int index = Integer.parseInt(tokens[1]);
        String newText = tokens[2];
        synchronized (document) {
            if (index >= 0 && index < document.size()) {
                document.set(index, newText);
                broadcast("LINE " + index + " " + newText);
            }
        }
        out.println("OK");
    }

    private static void handleAddLine(String[] tokens, PrintWriter out) {
        if (tokens.length < 3) return;
        int index = Integer.parseInt(tokens[1]);
        String newText = tokens[2];
        synchronized (document) {
            if (index >= 0 && index <= document.size()) {
                document.add(index, newText);
                broadcast("ADDL " + index + " " + newText);
            }
        }
        out.println("OK");
    }

    private static void handleRemoveLine(String[] tokens, PrintWriter out) {
        if (tokens.length < 2) return;
        int index = Integer.parseInt(tokens[1]);
        synchronized (document) {
            if (index >= 0 && index < document.size()) {
                document.remove(index);
                broadcast("RMVL " + index);
            }
        }
        out.println("OK");
    }

    private static void broadcast(String msg) {
        synchronized (clients) {
            for (PrintWriter client : clients) {
                client.println(msg);
            }
        }
        synchronized (peerWriters) {
            for (PrintWriter peer : peerWriters) {
                peer.println(msg);
            }
        }
    }
}

