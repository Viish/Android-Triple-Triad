package com.viish.apps.tripletriad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.viish.apps.tripletriad.cards.Card;
import com.viish.apps.tripletriad.cards.CompleteCardView;
import com.viish.apps.tripletriad.robots.Action;
import com.viish.apps.tripletriad.robots.BotEasy;
import com.viish.apps.tripletriad.robots.BotHard;
import com.viish.apps.tripletriad.robots.iBot;
import com.viish.apps.tripletriad.views.ElementView;
import com.viish.apps.tripletriad.views.HandView;
import com.viish.apps.tripletriad.views.TossView;

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
public class Game extends Activity implements EventFiredListener
{
	public static final boolean DEBUG = true;
	public static final int PLAYER = Engine.PLAYER;
	public static final int OPPONENT = Engine.OPPONENT;
	
	public static final int SLEEP_TIME_BEFORE = 1000;
	public static final int SLEEP_TIME_POPUP = 1000;
	public static final int SLEEP_TIME_ENDGAME = 3000;

	private RewardRule winningRule = RewardRule.One;
	private boolean isRegleRandom = true;
	private boolean isRegleOpen = false;
	private boolean isRegleIdentique = true;
	private boolean isRegleCombo = false;
	private boolean isReglePlus = true;
	private boolean isRegleMemeMur = true;
	private boolean isRegleElementaire = false;
	private boolean isBotVsBot = false;
	private boolean isPvp = false;
	private boolean isWaitingForUserToChooseACard = false;
	
	private int mScreenWidth, mScreenHeight, mTopMargin, mFieldWidth, mFieldHeight;
	private TossView mToss;
	private HandView mHand; 
	
	private Card[] mPlayerDeck, mOpponentDeck;
	private String[] mWonCards, mLoseCards;
	private Engine mEngine;
	private iBot mRobot, mFakePlayer;
	private Card mCurrentCard;
	private ProgressDialog mWaitingForOpponent;
	
	public void onCreate(Bundle b) 
    {
        super.onCreate(b);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.game);

        isPvp = getIntent().getBooleanExtra("PvP", false);
        boolean serveur = getIntent().getBooleanExtra("Serveur", false);
        isBotVsBot = getIntent().getBooleanExtra("BotVsBot", false);
        if (isBotVsBot) {
        	isRegleOpen = true;
        } else {
        	getRules();
        }
        
    	mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
    	mScreenHeight = getWindowManager().getDefaultDisplay().getHeight();
		mTopMargin = ((int) 7.6 * mScreenHeight / 100);
    	mFieldWidth = mScreenWidth / 2;
		mFieldHeight = mScreenHeight - 6 * mScreenHeight / 100 - mTopMargin;
        
        if (isRegleRandom) {
        	mPlayerDeck = getMyRandomDeck();
        }
        else 
        {
        	try
        	{
                //TODO
        		mPlayerDeck = getMyRandomDeck();
        	}
        	catch (Exception e)
        	{
            	mPlayerDeck = getMyRandomDeck();
        		e.printStackTrace();
        	}
        }

