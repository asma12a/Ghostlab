import java.io.*;
import java.util.*;
import java.math.*;
import java.net.*;

/**
 * @class Game
 * Ceci est le code d'une partie du Labyrinthe
 */

public class Game {

	// Choix durant le jeu
	private static final int LEFT = 0;
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;
	private Labyrinthe lab;

	private static int ipincrMul = 100;
	private static int portincrMul = 10000;
	private String ipMul = "225.103.207." + ipincrMul;
	private int portMul = portincrMul;
	private HashMap<String, Boolean> players;
	private ArrayList<int[]> coordPlayers;
	private ArrayList<Integer> scores;
	private int[][] fant;
	private int[] etatFantomes;
	private boolean begun, end, diff;
	private int nbFantomes;
	private InetSocketAddress ia;

	private int lastMoved, gameMode = 0, eater;
	private String nomEater;

	/**
	 * Constructeur d'une partie
	 * @param lab 
	 * @see Labyrinthe
	 */
	public Game(Labyrinthe lab) {
		this.lab = lab;
		this.players = new HashMap<String, Boolean>();
		nbFantomes = (lab.getWidth() + lab.getHeight()) / 2;
		fant = new int[nbFantomes][2];
		etatFantomes = new int[nbFantomes];
		this.begun = false;
		this.end = false;
		this.diff = false;
		coordPlayers = new ArrayList<>();
		scores = new ArrayList<>();
		ia = new InetSocketAddress(ipMul, portMul);
		for (int i = 0; i < etatFantomes.length; i++) {
			etatFantomes[i] = 1; // Présence du fantome
		}

		ipincrMul++;
		portincrMul++;
	}

	/**
    * @return l'ip de multidiffusion 
    */

	public String getAddressMultDif() {
		return ipMul;
	}


	/**
    * @return le port de multidiffusion 
    */

	public int getPortMultDif() {
		return portMul;
	}

	/**
	 * Mode de jeu
	 * @return i : l'entier correspondant à un mode de jeu (classique[0],Stealing[1],eater[2])
	 */
	public int getMode() {
		return gameMode;
	}

	/** 
	 * @param i : mode de jeu (classique[0],Stealing[1],eater[2])
	 * @return
	 */
	public void setMode(int i) {
		gameMode = i;
	}

	/**
	 * @param
	 * @return le mode du jeu en fonction de l'attribut gameMode
	 */
	public synchronized String gameModeToString() {
		switch (gameMode) {
			case 0:
				return "CLASS";
			case 1:
				return "STEAL";
			case 2:
				return "EATER";
			case 3:
				return "VERBE";
			default:
				return "CLASS";
		}
	}

	/**
	 * Ajout d'un joueur à la partie (suites à un NEW ou REG), on met un deuxième
	 * attribut d'office à false indiquant qu'il n'est pas près à jouer (changeable
	 * en faisant START***)
	 * 
	 * @param : nom le pseudo du joueur
	 */
	public synchronized void ajouterPlayer(String nom) {
		players.put(nom, false);
		placePlayer();
	}

	/**
	 * Récupérer l'id d'un joueur dans la partie en fonction de son nom
	 * 
	 * @param : nom le pseudo du joueur
	 */
	public int idToIndice(String nom) {
		int res = -1;
		for (Map.Entry j : players.entrySet()) {
			res++;
			if (((String) j.getKey()).equals(nom))
				return res;
		}
		return -1;
	}

	/**
	 * Changer le labyrinthe pour le mode verbeux
	 * 
	 */

	public Labyrinthe setLabVerbeux() {
		lab = new Labyrinthe("VERBEUX.txt");
		nbFantomes = 1;

		return lab;
	}

	/**
	 * Suppression d'un joueur de la partie
	 * 
	 * @param : nom le pseudo du joueur à supprimer
	 */
	public synchronized boolean delete(String nom) {
		int ind = idToIndice(nom);
		if (ind != (-1)) {
			players.remove(nom);
			if (!coordPlayers.isEmpty()) {
				coordPlayers.remove(ind);
				scores.remove(ind);
			}
			return true;
		}
		return false;
	}

	/**
	 * Récupère la position d'un joueur dans le labyrinthe
	 * 
	 * @param : nom le pseudo du joueur dont on veut la position
	 * @return tab
	 */
	public synchronized int[] getPos(String nom) {
		int ind = idToIndice(nom);
		if (ind != (-1)) {
			return coordPlayers.get(ind);
		}
		int tab[] = { -1, -1 };
		return tab;
	}

