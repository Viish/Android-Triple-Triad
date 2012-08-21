package com.viish.apps.tripletriad;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.viish.apps.tripletriad.cards.Card;
import com.viish.apps.tripletriad.cards.MinimalistCardView;

public class Cards extends Activity
{
    private static int NUM_VIEWS = 10; // One for each card's level
    
	private ViewPager myCards, shop;
	private TextView levelMyCards, levelShop;
	private Typeface typeface;
	private LinearLayout mainLayout;
	private Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cards);
        context = this;
        
        TabHost tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();
        
        TabSpec myCardsTab = tabHost.newTabSpec(getString(R.string.cards_my_cards));
        myCardsTab.setIndicator(getString(R.string.cards_my_cards));
        myCardsTab.setContent(R.id.tabMyCards);
 
        TabSpec shopTabs = tabHost.newTabSpec(getString(R.string.cards_shop));
        shopTabs.setIndicator(getString(R.string.cards_shop));
        shopTabs.setContent(R.id.tabShop);
 
        tabHost.addTab(myCardsTab);
        tabHost.addTab(shopTabs);
		
        Typeface tempTF = Typeface.createFromAsset(getAssets(), "ff8font.ttf");
        typeface = Typeface.create(tempTF, Typeface.BOLD);
        levelMyCards = (TextView) findViewById(R.id.levelMyCards);
        levelMyCards.setTypeface(typeface);
        levelMyCards.setText(getString(R.string.cards_level) + " 10");
        levelShop = (TextView) findViewById(R.id.levelShop);
        levelShop.setTypeface(typeface);
        levelShop.setText(getString(R.string.cards_level) + " 10");
        
        mainLayout = (LinearLayout) findViewById(R.id.mainLayout);
        final ViewTreeObserver viewTreeObserver = mainLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
        	viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	            @SuppressWarnings("deprecation")
				@Override
	            public void onGlobalLayout() {
	                MyCardsAdapter myCardsAdapter = new MyCardsAdapter();
	                myCards = (ViewPager) findViewById(R.id.cards);
	                myCards.setAdapter(myCardsAdapter);
	                myCards.setOnPageChangeListener(myCardsLevelChangePageListener);
	                
	                ShopAdapter shopAdapter = new ShopAdapter();
	                shop = (ViewPager) findViewById(R.id.shop);
	                shop.setAdapter(shopAdapter);
	                shop.setOnPageChangeListener(shopLevelChangePageListener);
	                
	        		mainLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	            }
        	});
        }
	}
	
	private OnPageChangeListener myCardsLevelChangePageListener = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int position) {
			levelMyCards.setText(getString(R.string.cards_level) + " " + String.valueOf(10 - position));
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}
	};
	
	private OnPageChangeListener shopLevelChangePageListener = new OnPageChangeListener() {
		
		@Override
		public void onPageSelected(int position) {
			levelShop.setText(getString(R.string.cards_level) + " " + String.valueOf(10 - position));
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}
	};

	class MyCardsAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return NUM_VIEWS;
		}
	
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((GridView) object);
		}      
		  
		@Override  
		public Object instantiateItem(View collection, int position) {  
			View v = getLayoutInflater().inflate(R.layout.grid, null);
			GridView grid = (GridView) v.findViewById(R.id.grid);
			DatabaseStream dbs = new DatabaseStream(context);
			grid.setAdapter(new CardAdapter(dbs.getMyCards(NUM_VIEWS - position)));
			dbs.close();
			
			((ViewPager) collection).addView(grid,0);
			return grid;
		}  
		  
		@Override  
		public void destroyItem(View collection, int position, Object view){  
			((ViewPager) collection).removeView((GridView) view);
		} 
	}
	
	class ShopAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return NUM_VIEWS;
		}
	
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((GridView) object);
		}      
		  
		@Override  
		public Object instantiateItem(View collection, int position) {
			View v = getLayoutInflater().inflate(R.layout.grid, null);
			GridView grid = (GridView) v.findViewById(R.id.grid);
			DatabaseStream dbs = new DatabaseStream(context);
			grid.setAdapter(new CardAdapter(dbs.getAllCards(NUM_VIEWS - position)));
			dbs.close();
			
			((ViewPager) collection).addView(grid,0);
			return grid;
		}  
		  
		@Override  
		public void destroyItem(View collection, int position, Object view){  
			((ViewPager) collection).removeView((GridView) view);
		} 
	}
	
	class CardAdapter extends BaseAdapter
	{	
	    private ArrayList<Card> mThumbIds;

	    public CardAdapter(ArrayList<Card> cards) 
	    {
	        mThumbIds = cards;
	    }

		public int getCount() 
	    {
	        return mThumbIds.size();
	    }

		public Object getItem(int position) 
	    {
	        return mThumbIds.get(position);
	    }

		public long getItemId(int position) 
	    {
	        return 0;
	    }

		public View getView(int position, View convertView, ViewGroup parent) 
	    {
	        View v;
	        MinimalistCardView cv;
	        
	        if(convertView == null)
	        {
                LayoutInflater li = ((Activity) context).getLayoutInflater();
                v = li.inflate(R.layout.icon, null); 
	        }
	        else
	        {
                v = convertView;
	        }
	        v.setPadding(5, 5, 5, 5);	   
	        
	        Card card = mThumbIds.get(position);
	        cv = (MinimalistCardView) v.findViewById(R.id.icon_image);
	        cv.setCard(card);
//	        cv.setOnClickListener((OnClickListener) context);
	        cv.resizePictures(mainLayout.getWidth() / 8, mainLayout.getHeight() / 4);
	        v.requestLayout();
	        return v;
	    }
	}
}
