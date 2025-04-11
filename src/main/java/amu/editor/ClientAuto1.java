package amu.editor;

import java.io.*;
import java.net.Socket;

public class ClientAuto1 {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 12346);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Thread.sleep(1000);

        out.println("MDFL 0 Modifié par Client 1");
        out.println("ADDL 2 Ajouté par Client 1");

        Thread.sleep(2000);

        out.println("GETD");  // Affiche le document final
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("[ClientAuto1] " + line);
            if (line.equals("DONE")) break;
        }

        socket.close();
    }
}

