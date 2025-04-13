import java.io.*;
import java.net.Socket;

public class ClientAutoPush {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 22222); // Connexion à ServerPush
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Attente pour que les autres serveurs soient démarrés
        Thread.sleep(1000);

        out.println("MDFL 1 Modifié par Push");

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