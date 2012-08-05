package com.viish.apps.tripletriad.robots;

import java.util.ArrayList;

import com.viish.apps.tripletriad.Engine;
import com.viish.apps.tripletriad.cards.Card;

public class BotEasy implements iBot
{
	public int ME;
	
	private Card[] deck, board;
	private String[] elements;
	private boolean elementaire, identique, plus, memeMur;
	
	public BotEasy(int me, Card[] deck, Card[] board, String[] elements, boolean identique, boolean plus, boolean memeMur, boolean combo, boolean elementaire)
	{	
		ME = me;
		this.deck = deck;
		this.board = board;
		this.elements = elements;
		this.identique = identique;
		this.plus = plus;
		this.memeMur = memeMur;
		
		this.elementaire = elementaire;
	}
	
	public int getPlayerValue()
	{
		return ME;
	}
	
	public boolean islastAction()
	{
		int empty = 0;
		for (int c = 0; c < board.length; c++)
		{
			if (board[c] == null)
				empty++;
		}
		return empty == 2;
	}
	
	public boolean isFirstTurn()
	{
		int empty = 0;
		for (int c = 0; c < board.length; c++)
		{
			if (board[c] == null)
				empty++;
		}
		return empty == board.length;
	}
	
	public boolean isNewCardStrongerForLastDefense(Card newCard, Card oldCard, int cell)
	{
		for (int c = 0; c < board.length; c++)
		{
			if (board[c] == null && c != cell)
			{
				if (cell == c - 1 && cell % 3 != 0) // On v�rifie la valeur de droite
				{
					return newCard.getRightValue() > oldCard.getRightValue();
				}
				else if (cell == c + 1 && c % 3 != 0) // On v�rifie la valeur de gauche
				{
					return newCard.getLeftValue() > oldCard.getLeftValue();
				}
				else if (cell == c + 3) // On v�rifie la valeur du haut
				{
					return newCard.getTopValue() > oldCard.getTopValue();
				}
				else if (cell == c - 3) // On v�rifie la valeur du bas
				{
					return newCard.getBottomValue() > oldCard.getBottomValue();
				}
			}
		}
		return false;
	}
	
	// Link� au moteur de jeu, permet de jouer le prochain coup
	public Action nextMove()
	{
		Card carteAJouer = null;
		int caseOuJouer = 0;
		int gain = -1;	
		
		if (isFirstTurn()) // Si c'est la premi�re carte � �tre pos�e sur le plateau
		{
			// On joue la carte de niveau le plus faible dans un angle laissant apparaitre ses 2 valeurs les plus faibles
			Card c = null;
			for (Card card : deck)
			{
				if (!card.isPlayed())
				{
					if (c == null || card.getLevel() < c.getLevel() || (card.getLevel() == c.getLevel() && card.getTotal() < c.getTotal()))
						c = card;
				}
			}
			
			boolean leftStronger = true, topStronger = true, betterToPlayMid = false;
			if (c.getLeftValue() < c.getRightValue())
				leftStronger = false;
			else if (c.getLeftValue() == c.getRightValue() && c.getLeftValue() >= 6)
				betterToPlayMid = true;
			
			if (c.getTopValue() < c.getBottomValue())
				topStronger = false;
			else if (c.getTopValue() == c.getBottomValue() && c.getTopValue() >= 6)
				betterToPlayMid = true;
			
			int cell = -1;
			if (betterToPlayMid)
				cell = 4;
			else if (topStronger && leftStronger)
				cell = 0;
			else if (topStronger)
				cell = 2;
			else if (leftStronger)
				cell = 6;
			else
				cell = 8;
			
			carteAJouer = c;
			caseOuJouer = cell;
			gain = 0;
		}
		else
		{	
			for (int c = 0; c < board.length; c++)
			{
				if (board[c] == null) // Pour chaque case libre du plateau
				{
					for (Card card : deck)
					{
						if (!card.isPlayed()) // Pour chaque carte pouvant �tre jou�e
						{
							int g = gain(board, c, card.clone());
							// On joue la carte de niveau le plus faible fournissant le gain maximal sauf si c'est le dernier tour o� alors on joue la carte de niveau max ayant le gain maximal;
							if (g > gain || (g == gain && ((!islastAction() && card.getLevel() < carteAJouer.getLevel()) || (islastAction() && isNewCardStrongerForLastDefense(card, carteAJouer, c)))))
							{
								carteAJouer = card;
								caseOuJouer = c;
								gain = g;
							}
						}
					}
				}
			}
		}
		// On joue la carte qui maximise le gain sur ce tour, en prenant celle de plus faible niveau en cas d'egalite
		return new Action(carteAJouer, caseOuJouer, gain);
	}
	
