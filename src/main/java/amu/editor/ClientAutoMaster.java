package amu.editor;

import java.io.*;
import java.net.Socket;

public class ClientAutoMaster {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 12345); // Connexion à ServerCentral
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Attente pour que les autres serveurs soient démarrés
        Thread.sleep(1000);


        out.println("ADDL 3 Ligne ajoutée par Master");
        Thread.sleep(1000);

        out.println("GETD"); // Récupération du document final
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("[ClientAutoReplica] " + line);
            if (line.equals("DONE")) break;
        }

        socket.close();
    }
}

