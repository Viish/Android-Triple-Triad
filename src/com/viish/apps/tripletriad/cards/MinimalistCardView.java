package com.viish.apps.tripletriad.cards;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("ViewConstructor")
public class MinimalistCardView extends ImageView
{
	private static final int PAINT_SIZE = 15;
	private static final int PAINT_COLOR = 0xff000000;
	private static final int SHADOW_COLOR = 0xffffffff;
	private static final float SHADOW_RADIUS = 1.0f;
    private static final int SHADOW_OFFSET = 1;
	
	private Paint paint;
	private Card card;
	private int posx, posy, taillePinceau;
	private boolean isChecked = false;
    
    private boolean showNumber;
    
	public MinimalistCardView(Context context, Bitmap picture, Card card, boolean showNumber)
	{
		super(context);
		this.card = card;
		this.showNumber = showNumber;
		
		initPaint();
		setImage(picture);
	}
	
	public MinimalistCardView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		showNumber = true;
		
		initPaint();
	}
	
	public MinimalistCardView clone(boolean showNumber)
	{
		return new MinimalistCardView(getContext(), card.getBlueFace(), card, showNumber);
	}
	
	public String getCardName()
	{
		return card.getName();
	}
	
	public int getCardLevel()
	{
		return card.getLevel();
	}

	public int getCardEdition() 
	{
		return card.getEdition();
	}
	
	private void initPaint()
	{
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "font.ttf");
		paint.setTypeface(typeface); 
		paint.setColor(PAINT_COLOR);
		paint.setTextAlign(Align.LEFT);
		paint.setTextSize(PAINT_SIZE);
	}
	
	public void setImage(Bitmap picture)
	{
		card.setBlueFace(picture);
		setImageBitmap(picture);
	}

	public void setChecked(boolean checked) 
	{
		isChecked = checked;
		invalidate();
	}
	
	public boolean isChecked()
	{
		return isChecked;
	}
	
	public void swapChecked() 
	{
		isChecked = !isChecked;
		invalidate();
	}
	
	public void setCard(Card c)
	{
		card = c;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		String number = "";
		if (showNumber) {
			number = "x" + card.getNumber();
		}
		
		Bitmap blueFace = card.getBlueFace();
		if (isChecked) 
		{
			canvas.drawBitmap(card.getRedFace(), 0, 0, null);
		}
		else 
		{
			canvas.drawBitmap(blueFace, 0, 0, null); 
		}
			
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.top, posx + (3 * taillePinceau / 2 + 2) / 2, posy + taillePinceau, paint);
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.top, posx + (3 * taillePinceau / 2 + 2) / 2, posy + taillePinceau, paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.top, posx + (3 * taillePinceau / 2 + 2) / 2, posy + taillePinceau, paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.top, posx + (3 * taillePinceau / 2 + 2) / 2, posy + taillePinceau, paint);
		
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.left, posx + 3, posy + 3 * taillePinceau / 2, paint);
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.left, posx + 3, posy + 3 * taillePinceau / 2, paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.left, posx + 3, posy + 3 * taillePinceau / 2, paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.left, posx + 3, posy + 3 * taillePinceau / 2, paint);
		
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.bot, posx + (3 * taillePinceau / 2 + 2) / 2, posy + 2 * taillePinceau, paint);
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.bot, posx + (3 * taillePinceau / 2 + 2) / 2, posy + 2 * taillePinceau, paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.bot, posx + (3 * taillePinceau / 2 + 2) / 2, posy + 2 * taillePinceau, paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.bot, posx + (3 * taillePinceau / 2 + 2) / 2, posy + 2 * taillePinceau, paint);
		
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.right, posx + (3 * taillePinceau / 2) - 2, posy + 3 * taillePinceau / 2, paint);
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.right, posx + (3 * taillePinceau / 2) - 2, posy + 3 * taillePinceau / 2, paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.right, posx + (3 * taillePinceau / 2) - 2, posy + 3 * taillePinceau / 2, paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(card.right, posx + (3 * taillePinceau / 2) - 2, posy + 3 * taillePinceau / 2, paint);
		
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(number, blueFace.getWidth() - taillePinceau, blueFace.getHeight() - (taillePinceau / 2), paint);
		paint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(number, blueFace.getWidth() - taillePinceau, blueFace.getHeight() - (taillePinceau / 2), paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(number, blueFace.getWidth() - taillePinceau, blueFace.getHeight() - (taillePinceau / 2), paint);
		paint.setShadowLayer(SHADOW_RADIUS, -SHADOW_OFFSET, -SHADOW_OFFSET, SHADOW_COLOR);
		canvas.drawText(number, blueFace.getWidth() - taillePinceau, blueFace.getHeight() - (taillePinceau / 2), paint);
	}
}
