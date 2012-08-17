package com.viish.apps.tripletriad.robots;

import java.util.ArrayList;

import com.viish.apps.tripletriad.cards.Card;

public class MinMaxEngine {
	private Card[] board;
	private String[] elements;
	private int cardOnBoard;
	private boolean regleIdentique, reglePlus, regleMemeMur, regleCombo, regleElementaire;
	
	public MinMaxEngine(Card[] b, String[] e, boolean identique, boolean plus, boolean mememur, boolean combo, boolean elementaire) {
		board = b;
		elements = e;
		
		cardOnBoard = 0;
		for (int i = 0; i < board.length; i++) {
			if (board[i] != null) {
				cardOnBoard++;
			}
		}
		
		regleIdentique = identique;
		reglePlus = plus;
		regleMemeMur = mememur;
		regleCombo = combo;
		regleElementaire = elementaire;
	}
	
	public void playCard(int player, Card card, int cell)
	{	
		board[cell] = card;
		cardOnBoard += 1;
    	
		if (regleElementaire) {
			applyElementaireRule(card, cell);
		}
		
    	if (cardOnBoard > 1)
    	{
			if (regleIdentique) {
				applySameRule(player, card, cell, false);
			}
			
			if (reglePlus) {
				applyPlusRule(player, card, cell, false);
			}
			
			applyBasicRule(player, card, cell, false);
    	}
	}
	
	private void applyBasicRule(int player, Card card, int cell, boolean combo)
	{
		if (cell % 3 == 0)
		{
			Card c = this.board[cell + 1];
			if (c != null)
			{
				if (c.getColor() != player && card.getRightValue() > c.getLeftValue())
				{
					c.swapColor();
				}
			}
		}
		else if (cell % 3 == 1) // Colonne du milieu
		{
			Card c = this.board[cell + 1];
			if (c != null) // Si il y a une carte a sa droite
			{
				if (c.getColor() != player && card.getRightValue() > c.getLeftValue())
				{
					c.swapColor(); // On retourne l'autre
				}
			}
			
			c = this.board[cell - 1];
			if (c != null)
			{
				if (c.getColor() != player && card.getLeftValue() > c.getRightValue())
				{
					c.swapColor();
				}
			}
		}
		else
		{
			Card c = this.board[cell - 1];
			if (c != null)
			{
				if (c.getColor() != player && card.getLeftValue() > c.getRightValue())
				{
					c.swapColor();
				}
			}
		}
		
		if (cell / 3 == 0)
		{
			Card c = this.board[cell + 3];
			if (c != null)
			{
				if (c.getColor() != player && card.getBottomValue() > c.getTopValue())
				{
					c.swapColor();
				}
			}
		}
		else if (cell / 3 == 1)
		{
			Card c = board[cell + 3];
			if (c != null)
			{
				if (c.getColor() != player && card.getBottomValue() > c.getTopValue())
				{
					c.swapColor();
				}
			}
			
			c = board[cell - 3];
			if (c != null) 
			{
				if (c.getColor() != player && card.getTopValue() > c.getBottomValue())
				{
					c.swapColor();
				}
			}
		}
		else 
		{
			Card c = this.board[cell - 3];
			if (c != null)
			{
				if (c.getColor() != player && card.getTopValue() > c.getBottomValue())
				{
					c.swapColor();
				}
			}
		}
	}
	
