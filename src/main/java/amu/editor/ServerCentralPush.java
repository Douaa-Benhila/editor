package amu.editor;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerCentralPush {

    private static final int PORT = 12346;


    private static final List<String> document = Collections.synchronizedList(new ArrayList<>(Arrays.asList(
            "FIRST WITCH  When shall we three meet again?",
            "   In thunder, lightning, or in rain?",
            "SECOND WITCH  When the hurly-burly’s done",
            "   When the battle’s lost and won.",
            "THIRD WITCH  That will be ere the set of sun"
    )));


    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>()); //Liste de tous les clients connectés

    public static void main(String[] args) {
        // methode pour federation connecte deux serveurs
        connectToServer("localhost", 12345);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Serveur PUSH démarré sur le port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecté : " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe interne pour gérer chaque client
    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void sendMessage(String msg) {
            if (out != null) {
                out.println(msg);
            }
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Envoie le document initial à la connexion
                synchronized (document) {
                    for (int i = 0; i < document.size(); i++) {
                        out.println("LINE " + i + " " + document.get(i));
                    }
                    out.println("DONE");
                }

                String line;
                while ((line = in.readLine()) != null) {
                    String[] tokens = line.split(" ", 3);
                    String command = tokens[0];

                    switch (command) {
                        case "MDFL":
                            handleModify(tokens);
                            break;
                        case "ADDL":
                            handleAdd(tokens);
                            break;
                        case "RMVL":
                            handleRemove(tokens);
                            break;
                        default:
                            out.println("ERRL Unknown command");
                    }
                }

            } catch (IOException e) {
                System.err.println("Client déconnecté");
            } finally {
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleModify(String[] tokens) {
            if (tokens.length < 3) {
                out.println("ERRL Invalid format");
                return;
            }

            int index = Integer.parseInt(tokens[1]);
            String newText = tokens[2];

            synchronized (document) {
                if (index >= 0 && index < document.size()) {
                    document.set(index, newText);
                    out.println("OK");

                    // Notifie les autres clients
                    broadcastAll("LINE " + index + " " + newText);
                } else {
                    out.println("ERRL Invalid index");
                }
            }
        }

        private void handleAdd(String[] tokens) {
            if (tokens.length < 3) {
                out.println("ERRL Invalid format");
                return;
            }

            int index = Integer.parseInt(tokens[1]);
            String newText = tokens[2];

            synchronized (document) {
                if (index >= 0 && index <= document.size()) {
                    document.add(index, newText);
                    out.println("OK");

                    // Notifie les autres clients
                    broadcastAll("ADDL " + index + " " + newText);
                } else {
                    out.println("ERRL Invalid index");
                }
            }
        }

        private void handleRemove(String[] tokens) {
            if (tokens.length < 2) {
                out.println("ERRL Invalid format");
                return;
            }

            int index = Integer.parseInt(tokens[1]);

            synchronized (document) {
                if (index >= 0 && index < document.size()) {
                    document.remove(index);
                    out.println("OK");

                    // Notifie les autres clients
                    broadcastAll( "RMVL " + index);
                } else {
                    out.println("ERRL Invalid index");
                }
            }
        }
    }

    private static void broadcastAll(String msg) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(msg);
            }
        }
    }
    // Méthode pour connecter ServerCentralPush à u ServerCentral
    public static void connectToServer(String host, int port) {
        new Thread(() -> {
            try {
                // Connexion à un autre serveur
                Socket socket = new Socket(host, port);
                System.out.println("onnexion au serveur central sur " + host + ":" + port);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                // demande la version actuelle du document
                out.println("GETD");

                String line;
                while ((line = in.readLine()) != null && !line.equals("DONE")) {
                    if (line.startsWith("LINE")) {
                        String[] parts = line.split(" ", 3);
                        int index = Integer.parseInt(parts[1]);
                        String content = parts[2];

                        // Met à jour le document local
                        synchronized (document) {
                            while (document.size() <= index) document.add("");
                            document.set(index, content);
                        }

                        // Notifie les clients push
                        broadcastAll("LINE " + index + " " + content);
                    }
                }

                // Ensuite, écoute les mises à jour (ADDL, MDFL, RMVL)
                while ((line = in.readLine()) != null) {
                    handleServerUpdate(line);
                }

            } catch (IOException e) {
                System.err.println("Erreur de fédération : " + e.getMessage());
            }
        }).start(); // Démarre un thread séparé
    }
    // Applique une mise à jour reçue depuis le serveur central
    private static void handleServerUpdate(String line) {
        String[] tokens = line.split(" ", 3);
        String command = tokens[0];

        synchronized (document) {
            try {
                switch (command) {
                    case "ADDL":
                        int addIndex = Integer.parseInt(tokens[1]);
                        String addText = tokens[2];


                        if (addIndex >= 0 && addIndex <= document.size()) {
                            document.add(addIndex, addText);
                            // Notifie tous les clients push
                            broadcastAll("ADDL " + addIndex + " " + addText);
                        }
                        break;

                    case "MDFL":
                    case "LINE":
                        int modIndex = Integer.parseInt(tokens[1]);
                        String modText = tokens[2];

                        // S'assure que la ligne existe
                        while (document.size() <= modIndex) {
                            document.add(""); // complète si ligne manquante
                        }

                        document.set(modIndex, modText);
                        broadcastAll("LINE " + modIndex + " " + modText);
                        break;

                    case "RMVL":
                        int rmIndex = Integer.parseInt(tokens[1]);


                        if (rmIndex >= 0 && rmIndex < document.size()) {
                            document.remove(rmIndex);
                            broadcastAll("RMVL " + rmIndex);
                        }
                        break;

                    default:
                        System.err.println("Commande inconnue du serveur central : " + command);
                }
            } catch (Exception e) {
                System.err.println("Erreur dans handleServerUpdate : " + e.getMessage());
            }
        }
    }



}

