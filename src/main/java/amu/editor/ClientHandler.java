package amu.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final List<String> document = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                handleRequest(request, out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(String request, PrintWriter out) {
        String[] parts = request.split(" ", 3);
        String command = parts[0];
        lock.lock();
        try {
            switch (command) {
                case "GETD":
                    for (int i = 0; i < document.size(); i++) {
                        out.println("LINE " + (i + 1) + " " + document.get(i));
                    }
                    out.println("DONE");
                    break;
                case "GETL":
                    int getIndex = Integer.parseInt(parts[1]) - 1;
                    if (getIndex >= 0 && getIndex < document.size()) {
                        out.println("LINE " + (getIndex + 1) + " " + document.get(getIndex));
                    } else {
                        out.println("ERRL " + parts[1] + " Ligne inexistante");
                    }
                    break;
                case "MDFL":
                    int modIndex = Integer.parseInt(parts[1]) - 1;
                    if (modIndex >= 0 && modIndex < document.size()) {
                        document.set(modIndex, parts[2]);
                    } else {
                        out.println("ERRL " + parts[1] + " Modification impossible");
                    }
                    break;
                case "RMVL":
                    int rmIndex = Integer.parseInt(parts[1]) - 1;
                    if (rmIndex >= 0 && rmIndex < document.size()) {
                        document.remove(rmIndex);
                    } else {
                        out.println("ERRL " + parts[1] + " Suppression impossible");
                    }
                    break;
                case "ADDL":
                    int addIndex = Integer.parseInt(parts[1]) - 1;
                    if (addIndex >= 0 && addIndex <= document.size()) {
                        document.add(addIndex, parts[2]);
                    } else {
                        out.println("ERRL " + parts[1] + " Ajout impossible");
                    }
                    break;
                default:
                    out.println("ERRL Commande inconnue");
            }
        } catch (Exception e) {
            out.println("ERRL Erreur interne");
        } finally {
            lock.unlock();
        }
    }
}


