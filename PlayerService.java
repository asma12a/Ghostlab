import java.net.*;
import java.io.*;
import java.util.*;
import java.math.*;

/**
 * Ceci est le thread qui va s'occuper de chaque client qui va se connecter
 */

public class PlayerService implements Runnable {

	private Server server;
	private Socket socket;
	private boolean creator;

	public PlayerService(Server serv, Socket s) {
		this.server = serv;
		this.socket = s;
		this.creator = false;
	}

	/**
	 * On tire un labyrinthe aléatoirement parmi ceux prédéfinis
	 */
	public String nomLab() {
		int id = (int) (Math.random() * (5));
		switch (id) {
			case 0:
				return "RAMPAGE.txt";
			case 1:
				return "PACMAN.txt";
			case 2:
				return "SMALLBLOCK.txt";
			case 3:
				return "RUST.txt";
			default:
				return "PACMAN.txt";
		}
	}

	/**
	 * Sert à envoyer un message multidiffusé
	 * 
	 * @param mess : le contenu du message
	 *             dso : la socket sur laquelle envoyer les messages
	 *             p : le port d'Envoi
	 */
	public void sendMultDiff(String mess, DatagramSocket dso, int p)
			throws Exception {
		byte[] data = mess.getBytes();
		InetSocketAddress ia = new InetSocketAddress(server.getGame(p).getAddressMultDif(),
				server.getGame(p).getPortMultDif());
		DatagramPacket paquet = new DatagramPacket(data, data.length, ia);
		dso.send(paquet);
	}

	/**
	 * Sert à fermer une connexion, donc on ferme un par un tout ce qui compose une
	 * connexion
	 * 
	 * @param : p : le port (pour enlever le joueur du serveur)
	 *          nom : pour trouver le joueur dont il faut fermer la connexion
	 *          pw : le printWriter sur lequel le joueur écrit
	 *          br : le bufferedReader sur lequel il lit
	 *          dso : sa socket d'écoute
	 */
	public void closeConnexion(int p, String nom, PrintWriter pw, BufferedReader br, DatagramSocket dso)
			throws Exception {
		server.getGame(p).delete(nom);
		server.remPlayer(nom);
		pw.close();
		br.close();
		dso.close();
		socket.close();
	}

	public String toStringTroisO(int i) {
		/**
		 * Codage des entiers sur 3 octets avec completion avec des 0
		 * 
		 * @arg i : l'entier à coder
		 * @return l'entier codé sur 3 octets
		 */
		String res = "" + i;
		if (res.length() == 1)
			res = "00" + res;
		else if (res.length() == 2)
			res = "0" + res;
		return res;
	}

	public String toStringQuantreO(int i) {
		/**
		 * Codage des entiers sur 4 octets avec completion avec des 0
		 * 
		 * @arg i : l'entier à coder
		 * @return l'entier codé sur 3 octets
		 */
		String res = "" + i;
		if (res.length() == 1)
			res = "000" + res;
		else if (res.length() == 2)
			res = "00" + res;
		else if (res.length() == 3)
			res = "0" + res;
		return res;
	}

