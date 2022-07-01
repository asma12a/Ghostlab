# PR6 2022

## Sommaire

1. [Projet](README.md#introduction-et-informations)
2. [Compilation et Exection du projet](README.md#compilation-et-Execution-du-projet)
3. [Extensions](README.md#extensions)

## Introduction et informations

**Informations générales**

- Numero du groupe : **36**

- Sujet du projet :
  C'est un programme permettant à des joueurs de se connecter à un serveur, créer des parties de "labyrinthe" et s'affronter.
  Des joueurs sont placés aléatoirement dans un labyrinthe hanté par des fantômes, leur but est de les capturer.
  Le joueurs ayant capturé le plus de fantômes gagne la partie (MODE CLASSIC).

- Le code du Serveur est écrit en **JAVA** et celui des Joueurs en **C\***.

**Membres du groupe**

1. Basile, HURET, 21952351

2. Marilyn, TRAN, 21954074

3. Asma, MOKEDDES, 21967552

## Compilation et Execution du projet

**Compilation**

Pour compiler le projet, il suffit d'entrer la commande suivante :

```
make
```

Pour supprimer les fichiers .o et les exécutables :

```
make clean
```

**Execution**

Pour exécuter le serveur :

```
java Server

```

Pour exécuter les clients :

```
./joueur <votre pseudo> <votre port UDP> <l'ip sur laquelle tourne le serveur>

```

## Extensions :

**STEALING**

Les joueurs doivent toujours attraper les fantômes mais en plus ils peuvent voler les fantômes des autres joueurs !

**JUGGERNAUT**

    Meme principe que l'autre  sauf qu'en plus en début de partie un joueur est désigné "juggernaut" et doit manger les
    autres joueurs pour gagner !
