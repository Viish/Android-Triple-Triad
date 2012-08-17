package com.viish.apps.tripletriad;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.viish.apps.tripletriad.cards.Card;
import com.viish.apps.tripletriad.cards.CompleteCardView;
import com.viish.apps.tripletriad.robots.Action;
import com.viish.apps.tripletriad.robots.BotHard;
import com.viish.apps.tripletriad.robots.iBot;
import com.viish.apps.tripletriad.views.ElementView;

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
	public static final int PLAYER = Engine.PLAYER;
	public static final int OPPONENT = Engine.OPPONENT;
	
	public static final int TIME_BEFORE_OPPONENT_MOVE = 1000;
	public static final int TIME_BEFORE_HIDING_EVENT = 1000;
	public static final int TIME_BEFORE_HIDING_RESULT = 3000;
	public static final int TIME_BEFORE_GOING_BACK = 2000;
	public static final int TIME_BEFORE_GOING_BACK_IF_BOT_WON = 4000;
	
	private Handler handler = new Handler();
	private Engine engine;
	
	private Card[] playerDeck, opponentDeck;
	private Card selectedCard;
	private String[] wonCards, loseCards;
	
	private FrameLayout mainLayout;
	private int layoutHeight, layoutWidth, fieldHeight, fieldWidth;
	
	private iBot botOpponent, botPlayerIfDemo;
	
	private boolean isPvp = false;
	private boolean isBotVsBot = false;
	private boolean isRegleRandom, isRegleOpen, isRegleSame, isReglePlus, isRegleCombo, isRegleSameWall, isRegleElementary, isReglePlusWall;
	private RewardRule rewardRule;
	private boolean isWaitingForUserToChooseACard;
	
	public void onCreate(Bundle b) 
    {
        super.onCreate(b);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.game);
        
		initRules();
        
        playerDeck = getRandomDeck(PLAYER);
        opponentDeck = getRandomDeck(OPPONENT);
		
		engine = new Engine(Game.this, playerDeck, isPvp, false, isRegleSame, isReglePlus, isRegleSameWall, isRegleCombo, isReglePlusWall, isRegleElementary, null);
		engine.addEventFiredListener(Game.this);
		
		botOpponent = new BotHard(OPPONENT, PLAYER, opponentDeck, playerDeck, engine.getBoard(), engine.getElements(), isRegleSame, isReglePlus, isRegleSameWall, isRegleCombo, isReglePlusWall, isRegleElementary);
        
        mainLayout = (FrameLayout) findViewById(R.id.cardsLayout);
        final ViewTreeObserver viewTreeObserver = mainLayout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
        	viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	            @Override
	            public void onGlobalLayout() {
	            	displayGame();
	        		mainLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	            }
        	});
        }
    }
	
	private void initRules()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		isRegleRandom = prefs.getBoolean(getString(R.string.pref_rule_random_key), false);
		isRegleOpen = prefs.getBoolean(getString(R.string.pref_rule_open_key), false);
		isRegleSame = prefs.getBoolean(getString(R.string.pref_rule_same_key), false);
		isReglePlus = prefs.getBoolean(getString(R.string.pref_rule_plus_key), false);
		isRegleCombo = prefs.getBoolean(getString(R.string.pref_rule_combo_key), false);
		isRegleSameWall = prefs.getBoolean(getString(R.string.pref_rule_same_wall_key), false);
		isReglePlusWall = prefs.getBoolean(getString(R.string.pref_rule_plus_wall_key), false);
		isRegleElementary = prefs.getBoolean(getString(R.string.pref_rule_element_key), false);
		
		String reward = prefs.getString(getString(R.string.pref_rule_reward_key), "One");
		if (reward.equals("Direct")) {
			rewardRule = RewardRule.Direct;
		}
		else if (reward.equals("All")) {
			rewardRule = RewardRule.All;
		}
		else {
			rewardRule = RewardRule.One;
		}
	}
	
	private void displayGame() {
    	layoutWidth = mainLayout.getWidth();
    	layoutHeight = mainLayout.getHeight();
    	fieldWidth = layoutWidth / 2;
    	fieldHeight = layoutHeight;
    	
        showDeck(PLAYER, playerDeck);
        showDeck(OPPONENT, opponentDeck);
        
        if (isRegleElementary) {
			drawElements();
		}
        	
        startGame();
	}
	
	private void startGame() {	
		engine.startGame();
		int startingPlayer = engine.getStartingPlayer();
		
		if (!isPvp)
		{
			if (startingPlayer == OPPONENT) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(TIME_BEFORE_OPPONENT_MOVE);
						} catch (InterruptedException e) {}
						
						playOpponentBotMove(botOpponent);
					}
				}).start();
			}
			else if (startingPlayer == PLAYER && isBotVsBot) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(TIME_BEFORE_OPPONENT_MOVE);
						} catch (InterruptedException e) {}

						playOpponentBotMove(botPlayerIfDemo);
					}
				}).start();
			}
		}
	}

	/** Create a deck with random cards */
	private Card[] getRandomDeck(int player)
	{
		int howMuch = (Engine.BOARD_SIZE + 1) / 2;
		Card[] deck = new Card[howMuch];
		
		DatabaseStream dbs = new DatabaseStream(this);
		ArrayList<Card> cards;
		if (player == PLAYER) {
			cards = dbs.getXRandomCards(null, howMuch); //FIXME getXRandomMyCards;
		} else {
			cards = dbs.getXRandomCards(null, howMuch);
		}
		dbs.close();

		for (int c = 0; c < cards.size(); c++)
		{
			deck[c] = cards.get(c);			
		}
		
		return deck;
	}
	
	/** Display player's cards (left of the screen if player == 1, else at right of the screen) */
	private void showDeck(int player, Card[] deck)
    {    	
    	FrameLayout fl = (FrameLayout) findViewById(R.id.cardsLayout);    	
    	for (int i = 0; i < deck.length; i++)
    	{
    		CompleteCardView cardView = new CompleteCardView(this, deck[i]);
    		deck[i].setCardView(cardView);
    		deck[i].setColor(player);
    		cardView.resizePictures(layoutWidth / 2 / 3, layoutHeight / 4);
    		
    		if (player == OPPONENT)
    		{
	    		cardView.move(7 * layoutWidth / 8 - cardView.getBitmap().getWidth() / 2, i * (layoutHeight / 6) + ((layoutHeight - ((layoutHeight / 6) * 5)) / 6));
	        	fl.addView(cardView);
	        	if (!isRegleOpen) {
	        		deck[i].flipCard();
	        	}
    		}
    		else if (player == PLAYER)
    		{
    			try
    			{
    				cardView.move(layoutWidth / 8 - cardView.getBitmap().getWidth() / 2, i * (layoutHeight / 6) + ((layoutHeight - ((layoutHeight / 6) * 5)) / 6));
    				fl.addView(cardView);
    			}
    			catch (Exception e) { e.printStackTrace(); }
    		}
    	}
    }
	
	public boolean onTouchEvent(MotionEvent event)
	{
		if (engine == null || !engine.isGameStarted()) {
			return false;
		}
		
		if (engine.isGameOver() && isWaitingForUserToChooseACard)
		{
			Card chosenCard = null;
			for (Card card : opponentDeck)
	    	{
	    		CompleteCardView cardView = card.getView();
	    		if (event.getX() >= cardView.getPositionX() && event.getX() <= cardView.getPositionX() + cardView.getRealWidth())
	    		{
	    			if (event.getY() >= cardView.getPositionY() && event.getY() <= cardView.getPositionY() + cardView.getRealHeight())
	    			{
	    				chosenCard = card;
	    				break;
	    			}
	    		}
	    	}
			
			if (chosenCard != null) {
				chosenCard.swapColor();
				isWaitingForUserToChooseACard = false;
				oneCardRewardChoosed(chosenCard);
				return true;
			}
			return false;
		}
		else if (!engine.isGameOver() && engine.isPlayerTurn() && !isBotVsBot)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
	    	{
	    		for (Card card : playerDeck)
		    	{
		    		CompleteCardView cardView = card.getView();
		    		if (event.getX() >= cardView.getPositionX() && event.getX() <= cardView.getPositionX() + cardView.getRealWidth())
		    		{
		    			if (event.getY() >= cardView.getPositionY() && event.getY() <= cardView.getPositionY() + cardView.getRealHeight())
		    			{
		    				if (!card.isPlayed() && !card.equals(selectedCard)) 
		    				{
		    					if (card.isSelected())
		    					{
		    						unselectCards(PLAYER);
		    						selectedCard = null;
		    					}
		    					else
		    					{
			    					unselectCards(PLAYER);
		    						selectCard(card);
		    						selectedCard = card;
		    					}
		    		    		return true;
		    				}
		    			}
		    		}
		    	}
	    		return false;
	    	}
			else if (event.getAction() == MotionEvent.ACTION_UP)
			{
				if (selectedCard == null && !engine.isGameOver())
				{
					Card selectedCard = null;
					for (Card card : playerDeck)
					{
						if (card.isSelected())
						{
							selectedCard = card;
						}
					}
					
					if (selectedCard != null)
					{
						if (event.getX() >= (layoutWidth / 4) && event.getX() <= (3 * (layoutWidth / 4)))
	    				{
    						int boardX = ((int) ((event.getY()) / (fieldHeight / 3)));
    						int boardY = ((int) ((event.getX() - (layoutWidth / 4)) / (fieldWidth / 3)));
    						
    						int cell = getCell(boardX, boardY);
    						if (engine.isCellEmpty(cell))
    						{
    							moveCardToTargetCell(selectedCard, boardX, boardY);
    							engine.playCard(PLAYER, selectedCard, cell);
    							
    							if (!engine.isGameOver() && !isPvp) {
    								new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												Thread.sleep(TIME_BEFORE_OPPONENT_MOVE);
											} catch (InterruptedException e) {}
											
											playOpponentBotMove(botOpponent);
										}
									}).start();
    							}
    							else if (engine.isGameOver()) {
    								finishGame();
    							}
    						}
	    				}
			    		return true;
					}
				}
				selectedCard = null;
	    		return true;
			}
		}
    	return false;
	}
	
	private void moveCardToTargetCell(Card card, int x, int y) {
		int screenX = (layoutWidth / 4) + (y * (fieldWidth / 3));
    	int screenY = x * (fieldHeight / 3);
    	
    	card.getView().resizePictures(fieldWidth / 3, fieldHeight / 3);
    	card.setSelected(false);
    	card.getView().move(screenX, screenY);
	}
	
	// Assert : we are in a separated thread
	private void playOpponentBotMove(final iBot bot)
	{
		Action move = bot.nextMove();
		final Card card = move.getCard();
		final int cell = move.getCell();
		

		handler.post(new Runnable() {
			@Override
			public void run() {
				if (!card.isFaceUp()) { 
					card.flipCard();
				}
				
				int boardX = cell / 3;
				int boardY = cell % 3;
				moveCardToTargetCell(card, boardX, boardY);  
				engine.playCard(bot.getPlayerValue(), card, cell);
				
				if (engine.isGameOver()) {
					finishGame();
				}
			}
		}); 		
		
		if (isBotVsBot && !engine.isGameOver())
		{
			iBot botToPlay;
			if (bot.equals(botOpponent)) {
				botToPlay = botPlayerIfDemo;
			}
			else {
				botToPlay = botOpponent;
			}
			
			try {
				Thread.sleep(TIME_BEFORE_OPPONENT_MOVE);
			} catch (InterruptedException e) {}
			
			playOpponentBotMove(botToPlay);
		}
	}
	
	private void finishGame()
	{
		FrameLayout fl = (FrameLayout) findViewById(R.id.cardsLayout);
		ImageView result = new ImageView(this);
		
		int playerScore = engine.getPlayerScore();
		int opponentScore = engine.getOpponentScore();
		if (playerScore > opponentScore) {
			result.setImageResource(R.drawable.win);
		}
		else if (playerScore == opponentScore) {
			result.setImageResource(R.drawable.draw);
		}
		else {
			result.setImageResource(R.drawable.lose);
		}
		
		fl.addView(result);
		hideViewAfterAndRun(result, TIME_BEFORE_HIDING_RESULT, new Runnable() {
			@Override
			public void run() {
				displayRewardScreen();
			}
		});
	}
	
	private void displayRewardScreen() {
		if (isBotVsBot)
		{
			setResult(RESULT_OK);
			finish();
			return;
		}
		
		ImageView background = (ImageView) findViewById(R.id.background);
		background.setImageResource(R.drawable.reward);
		
		int winner = 0;
		if (engine.getPlayerScore() > engine.getOpponentScore())
		{
			winner = PLAYER;
			DatabaseStream dbs = new DatabaseStream(this);
			dbs.setGils(dbs.getGils() + Engine.GILS_WIN);
			dbs.close();
		}
		else if (engine.getOpponentScore() > engine.getPlayerScore())
		{
			winner = OPPONENT;
			DatabaseStream dbs = new DatabaseStream(this);
			dbs.setGils(dbs.getGils() + Engine.GILS_LOOSE);
			dbs.close();
		}
		else // Draw
		{
			DatabaseStream dbs = new DatabaseStream(this);
			dbs.setGils(dbs.getGils() + Engine.GILS_DRAW);
			dbs.close();   
			
			if (rewardRule != RewardRule.Direct)
			{
				setResult(RESULT_OK);
				finish();
				return;
			} 		    					
		}
		
		for (Card c : playerDeck) {
			mainLayout.removeView(c.getView());
		}
		for (Card c : opponentDeck) {
			mainLayout.removeView(c.getView());
		}
		
		displayDeckForReward(PLAYER, playerDeck);
		displayDeckForReward(OPPONENT, opponentDeck);
		
		mainLayout.invalidate();
		
		if (rewardRule == RewardRule.All)
			{
				if (winner == PLAYER)
				{
					wonCards = new String[5];
					loseCards = new String[0];
					for (int c = 0; c < opponentDeck.length; c++)
					{
						Card card = opponentDeck[c];
						card.swapColor();
						wonCards[c] = card.getFullName();
					}
				}
				else if (winner == OPPONENT)
				{
					wonCards = new String[0];
					loseCards = new String[5];
					for (int c = 0; c < playerDeck.length; c++)
					{
						Card card = playerDeck[c];
						card.swapColor();
						loseCards[c] = card.getFullName();
					}
				}
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						endReward();
					}
				}, TIME_BEFORE_GOING_BACK_IF_BOT_WON);
			}
			else if (rewardRule == RewardRule.Direct)
			{
				int iLose = 0;
				loseCards = new String[5];
				int iWon = 0;
				wonCards = new String[5];
				
				for (int c = 0; c < playerDeck.length; c++)
				{
					Card card = playerDeck[c];
					if (card.getColor() != card.getRewardDirectColor())
					{
						card.swapColor();
						loseCards[iLose] = card.getFullName();
						iLose++;
					}
				}
				for (int c = 0; c < opponentDeck.length; c++)
				{
					Card card = opponentDeck[c];
					if (card.getColor() != card.getRewardDirectColor())
					{
						card.swapColor();
						wonCards[iWon] = card.getFullName();
						iWon++;
					}
				}
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						endReward();
					}
				}, TIME_BEFORE_GOING_BACK_IF_BOT_WON);
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
	
	private void oneCardRewardChoosed(final Card card)
	{				
		if (isPvp) {
			engine.sendLostCard(card);
		}

		wonCards = new String[1];
		wonCards[0] = card.getFullName();
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				endReward();
			}
		}, TIME_BEFORE_GOING_BACK);
	}
	
	private void botChooseBetterCard()
	{
		loseCards = new String[1];
		Card loseC = playerDeck[0]; 
		for (Card card : playerDeck)
		{
			if (card.getLevel() > loseC.getLevel() || (card.getLevel() == loseC.getLevel() && card.getTotal() > loseC.getTotal())) {
				loseC = card;
			}
		}
		loseCards[0] = loseC.getFullName();
		loseC.swapColor();

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				endReward();
			}
		}, TIME_BEFORE_GOING_BACK_IF_BOT_WON);
	}
	
	private void endReward()
	{
		DatabaseStream dbs = new DatabaseStream(this);
		if (wonCards != null)
		{
			for (String cardFullName : wonCards)
			{
				if (cardFullName != null && cardFullName != "") {
					dbs.nouvelleCarte(cardFullName);
				}
			}
		}
		if (loseCards != null)
		{
			for (String cardFullName : loseCards)
			{
				if (cardFullName != null && cardFullName != "") {
					dbs.supprimerCarte(cardFullName);
				}
			}
		}
		dbs.close();
		
		setResult(RESULT_OK);
		finish();
	}
	
	private void displayDeckForReward(int player, Card[] deck) {
		for (int i = 0; i < deck.length; i++)
    	{
			Card card = deck[i];
    		CompleteCardView cardView = card.getView();
    		card.resetBonusMalusIfNeeded();
			mainLayout.addView(cardView);
    		cardView.resizePictures(fieldWidth / 3, fieldHeight / 3);
    		
    		if (rewardRule == RewardRule.Direct) {
    			card.setRewardDirectColor(card.getColor());
    		}
    		
    		card.setColor(player);
			if (!card.isFaceUp()) {
				card.flipCard();
			}

			int marge = ((layoutWidth / 5) - cardView.getRealWidth()) / 2;
    		if (player == PLAYER)
    		{
    			cardView.move((i * (layoutWidth / 5)) + marge, (layoutHeight / 4) - (cardView.getRealHeight() / 2));
    		}
    		else if (player == OPPONENT)
    		{
    			cardView.move((i * (layoutWidth / 5)) + marge, (3 * layoutHeight / 4) - (cardView.getRealHeight() / 2));
    		}
    	}
	}

	private int getCell(int x, int y)
	{
		return y + 3 * x;
	}
	
	private void selectCard(Card card)
	{
		card.getView().move(card.getView().getPositionX() + card.getView().getRealWidth() / 3, card.getView().getPositionY());
		card.setSelected(true);
	}
	
	private void unselectCards(int joueur)
	{
		if (joueur == PLAYER)
		{
			for (Card card : playerDeck)
			{
				if (card.isSelected()) {
					card.getView().move(card.getView().getPositionX() - card.getView().getRealWidth() / 3, card.getView().getPositionY());
				}
				card.setSelected(false);
			}
		}
		else if (joueur == OPPONENT)
		{
			for (Card card : opponentDeck)
			{
				if (card.isSelected()) {
					card.getView().move(card.getView().getPositionX() - card.getView().getRealWidth() / 3, card.getView().getPositionY());
				}
				card.setSelected(false);
			}
		}
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
			
			String[] elements = engine.getElements();
			FrameLayout fl = (FrameLayout) findViewById(R.id.cardsLayout);
			
			for (int e = 0; e < Engine.BOARD_SIZE; e++)
			{
				String element = elements[e];
				Bitmap bm = bitmapsElements.get(element);
				
				if (bm != null)
				{
					int x = (layoutWidth / 4) + ((e % 3) * (fieldWidth / 3)) + ((fieldWidth /3) / 2) - (bm.getWidth() / 2);
			    	int y = ((e / 3) * fieldHeight / 3) + ((fieldHeight /3) / 2) - (bm.getHeight() / 2);
					ElementView elementView = new ElementView(this, bm, x, y);
					fl.addView(elementView);
				}
			}
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	private void hideViewAfterAndRun(final View view, int after, final Runnable runnable)
	{
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mainLayout.removeView(view);
				
				if (runnable != null) {
					handler.post(runnable);
				}
			} 
		}, after);
	}
	
	private void hideViewAfter(final View view, int after) {
		hideViewAfterAndRun(view, after, null);
	}
	
	private void displayEvent(final int resource, final int text) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				try
				{
					ImageView event = new ImageView(Game.this);
					event.setImageResource(resource);
					mainLayout.addView(event);
					
					hideViewAfter(event, TIME_BEFORE_HIDING_EVENT);
				}
				catch (Exception e)
				{
					Toast.makeText(Game.this, getString(text), TIME_BEFORE_HIDING_EVENT).show();
				}
			}
		});
	}
	
	@Override
	public void eventSameWallTriggered() {
		displayEvent(R.drawable.samewall, R.string.event_same_wall);
	}

	@Override
	public void eventSameTriggered() {
		displayEvent(R.drawable.same, R.string.event_same);
	}

	@Override
	public void eventPlusWallTriggered() {
		displayEvent(R.drawable.pluswall, R.string.event_plus_wall);
	}

	@Override
	public void eventPlusTriggered() {
		displayEvent(R.drawable.plus, R.string.event_plus);
	}

	@Override
	public void eventComboTriggered() {
		displayEvent(R.drawable.combo, R.string.event_combo);
	}

	@Override
	public void eventOpponentPlayed(Action move) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventPvPGameReadyToStart(Card[] opponentDeck) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventOpponentChoosedReward(final Card card) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				card.swapColor();
				
				loseCards = new String[1];
				loseCards[0] = card.getFullName();
				
				endReward();
			}
		});
	}

}
