package amu.editor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;




public class ServerCentral {
    private static List<String> document = new ArrayList<>(); // mon document partagé je le modelise avec une array list
    private static int port = 1234;// j edefinis mon port

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur démarré sur le port " + port);
            while (true) {// j'attend la connexion du client
                Socket socket = serverSocket.accept();// j'ai créer un serveur qui est capable d'accepter un client a n importe qulle moment donnée
                System.out.println("Nouveau client connecté : " + socket.getInetAddress());
                // chaque fois un client est connecté je crée pour luis on propre thread avec Editeur une classe que je vais créer ulterieurment
                new Editeur(socket, document).run();



            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


