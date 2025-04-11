package amu.editor;

import java.io.*;
import java.net.Socket;

public class ClientAuto2 {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 12346);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        Thread.sleep(1000);

        out.println("MDFL 1 Modifié par Client 2");
        out.println("ADDL 3 Ajouté par Client 2");

        Thread.sleep(2000);

        out.println("GETD");
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println("[ClientAuto2] " + line);
            if (line.equals("DONE")) break;
        }

        socket.close();
    }
}

