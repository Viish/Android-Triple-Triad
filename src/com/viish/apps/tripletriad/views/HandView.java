package com.viish.apps.tripletriad.views;

import com.viish.apps.tripletriad.Engine;
import com.viish.apps.tripletriad.Log;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.widget.ImageView;

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
public class HandView extends ImageView
{
	private Bitmap handPicture;
	private int currentPlayer, posx, posy, screenHeight, screenWidth;
	
	public HandView(Context context, int currentPlayer, int screenWidth, int screenHeight) 
	{
		super(context);
		this.currentPlayer = currentPlayer;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		
		try
		{
			handPicture = BitmapFactory.decodeStream(context.getResources().getAssets().open("toss.png"));
		}
		catch (Exception e)
		{ Log.e(e); }
		
		if (currentPlayer == Engine.OPPONENT)
		{			
			posx = (7 * (screenWidth / 8)) - (handPicture.getWidth() / 2);
			posy = screenHeight - handPicture.getHeight();
		}
		else 
		{
			posx = (screenWidth / 8) - (handPicture.getWidth() / 2);
			posy = screenHeight - handPicture.getHeight();
		}
	}
	
	public void swapPlayer()
	{
		if (currentPlayer == Engine.PLAYER)
		{
			currentPlayer = Engine.OPPONENT;
			
			posx = (7 * (screenWidth / 8)) - (handPicture.getWidth() / 2);
			posy = screenHeight - handPicture.getHeight();
		}
		else 
		{
			currentPlayer = Engine.PLAYER;
			
			posx = (screenWidth / 8) - (handPicture.getWidth() / 2);
			posy = screenHeight - handPicture.getHeight();
		}
		
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		canvas.drawBitmap(handPicture, posx, posy, null);
	}
}
