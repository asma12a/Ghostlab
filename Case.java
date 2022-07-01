import java.util.*;

public class Case{
    private static final int MUR = 0;
    private static final int SANS_MUR = 1;
    private static final int FANTOME = 2;
    private static final int JOUEUR = 3;

    private int statut;

    /**
    * Constructeur de la classe Case
    * @param s : statut d'une case
    */
    public Case(int s){
	this.statut=s;
    }

    /**
    * @return statut d'une case 
    */
    public int getStatut(){
	return statut;
    }

    /**
    * Change le statut d'une case 
    * @param stat : le nouveau statut en fonction de s'il y a un fantôme, joueur, mur ou rien
    * @return
    */
    public void setStatut(int stat){
	this.statut = stat;
    }

    /**  Donne le format d'une case ( sous forme d'une représentation textuelle ) */
    public String toString(){
	if(statut==MUR) return "X";
	else if(statut==SANS_MUR) return " ";
	else if(statut==JOUEUR) return "J";
	else return "F";
    }
}
