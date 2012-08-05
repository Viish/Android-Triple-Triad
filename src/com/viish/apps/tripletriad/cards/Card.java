package com.viish.apps.tripletriad.cards;

import android.graphics.Bitmap;


public class Card
{
	private String name;
	private String element;
	private int level, edition, number;
	private boolean locked = false, selected = false;
	private Bitmap redFace, blueFace, backFace;
	private CompleteCardView cardView;
	
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
		
		blueFace = blue;
		redFace = red;
		backFace = back;
		
		cardView = null;
	}
	
	public CompleteCardView getCardView() {
		return cardView;
	}

	public void setCardView(CompleteCardView cardView) {
		this.cardView = cardView;
	}

	@Override
	public Card clone()
	{
		Card clone = new Card(name, level, edition, top, left, bot, right, element, number, blueFace, redFace, backFace);
		clone.setSelected(selected);
		
		if (locked) {
			clone.lock();
		}
		else {
			clone.unlock();
		}
		
		return clone;
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
	
	public void bonusElementaire()
	{
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
	}
	
	public void malusElementaire()
	{
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
