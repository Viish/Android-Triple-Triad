package com.viish.apps.tripletriad.robots;

import com.viish.apps.tripletriad.cards.Card;

/*  Copyright (C) <2011-2012>  <Sylvain "Viish" Berfini>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
