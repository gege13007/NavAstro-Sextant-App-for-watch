package com.samblancat.vastro;

//Variables Globales
public class glob {

    //les caractères du nombre en cours d'édition
    static public int[] car;
    //signe du nombre en édition (1 ou -1)
    static public int sign=1;

    //Position courante / hauteur astre / h mètre
    static public double latit, longit, hmes, hmetre, collim;

    //Paramètres envoi/retour valeur en input
    static public String legende;
    static public String unit;    // préfixe de l'unité après les chiffres
    static public int nval;    // index de la valeur (pour tri facile retour)
    //valeur float en retour
    static public double inp;

}