	private void appliquerRegleElementaire(Card c, int cell)
	{
		if (elements[cell] == null || elements[cell] == "")
			return;
		
		if (elements[cell] == c.getElement())
			c.bonusElementaire();
		else
			c.malusElementaire();
	}
	
	// Renvoie le nombre de cartes retournees en posant la carte What en Where
	private int gain(Card[] plateau, int where, Card what)
	{
		if (elementaire)
			appliquerRegleElementaire(what, where);
		
		int gain = 0;
		
		if (where % 3 == 0) // Carte colonne gauche
		{
			Card c = plateau[where + 1];
			if (c != null) // Si il y a une carte a sa droite
			{
				if (c.getCardView().getColor() != ME && what.getRightValue() > c.getLeftValue()) // Celle jou�e est plus forte
				{
					gain += 1;
				}
			}
		}
		else if (where % 3 == 1) // Colonne du milieu
		{
			Card c = plateau[where + 1];
			if (c != null) // Si il y a une carte a sa droite
			{
				if (c.getCardView().getColor() != ME && what.getRightValue() > c.getLeftValue()) // Celle jou�e est plus forte
				{
					gain += 1; // On retourne l'autre
				}
			}
			
			c = plateau[where - 1];
			if (c != null) // Si il y a une carte a sa gauche
			{
				if (c.getCardView().getColor() != ME && what.getLeftValue() > c.getRightValue()) // Celle jou�e est plus forte
				{
					gain += 1; // On retourne l'autre
				}
			}
		}
		else // Colonne de droite
		{
			Card c = plateau[where - 1];
			if (c != null) // Si il y a une carte a sa gauche
			{
				if (c.getCardView().getColor() != ME && what.getLeftValue() > c.getRightValue()) // Celle jou�e est plus forte
				{
					gain += 1; // On retourne l'autre
				}
			}
		}
		
		if (where / 3 == 0) // Ligne du haut
		{
			Card c = plateau[where + 3];
			if (c != null) // Si il y a une carte en dessous
			{
				if (c.getCardView().getColor() != ME && what.getBottomValue() > c.getTopValue()) // Celle jou�e est plus forte
				{
					gain += 1; // On retourne l'autre
				}
			}
		}
		else if (where / 3 == 1) // Ligne du milieu
		{
			Card c = plateau[where + 3];
			if (c != null) // Si il y a une carte en dessous
			{
				if (c.getCardView().getColor() != ME && what.getBottomValue() > c.getTopValue()) // Celle jou�e est plus forte
				{
					gain += 1; // On retourne l'autre
				}
			}
			
			c = plateau[where - 3];
			if (c != null) // Si il y a une carte en dessus
			{
				if (c.getCardView().getColor() != ME && what.getTopValue() > c.getBottomValue()) // Celle jou�e est plus forte
				{
					gain += 1; // On retourne l'autre
				}
			}
		}
		else // Ligne du bas
		{
			Card c = plateau[where - 3];
			if (c != null) // Si il y a une carte en dessus
			{
				if (c.getCardView().getColor() != ME && what.getTopValue() > c.getBottomValue()) // Celle jou�e est plus forte
				{
					gain += 1; // On retourne l'autre
				}
			}
		}

		if (identique)
		{
			int gainSame = gainSame(Engine.OPPONENT, plateau, what, where);
			if (gainSame > 0)
				gain += gainSame;
		}
		if (plus)
		{
			int gainPlus = gainPlus(Engine.OPPONENT, plateau, what, where);
			if (gainPlus > 0)
				gain += gainPlus;
		}
		
		return gain;
	}
	
