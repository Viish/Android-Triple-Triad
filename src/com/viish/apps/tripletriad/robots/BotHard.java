package com.viish.apps.tripletriad.robots;

import java.util.ArrayList;

import com.viish.apps.tripletriad.Engine;
import com.viish.apps.tripletriad.cards.Card;

public class BotHard implements iBot
{
	public static final int ME = 1;
	public static final int HIM = 2;
	public static final int MAX_VALUE = 10;
	public static final int MIN_VALUE = -10;
	
	private Engine moteur;
	private Card[] myDeck, hisDeck, board;
	private String[] boardElements;
	private boolean regleElementaire, regleIdentique, reglePlus, regleMemeMur, regleCombo;
	private boolean botPlayingFirst;
	
	public BotHard(Engine moteur, Card[] myDeck, Card[] hisDeck, Card[] board, String[] elements, boolean identique, boolean plus, boolean memeMur, boolean combo, boolean elementaire)
	{
		this.moteur = moteur;
		this.botPlayingFirst = moteur.isBotPlayingFirst();
		
		this.myDeck = myDeck;
		this.hisDeck = hisDeck;
		this.board = board;
		this.boardElements = elements;
		
		this.regleElementaire = elementaire;
		this.regleMemeMur = memeMur;
		this.reglePlus = plus;
		this.regleIdentique = identique;
		this.regleCombo = combo;
	}
	
	// Link� au moteur de jeu, permet de jouer le prochain coup
	public Action nextMove()
	{
		int nbCoupsAJouerRestants = moteur.getNbToursRestants();
		return max(board.clone(), nbCoupsAJouerRestants);
	}
	
	public Action min(Card[] board, int nbCoupsRestants) // Renvoie la meilleure action a jouer pour le joueur humain
	{
		nbCoupsRestants -= 1;
		
		ArrayList<Card[]> possibilites = getPossiblesBoards(board, HIM);
		Action[] actions = new Action[possibilites.size()];
		for (int p = 0; p < possibilites.size() - 1; p++)
		{
			Card[] poss = possibilites.get(p);
			
			if (nbCoupsRestants == 1)
				return new Action(getActionCardName(board, poss), getActionPosition(board, poss), evaluationSituation(poss));
			else
				actions[p] = max(poss, nbCoupsRestants);
		}
		
		int minimum = MAX_VALUE;
		int imin = -1;
		for (int p = 0; p < actions.length; p++)
		{
			if (actions[p].getValue() < minimum)
			{
				minimum = actions[p].getValue();
				imin = p;
			}
		}
		
		return actions[imin];
	}
	
	public Action max(Card[] board, int nbCoupsRestants) // Renvoie la meilleur action � jouer pour l'ordinateur
	{
		nbCoupsRestants -= 1;
		
		ArrayList<Card[]> possibilites = getPossiblesBoards(board, ME);
		Action[] actions = new Action[possibilites.size()];
		for (int p = 0; p < possibilites.size() - 1; p++)
		{
			Card[] poss = possibilites.get(p);
			
			if (nbCoupsRestants == 1)
				return new Action(getActionCardName(board, poss), getActionPosition(board, poss), evaluationSituation(poss));
			else
				actions[p] = min(poss, nbCoupsRestants);
		}
		
		int maximum = MIN_VALUE;
		int imax = -1;
		for (int p = 0; p < actions.length; p++)
		{
			if (actions[p].getValue() > maximum)
			{
				maximum = actions[p].getValue();
				imax = p;
			}
		}
		return actions[imax];
	}
	
	private ArrayList<Card[]> getPossiblesBoards(Card[] board, int aQuiDeJouer)
	{
		ArrayList<Card[]> possibilites = new ArrayList<Card[]>();
		Card[] deck;
		if (aQuiDeJouer == ME)
			deck = myDeck.clone();
		else 
			deck = hisDeck.clone();
		
		//Pour chaque carte du deck du joueur
		for (Card card : deck)
		{
			if (!card.isPlayed()) //qui n'est pas sur le plateau
			{
				//Pour chaque case du plateau
				for (int iP = 0; iP < board.length; iP++)
				{
					if (board[iP] == null) //qui est libre
					{
						Card[] newBoard = board.clone();
						playCard(aQuiDeJouer, newBoard, iP, card); //On joue la carte � l'emplacement libre
						possibilites.add(newBoard);
					}
				}
			}
		}
		
		return possibilites;
	}
	
	private int getActionPosition(Card[] oldBoard, Card[] newBoard)
	{
		for (int i = 0; i < oldBoard.length; i++)
		{
			if (oldBoard[i] == null && newBoard[i] != null)
				return i;
		}
		
		return -1;
	}
	
	private Card getActionCardName(Card[] oldBoard, Card[] newBoard)
	{
		for (int i = 0; i < oldBoard.length; i++)
		{
			if (oldBoard[i] == null && newBoard[i] != null)
				return newBoard[i];
		}
		
		return null;
	}
	
	private int evaluationSituation(Card[] board) // Renvoie la valeur du jeu pour l'ordinateur. Une valeur positive = victoire, une valeur n�gative = d�faite, une valeur nulle = �galit�
	{
		int myScore = 0, hisScore = 0;
		if (botPlayingFirst)
			hisScore += 1;
		else
			myScore += 1;
		
		for (Card c : board)
		{
			if (c.getCardView().getColor() == ME)
				myScore += 1;
			else
				hisScore += 1;
		}
		
		return myScore - hisScore;
	}
	
