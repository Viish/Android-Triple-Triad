package com.viish.apps.tripletriad.robots;

import com.viish.apps.tripletriad.cards.Card;

public class Action 
{
	private int valeur, position;
	private Card card;
	
	public Action(Card c, int pos, int val)
	{
		valeur = val;
		card = c;
		position = pos;
	}
	
	public int getValue()
	{
		return valeur;
	}
	
	public String getCardName()
	{
		return card.getFullName();
	}
	
	public Card getCard()
	{
		return card;
	}
	
	public int getCell()
	{
		return position;
	}
}
