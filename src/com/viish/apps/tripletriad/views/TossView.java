package com.viish.apps.tripletriad.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class TossView extends ImageView
{
	private int firstToPlay;
	private Bitmap tossPicture;
	
	public TossView(Context context, int joueur) 
	{
		super(context);
		this.firstToPlay = joueur;
		
		try
		{
			float degrees;
			if (firstToPlay == 1) degrees = -90.0f;
			else degrees = 90.0f;
			
			tossPicture = rotate(BitmapFactory.decodeStream(context.getResources().getAssets().open("toss.png")), degrees);
		}
		catch (Exception e)
		{
			Log.i("Error", e.toString());
		}
	}

	public void animateToss()
	{
		Animation animation = new RotateAnimation(0.0f, 3600.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(1500);
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		setAnimation(animation);
		animation.start();
	}
	
	@Override
	protected void onDraw(Canvas canvas) 
	{
		canvas.drawBitmap(tossPicture, (canvas.getWidth() / 2) - (tossPicture.getWidth() / 2), (canvas.getHeight() / 2) - (tossPicture.getHeight() / 2), null);
	}
	
	private Bitmap rotate(Bitmap bm, float degrees)
	{
		int width = bm.getWidth();
		int height = bm.getHeight();
		
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

		return resizedBitmap;
	}
}
