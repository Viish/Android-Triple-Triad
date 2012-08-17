package com.viish.apps.tripletriad.robots;

import java.util.ArrayList;

import com.viish.apps.tripletriad.cards.Card;

public class MinMaxEngine {
	private Card[] board;
	private String[] elements;
	private int cardOnBoard;
	private boolean ruleSame, rulePlus, ruleSameWall, ruleCombo, ruleElementary, rulePlusWall;
	
	public MinMaxEngine(Card[] b, String[] e, boolean identique, boolean plus, boolean mememur, boolean combo, boolean plusWall, boolean elementaire) {
		board = b;
		elements = e;
		
		cardOnBoard = 0;
		for (int i = 0; i < board.length; i++) {
			if (board[i] != null) {
				cardOnBoard++;
			}
		}
		
		ruleSame = identique;
		rulePlus = plus;
		ruleSameWall = mememur;
		ruleCombo = combo;
		ruleElementary = elementaire;
		rulePlusWall = plusWall;
	}
	
	public void playCard(int player, Card card, int cell)
	{	
		board[cell] = card;
		cardOnBoard += 1;
    	
		if (ruleElementary) {
			applyElementaireRule(card, cell);
		}
		
    	if (cardOnBoard > 1)
    	{
			if (ruleSame) {
				applySameRule(player, card, cell, false);
			}
			
			if (rulePlus) {
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
		else if (cell - 3 < 0 && ruleSameWall)
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
		else if (cell + 3 >= this.board.length && ruleSameWall)
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
		else if (cell % 3 == 2 && ruleSameWall)
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
		else if (cell % 3 == 0 && ruleSameWall)
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
			
			if (ruleCombo)
			{
				for (int c : swapped) 
				{
					Card card = this.board[c];
					applySameRule(player, card, c, true);
					if (rulePlus) {
						applyPlusRule(player, card, c, true);
					}
					applyBasicRule(player, card, c, true);
				}
			}
		}
	}
	
	private void applyPlusRule(int player, Card what, int cell, boolean combo) {
		int[] sums = new int[4];
		int[] cells = new int[4];
		Card[] cards = new Card[4];
		ArrayList<Integer> swapped = new ArrayList<Integer>();
		
		Card rulePlusWallCard = new Card("", 0, 0, "10", "10", "10", "10", "", 1, null, null, null);
		rulePlusWallCard.setColor(player);
		
		cells[0] = cell - 3;
		cells[1] = cell + 3;
		cells[2] = cell + 1;
		cells[3] = cell - 1;
		
		cards[0] = cell - 3 >= 0 ? board[cells[0]] : (rulePlusWall ? rulePlusWallCard : null);
		cards[1] = cell + 3 < board.length ? board[cells[1]] : (rulePlusWall ? rulePlusWallCard : null);
		cards[2] = cell % 3 <= 1 ? board[cells[2]] : (rulePlusWall ? rulePlusWallCard : null);
		cards[3] = cell % 3 >= 1 ? board[cells[3]] : (rulePlusWall ? rulePlusWallCard : null);
		
		sums[0] = cards[0] != null ? what.getTopValue() + cards[0].getBottomValue() : -1;
		sums[1] = cards[1] != null ? what.getBottomValue() + cards[1].getTopValue() : -1;
		sums[2] = cards[2] != null ? what.getLeftValue() + cards[2].getRightValue() : -1;
		sums[3] = cards[3] != null ? what.getRightValue() + cards[3].getLeftValue() : -1;
		
		for (int i = 0; i < sums.length; i++) {
			for (int j = 0; j < sums.length; j++) {
				if (i != j && sums[i] == sums[j] && sums[i] != -1) {
					// We need at least an opponent card to trigger plus rule
					boolean atLeastOneOpponentCardCondition = false;
					atLeastOneOpponentCardCondition |= (cards[i] != null && cards[i].getColor() != player);
					atLeastOneOpponentCardCondition |= (cards[j] != null && cards[j].getColor() != player);
					
					if (atLeastOneOpponentCardCondition) {
						if (cards[i] != null && cards[i].getColor() != player)
						{
							cards[i].swapColor();
							swapped.add(i);
						}
						if (cards[j] != null && cards[j].getColor() != player) 
						{
							cards[j].swapColor();
							swapped.add(j);
						}
					}
				}
			}
		}
		
		if (ruleCombo)
		{
			for (int i : swapped)
			{
				int c = cells[i];
				Card card = cards[i];
				if (ruleSame) {
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
