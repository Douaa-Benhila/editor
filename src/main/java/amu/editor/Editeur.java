package amu.editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class Editeur implements Runnable {
    private Socket socket;
    private List<String> document;

    public Editeur(Socket socket,List<String> document) {
        this.socket = socket;
        this.document = document;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request;
            while ((request = in.readLine()) != null) {
                String[] parts = request.split(" ", 2);
                String command = parts[0];
                String response = "";

                switch (command) {
                    case "GETD":
                        synchronized (document) {
                            for (int i = 0; i < document.size(); i++) {
                                out.println("LINE " + (i + 1) + " " + document.get(i));
                            }
                        }
                        out.println("DONE");
                        break;

                    case "GETL":
                        int lineNumber = Integer.parseInt(parts[1]) - 1;
                        synchronized (document) {
                            if (lineNumber >= 0 && lineNumber < document.size()) {
                                out.println("LINE " + (lineNumber + 1) + " " + document.get(lineNumber));
                            } else {
                                out.println("ERRL " + (lineNumber + 1) + " Line does not exist");
                            }
                        }
                        break;

                    case "MDFL":
                        String[] mdfParts = parts[1].split(" ", 2);
                        int mdfLineNumber = Integer.parseInt(mdfParts[0]) - 1;
                        synchronized (document) {
                            if (mdfLineNumber >= 0 && mdfLineNumber < document.size()) {
                                document.set(mdfLineNumber, mdfParts[1]);
                                out.println("OK");
                            } else {
                                out.println("ERRL " + (mdfLineNumber + 1) + " Line does not exist");
                            }
                        }
                        break;

                    case "RMVL":
                        int rmvLineNumber = Integer.parseInt(parts[1]) - 1;
                        synchronized (document) {
                            if (rmvLineNumber >= 0 && rmvLineNumber < document.size()) {
                                document.remove(rmvLineNumber);
                                out.println("OK");
                            } else {
                                out.println("ERRL " + (rmvLineNumber + 1) + " Line does not exist");
                            }
                        }
                        break;

                    case "ADDL":
                        String[] addParts = parts[1].split(" ", 2);
                        int addLineNumber = Integer.parseInt(addParts[0]) - 1;
                        synchronized (document) {
                            if (addLineNumber >= 0 && addLineNumber <= document.size()) {
                                document.add(addLineNumber, addParts[1]);
                                out.println("OK");
                            } else {
                                out.println("ERRL " + (addLineNumber + 1) + " Invalid line number");
                            }
                        }
                        break;

                    default:
                        out.println("ERRL Invalid command");
                        break;
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
