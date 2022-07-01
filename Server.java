import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Ceci est la classe Server
 * Gère les connexions des clients, des threads ...
 */

public class Server {

    private HashMap<String, Integer> allPlayers;
    private HashMap<String, String> playersMachines;
    private ArrayList<Game> parties;
    private int addresseMultDif;
    private int portMultDif;

    public Server() {
        allPlayers = new HashMap<>();
        playersMachines = new HashMap<>();
        parties = new ArrayList<>();
    }

    /**
     * Ajout d'un joueur au serveur quand un client entre la commande NEW ou REG
     * 
     * @param : id son pseudo
     *          port : son port UDP
     *          machine : (ou l'ip de la machine) sur lequel tourne le serveur
     */
    public synchronized boolean newPlayer(String id, int port, String machine) {
        if (allPlayers.putIfAbsent(id, port) != null) {
            return false;
        }
        playersMachines.put(id, machine);
        return true;
    }

    /**
     * Suppression d'un joueur du serveur, quand sa partie est finie ou quand lui
     * même demande à quitter
     * 
     * @param : id le pseudo du joueur à enlever
     */
    public synchronized void remPlayer(String id) {
        allPlayers.remove(id);
        playersMachines.remove(id);
    }

    /**
     * Récupération du la liste de tout les joueurs (id + port UDP)
     */
    public HashMap<String, Integer> getPlayers() {
        return allPlayers;
    }

    /**
     * Récupération du port UDP d'un joueur précis
     * 
     * @param : id son pseudo
     */
    public int getPortPlayer(String id) {
        return allPlayers.get(id);
    }

    /**
     * Récupération de la machine sur laquelle joue le joueur
     * 
     * @param : id son pseudo
     */
    public String getAddrPlayer(String id) {
        return playersMachines.get(id);
    }

    /**
     * Récupération d'une game donc d'une partie
     * 
     * @param : num le numéro donc l'id de la partie
     */
    public synchronized Game getGame(int num) {
        return parties.get(num);
    }

    /**
     * Ajout d'une partie à la liste du serveur
     * 
     * @param : game la partie en question
     */
    public synchronized void addGame(Game g) {
        parties.add(g);
        addresseMultDif++;
        portMultDif++;
    }

    /**
     * Retourne le nombre de parties sur le serveur
     */
    public synchronized int getNbParties() {
        return parties.size();
    }

    /**
     * Retourne le nombre de parties qui n'ont pas commencées
     */
    public synchronized int getNbPartiesNC() {
        int res = 0;
        for (int i = 0; i < parties.size(); i++) {
            if (!parties.get(i).isBegun()) {
                res++;
            }
        }
        return res;
    }

    /**
     * Verifie si l'id existe déjà dans le serveur, 0 existe pas et 1 existe déjà
     */
    public synchronized int existAlreadyID(String id, int port) {
        for (HashMap.Entry<String, Integer> j : allPlayers.entrySet()) {
            if (j.getKey() == id || j.getValue() == port) {
                return 1;
            }
        }
        return 0;

    }

    /**
     * Fonction main 
     * @param args
     */
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(3773);
            System.out.println("Le port local est " + server.getLocalPort());
            System.out.println("La machine locale est " + InetAddress.getLocalHost() +
                    "\n");
            Server serv = new Server();
            while (true) {
                Socket socket = server.accept();
                PlayerService pserv = new PlayerService(serv, socket);
                Thread t = new Thread(pserv);
                t.start();
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
