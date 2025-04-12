package amu.editor;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerCentralPush2 {

    private static final int PORT = 12348;


    private static final List<String> document = Collections.synchronizedList(new ArrayList<>(Arrays.asList(
            "FIRST WITCH  When shall we three meet again?",
            "   In thunder, lightning, or in rain?",
            "SECOND WITCH  When the hurly-burly’s done",
            "   When the battle’s lost and won.",
            "THIRD WITCH  That will be ere the set of sun"
    )));


    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>()); //Liste de tous les clients connectés
    private static PrintWriter serverCentralOut; // Writer vers le serveur central


    public static void main(String[] args) {
        // methode pour federation connecte deux serveurs tache 3 et 4
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
            System.err.println("Erreur au démarrage du serveur push : " + e.getMessage());
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
                    if (serverCentralOut != null) serverCentralOut.println("MDFL " + index + " " + newText);
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
                    if (serverCentralOut != null) serverCentralOut.println("ADDL " + index + " " + newText);
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
                    if (serverCentralOut != null) serverCentralOut.println("RMVL " + index);
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
    // Méthode pour connecter ServerCentralPush2 à  ServerCentral1
    public static void connectToServer(String host, int port) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(host, port);
                System.out.println("Connexion au serveur central sur " + host + ":" + port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                serverCentralOut = new PrintWriter(socket.getOutputStream(), true);
                serverCentralOut.println("GETD");

                String line;
                while ((line = in.readLine()) != null && !line.equals("DONE")) {
                    if (line.startsWith("LINE")) {
                        String[] parts = line.split(" ", 3);
                        int index = Integer.parseInt(parts[1]);
                        String content = parts[2];
                        synchronized (document) {
                            while (document.size() <= index) document.add("");
                            document.set(index, content);
                        }
                        broadcastAll("LINE " + index + " " + content);
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur de fédération : " + e.getMessage());
            }
        }).start();
    }}



