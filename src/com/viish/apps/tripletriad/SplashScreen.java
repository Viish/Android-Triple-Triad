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