	/**
	 * Surcharge de la fonction précédente pour avoir la position d'un fantôme
	 * 
	 * @param : i, l'id du fantôme en question dans le tableau de coordonnées des
	 *          fantômes
	 */
	public synchronized int[] getPos(int i) {
		return fant[i];
	}

	/**
	 * Fonction qui print le labyrinthe, les joueurs, les fantômes côté serveur
	 */
	public void affiche() {
		lab.afficheLabyrinthe();
	}

	/**
	 * Renvoie les dimensions du labyrinthe
	 */
	public int[] getLabTaille() {
		int[] tab = new int[2];
		tab[0] = lab.getHeight();
		tab[1] = lab.getWidth();
		return tab;
	}

	/**
	 * Renvoi le nombre de fantômes dans le labyrinthe
	 */
	public synchronized int getNbFantomes() {
		return nbFantomes;
	}

	/**
	 * Renvoi true si la partie est finie, false sinon
	 */
	public synchronized boolean getEnd() {
		return end;
	}

	/**
	 * Change l'état de la partie si elle est finie en mettant à true
	 */
	public synchronized void setEnd() {
		end = true;
	}

	/**
	 * Renvoi l'attribut diff (utilisé dans PlayerService)
	 */
	public boolean getDiff() {
		return diff;
	}

	/**
	 * Change la valeur de diff
	 */
	public void setDiff() {
		diff = true;
	}

	/**
	 * Retourne le nombre de joueurs présents dans la partie
	 */
	public synchronized int getNbJoueurs() {
		return players.size();
	}

	/**
	 * Renvoie un tableau composé des pseudos des joueurs
	 */
	public synchronized String[] getJoueurs() {
		String[] res = new String[getNbJoueurs()];
		int i = 0;
		for (Map.Entry e : players.entrySet()) {
			res[i++] = (String) e.getKey();
		}
		return res;
	}

	/**
	 * Retourne une InetSocketAddress de la partie elle-même
	 */
	public InetSocketAddress getInet() {
		return ia;
	}

	/**
	 * Retourne les points d'un joueur en fonction de son pseudo
	 * 
	 * @param : nom le pseudo du joueur
	 */
	public int getPoints(String nom) {
		return scores.get(idToIndice(nom));
	}

	/**
	 * Renvoi true si la partie à commencée, false sinon
	 */
	public synchronized boolean isBegun() {
		return begun;
	}

	/**
	 * Change le statut de la partie quand elle commence
	 * et place les fantomes dans le labyrinthe
	 */
	public synchronized void setBegun() {
		if (!begun) {
			begun = true;
			moveGhosts();
			if (gameMode == 2) {
				eater = (int) (Math.random() * (getNbJoueurs()));
				nomEater = getJoueurs()[eater];
			}
		}
	}

	/**
	 * Change le statut d'un joueur en le mettant à "prêt"
	 */
	public void isReady(String nom) {
		players.replace(nom, true);
	}

	/**
	 * Permet de savoir si un joueur est encore vivant ou non dans le
	 * mode eater
	 * 
	 * @param nom : le pseudo du joueur
	 */
	public boolean live(String nom) {
		return players.get(nom);
	}

