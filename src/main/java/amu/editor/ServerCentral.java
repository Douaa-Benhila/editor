package amu.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class ServerCentral {
    private static final int PORT = 12345;// je definis mon port
    private static final List<String> document = Collections.synchronizedList(new ArrayList<>(Arrays.asList(
            "FIRST WITCH  When shall we three meet again?",
            "   In thunder, lightning, or in rain?",
            "SECOND WITCH  When the hurly-burly’s done",
            "   When the battle’s lost and won.",
            "THIRD WITCH  That will be ere the set of sun"
    )));//mon document partagé je le modelise avec  liste

    // tache3 et 4
    private static final List<PrintWriter> clients = Collections.synchronizedList(new ArrayList<>());// liste des clients connectées ca va m'aider dans la federation entre serveurs
    // tache 5
    // Liste des sockets vers les pairs (autres serveurs de la fédération)
    private static final List<Socket> peerSockets = new ArrayList<>();
    private static final List<PrintWriter> peerWriters = new ArrayList<>();

    public static void main(String[] args) {
        try {

            // Étape 1 : je charge mon fichier peers pour savoir si je suis maître ou pair
            PeerConfig config = PeerConfig.load("peers.cfg", PORT);

            if (config.isMaster()) { // Si je suis le maître
                for (PeerConfig.Peer peer : config.getPeers()) { // Je parcours tous les pairs
                    try {
                        // Je me connecte à chaque pair
                        Socket peerSocket = new Socket(peer.host(), peer.port());
                        peerSockets.add(peerSocket); // Je garde la socket pour l'utiliser plus tard
                        peerWriters.add(new PrintWriter(peerSocket.getOutputStream(), true)); // Pour écrire facilement
                        System.out.println("Connecté au pair : " + peer.host() + ":" + peer.port());
                    } catch (IOException e) {
                        // Si je n’arrive pas à me connecter à un pair
                        System.err.println("Erreur de connexion au pair : " + peer.host() + ":" + peer.port());
                    }
                }
            }

            // Étape 2 : Démarrage du serveur (écoute des clients)
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Serveur Central démarré sur le port  " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nouveau client connecté  " + clientSocket.getInetAddress());
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            }

        } catch (Exception e) {
            // Catch global si quelque chose échoue avant le démarrage du serveur
            System.err.println("Erreur au démarrage du serveur central : " + e.getMessage());
            e.printStackTrace();
        }

    }

    //Gérer la communication avec un client
    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
             clients.add(out); // j'ajoute mes clients a la liste



            String line;
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split(" ", 3);
                String command = tokens[0];
                System.out.println(command);
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
                    case "GETL":
                        handleGetLine(tokens, out);
                        break;
                    default:
                        out.println("ERRL commande inconnue: " + command);
                        out.println("DONE");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client est déconnecté .");
        } catch (NumberFormatException e) {
            System.err.println("format de numero est invalide dans cette commande ");
        }
    }

    private static void handleGetDocument(PrintWriter out) {
        synchronized (document) {
            if (document.isEmpty()) {
                out.println("DONE");
            } else {
                for (int i = 0; i < document.size(); i++) {
                    out.println("LINE " + i + " " + document.get(i));
                }
                out.println("DONE");
            }
        }
    }

    private static void handleModifyLine(String[] tokens, PrintWriter out) {
        if (tokens.length < 3) {
            out.println("ERRL FORMAT INVALIDE ");
            out.println("OK");
            return;
        }

        int mdfIndex = Integer.parseInt(tokens[1]);
        String newText = tokens[2];

        synchronized (document) {
            if (mdfIndex >= 0 && mdfIndex < document.size()) {
                document.set(mdfIndex, newText);
                System.out.println("[SERVER] Ligne modifiée à l'index " + mdfIndex + " : " + newText);

                broadcastAll("LINE " + mdfIndex + " " + newText);
                // tache 5
                broadcastToPeers("LINE " + mdfIndex + " " + newText);

            } else {
                out.println("ERRL " + mdfIndex + " INDEX INVALIDE");
            }
        }
        out.println("OK");
    }


    private static void handleAddLine(String[] tokens, PrintWriter out) {
        if (tokens.length < 3) {
            out.println("ERRL FORMAT INVALIDE");
            out.println("OK");
            return;
        }

        int addIndex = Integer.parseInt(tokens[1]);
        String addText = tokens[2];

        synchronized (document) {
            if (addIndex >= 0 && addIndex <= document.size()) {
                document.add(addIndex, addText);
                System.out.println("[SERVER] Ligne ajoutée à l'index " + addIndex + " : " + addText);

                // tache 3 et 4
                broadcastAll("ADDL " + addIndex + " " + addText);
                // tache 5
                broadcastToPeers("ADDL " + addIndex + " " + addText); // Envoie au pair
            } else {
                out.println("ERRL " + addIndex + " INDEX INVALIDE");
            }
        }
        out.println("OK");
    }


    private static void handleRemoveLine(String[] tokens, PrintWriter out) {
        if (tokens.length < 2) {
            out.println("ERRL FORMAT INVALIDE ");
            out.println("OK");
            return;
        }

        int rmIndex = Integer.parseInt(tokens[1]);

        synchronized (document) {
            if (rmIndex >= 0 && rmIndex < document.size()) {
                document.remove(rmIndex);
                System.out.println("[SERVER] Ligne supprimée à l'index " + rmIndex);

                // tache 3 et 4
                broadcastAll("RMVL " + rmIndex);
                // tache 5
                broadcastToPeers("RMVL " + rmIndex); // Envoie au pair
            } else {
                out.println("ERRL " + rmIndex + " INDEX INVALIDE ");
            }
        }
        out.println("OK");
    }

    private static void handleGetLine(String[] tokens, PrintWriter out) {
        if (tokens.length < 2) {
            out.println("ERRL format invalide");
            out.println("OK");
            return;
        }
        try {
            int index = Integer.parseInt(tokens[1]);
            synchronized (document) {
                if (index >= 0 && index < document.size()) {
                    out.println("LINE " + index + " " + document.get(index));
                } else {
                    out.println("ERRL " + index + " Invalid index");
                }
            }
        } catch (NumberFormatException e) {
            out.println("ERRL Invalid number format");
        }
        out.println("OK");
    }

    // methode qui envoie les changements a tous les clients
    private static void broadcastAll(String msg) {
        synchronized (clients) {
            for (PrintWriter client : clients) {
                client.println(msg);
            }
        }
    }
    // Envoie un message à tous les pairs
    private static void broadcastToPeers(String msg) {
        synchronized (peerWriters) {
            for (PrintWriter peer : peerWriters) {
                peer.println(msg);
            }
        }
    }



}