        String pvpServerIp = "";
        if (isPvp)
        {
        	pvpServerIp = getIntent().getExtras().getString("IpAddress");
			mEngine = new Engine(this, mPlayerDeck, isPvp, serveur, isRegleIdentique, isReglePlus, isRegleMemeMur, isRegleCombo, isRegleElementaire, pvpServerIp);
			mEngine.addEventFiredListener(this);
			if (serveur)
			{
				String ipAdress = mEngine.getIpAddress();
				if (ipAdress != null) {
					waitingForConnection(ipAdress, getPort());
				}
				else
				{
					Intent i = new Intent();
					i.putExtra("isServer", serveur);
					setResult(RESULT_CANCELED, i);
					finish();
				}
			}
        }
        else
		{        	
        	mEngine = new Engine(this, mPlayerDeck, isPvp, serveur, isRegleIdentique, isReglePlus, isRegleMemeMur, isRegleCombo, isRegleElementaire, pvpServerIp);
			mEngine.addEventFiredListener(this);
        	
	        mOpponentDeck = randomDeckRobot(mPlayerDeck);
	        
			showDeck(PLAYER, mPlayerDeck);
			showDeck(OPPONENT, mOpponentDeck);
			
			mRobot = new BotEasy(OPPONENT, mOpponentDeck, mEngine.getBoard(), mEngine.getElements(), isRegleIdentique, isReglePlus, isRegleMemeMur, isRegleCombo, isRegleElementaire);
			if (isBotVsBot) {
				mFakePlayer = new BotEasy(PLAYER, mPlayerDeck, mEngine.getBoard(), mEngine.getElements(), isRegleIdentique, isReglePlus, isRegleMemeMur, isRegleCombo, isRegleElementaire);
			}
			mRobot = new BotHard(OPPONENT, PLAYER, mOpponentDeck, mPlayerDeck, mEngine.getBoard(), mEngine.getElements(), isRegleIdentique, isReglePlus, isRegleMemeMur, isRegleCombo, isRegleElementaire);
			
			startGame();
		}
    }
	
	private void getRules()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		isRegleRandom = prefs.getBoolean("RandomRule", false);
		isRegleOpen = prefs.getBoolean("OpenRule", false);
		isRegleIdentique = prefs.getBoolean("SameRule", false);
		isReglePlus = prefs.getBoolean("PlusRule", false);
		isRegleCombo = prefs.getBoolean("ComboRule", false);
		isRegleMemeMur = prefs.getBoolean("SameWallRule", false);
		isRegleElementaire = prefs.getBoolean("ElementRule", false);
		
		String reward = prefs.getString("RewardRule", "One");
		if (reward.equals("Direct")) {
			winningRule = RewardRule.Direct;
		}
		else if (reward.equals("All")) {
			winningRule = RewardRule.All;
		}
		else {
			winningRule = RewardRule.One;
		}
	}
	
	private int getPort()
	{
		SharedPreferences customSharedPreference = getSharedPreferences("TripleTriad", Activity.MODE_PRIVATE);
		int port = customSharedPreference.getInt("Port", 7666);
		Log.d("Network Port", "actual value " + port);
		return port;
	}
	
	private void startGame()
	{				
		// Si regle Elementaire on dessine les elements
		if (isRegleElementaire) {
			drawElements();
		}
		
		// Init toss
		int startingPlayer = mEngine.getStartingPlayer();
		mToss = new TossView(this, startingPlayer);
		FrameLayout fl = (FrameLayout) this.findViewById(R.id.layout);    	
		fl.addView(mToss);
		mToss.animateToss();
		
		if (!isPvp)
		{
			if (startingPlayer == OPPONENT) {
				fireOpponentMove(mRobot);
			}
			else if (startingPlayer == PLAYER && isBotVsBot) {
				fireOpponentMove(mFakePlayer);
			}
		}
    }
	
	public void eventPvPGameReadyToStart(Card[] opponentD)
	{
		if (mWaitingForOpponent != null && mWaitingForOpponent.isShowing())
		{
			mWaitingForOpponent.dismiss();
		}
		
		mOpponentDeck = opponentD;
		Runnable start = new Runnable()
		{
			public void run() 
			{
				startGame();
			}
		};
		runOnUiThread(start);
	}
	
	/** Affiche les cartes d'un joueur (a gauche si joueur = 1, a droite sinon) */
	private void showDeck(int joueur, Card[] deck) // Affiche les cartes sur le plateau
    {    	
    	FrameLayout fl = (FrameLayout) this.findViewById(R.id.layout);    	
    	for (int i = 0; i < deck.length; i++)
    	{
			if (DEBUG) Log.d("Current card", deck[i].toString());
    		CompleteCardView cardView = new CompleteCardView(this, deck[i]);
    		deck[i].setCardView(cardView);
    		cardView.setColor(joueur);
    		cardView.resizePictures(mScreenWidth / 2 / 3, mScreenHeight / 4);
    		
    		if (joueur == OPPONENT) // Affichage des cartes � droite
    		{
	    		cardView.move(7 * mScreenWidth / 8 - cardView.getBitmap().getWidth() / 2, i*(mScreenHeight / 6) + ((mScreenHeight - ((mScreenHeight / 6) * 5)) / 6));
	        	fl.addView(cardView);
	        	if (!isRegleOpen) {
	        		cardView.flipCard(); // Joueur ne voit pas les cartes de CPU
	        	}
    		}
    		else if (joueur == PLAYER) // Affichage des cartes � gauche
    		{
    			try
    			{
    				cardView.move(mScreenWidth / 8 - cardView.getBitmap().getWidth() / 2, i*(mScreenHeight / 6) + ((mScreenHeight - ((mScreenHeight / 6) * 5)) / 6));
    				fl.addView(cardView);
    			}
    			catch (Exception e) { e.printStackTrace(); }
    		}
    	}
    }
	
	/** Génère un deck aléatoire pour le joueur robot de niveau équivalent */
	private Card[] randomDeckRobot(Card[] myDeck)
	{
		Card[] robotDeck = new Card[myDeck.length];
		DatabaseStream dbs = new DatabaseStream(this);
		
		for (int card = 0; card < myDeck.length; card++)
		{
			robotDeck[card] = dbs.getRandomCard(myDeck[card].getLevel());
			if (card > 0) {
				for (int j = 0; j < card; j++)
				{
					if (robotDeck[j].getFullName().equals(robotDeck[card].getFullName()))
					{
						card--;
						break;
					}
				}
			}
		}
		
		dbs.close();
		return robotDeck;
	}
	
	/** Crée le deck à partir des cartes choisies par l'utilisateur */
	private Card[] getMyRandomDeck()
	{
		int howMuch = (Engine.BOARD_SIZE + 1) / 2;
		Card[] deck = new Card[howMuch];
		DatabaseStream dbs = new DatabaseStream(this);
		ArrayList<Card> mycards = dbs.getMyCards();
		
		// Génère howMuch entiers aléatoires différents
		int[] randoms = new int[howMuch];
		Random random = new Random();
    	for (int i = 0; i < howMuch; i++)
    	{
    		boolean existeDeja = false;
    		int rand = random.nextInt(mycards.size() - 1) + 1;
    		
    		for (int j = 0; j < i; j++)
    		{
    			if (randoms[j] == rand) {
    				existeDeja = true;
    			}
    		}
    		
    		if (existeDeja) {
    			i -= 1;
    		}
    		else { 
    			randoms[i] = rand;
    		}
    	}
    	
		for (int c = 0; c < howMuch; c++)
		{
			deck[c] = mycards.get(randoms[c]);			
			if (DEBUG) Log.d("My Deck", deck[c].toString() + " added !");
		}
		
		dbs.close();
		return deck;
	}
	
	private void selectCard(Card card)
	{
		card.getCardView().move(card.getCardView().getPositionX() + card.getCardView().getRealWidth() * 1 / 3, card.getCardView().getPositionY());
		card.setSelected(true);
	}
	
	private void unselectCards(int joueur)
	{
		if (joueur == PLAYER)
		{
			for (Card card : mPlayerDeck)
			{
				if (card.isSelected()) {
					card.getCardView().move(card.getCardView().getPositionX() - card.getCardView().getRealWidth() * 1 / 3, card.getCardView().getPositionY());
				}
				card.setSelected(false);
			}
		}
		else if (joueur == OPPONENT)
		{
			for (Card card : mOpponentDeck)
			{
				if (card.isSelected()) {
					card.getCardView().move(card.getCardView().getPositionX() - card.getCardView().getRealWidth() * 1 / 3, card.getCardView().getPositionY());
				}
				card.setSelected(false);
			}
		}
	}
	
	private int getCell(int x, int y)
	{
		return y + 3 * x;
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		if (mEngine.isGameOver() && isWaitingForUserToChooseACard)
		{
			Card choosedCard = null;
			for (Card card : mOpponentDeck)
	    	{
	    		CompleteCardView cardView = card.getCardView();
	    		if (event.getX() >= cardView.getPositionX() && event.getX() <= cardView.getPositionX() + cardView.getRealWidth())
	    		{
	    			if (event.getY() >= cardView.getPositionY() && event.getY() <= cardView.getPositionY() + cardView.getRealHeight())
	    			{
	    				choosedCard = card;
	    			}
	    		}
	    	}
			
			if (choosedCard != null) {
				oneCardRewardChoosed(choosedCard);
			}
	    				
		}
		else if (!mEngine.isGameOver() && mEngine.isPlayerTurn() && !isBotVsBot)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
	    	{
	    		for (Card card : mPlayerDeck)
		    	{
		    		CompleteCardView cardView = card.getCardView();
		    		if (event.getX() >= cardView.getPositionX() && event.getX() <= cardView.getPositionX() + cardView.getRealWidth())
		    		{
		    			if (event.getY() >= cardView.getPositionY() && event.getY() <= cardView.getPositionY() + cardView.getRealHeight())
		    			{
		    				if (!card.isPlayed() && !card.equals(mCurrentCard)) 
		    				{
			    				if (DEBUG) Log.d("Current Card", card.toString());
		    					if (card.isSelected())
		    					{
		    						unselectCards(PLAYER);
		    						mCurrentCard = null;
		    					}
		    					else
		    					{
			    					unselectCards(PLAYER);
		    						selectCard(card);
		    						mCurrentCard = card;
		    					}
		    				}
		    			}
		    		}
		    	}
	    	}
			else if (event.getAction() == MotionEvent.ACTION_UP)
			{
				if (mToss != null) {
					deleteToss();
				}
				
				if (mCurrentCard == null && !mEngine.isGameOver()) // Le joueur n'a pas cliqué sur une carte de son Deck 
				{
					// Si une carte est selectionnée 
					Card selectedCard = null;
					for (Card card : mPlayerDeck)
					{
						if (card.isSelected())
						{
							selectedCard = card;
						}
					}
					
					if (selectedCard != null) // Une carte est selectionnée
					{
						// Calcul de la case choisie
						if (event.getX() >= (mScreenWidth / 4) && event.getX() <= (3 * (mScreenWidth / 4)))
	    				{

		    				
	    					if (event.getY() >= mTopMargin && event.getY() <= mTopMargin + mFieldHeight)
	    					{
	    						int boardX = ((int) ((event.getY() - mTopMargin) / (mFieldHeight / 3)));
	    						int boardY = ((int) ((event.getX() - (mScreenWidth / 4)) / (mFieldWidth / 3)));
	    						
	    						int screenX = (mScreenWidth / 4) + (boardY * (mFieldWidth / 3));
	    				    	int screenY = mTopMargin + (boardX * mFieldHeight / 3);
	    						
	    						// Vérifions si la case choisie est libre
	    						int cell = getCell(boardX, boardY);
	    						if (mEngine.isCellEmpty(cell))
	    						{
	    							// On joue le coup choisi par le joueur
	    							selectedCard.getCardView().resizePictures(mFieldWidth / 3, mFieldHeight / 3);
	    							selectedCard.setSelected(false);
	    							selectedCard.getCardView().move(screenX, screenY);
	    							mEngine.playCard(PLAYER, selectedCard, cell);
	    							mHand.swapPlayer();
	    							
	    							// Puis on demande au robot de jouer si la partie n'est pas finie
	    							if (!mEngine.isGameOver() && !isPvp) {
	    								fireOpponentMove(mRobot);
	    							}
	    							else if (mEngine.isGameOver()) {
	    								fireEndGame();
	    							}
	    						}
	    					}
	    				}
					}
				}
				mCurrentCard = null;
			}
		}
    	return super.onTouchEvent(event);
	}
	
	private void fireOpponentMove(final iBot bot)
	{
		final Activity activity = this;
		new Thread() 
    	{
			public void run() 
    		{
    			try
    			{
    				sleep(SLEEP_TIME_BEFORE);
    				
    				final Runnable runInUIThread = new Runnable() 
    				{
						public void run() 
    		    	    {
							opponentMove(bot);
    		    	    }
    		    	};
    		    	  
    		    	activity.runOnUiThread(runInUIThread);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}
    	    	
		    }
    	}.start();
	}
	
	private void opponentMove(iBot bot)
	{
		if (mToss != null) {
			deleteToss();
		}
		
		Action move = bot.nextMove();
		Card card = move.getCard();
		int cell = move.getCell();
		
		if (!isRegleOpen) { 
			card.getCardView().flipCard();
		}
		card.getCardView().resizePictures(mFieldWidth / 3, mFieldHeight / 3);
		
		int boardX = cell / 3;
		int boardY = cell % 3;
		int screenX = (mScreenWidth / 4) + (boardY * (mFieldWidth / 3));
    	int screenY = mTopMargin + (boardX * mFieldHeight / 3);
		card.getCardView().move(screenX, screenY);	    		
		
		mEngine.playCard(bot.getPlayerValue(), card, cell);
		if (mHand != null) {
			mHand.swapPlayer();
		}
		if (DEBUG) Log.d("Next Move", card + " en " + cell + ", gain " + move.getValue());
		
		if (mEngine.isGameOver()) {
			fireEndGame();
		}
		else if (isBotVsBot)
		{
			iBot botToPlay;
			if (bot.equals(mRobot)) {
				botToPlay = mFakePlayer;
			}
			else {
				botToPlay = mRobot;
			}
			fireOpponentMove(botToPlay);
		}
	}
	
	private void fireEndGame()
	{
		new Thread() 
    	{
			public void run() 
    		{
    			try
    			{
    				sleep(SLEEP_TIME_BEFORE);
    				
    				final Runnable runInUIThread = new Runnable() 
    				{
						public void run() 
    		    	    {
							endGame();
    		    	    }
    		    	};
    		    	  
    		    	runOnUiThread(runInUIThread);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}
    	    	
		    }
    	}.start();
	}
	
	private void endGame()
	{
		FrameLayout fl = (FrameLayout) findViewById(R.id.layout);
		ImageView v = new ImageView(this);
		
		int playerScore = mEngine.getPlayerScore();
		if (Game.DEBUG) Log.d("PlayerScore", playerScore+"");
		int opponentScore = mEngine.getOpponentScore();
		if (Game.DEBUG) Log.d("OpponnentScore", opponentScore+"");
		if (playerScore > opponentScore) {
			v.setImageResource(R.drawable.win);
		}
		else if (playerScore == opponentScore) {
			v.setImageResource(R.drawable.draw);
		}
		else {
			v.setImageResource(R.drawable.lose);
		}
		
		fl.addView(v);
		timerToHide(v, true);
	}
	
	// Supprime la main au centre de l'�cran indiquant quel joueur doit commencer
	public void deleteToss()
	{		
		FrameLayout fl = (FrameLayout) this.findViewById(R.id.layout);
		fl.removeView(mToss);
		mToss = null;
		
		// Ajout de la main sous les cartes du joueur dont le tour est en cours
		
		int player;
		if (mEngine.isPlayerTurn()) {
			player = PLAYER;
		}
		else {
			player = OPPONENT;
		}
		
		mHand = new HandView(this, player, mScreenWidth, mScreenHeight);
		fl.addView(mHand);
	}
	
	private void drawElements()
	{
		HashMap<String, Bitmap> bitmapsElements = new HashMap<String, Bitmap>();
		try
		{
			bitmapsElements.put("Earth", BitmapFactory.decodeStream(getResources().getAssets().open("Earth.png")));
			bitmapsElements.put("Fire", BitmapFactory.decodeStream(getResources().getAssets().open("Fire.png")));
			bitmapsElements.put("Wind", BitmapFactory.decodeStream(getResources().getAssets().open("Wind.png")));
			bitmapsElements.put("Holy", BitmapFactory.decodeStream(getResources().getAssets().open("Holy.png")));
			bitmapsElements.put("Poison", BitmapFactory.decodeStream(getResources().getAssets().open("Poison.png")));
			bitmapsElements.put("Thunder", BitmapFactory.decodeStream(getResources().getAssets().open("Thunder.png")));
			bitmapsElements.put("Water", BitmapFactory.decodeStream(getResources().getAssets().open("Water.png")));
			bitmapsElements.put("Ice", BitmapFactory.decodeStream(getResources().getAssets().open("Ice.png")));
			
			String[] elements = mEngine.getElements();
			FrameLayout fl = (FrameLayout) findViewById(R.id.layout);
			
			for (int e = 0; e < Engine.BOARD_SIZE; e++)
			{
				String element = elements[e];
				Bitmap bm = bitmapsElements.get(element);
				
				if (bm != null)
				{
					int x = (mScreenWidth / 4) + ((e % 3) * (mFieldWidth / 3)) + ((mFieldWidth /3) / 2) - (bm.getWidth() / 2);
			    	int y = mTopMargin + ((e / 3) * mFieldHeight / 3) + ((mFieldHeight /3) / 2) - (bm.getHeight() / 2);
					ElementView elementView = new ElementView(this, bm, x, y);
					fl.addView(elementView);
				}
			}
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public void waitingForConnection(final String serverIp, final int port)
	{
		Runnable runn = new Runnable()
		{
			public void run() 
			{
				mWaitingForOpponent = new ProgressDialog(Game.this);
				mWaitingForOpponent.setCancelable(true);
				mWaitingForOpponent.setTitle(R.string.waiting_for_client);
				mWaitingForOpponent.setMessage(getString(R.string.ipaddress) + " " + serverIp + "\n" + getString(R.string.port) + " " + port);
				mWaitingForOpponent.setOnCancelListener(new OnCancelListener() 
				{
					public void onCancel(DialogInterface dialog) 
					{
						if (mEngine != null) {
							mEngine.shutdownSocket();
						}
						setResult(RESULT_FIRST_USER);
						finish();
					}
				});
				mWaitingForOpponent.show();
			}
		};
		runOnUiThread(runn);
	}
	
	public void eventOpponentPlayed(Action move)
	{
		Card card = move.getCard();
		int cell = move.getCell();
		
		if (!isRegleOpen) {
			card.getCardView().flipCard();
		}
		card.getCardView().resizePictures(mFieldWidth / 3, mFieldHeight / 3);
		
		int boardX = cell / 3;
		int boardY = cell % 3;
		int screenX = (mScreenWidth / 4) + (boardY * (mFieldWidth / 3));
    	int screenY = mTopMargin + (boardX * mFieldHeight / 3);
		card.getCardView().move(screenX, screenY);
		mEngine.playCard(OPPONENT, card, cell);	
		
		if (mToss != null) {
			deleteToss();
		}
		else if (mHand != null) { 
			mHand.swapPlayer();
		}
		
		if (mEngine.isGameOver()) {
			fireEndGame();		
		}
	}

	public void eventSameWallTriggered() 
	{
		if (Game.DEBUG) Log.d("SameWallRule", "POPUP");
		try
		{
			FrameLayout fl = (FrameLayout) findViewById(R.id.layout);
			ImageView v = new ImageView(this);
			v.setImageResource(R.drawable.samewall);
			fl.addView(v);
			timerToHide(v, false);
		}
		catch (StackOverflowError e)
		{
			e.printStackTrace();
			Toast.makeText(this, "SAME WALL", SLEEP_TIME_POPUP).show();
		}
	}

	public void eventSameTriggered() 
	{
		if (Game.DEBUG) Log.d("SameRule", "POPUP");
		try
		{
			FrameLayout fl = (FrameLayout) findViewById(R.id.layout);
			ImageView v = new ImageView(this);
			v.setImageResource(R.drawable.same);
			fl.addView(v);
			timerToHide(v, false);
		}
		catch (StackOverflowError e)
		{
			e.printStackTrace();
			Toast.makeText(this, "SAME", SLEEP_TIME_POPUP).show();
		}
	}
	
	public void eventPlusTriggered() 
	{
		if (Game.DEBUG) Log.d("PlusRule", "POPUP");
		try
		{
			FrameLayout fl = (FrameLayout) findViewById(R.id.layout);
			ImageView v = new ImageView(this);
			v.setImageResource(R.drawable.plus);
			fl.addView(v);
			timerToHide(v, false);
		}
		catch (StackOverflowError e)
		{
			e.printStackTrace();
			Toast.makeText(this, "PLUS", SLEEP_TIME_POPUP).show();
		}
	}

	public void eventComboTriggered() 
	{
		try
		{
			FrameLayout fl = (FrameLayout) findViewById(R.id.layout);
			ImageView v = new ImageView(this);
			v.setImageResource(R.drawable.combo);
			fl.addView(v);
			timerToHide(v, false);
		}
		catch (StackOverflowError e)
		{
			e.printStackTrace();
			Toast.makeText(this, "COMBO", SLEEP_TIME_POPUP).show();
		}
	}
	
	private void timerToHide(final View v, final boolean isEndGame)
	{
		new Thread()
		{
			public void run()
			{
				try
    			{
					if (isEndGame) {
						sleep(SLEEP_TIME_ENDGAME);
					}
					else {
						sleep(SLEEP_TIME_POPUP);
					}
    				
    				final Runnable runInUIThread = new Runnable() 
    				{
						public void run() 
    		    	    {
    		    			v.setVisibility(View.GONE);
    		    			if (isEndGame)
    		    			{
    		    				if (isBotVsBot)
    		    				{
    		    					setResult(RESULT_OK);
    		    					finish();
    		    					return;
    		    				}
    		    				
    		    				int winner = 0;
    		    				if (mEngine.getPlayerScore() > mEngine.getOpponentScore()) // Win
    		    				{
    		    					winner = PLAYER;
    		    					DatabaseStream dbs = new DatabaseStream(v.getContext());
    		    					dbs.setGils(dbs.getGils() + Engine.GILS_WIN);
    		    					dbs.close();
    		    				}
    		    				else if (mEngine.getOpponentScore() > mEngine.getPlayerScore()) // Lose
    		    				{
    		    					winner = OPPONENT;
    		    					DatabaseStream dbs = new DatabaseStream(v.getContext());
    		    					dbs.setGils(dbs.getGils() + Engine.GILS_LOOSE);
    		    					dbs.close();
    		    				}
    		    				else // Draw
    		    				{
    		    					DatabaseStream dbs = new DatabaseStream(v.getContext());
    		    					dbs.setGils(dbs.getGils() + Engine.GILS_DRAW);
    		    					dbs.close();   
    		    					
        		    				if (winningRule != RewardRule.Direct) // m�me en cas de Draw la r�gle directe s'applique
        		    				{
        		    					setResult(RESULT_OK);
        		    					finish();
        		    					return;
        		    				} 		    					
    		    				}
    		    				
    		    				
    		    				cleanAll();
    		    				showDeckForReward(Game.PLAYER, mPlayerDeck);
    		    		        showDeckForReward(Game.OPPONENT, mOpponentDeck);
    		    		        
    		    		        if (winningRule == RewardRule.All)
        		    			{
        		    				if (winner == PLAYER)
        		    				{
        		    					mWonCards = new String[5];
        		    					mLoseCards = new String[0];
        		    					for (int c = 0; c < mOpponentDeck.length; c++)
        		    					{
        		    						Card card = mOpponentDeck[c];
        		    						card.getCardView().swapColor();
        		    						mWonCards[c] = card.getFullName();
        		    					}
        		    				}
        		    				else if (winner == OPPONENT)
        		    				{
        		    					mWonCards = new String[0];
        		    					mLoseCards = new String[5];
        		    					for (int c = 0; c < mPlayerDeck.length; c++)
        		    					{
        		    						Card card = mPlayerDeck[c];
        		    						card.getCardView().swapColor();
        		    						mLoseCards[c] = card.getFullName();
        		    					}
        		    				}
        		    				triggerEndReward();
        		    			}
        		    			else if (winningRule == RewardRule.Direct)
        		    			{
        		    				int iLose = 0;
        		    				mLoseCards = new String[5];
        		    				int iWon = 0;
        		    				mWonCards = new String[5];
        		    				
        		    				for (int c = 0; c < mPlayerDeck.length; c++)
        		    				{
        		    					Card card = mPlayerDeck[c];
        		    					if (card.getCardView().getColor() != card.getCardView().getAlternativeColor())
        		    					{
        		    						card.getCardView().swapColor();
        		    						mLoseCards[iLose] = card.getFullName();
        		    						iLose++;
        		    					}
        		    				}
        		    				for (int c = 0; c < mOpponentDeck.length; c++)
        		    				{
        		    					Card card = mOpponentDeck[c];
        		    					if (card.getCardView().getColor() != card.getCardView().getAlternativeColor())
        		    					{
        		    						card.getCardView().swapColor();
        		    						mWonCards[iWon] = card.getFullName();
        		    						iWon++;
        		    					}
        		    				}
        		    				triggerEndReward();
        		    			}
        		    			else
        		    			{
        		    				if (winner == PLAYER) {
        		    					isWaitingForUserToChooseACard = true;
        		    				}
        		    				else if(winner == OPPONENT && !isPvp) {
    		    						botChooseBetterCard();
        		    				}
        		    			}
    		    			}
    		    	    }
    		    	};
    		    	  
    		    	runOnUiThread(runInUIThread);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}
			}
		}.start();
	}
	
	public void eventOpponentChoosedReward(final Card card)
	{
		card.getCardView().swapColor();
		mLoseCards = new String[1];
		mLoseCards[0] = card.getFullName();
		
		Runnable r = new Runnable()
		{
			public void run()
			{
				triggerEndReward();
			}
		};
		new Thread(r).start();
	}
	
	private void oneCardRewardChoosed(final Card card)
	{		
		if (isPvp) {
			mEngine.sendLostCard(card);
		}
		
		card.getCardView().swapColor();
		isWaitingForUserToChooseACard = false;
		mWonCards = new String[1];
		mWonCards[0] = card.getFullName();
		
		Runnable r = new Runnable()
		{
			public void run()
			{
				triggerEndReward();
			}
		};
		new Thread(r).start();
	}
	
	private void botChooseBetterCard()
	{
		mLoseCards = new String[1];
		Card loseC = mPlayerDeck[0]; 
		for (Card card : mPlayerDeck)
		{
			if (card.getLevel() > loseC.getLevel() || (card.getLevel() == loseC.getLevel() && card.getTotal() > loseC.getTotal())) {
				loseC = card;
			}
		}
		mLoseCards[0] = loseC.getFullName();
		loseC.getCardView().swapColor();
		
		triggerEndReward();
	}
	
	private void cleanAll()
	{
		FrameLayout fl = (FrameLayout) this.findViewById(R.id.layout);
		fl.removeAllViews();
	}
	
	private void showDeckForReward(int player, Card[] deck)
	{
		FrameLayout fl = (FrameLayout) this.findViewById(R.id.layout);
		for (int i = 0; i < deck.length; i++)
    	{
    		CompleteCardView cardView = deck[i].getCardView();
    		cardView.resetElement();
			fl.addView(cardView);
    		cardView.resizePictures(mFieldWidth / 3, mFieldHeight / 3);
    		
    		if (winningRule == RewardRule.Direct) {
    			cardView.setAlternativeColor(cardView.getColor());
    		}
			cardView.setColor(player);
			if (!cardView.isFaceUp())
				cardView.flipCard();

			int marge = ((mScreenWidth / 5) - cardView.getRealWidth()) / 2;
    		if (player == Game.PLAYER) // Affichage des cartes en haut
    		{
    			cardView.move((i * (mScreenWidth / 5)) + marge, (mScreenHeight / 4) - (cardView.getRealHeight() / 2));
    		}
    		else if (player == Game.OPPONENT) // Affichage des cartes en bas
    		{
    			cardView.move((i * (mScreenWidth / 5)) + marge, (3 * mScreenHeight / 4) - (cardView.getRealHeight() / 2));
    		}
    	}
	}
	
	private void endReward()
	{
		DatabaseStream dbs = new DatabaseStream(this);
		if (mWonCards != null)
		{
			for (String cardFullName : mWonCards)
			{
				if (cardFullName != null && cardFullName != "") {
					dbs.nouvelleCarte(cardFullName);
				}
			}
		}
		if (mLoseCards != null)
		{
			for (String cardFullName : mLoseCards)
			{
				if (cardFullName != null && cardFullName != "") {
					dbs.supprimerCarte(cardFullName);
				}
			}
		}
		dbs.close();

		setResult(RESULT_OK);
		finish();
		return;
	}
	
	private void triggerEndReward()
	{
		new Thread()
		{
			public void run()
			{
				try
    			{
					sleep(SLEEP_TIME_ENDGAME);
    				
    				final Runnable runInUIThread = new Runnable() 
    				{
						public void run() 
    		    	    {
    		    			endReward();
    		    	    }
    		    	};
    		    	  
    		    	runOnUiThread(runInUIThread);
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}
			}
		}.start();
	}
	
	protected void onPause() 
	{
		// On termine la partie en cours
		setResult(RESULT_CANCELED);
		finish();
		super.onPause();
	}
	
	protected void onDestroy() 
	{
		if (mEngine != null) {
			mEngine.shutdownSocket();
		}
		super.onDestroy();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) && mEngine.isGameStarted() && !isBotVsBot)
		{
			return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			setResult(RESULT_FIRST_USER);
			if (mEngine != null) {
				mEngine.shutdownSocket();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
