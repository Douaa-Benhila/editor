package amu.editor;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeerConfig {
    private boolean isMaster = false; // Indique si ce serveur est le maître
    private String masterHost;        // Adresse IP du maître
    private int masterPort;           // Port du maître
    private final List<Peer> peers = new ArrayList<>();


    public PeerConfig(String myHost, int myPort) {
        try (BufferedReader reader = new BufferedReader(new FileReader("peers.cfg"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); // Supprime les espaces en début/fin de ligne
                if (line.startsWith("master =")) {
                    String[] parts = line.substring(8).trim().split(" ");
                    masterHost = parts[0]; // Récupère l'adresse IP du maître
                    masterPort = Integer.parseInt(parts[1]); // Récupère le port du maître
                    // Vérifie si ce serveur est le maître en comparant l’adresse et le port
                    if (masterHost.equals(myHost) && masterPort == myPort) {
                        isMaster = true;
                    }
                } else if (line.startsWith("peer =")) {
                    String[] parts = line.substring(6).trim().split(" ");
                    String peerHost = parts[0];
                    int peerPort = Integer.parseInt(parts[1]);peers.add(new Peer(peerHost, peerPort));

                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture de peers.cfg : " + e.getMessage());
        }
    }

    public boolean isMaster() {
        return isMaster; // Renvoie vrai si ce serveur est le maître
    }

    public String getMasterHost() {
        return masterHost; // Renvoie l’adresse du maître
    }

    public int getMasterPort() {
        return masterPort; // Renvoie le port du maître
    }

    public List<Peer> getPeers() {
        return peers; // Renvoie la liste des pairs
    }
    public static PeerConfig load(String filePath, int myPort) {
        // Utilise 127.0.0.1 comme IP par défaut
        return new PeerConfig("127.0.0.1", myPort);
    }

    public static class Peer {
        private final String host;
        private final int port;

        public Peer(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String host() {
            return host;
        }

        public int port() {
            return port;
        }
    }

}
