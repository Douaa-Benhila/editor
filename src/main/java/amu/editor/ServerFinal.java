package amu.editor;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServerFinal {
    private static final List<String> document = Collections.synchronizedList(new ArrayList<>());
    private static final List<PrintWriter> clients = new ArrayList<>();
    private static final Set<String> applied = Collections.synchronizedSet(new HashSet<>());

    private static int myPort = 33333;
    private static final int[] peerPorts = {33333, 22222, 11111};

    public static void main(String[] args) throws IOException {
        document.addAll(List.of(
                "FIRST WITCH  When shall we three meet again?",
                "   In thunder, lightning, or in rain?",
                "SECOND WITCH  When the hurly-burly’s done",
                "   When the battle’s lost and won.",
                "THIRD WITCH  That will be ere the set of sun"
        ));

        for (int port : peerPorts) {
            if (port != myPort) {
                new Thread(() -> connectToPeer(port)).start();
            }
        }

        ServerSocket serverSocket = new ServerSocket(myPort);
        System.out.println("[⚙] Serveur lancé sur le port " + myPort);

        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    synchronized (clients) { clients.add(out); }
                    new Thread(() -> handleInput(in)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void connectToPeer(int port) {
        try {
            Socket socket = new Socket("localhost", port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            synchronized (clients) { clients.add(out); }
            new Thread(() -> handleInput(in)).start();
        } catch (IOException e) {
            System.out.println("[WARN] Connexion échouée au port " + port);
        }
    }

    private static void handleInput(BufferedReader in) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("GETD")) {
                    sendDocument();
                } else if (line.startsWith("MSG")) {
                    String[] parts = line.split(" ", 5);
                    if (parts.length < 5) return;
                    String uuid = parts[1];
                    String cmd = parts[2];
                    int index = Integer.parseInt(parts[3]);
                    String text = parts[4];
                    if (!applied.contains(uuid)) {
                        applied.add(uuid);
                        applyCommand(cmd, index, text);
                        synchronized (clients) {
                            for (PrintWriter client : clients) {
                                client.println("MSG " + uuid + " " + cmd + " " + index + " " + text);
                            }
                        }
                        broadcast(line);
                    }
                } else if (line.startsWith("ADDL") || line.startsWith("MDFL") || line.startsWith("RMVL")) {
                    String[] parts = line.split(" ", 3);
                    if (parts.length < 2) return;
                    String cmd = parts[0];
                    int index = Integer.parseInt(parts[1]);
                    String text = parts.length == 3 ? parts[2] : "";
                    String uuid = UUID.randomUUID().toString();
                    String fullMsg = "MSG " + uuid + " " + cmd + " " + index + " " + text;
                    applied.add(uuid);
                    applyCommand(cmd, index, text);
                    synchronized (clients) {
                        for (PrintWriter client : clients) {
                            client.println("MSG " + uuid + " " + cmd + " " + index + " " + text);
                        }
                    }
                    broadcast(fullMsg);
                }
            }
        } catch (IOException e) {
            System.out.println("[INFO] Déconnecté : " + e.getMessage());
        }
    }

    private static void applyCommand(String cmd, int index, String text) {
        synchronized (document) {
            switch (cmd) {
                case "ADDL":
                    if (index >= 0 && index <= document.size()) document.add(index, text);
                    break;
                case "MDFL":
                    if (index >= 0 && index < document.size()) document.set(index, text);
                    break;
                case "RMVL":
                    if (index >= 0 && index < document.size()) document.remove(index);
                    break;
            }
        }
    }

    private static void broadcast(String msg) {
        for (int port : peerPorts) {
            if (port == myPort) continue;
            try {
                Socket s = new Socket("localhost", port);
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                out.println(msg);
                s.close();
            } catch (IOException ignored) {}
        }
    }

    private static void sendDocument() {
        synchronized (clients) {
            for (PrintWriter client : clients) {
                synchronized (document) {
                    for (int i = 0; i < document.size(); i++) {
                        client.println("LINE " + i + " " + document.get(i));
                    }
                }
                client.println("DONE");
            }
        }
    }
}


