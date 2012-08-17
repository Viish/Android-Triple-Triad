package com.viish.apps.tripletriad.cards;

import android.graphics.Bitmap;

import com.viish.apps.tripletriad.Engine;

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
public class Card
{
	private String name;
	private String element;
	private int level, edition, number;
	private boolean locked = false, selected = false, hasMalus = false, hasBonus = false;
	private Bitmap redFace, blueFace, backFace;
	private CompleteCardView cardView;
	private int color, rewardDirectColor;
	private boolean visible;
	
	public String top, left, bot, right;
	
	public Card(String name, int level, int edition, String top, String left, String bot, String right, String element, int number, Bitmap blue, Bitmap red, Bitmap back)
	{
		this.name = name;
		this.level = level;
		this.edition = edition;
		this.top = top;
		this.left = left;
		this.bot = bot;
		this.right = right;
		this.element = element;
		this.number = number;
		
		color = rewardDirectColor = Engine.BLUE;
		visible = true;
		
		blueFace = blue;
		redFace = red;
		backFace = back;
		
		cardView = null;
	}
	
	public CompleteCardView getView() {
		return cardView;
	}

	public void setCardView(CompleteCardView cardView) {
		this.cardView = cardView;
	}

	@Override
	public Card clone()
	{
		Card clone = new Card(name, level, edition, top, left, bot, right, element, number, blueFace, redFace, backFace);
		clone.setColor(color);
		clone.setRewardDirectColor(rewardDirectColor);
		clone.setSelected(selected);
		
		if (locked) {
			clone.lock();
		}
		else {
			clone.unlock();
		}
		
		return clone;
	}
	
	public void setColor(int color)
	{
		this.color = color;
	}
	
	public void setRewardDirectColor(int color) {
		rewardDirectColor = color;
	}
	
	public int getRewardDirectColor() {
		return rewardDirectColor;
	}
	
	public void swapColor()
	{
		if (color == Engine.BLUE) {
			color = Engine.RED;
		} else {
			color = Engine.BLUE;
		}
		
		if (cardView != null) {
			cardView.swapColor();
		}
	}
	
	public int getColor()
	{
		return color;
	}
	
	public boolean isFaceUp()
	{
		return visible;
	}
	
	public void flipCard()
	{
		visible = !visible;
		
		if (cardView != null) {
			cardView.invalidate();
		}
	}
	
	public int getTotal()
	{
		return getTopValue() + getBottomValue() + getLeftValue() + getRightValue();
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public void setNumber(int n)
	{
		number = n;
	}
	
	public void resetBonusMalusIfNeeded() {
		resetBonusMalusIfNeeded(false);
	}
	public void resetBonusMalusIfNeeded(boolean simulating) {
		if (hasBonus) {
			malusElementaire(simulating);
			hasMalus = hasBonus = false;
		} else if (hasMalus) {
			bonusElementaire(simulating);
			hasMalus = hasBonus = false;
		}
		
		if (cardView != null && !simulating) {
			cardView.invalidate();
		}
	}
	
	public void bonusElementaire() {
		bonusElementaire(false);
	}
	public void bonusElementaire(boolean simulating)
	{
		hasBonus = true;
		
		if (!top.equals("A"))
		{
			int temp = Integer.parseInt(top);
			temp += 1;
			if (temp == 10) { top = "A"; }
			else { top = String.valueOf(temp); }
		}
		
		if (!left.equals("A"))
		{
			int temp = Integer.parseInt(left);
			temp += 1;
			if (temp == 10) { left = "A"; }
			else { left = String.valueOf(temp); }
		}
		
		if (!bot.equals("A"))
		{
			int temp = Integer.parseInt(bot);
			temp += 1;
			if (temp == 10) { bot = "A"; }
			else { bot = String.valueOf(temp); }
		}
		
		if (!right.equals("A"))
		{
			int temp = Integer.parseInt(right);
			temp += 1;
			if (temp == 10) { right = "A"; }
			else { right = String.valueOf(temp); }
		}
		
		if (cardView != null && !simulating) {
			cardView.invalidate();
		}
	}

	public void malusElementaire() {
		malusElementaire(false);
	}
	public void malusElementaire(boolean simulating)
	{
		hasMalus = true;
		
		if (!top.equals("A"))
		{
			int temp = Integer.parseInt(top);
			temp -= 1;
			top = String.valueOf(temp);
		}
		else { top = "9"; }
		
		if (!left.equals("A"))
		{
			int temp = Integer.parseInt(left);
			temp -= 1;
			left = String.valueOf(temp);
		}
		else { left = "9"; }
		
		if (!bot.equals("A"))
		{
			int temp = Integer.parseInt(bot);
			temp -= 1;
			bot = String.valueOf(temp);
		}
		else { bot = "9"; }
		
		if (!right.equals("A"))
		{
			int temp = Integer.parseInt(right);
			temp -= 1;
			right = String.valueOf(temp);
		}
		else { right = "9"; }
		
		if (cardView != null && !simulating) {
			cardView.invalidate();
		}
	}
	
	@Override
	public String toString()
	{
		return name + " (level " +  level + ") : " + top + ", " + left + ", " + bot + ", " + right + ", " + element; 
	}
	
	public void lock()
	{
		locked = true;
	}
	
	public void unlock()
	{
		locked = false;
	}
	
	public boolean isPlayed()
	{
		return locked;
	}
	
	public boolean isSelected()
	{
		return selected;
	}
	
	public void setSelected(boolean isSelected)
	{
		selected = isSelected;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getFullName()
	{
		return "ff" + edition + "/" + name;
	}
	
	public int getEdition()
	{
		return edition;
	}
	
	public String getElement()
	{
		return element;
	}
	
	public int getTopValue()
	{
		try
		{
			return Integer.parseInt(top);
		}
		catch (NumberFormatException e)
		{
			return 10;
		}
	}
	
	public int getLeftValue()
	{
		try
		{
			return Integer.parseInt(left);
		}
		catch (NumberFormatException e)
		{
			return 10;
		}
	}
	
	public int getBottomValue()
	{
		try
		{
			return Integer.parseInt(bot);
		}
		catch (NumberFormatException e)
		{
			return 10;
		}
	}
	
	public int getRightValue()
	{
		try
		{
			return Integer.parseInt(right);
		}
		catch (NumberFormatException e)
		{
			return 10;
		}
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public Bitmap getRedFace() {
		return redFace;
	}
	
	public Bitmap getBlueFace() {
		return blueFace;
	}
	
	public Bitmap getBackFace() {
		return backFace;
	}

	public void setRedFace(Bitmap redFace) {
		this.redFace = redFace;
	}

	public void setBlueFace(Bitmap blueFace) {
		this.blueFace = blueFace;
	}

	public void setBackFace(Bitmap backFace) {
		this.backFace = backFace;
	}
}
