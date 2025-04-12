package amu.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

// serveur tache 1
public class ServerCentral1 {
    private static final int PORT = 12345;// je definis mon port
    private static final List<String> document = Collections.synchronizedList(new ArrayList<>(Arrays.asList(
            "FIRST WITCH  When shall we three meet again?",
            "   In thunder, lightning, or in rain?",
            "SECOND WITCH  When the hurly-burly’s done",
            "   When the battle’s lost and won.",
            "THIRD WITCH  That will be ere the set of sun"
    )));//mon document partagé je le modelise avec  liste


    public static void main(String[] args) {
       //Démarrage du serveur (écoute des clients)
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Serveur Central démarré sur le port  " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nouveau client connecté  " + clientSocket.getInetAddress());
                    new Thread(() -> handleClient(clientSocket)).start();
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

}