	private void playCard(int player, Card[] board, int cell, Card card)
	{
		// Applique la Regle Elementaire
		if (regleElementaire) applyElementaireRule(card, cell);
		
    	// Applique la Regle Identique
		if (regleIdentique) applySameRule(player, board, card, cell);
		
		// Applique la Regle Plus
		if (reglePlus) applyPlusRule(player, board, card, cell);
		
		// Applique la Regle de base
		applyBasicRule(player, board, card, cell);
	}
	
	private void applyBasicRule(int player, Card[] board, Card card, int cell)
	{
		board[cell] = card;
		card.lock();
		if (cell % 3 == 0) // Carte colonne gauche
		{
			Card c = board[cell + 1];
			if (c != null) // Si il y a une carte a sa droite
			{
				if (c.getCardView().getColor() != player && card.getRightValue() > c.getLeftValue()) // Celle jou�e est plus forte
				{
					c.getCardView().swapColor(); // On retourne l'autre
				}
			}
		}
		else if (cell % 3 == 1) // Colonne du milieu
		{
			Card c = board[cell + 1];
			if (c != null) // Si il y a une carte a sa droite
			{
				if (c.getCardView().getColor() != player && card.getRightValue() > c.getLeftValue()) // Celle jou�e est plus forte
				{
					c.getCardView().swapColor(); // On retourne l'autre
				}
			}
			
			c = board[cell - 1];
			if (c != null) // Si il y a une carte a sa gauche
			{
				if (c.getCardView().getColor() != player && card.getLeftValue() > c.getRightValue()) // Celle jou�e est plus forte
				{
					c.getCardView().swapColor(); // On retourne l'autre
				}
			}
		}
		else // Colonne de droite
		{
			Card c = board[cell - 1];
			if (c != null) // Si il y a une carte a sa gauche
			{
				if (c.getCardView().getColor() != player && card.getLeftValue() > c.getRightValue()) // Celle jou�e est plus forte
				{
					c.getCardView().swapColor(); // On retourne l'autre
				}
			}
		}
		
		if (cell / 3 == 0) // Ligne du haut
		{
			Card c = board[cell + 3];
			if (c != null) // Si il y a une carte en dessous
			{
				if (c.getCardView().getColor() != player && card.getBottomValue() > c.getTopValue()) // Celle jou�e est plus forte
				{
					c.getCardView().swapColor(); // On retourne l'autre
				}
			}
		}
		else if (cell / 3 == 1) // Ligne du milieu
		{
			Card c = board[cell + 3];
			if (c != null) // Si il y a une carte en dessous
			{
				if (c.getCardView().getColor() != player && card.getBottomValue() > c.getTopValue()) // Celle jou�e est plus forte
				{
					c.getCardView().swapColor(); // On retourne l'autre
				}
			}
			
			c = board[cell - 3];
			if (c != null) // Si il y a une carte en dessus
			{
				if (c.getCardView().getColor() != player && card.getTopValue() > c.getBottomValue()) // Celle jou�e est plus forte
				{
					c.getCardView().swapColor(); // On retourne l'autre
				}
			}
		}
		else // Ligne du bas
		{
			Card c = board[cell - 3];
			if (c != null) // Si il y a une carte en dessus
			{
				if (c.getCardView().getColor() != player && card.getTopValue() > c.getBottomValue()) // Celle jou�e est plus forte
				{
					c.getCardView().swapColor(); // On retourne l'autre
				}
			}
		}
	}
	// Applique la regle Identique sur le plateau
	private void applySameRule(int player, Card[] board, Card what, int cell)
	{
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
		else if (cell - 3 < 0 && regleMemeMur) // Regle MemeMur
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
		else if (cell + 3 >= board.length && regleMemeMur) // Regle MemeMur
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
		else if (cell % 3 == 2 && regleMemeMur) // Regle MemeMur
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
		else if (cell % 3 == 0 && regleMemeMur) // Regle MemeMur
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
					{
						card.getCardView().swapColor();
						swapped.add(c);
					}
				}
			}
			
			if (regleCombo) // Si regle Combo
			{
				for (int c : swapped) // Pour chaque carte retourn�e, on fait suivre la combo
				{
					Card card = board[c];
					applySameRule(player, board, card, c);
					if (reglePlus) applyPlusRule(player, board, card, c);
					applyBasicRule(player, board, card, c);
				}
			}
		}
	}
	
	// Applique la regle Plus sur le plateau
	private void applyPlusRule(int player, Card[] board, Card what, int cell)
	{
		Card[] cards = new Card[4];
		int[] numeros = new int[4];
		ArrayList<Integer> swapped = new ArrayList<Integer>();
		
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
								cards[i].getCardView().swapColor();
								swapped.add(i);
							}
							if (cards[j].getCardView().getColor() != player) 
							{
								cards[j].getCardView().swapColor();
								swapped.add(j);
							}
						}
					}
				}
			}
		}
		
		if (regleCombo)  // Si regle Combo
		{
			for (int i : swapped) // Pour chaque carte retourn�e, on fait suivre la combo
			{
				int c = numeros[i];
				Card card = cards[i];
				if (regleIdentique) applySameRule(player, board, card, c);
				applyPlusRule(player, board, card, c);
				applyBasicRule(player, board, card, c);
			}
		}
	}
	
	private void applyElementaireRule(Card c, int cell)
	{
		if (boardElements[cell] == null || boardElements[cell] == "")
			return;
		
		if (boardElements[cell] == c.getElement())
			c.bonusElementaire();
		else
			c.malusElementaire();
	}
	
	public int getPlayerValue()
	{
		return ME;
	}
}