	/**
	 * Vérifie si la partie peut commencer (tout les joueurs sont prêts)
	 */
	public boolean canStart() {
		for (Map.Entry i : players.entrySet()) {
			if (!(boolean) i.getValue()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Place un joueur aléatoirement dans le labyrinthe
	 */
	public void placePlayer() {
		boolean flag = false;
		int coordx;
		int coordy;
		while (!flag) {
			coordy = (int) (Math.random() * (lab.getWidth()));
			coordx = (int) (Math.random() * (lab.getHeight()));
			if (lab.getCase(coordx, coordy).getStatut() == 1) {
				int tab[] = { coordx, coordy };
				coordPlayers.add(tab);
				scores.add(0);
				flag = true;
				lab.getCase(coordx, coordy).setStatut(3);
			}
		}
	}

	/**
	 * Bouge le joueur qui a demandé de faire un déplacement
	 * 
	 * @param : str le pseudo du joueur, dist la distance à parcourir, dir la
	 *          direction vers laquelle il veut aller
	 */
	public synchronized int[] movePlayer(String str, int dist, int dir) {
		int id = idToIndice(str);
		int tmp = 0;
		int prevY = coordPlayers.get(id)[1];
		int prevX = coordPlayers.get(id)[0];
		int newX = prevX;
		int newY = prevY;
		int tab[] = { prevX, prevY, -1, 0 };
		boolean flag = false;
		// tab[0] : coord x du joueur, tab[1] : coord y
		// tab[2] : 0 si le joueur bouffe un fantome
		// 1 si le joueur vole les points d'un autre joueur dans le mode stealing
		// 2 muvement du eater ou décès d'un joueur dans le mode eater
		// -1 sinon
		// tab[3] : les points du joueur
		// l'indice du joueur bouffé dans le mode eater ou -1 si le eater se
		// deplace sans bouffer

		if (gameMode == 3) {
			this.lab = new Labyrinthe("VERBEUX.txt");
		}

		eater = idToIndice(nomEater);

		// supression du joueur de sa case prec
		if (gameMode == 2) {
			for (int i = 0; i < fant.length; i++) {
				if (fant[i][0] == prevX && fant[i][1] == prevY) {
					lab.getCase(prevX, prevY).setStatut(2);
					flag = true;
					break;
				}
			}
		}
		if (!flag && (!otherPlayerOnCase(id, prevX, prevY) || gameMode == 2))
			lab.getCase(prevX, prevY).setStatut(1);

		// détermination de la case max où le joueur peut se déplacer
		while (tmp <= dist && newX >= 0 && newY >= 0 && newY < lab.getWidth() && newX < lab.getHeight()
				&& lab.getCase(newX, newY).getStatut() != 0 && scores.get(id) != -1) {
			tmp++;
			switch (dir) {
				case LEFT:
					prevX = newX;
					newX--;
					break;
				case RIGHT:
					prevX = newX;
					newX++;
					break;
				case UP:
					prevY = newY;
					newY--;
					break;
				case DOWN:
					prevY = newY;
					newY++;
					break;
				default:
					break;
			}
		}

		// Dans le mode STEAL(1), le dernier joueur arrivé sur la case
		// vole les points du joueur présent avant
		if (gameMode == 1 && lab.getCase(prevX, prevY).getStatut() == 3) {
			capturedPlayerFants(id, prevX, prevY);
			tab[2] = 1;
			tab[3] = scores.get(id);
		}

		// mis à jour des coordonnées du joueur
		tab[0] = prevX;
		tab[1] = prevY;
		int[] _tab = { prevX, prevY };
		coordPlayers.set(id, _tab);

		// Dans le mode EATER(2), le eater mange le joueur qui est dans la
		// même case que lui et celui-ci est éliminée de la partie
		if (gameMode == 2) {
			if (eater == id) {
				tab[2] = 2;
				tab[3] = -1;
			}
			int xj = coordPlayers.get(eater)[0];
			int yj = coordPlayers.get(eater)[1];
			int x, y;
			for (int i = 0; i < coordPlayers.size(); i++) {
				x = coordPlayers.get(i)[0];
				y = coordPlayers.get(i)[1];
				if (xj == x && yj == y && eater != i && scores.get(i) != -1) {
					scores.set(eater, scores.get(eater) + scores.get(i)); // le score du eater est mis à
																			// jour
					scores.set(i, -1); // Le score du joueur capturé par le eater est mis à -1
					players.replace(getJoueurs()[i], false); // le joueur est éliminé de la partie
					tab[2] = 2;
					tab[3] = i; // le joueur capturé
					break;
				}
			}
		}

		// Chaque fantome capturée par un joueur lui raaporte 100 points
		if (lab.getCase(prevX, prevY).getStatut() == 2 && (gameMode != 2 || id != eater)) {
			scores.set(id, scores.get(id) + 100);
			tab[2] = 0;
			tab[3] = scores.get(id);
			capturedGhosts(id);
		}
		lab.getCase(prevX, prevY).setStatut(3);

		// A chaque deplacement de joueur, un fantome se deplace

		int idfant;
		do {
			idfant = (int) (Math.random() * (fant.length));
		} while (etatFantomes[idfant] != 1 && nbFantomes > 0);
		moveOneGhost(idfant);

		return tab;
	}

	/**
	 * On vérifie si un joueur à été capturé par un autre
	 * 
	 * @param : ind l'id du joueur dans la liste, x la position en abscisse, y la
	 *          position en ordonnées
	 */
	public synchronized void capturedPlayerFants(int ind, int x, int y) {
		for (int i = 0; i < coordPlayers.size(); i++) {
			int _x = coordPlayers.get(i)[0];
			int _y = coordPlayers.get(i)[1];
			if (x == _x && y == _y) {
				scores.set(ind, scores.get(ind) + scores.get(i));
				scores.set(i, 0);
				return;
			}
		}
	}

	/**
	 * On vérifie si un joueur est déjà sur la case sur laquelle un joueur veut
	 * bouger
	 * 
	 * @param : ind l'id du joueur dans la liste, x la position en abscisse, y la
	 *          position en ordonnées
	 */
	public boolean otherPlayerOnCase(int ind, int x, int y) {
		for (int i = 0; i < coordPlayers.size(); i++) {
			int _x = coordPlayers.get(i)[0];
			int _y = coordPlayers.get(i)[1];
			if (x == _x && y == _y && ind != i)
				return true;
		}
		return false;
	}

	/**
	 * On vérifie si un joueur va capturer un fantôme suites à son déplacement
	 * 
	 * @param : j l'id du joueur dans la liste
	 */
	public synchronized void capturedGhosts(int j) {
		for (int i = 0; i < fant.length; i++) {
			int x = coordPlayers.get(j)[0];
			int y = coordPlayers.get(j)[1];
			if (fant[i][0] == x && fant[i][1] == y) {
				etatFantomes[i] = 0;
				fant[i][0] = -1;
				fant[i][1] = -1;
				nbFantomes--;
				break;
			}
		}
	}

	/**
	 * Retourne l'id du joueur étant eater
	 */
	public int geteater() {
		return eater;
	}

	/**
	 * Retourne le pseudo du joueur étant eater
	 */
	public String getNomEater() {
		return nomEater;
	}

	/**
	 * Retourne les points du eater
	 */
	public int getPointsEater() {
		return scores.get(eater);
	}

	/**
	 * Déplacement des fantômes
	 */
	public synchronized void moveGhosts() {
		for (int i = 0; i < fant.length; i++) {
			moveOneGhost(i);
		}
	}

	/**
	 * Déplacement d'un fantôme en particulier
	 * 
	 * @param : i l'id du fantôme dans la liste des fantômes
	 */
	public synchronized void moveOneGhost(int i) {
		boolean flag;
		int coordx;
		int coordy;
		reinitialiseFantome(i);
		if (this.gameMode == 3 && etatFantomes[0] == 1) {
			fant[0][0] = 2;
			fant[0][1] = 2;
			lab.getCase(2, 2).setStatut(2);
		} else if (etatFantomes[i] == 1) {
			flag = false;
			// Choix d'une case aléatoire vide pour accueillir le fantôme
			while (!flag) {
				coordy = (int) (Math.random() * (lab.getWidth()));
				coordx = (int) (Math.random() * (lab.getHeight()));
				if (lab.getCase(coordx, coordy).getStatut() == 1) {
					fant[i][0] = coordx;
					fant[i][1] = coordy;
					flag = true;
					lab.getCase(coordx, coordy).setStatut(2);
				}
			}
		}
		lastMoved = i;
	}

	/**
	 * Réinitialise le labyrinthe après un déplacement de fantôme
	 */
	public synchronized void reinitialiseFantome(int i) {
		if (etatFantomes[i] == 1) {
			toEmptyCase(fant[i][0], fant[i][1]);
		}
	}

	/**
	 * Retourne l'id du dernier fantôme ayant bougé
	 */
	public int getLast() {
		if (etatFantomes[lastMoved] == 1)
			return lastMoved;
		return -1;
	}

	/**
	 * Met une case du labyrinthe à "vide"
	 * 
	 * @param : x et y les coordonnées de la case
	 */
	public void toEmptyCase(int x, int y) {
		if (lab.getCase(x, y).getStatut() != 0)
			lab.getCase(x, y).setStatut(1);
	}

	/**
	 * Fonction qui renvoi true si c'est le eater qui gagne la partie
	 */
	public boolean winOfEater() {
		String[] names = getJoueurs();
		for (int i = 0; i < players.size(); i++) {
			if (i != eater && players.get(names[i]))
				return false;
		}
		return true;
	}

	/**
	 * Revoi true si la partie est finie, false sinon
	 */
	public boolean endGame(String nom) {
		if ((nbFantomes == 0 || players.size() == 0) ||
				(gameMode == 2 && (!live(nom) || winOfEater())))
			return true;
		return false;
	}

	/**
	 * Renvoi l'id du joueur qui a gagné
	 */
	public String winner() {
		if (winOfEater())
			return getJoueurs()[eater];
		else {
			int max = 0;
			for (int i = 0; i < scores.size(); i++) {
				if (scores.get(i) > scores.get(max)) {
					max = i;
				}
			}
			return getJoueurs()[max];
		}
	}
}
