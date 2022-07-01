#ifndef _JOUEUR_H
#define _JOUEUR_H

typedef struct joueur_s{
  char id[9];
  int port;
} joueur;

joueur* creer_joueur(char i[9], int p);
int verify_id(char id[9]);
char* get_id(joueur j);
void* receive_private(void* j);
void* read_multidif();
void* jouer(void* j);
  
#endif