	private void applySameRule(int player, Card what, int cell, boolean combo)
	{
		ArrayList<Integer> cards = new ArrayList<Integer>(); 
		int carteAdverse = 0;
		
		if (cell - 3 >= 0 && this.board[cell - 3] != null)
		{
			if (this.board[cell - 3].getBottomValue() == what.getTopValue())
			{
				cards.add(cell - 3);
				if (this.board[cell - 3].getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell - 3 < 0 && regleMemeMur)
		{
			if (10 == what.getTopValue())
			{
				cards.add(-1);
			}
		}
		if (cell + 3 < this.board.length && this.board[cell + 3] != null)
		{
			if (this.board[cell + 3].getTopValue() == what.getBottomValue())
			{
				cards.add(cell + 3);
				if (this.board[cell + 3].getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell + 3 >= this.board.length && regleMemeMur)
		{
			if (10 == what.getBottomValue())
			{
				cards.add(-1);
			}
		}
		if (cell % 3 <= 1 && this.board[cell + 1] != null)
		{
			if (this.board[cell + 1].getLeftValue() == what.getRightValue())
			{
				cards.add(cell + 1);
				if (this.board[cell + 1].getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell % 3 == 2 && regleMemeMur)
		{
			if (10 == what.getRightValue())
			{
				cards.add(-1);
			}
		}
		if (cell % 3 >= 1 && this.board[cell - 1] != null)
		{
			if (this.board[cell - 1].getRightValue() == what.getLeftValue())
			{
				cards.add(cell - 1);
				if (this.board[cell - 1].getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell % 3 == 0 && regleMemeMur)
		{
			if (10 == what.getLeftValue())
			{
				cards.add(-1);
			}
		}
		
		if (cards.size() >= 2 && carteAdverse >= 1)
		{
			ArrayList<Integer> swapped = new ArrayList<Integer>();
			for (int c : cards)
			{
				if (c != -1)
				{
					Card card = this.board[c];
					if (card.getColor() != player)
					{
						card.swapColor();
						swapped.add(c);
					}
				}
			}
			
			if (regleCombo)
			{
				for (int c : swapped) 
				{
					Card card = this.board[c];
					applySameRule(player, card, c, true);
					if (reglePlus) {
						applyPlusRule(player, card, c, true);
					}
					applyBasicRule(player, card, c, true);
				}
			}
		}
	}
	
	private void applyPlusRule(int player, Card what, int cell, boolean combo)
	{
		Card[] cards = new Card[4];
		int[] numeros = new int[4];
		ArrayList<Integer> swapped = new ArrayList<Integer>();
		
		numeros[0] = cell - 3;
		numeros[1] = cell - 1;
		numeros[2] = cell + 3;
		numeros[3] = cell + 1;
		if (cell - 3 >= 0) cards[0] = this.board[cell - 3];
		if (cell + 3 < this.board.length) cards[2] = this.board[cell + 3];
		if (cell % 3 <= 1) cards[3] = this.board[cell + 1];
		if (cell % 3 >= 1) cards[1] = this.board[cell - 1];
		
		for (int i = 0; i < 3; i ++)
		{ 
			boolean condition = false;
			if (cards[i] != null)
			{
				int somme = 0;
				if (i == 0) somme = what.getTopValue() + cards[i].getBottomValue();
				else if (i == 1) somme = what.getLeftValue() + cards[i].getRightValue();
				else if (i == 2) somme = what.getBottomValue() + cards[i].getTopValue();
				
				if (player != cards[i].getColor()) condition = true;
				
				for (int j = i+1; j < 4; j++)
				{
					if (cards[j] != null)
					{
						int somme2 = 0;
						if (j == 3) somme2 = what.getRightValue() + cards[j].getLeftValue();
						else if (j == 1) somme2 = what.getLeftValue() + cards[j].getRightValue();
						else if (j == 2) somme2 = what.getBottomValue() + cards[j].getTopValue();
						
						if (player != cards[j].getColor()) condition = true;
						
						if (somme == somme2 && condition)
						{
							if (cards[i].getColor() != player)
							{
								cards[i].swapColor();
								swapped.add(i);
							}
							if (cards[j].getColor() != player) 
							{
								cards[j].swapColor();
								swapped.add(j);
							}
						}
					}
				}
			}
		}
		
		if (regleCombo)
		{
			for (int i : swapped)
			{
				int c = numeros[i];
				Card card = cards[i];
				if (regleIdentique) {
					applySameRule(player, card, c, true);
				}
				applyPlusRule(player, card, c, true);
				applyBasicRule(player, card, c, true);
			}
		}
	}
	
	private void applyElementaireRule(Card card, int cell)
	{
		if (elements[cell] == null || elements[cell] == "")
			return;
		
		if (elements[cell].equals(card.getElement())) {
			card.bonusElementaire(true);
		}
		else {
			card.malusElementaire(true);
		}
	}
}
