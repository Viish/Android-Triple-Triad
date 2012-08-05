package com.viish.apps.tripletriad;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class SplashScreen extends Activity
{
	public static final int SPLASH_SCREEN_TIME = 2000;
	public static final int MAIN_ACTIVITY = 101;
	
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        
        Typeface typeface = Typeface.createFromAsset(getAssets(), "ff8font.ttf");
        TextView tvSplashScreen = (TextView) findViewById(R.id.tvSplash);
        tvSplashScreen.setTypeface(typeface);
        tvSplashScreen.setShadowLayer(2f, 2f, 2f, Color.BLACK);
        TextView tvSplashScreen2 = (TextView) findViewById(R.id.tvSplash2);
        tvSplashScreen2.setTypeface(typeface);
        tvSplashScreen2.setShadowLayer(2f, 2f, 2f, Color.BLACK);
        
        final Context context = this;
        Timer t = new Timer();
        t.schedule(new TimerTask() 
        {
			@Override
			public void run() 
			{
				startActivityForResult(new Intent(context, MainMenu.class), MAIN_ACTIVITY);
			}
		}, new Date(Calendar.getInstance().getTimeInMillis() + SPLASH_SCREEN_TIME));
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
}