	/**
	 * Cette fonction gère un joueur connecté :
	 * - Les commandes avant de s'enregistrer (NEW, REG, LIST ...)
	 * - Les commandes dans l'acceuil d'avant partie (LIST , ... +
	 * EATER,CLASSIC,STEALING pour les modes de jeu si le joueur a créé le jeu)
	 * - Les commandes pendant la partie (UP, DOWN, GLIST, ...)
	 * - Sa déconnexion
	 */
	public void run() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			DatagramSocket dso = new DatagramSocket();
			String addr = socket.getRemoteSocketAddress().toString().substring(1).split(":")[0];
			String[] repJoueur;
			boolean start = false, reg = false, left = false; // jeu commencé, joueur enregistré, est quitté de la
																// partie
			int votrePartie = 0;
			String nom = "";
			try {
				// Envoi des jeux créés non commencés
				pw.print("GAMES " + server.getNbPartiesNC() + "***");
				pw.flush();
				for (int i = 0; i < server.getNbParties(); i++) {
					if (server.getGame(i).isBegun() == false) {
						pw.print("OGAME " + i + " " + server.getGame(i).getJoueurs().length + "***");
						pw.flush();
						Thread.sleep(1);// On laisse le temps au client de recevoir les messages
						// un par un
					}
				}

				// "Pré-partie" :
				// NEWPL REGIS SIZE? LIST? UNREG GAME?
				// creer une partie , s'inscrire, voir la taille du labyrinthe,
				// la liste des joueurs de la partie, se désinscrire, changer de mode
				while (start == false) {
					repJoueur = br.readLine().split(" ");
					if (repJoueur.length > 2 && (repJoueur[0].equals("NEWPL") || repJoueur[0].equals("REGIS"))) {
						nom = repJoueur[1];
						// Creation d'une nouvelle partie
						if (reg == false && repJoueur[0].equals("NEWPL") && repJoueur.length == 3) {
							try {
								if (server.newPlayer(nom, Integer.parseInt(repJoueur[2].split("\\*")[0]), addr)) {
									Game g = new Game(new Labyrinthe(nomLab()));
									g.ajouterPlayer(nom);
									server.addGame(g);
									votrePartie = server.getNbParties() - 1;
									pw.print("REGOK " + votrePartie + "***");
									pw.flush();
									reg = true;
									creator = true;
								} else {
									pw.print("REGNO***");
									pw.flush();
								}
							} catch (Exception e) {
								pw.print("REGNO***");
								pw.flush();
							}

							// Enregistrement à une partie existante
						} else if (reg == false && repJoueur[0].equals("REGIS") && repJoueur.length == 4) {
							try {
								int p = Integer.parseInt(repJoueur[2]);
								votrePartie = Integer.parseInt(repJoueur[3].split("\\*")[0]);
								if (server.getGame(votrePartie).isBegun() == false && server.newPlayer(nom, p, addr)) {
									server.getGame(votrePartie).ajouterPlayer(nom);
									pw.print("REGOK " + votrePartie + "***");
									pw.flush();
									reg = true;
								} else {
									pw.print("REGNO***");
									pw.flush();
								}
							} catch (Exception e) {
								pw.print("REGNO***");
								pw.flush();
							}
						} else {
							pw.print("REGNO***");
							pw.flush();
						}

					} else {
						switch (repJoueur[0]) {

							// EXTENSION : Chgmt de mode (mode stealing : les joueurs peuvent se voler les
							// fantômes entre eux

							case "STEAL***":
								if (reg && creator) {
									server.getGame(votrePartie).setMode(1);
									pw.print("MODE! STEAL***");
									pw.flush();
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							// EXTENSION : Changement de mode (mode eater : ce joueur peut manger tous les fantômes
							// d'un autre
							case "EATER***":
								if (reg && creator) {
									server.getGame(votrePartie).setMode(2);
									pw.print("MODE! EATER***");
									pw.flush();
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							// EXTENSION : Changement de mode (classic : le jeu de base)
							// seul le joueur qui crée la partie peut changer le mode
							case "CLASS***":
								if (reg && creator) {
									server.getGame(votrePartie).setMode(0);
									pw.print("MODE! CLASSIC***");
									pw.flush();
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							case "VERBE***":
								if (reg && creator) {
									server.getGame(votrePartie).setMode(3);
									server.getGame(votrePartie).setLabVerbeux();
									pw.print("MODE! VERBEUX***");
									pw.flush();
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							// Désinscription du joueur
							case "UNREG***":
								if (reg) {
									if (server.getGame(votrePartie).delete(nom)) {
										server.remPlayer(nom);
										reg = false;
										creator = false;
										pw.print("UNREGOK " + votrePartie + "***");
										pw.flush();
									} else {
										pw.print("DUNNO***");
										pw.flush();
									}
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							// Taille du labyrinthe de la partie
							case "SIZE?":
								if (repJoueur.length == 2) {
									try {
										int i = Integer.parseInt(repJoueur[1].split("\\*")[0]);
										pw.print("SIZE! " + i + " " + server.getGame(i).getLabTaille()[0] + " "
												+ server.getGame(i).getLabTaille()[1] + "***");
										pw.flush();
									} catch (Exception e) {
										// si i<0 ou i>nbGame, une exeption sera catchée
										pw.print("DUNNO***");
										pw.flush();
									}
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							// Liste des joueurs d'une partie
							case "LIST?":
								if (repJoueur.length == 2) {
									try {
										int i = Integer.parseInt(repJoueur[1].split("\\*")[0]);
										if (i >= 0 && i <= server.getNbParties()) {
											String[] j = server.getGame(i).getJoueurs();
											pw.print("LIST! " + i + " " + j.length + "***");
											pw.flush();
											for (String id : j) {
												pw.print("PLAYR " + id + "***");
												pw.flush();
												Thread.sleep(1);
											}
										} else {
											pw.print("DUNNO***");
											pw.flush();
										}
									} catch (Exception e) {
										pw.print("DUNNO***");
										pw.flush();
									}
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							// EXTENSION : Mode de la partie
							case "MODE?":
								if (repJoueur.length == 2) {
									try {
										int i = Integer.parseInt(repJoueur[1].split("\\*")[0]);
										pw.print("MODE " + server.getGame(i).gameModeToString() + "***");
										pw.flush();
									} catch (Exception e) {
										pw.print("DUNNO***");
										pw.flush();
									}
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							// La liste des parties non commencées
							case "GAME?***":
								pw.print("GAMES " + server.getNbPartiesNC() + "***");
								pw.flush();
								for (int y = 0; y < server.getNbParties(); y++) {
									if (!server.getGame(y).isBegun()) {
										pw.print("OGAME " + y + " " + server.getGame(y).getJoueurs().length + "***");
										pw.flush();
										Thread.sleep(1);
									}
								}
								break;

							// Le joueur est prêt pour le debut de la partie
							case "START***":
								if (reg) { // Le joueur ne peut envoyer START que s'il est déja enregistré à une partie
									// EXTENSION : Dans le cas du mode eater, la partie necessite au min 2
									// joueurs
									if (server.getGame(votrePartie).getMode() == 2 &&
											server.getGame(votrePartie).getNbJoueurs() < 2) {
										pw.print("2 PLAYERS MIN REQUIRED FOR THIS MODE. WAIT REG***");
										pw.flush();
									} else {
										server.getGame(votrePartie).isReady(nom);
										pw.flush();
										start = true; // Permet de sortir de la boucle de "pré-partie"
									}
									server.getGame(votrePartie).isReady(nom);
									pw.flush();
									start = true; // sort de la boucle pré-partie
								} else {
									pw.print("DUNNO***");
									pw.flush();
								}
								break;

							default:
								pw.print("DUNNO***");
								pw.flush();
						}
					}
				}

				// Le joueur attend que tous les autres soient prêts
				synchronized (server) {
					while (!server.getGame(votrePartie).canStart()) {
						server.wait();
					}
					server.notifyAll();
				}

				// Debut de la partie
				server.getGame(votrePartie).setBegun();
				// Envoie des informations sur la partie
				pw.println("WELCO " + votrePartie + " " +
						server.getGame(votrePartie).getLabTaille()[0] + " " + // hauteur du labyrinthe
						server.getGame(votrePartie).getLabTaille()[1] + " " + // largeur du labyrinthe
						server.getGame(votrePartie).getNbFantomes() + " " +
						server.getGame(votrePartie).getAddressMultDif() + " " +
						server.getGame(votrePartie).getPortMultDif() + "***");

				// Envoie de la position du joueur
				pw.print("POSIT " + nom + " " +
						toStringTroisO(server.getGame(votrePartie).getPos(nom)[1]) + " " +
						toStringTroisO(server.getGame(votrePartie).getPos(nom)[0]) + "***");
				pw.flush();

				// Envoie du mode en multi-diffusion
				if (!server.getGame(votrePartie).getDiff()) {
					server.getGame(votrePartie).setDiff();
					String mess = "MODE! ";
					if (server.getGame(votrePartie).getMode() == 0) {
						mess += "CLASSIC+++";
					} else if (server.getGame(votrePartie).getMode() == 1) {
						mess += "STEAL\n" +
								"#DESC :\n" +
								"#THE LAST PLAYER WHO MOVED ON THE CASE STEALS THE TOTAL POINTS OF THE OTHER WHO WERE ON THE CASE"
								+
								"DONT GET CAUGHT !+++\n";
					} else if (server.getGame(votrePartie).getMode() == 2) {
						mess += "EATER\n##PLAYER_EATER : " + server.getGame(votrePartie).getNomEater() +
								"\n#DESC :\n" +
								" #YOU GET EATEN BY THE EATER : YOU LOSE\n" +
								" #ALL PLAYERS ARE EATEN : WIN OF EATER\n" +
								" #NO MORE GHOSTS IN THE MAZE : WIN OF PLAYER WITH MAX SCORE+++";
					} else if (server.getGame(votrePartie).getMode() == 3) {
						mess += "VERBEUX\n#IN THIS GAMEMOD, YOU CAN ONLY TRY HOW A SIMPLE GAME WORKS+++";
					}
					sendMultDiff(mess, dso, votrePartie);
				}

				repJoueur = br.readLine().split(" ", 3);
				// Boucle principale : déroulement du jeu
				while (!left && !server.getGame(votrePartie).endGame(nom)) {

					// Déplacement du joueur
					if (repJoueur[0].equals("LEMOV") || repJoueur[0].equals("UPMOV")
							|| repJoueur[0].equals("RIMOV") || repJoueur[0].equals("DOMOV")) {
						int dir = 0;

						// Enregistrement de la direction du déplacement
						switch (repJoueur[0]) {
							case "LEMOV":
								dir = 0;
								break;
							case "UPMOV":
								dir = 1;
								break;
							case "RIMOV":
								dir = 2;
								break;
							case "DOMOV":
								dir = 3;
								break;
							default:
								pw.print("NOT MOVED***");
								pw.flush();
								break;
						}
						// Exécution du déplacement
						if (repJoueur.length == 2) {
							try {
								int[] coord = server.getGame(votrePartie).movePlayer(nom,
										Integer.parseInt(repJoueur[1].split("\\*")[0]), dir);

								// Le joueur a capturé des points dans le mode normal ou le mode stealing
								if (coord[2] == 0 || coord[2] == 1) {
									String mess;
									if (coord[2] == 0) {// Le joueur a capturé un fantome
										pw.print("MOVEF " + toStringTroisO(coord[1]) + " " + toStringTroisO(coord[0])
												+ " " +
												toStringQuantreO(coord[3]) + "***");
										pw.flush();
										mess = "SCORE " + nom + " " + toStringQuantreO(coord[3]) + " "
												+ toStringTroisO(coord[1]) + " " +
												toStringTroisO(coord[0]) + "+++";
									} else {// Le joueur a capturé les points d'un autre
										pw.print("MOJ " + toStringTroisO(coord[1]) + " " + toStringTroisO(coord[0])
												+ " " +
												toStringQuantreO(coord[3]) + "***");
										pw.flush();
										mess = "PLAYER " + nom + " ATE PLAYER AT POS " + toStringTroisO(coord[1]) + " "
												+ toStringTroisO(coord[0]) +
												"\n" + nom + " SCOR " + toStringQuantreO(coord[3]) + "+++";
									}
									Thread.sleep(1);
									sendMultDiff(mess, dso, votrePartie);
								}

								// Mode eater
								else if (coord[2] == 2) {
									if (coord[3] != -1) {
										String name = server.getGame(votrePartie).getJoueurs()[coord[3]];
										if (nom.equals(server.getGame(votrePartie).getNomEater())) {
											pw.print("PLAYERS ALL DEAD***");
											pw.flush();
										} else {
											pw.print("RIP***");
											pw.flush();
										}
										String mess = name + " EATEN+++";
										sendMultDiff(mess, dso, votrePartie);
									} else {
										pw.print("MOVE! " + toStringTroisO(coord[1]) + " " + toStringTroisO(coord[0])
												+ "***");
										pw.flush();
									}
									String mess = "EATER " + toStringTroisO(coord[1]) + " "
											+ toStringTroisO(coord[0]) + "+++";
									byte[] data = mess.getBytes();
									DatagramPacket paquet = new DatagramPacket(data, data.length);
									paquet = new DatagramPacket(data, data.length,
											server.getGame(votrePartie).getInet());
									dso.send(paquet);
								}

								// Le joueur n'a capturé aucun point
								else {
									pw.print(
											"MOVE! " + toStringTroisO(coord[1]) + " " + toStringTroisO(coord[0])
													+ "***");
									pw.flush();
								}

								// Multidiffusion du déplacement d'un fantôme
								int fantom;
								if ((fantom = server.getGame(votrePartie).getLast()) != -1) {
									int coord2[] = server.getGame(votrePartie).getPos(fantom);
									String messMD = "GHOST " + toStringTroisO(coord2[1]) + " "
											+ toStringTroisO(coord2[0]) + "+++";
									sendMultDiff(messMD, dso, votrePartie);
								}

							} catch (NumberFormatException e) {
								pw.print("NOT MOVED***");
								pw.flush();
							}

						} else {
							pw.print("NOT MOVED***");
							pw.flush();
						}

					
					}

					else {
						switch (repJoueur[0]) {

							// la liste des joueurs de la partie en cours
							case "GLIS?***":
								String[] j = server.getGame(votrePartie).getJoueurs();
								pw.print("GLIS! " + j.length + "***\n");
								for (String id : j) {
									int[] posJ = server.getGame(votrePartie).getPos(id);
									pw.print("GPLYR " + id + " " + toStringTroisO(posJ[0]) + " "
											+ toStringTroisO(posJ[1]) + " " + server.getGame(votrePartie).getPoints(id)
											+ "***\n");
									// pw.flush();
									Thread.sleep(1);
								}
								pw.flush();
								break;

							// EXTENSION : le mode de la partie en cours
							case "MODE?***":
								pw.print("MODE! " + server.getGame(votrePartie).gameModeToString() + "***");
								pw.flush();
								break;

							// EXTENSION : le score du joueur dans la partie
							case "SCOR?***":
								pw.print("SCOR! " + nom + " "
										+ toStringQuantreO(server.getGame(votrePartie).getPoints(nom)) + "***");
								pw.flush();
								break;

							// Message multidiffusé dans la partie
							case "MALL?":
								String mess = "";
								if (repJoueur.length >= 3) {
									mess = "MESSA " + nom + " " + repJoueur[1] + " " + repJoueur[2].split("\\*")[0]
											+ "+++";
									sendMultDiff(mess, dso, votrePartie);
									pw.print("MALL!");
									pw.flush();
								} else {
									pw.print("DUNNO mall***");
									pw.flush();
								}
								break;

							// envoi d'un message personnel :
							// Un joueur peut envoyer un message personnel à tout joueur s'étant enregistré
							// sur le serveur,
							// qu'il fasse partie de la même partie que lui ou non, une fois sa partie
							// lancée
							case "SEND?":
								if (repJoueur.length == 3) {
									try {
										mess = "MESSP " + nom + " " + repJoueur[2].split("\\*")[0] + "+++";
										byte[] data = mess.getBytes();
										Integer p;
										if ((p = server.getPortPlayer(repJoueur[1])) != null) {
											DatagramPacket paquet = new DatagramPacket(data, data.length,
													InetAddress.getByName(server.getAddrPlayer(repJoueur[1])), p);
											dso.send(paquet);
											pw.print("SEND!***");
											pw.flush();
										}
									} catch (Exception e) {
										pw.print("NSEND***");
										pw.flush();
									}
								} else {
									pw.print("NSEND***");
									pw.flush();
								}
								break;

							// Sortie d'une partie en cours...
							case "IQUIT***":
								pw.print("GOBYE***");
								pw.flush();
								left = true;
								// suppression de l'image du joueur dans le labyrinthe
								server.getGame(votrePartie).toEmptyCase(server.getGame(votrePartie).getPos(nom)[0],
										server.getGame(votrePartie).getPos(nom)[1]);
								System.out.println("PLAYER " + nom + " HAS LEFT");
								closeConnexion(votrePartie, nom, pw, br, dso);
								break;
							default:
								pw.print("DUNNO***");
								pw.flush();
								break;
						}
					}
					if (!left)
						repJoueur = br.readLine().split(" ", 3);
				}

				// Fin de la partie

				// le joueur n'a pas quitté la partie
				if (!left) {
					if (server.getGame(votrePartie).live(nom)) {
						// Envoie du nom du vainquer en multidiffusion
						if (server.getGame(votrePartie).getNbJoueurs() > 0 && !server.getGame(votrePartie).getEnd()) {
							server.getGame(votrePartie).setEnd();
							String vainqueur = server.getGame(votrePartie).winner();
							String mess = "END " + vainqueur + " "
									+ toStringQuantreO(server.getGame(votrePartie).getPoints(vainqueur)) + "+++";
							sendMultDiff(mess, dso, votrePartie);
						}
						pw.print("GOBYE***");
						pw.flush();
						closeConnexion(votrePartie, nom, pw, br, dso);
					} else {
						pw.print("GOBYE***");
						pw.flush();
						closeConnexion(votrePartie, nom, pw, br, dso);
					}
				}
			} catch (Exception e) {
				// traitement d'une coupure de connexion inatendue
				System.out.println("PLAYER " + nom + " HAS LEFT");
				if (server.getNbParties() > 0) {
					server.getGame(votrePartie).delete(nom);
					server.getPlayers().remove(nom);
				}
				pw.close();
				br.close();
				dso.close();
				socket.close();
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
}