	private int gainSame(int player, Card[] board, Card what, int cell)
	{
		int gain = 0;
		ArrayList<Integer> cards = new ArrayList<Integer>(); // Cartes subissant/permettant Identique
		int carteAdverse = 0;
		
		if (cell - 3 >= 0 && board[cell - 3] != null) // carte du dessus si existante
		{
			if (board[cell - 3].getBottomValue() == what.getTopValue())
			{
				cards.add(cell - 3);
				if (board[cell - 3].getCardView().getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell - 3 < 0 && memeMur) // Regle MemeMur
		{
			if (10 == what.getTopValue())
			{
				cards.add(-1);
			}
		}
		if (cell + 3 < board.length && board[cell + 3] != null) // carte du dessous si existante
		{
			if (board[cell + 3].getTopValue() == what.getBottomValue())
			{
				cards.add(cell + 3);
				if (board[cell + 3].getCardView().getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell + 3 >= board.length && memeMur) // Regle MemeMur
		{
			if (10 == what.getBottomValue())
			{
				cards.add(-1);
			}
		}
		if (cell % 3 <= 1 && board[cell + 1] != null) // colonne gauche ou milieu
		{
			if (board[cell + 1].getLeftValue() == what.getRightValue())
			{
				cards.add(cell + 1);
				if (board[cell + 1].getCardView().getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell % 3 == 2 && memeMur) // Regle MemeMur
		{
			if (10 == what.getRightValue())
			{
				cards.add(-1);
			}
		}
		if (cell % 3 >= 1 && board[cell - 1] != null) // colonne droite ou milieu
		{
			if (board[cell - 1].getRightValue() == what.getLeftValue())
			{
				cards.add(cell - 1);
				if (board[cell - 1].getCardView().getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell % 3 == 0 && memeMur) // Regle MemeMur
		{
			if (10 == what.getLeftValue())
			{
				cards.add(-1);
			}
		}
		
		if (cards.size() >= 2 && carteAdverse >= 1)
		{
			ArrayList<Integer> swapped = new ArrayList<Integer>(); // Cartes swapped
			for (int c : cards) // Pour chaque carte, on les retourne si besoin est
			{
				if (c != -1)
				{
					Card card = board[c];
					if (card.getCardView().getColor() != player)
						swapped.add(c);
				}
			}
			gain = carteAdverse;
		}
		
		return gain;
	}
	
	// Applique la regle Plus sur le plateau
	private int gainPlus(int player, Card[] board, Card what, int cell)
	{
		Card[] cards = new Card[4];
		int[] numeros = new int[4];
		ArrayList<Integer> gain = new ArrayList<Integer>();
		
		numeros[0] = cell - 3;
		numeros[1] = cell - 1;
		numeros[2] = cell + 3;
		numeros[3] = cell + 1;
		if (cell - 3 >= 0) cards[0] = board[cell - 3];
		if (cell + 3 < board.length) cards[2] = board[cell + 3];
		if (cell % 3 <= 1) cards[3] = board[cell + 1];
		if (cell % 3 >= 1) cards[1] = board[cell - 1];
		
		for (int i = 0; i < 3; i ++)
		{ 
			boolean condition = false;
			if (cards[i] != null)
			{
				int somme = 0;
				if (i == 0) somme = what.getTopValue() + cards[i].getBottomValue();
				else if (i == 1) somme = what.getLeftValue() + cards[i].getRightValue();
				else if (i == 2) somme = what.getBottomValue() + cards[i].getTopValue();
				
				if (player != cards[i].getCardView().getColor()) condition = true;
				
				for (int j = i+1; j < 4; j++)
				{
					if (cards[j] != null)
					{
						int somme2 = 0;
						if (j == 3) somme2 = what.getRightValue() + cards[j].getLeftValue();
						else if (j == 1) somme2 = what.getLeftValue() + cards[j].getRightValue();
						else if (j == 2) somme2 = what.getBottomValue() + cards[j].getTopValue();
						
						if (player != cards[j].getCardView().getColor()) condition = true;
						
						if (somme == somme2 && condition) // Toutes les conditions remplies
						{
							if (cards[i].getCardView().getColor() != player)
							{
								gain.add(i);
							}
							if (cards[j].getCardView().getColor() != player) 
							{
								gain.add(j);
							}
						}
					}
				}
			}
		}
		
		return gain.size();
	}
}
