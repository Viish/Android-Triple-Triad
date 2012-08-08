package com.viish.apps.tripletriad.views;

import android.content.Context;
import android.graphics.Bitmap;
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
public class ElementView extends ImageView
{
	private Bitmap picture;
	private int positionX, positionY;
	
	public ElementView(Context context, Bitmap picture, int positionX, int positionY) 
	{
		super(context);
		this.picture = picture;
		this.positionX = positionX;
		this.positionY = positionY;
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		canvas.drawBitmap(this.picture, this.positionX, this.positionY, null);
	}
}
