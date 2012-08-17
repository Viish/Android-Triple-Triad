package com.viish.apps.tripletriad;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.viish.apps.tripletriad.cards.Card;
import com.viish.apps.tripletriad.reseau.WifiConnection;
import com.viish.apps.tripletriad.robots.Action;

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
public class Engine
{
	public static final int PLAYER = 1;
	public static final int OPPONENT = 2;
	public static final int BLUE = PLAYER;
	public static final int RED = OPPONENT;
	
	public static final int GILS_WIN = 100;
	public static final int GILS_LOOSE = 10;
	public static final int GILS_DRAW = 50;
	
	public static final int BOARD_SIZE = 9;
	public static final int MAX_ACTIONS = BOARD_SIZE;
	public static final int DEFAULT_PORT = 7666;
	public static final int DEFAULT_TTACC = 300000;
	
	private Activity context;
	private boolean ruleSame, rulePlus, ruleSameWall, ruleCombo, ruleElementary, rulePlusWall;
	private int currentPlayer, startingPlayer;
	private Card[] board, playerDeck, opponentDeck;
	private String[] boardElements;
	private int nbActionsPlayed;
	private final ArrayList<EventFiredListener> listeners = new ArrayList<EventFiredListener>();
	private WifiConnection pvpConnection;
	
	private boolean pvp, isServer, isOpponentReady, gameStarted;
	
	public Engine(Activity c, Card[] playerDeck, boolean pvp, boolean serveur, boolean same, boolean plus, boolean samewall, boolean combo, boolean pluswall, boolean elementary, String pvpServerIp)
	{
		context = c;
		this.playerDeck = playerDeck;
		opponentDeck = new Card[playerDeck.length];
		isServer = serveur;
		this.pvp = pvp;
		isOpponentReady = false;
		gameStarted = false;
		
		ruleSame = same;
		rulePlus = plus;
		ruleSameWall = samewall;
		ruleCombo = combo;
		ruleElementary = elementary;
		rulePlusWall = pluswall;

		nbActionsPlayed = 0;
		board = new Card[BOARD_SIZE];
		
		if (ruleElementary) {
			boardElements = new String[BOARD_SIZE];
		}
		
		if (!pvp || (pvp && isServer))
		{
			int firstToPlay = new Random().nextInt(2) + 1; // Renvoie une valeur au hasard entre 1 et 2
			
			if (ruleElementary) {
				initializeElements();
			}
			
			if (pvp && isServer)
			{
				firstToPlay = PLAYER;
				pvpConnection = new WifiConnection(true, getPort(), getTtacc());
				pvpConnection.addNetworkListener(this);
				new Thread(pvpConnection).start();
			}
			
			currentPlayer = firstToPlay;
			startingPlayer = firstToPlay;
		}
		else if (pvp && !isServer)
		{
			currentPlayer = OPPONENT;
			startingPlayer = OPPONENT;
			
			pvpConnection = new WifiConnection(false, pvpServerIp, getPort(), getTtacc());
			pvpConnection.addNetworkListener(this);
			new Thread(pvpConnection).start();
		}
	}
	
	public void startGame() {
		gameStarted = true;
	}
	
	private int getPort()
	{
		SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
		int port = customSharedPreference.getInt(context.getResources().getString(R.string.pref_port_key), DEFAULT_PORT);
		Log.d("Network Port", "actual value " + port);
		return port;
	}
	
	private int getTtacc()
	{
		SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
		int ttacc = customSharedPreference.getInt(context.getResources().getString(R.string.pref_ttacc_key), DEFAULT_TTACC);
		Log.d("Time to accept client connection", "actual value " + ttacc);
		return ttacc;
	}
	
	public boolean isGameStarted()
	{
		return gameStarted;
	}
	
	public void onErrorOccured() 
	{
		Intent intent = new Intent();
		intent.putExtra("isServer", isServer);
		context.setResult(Activity.RESULT_CANCELED, intent);
		context.finish();
	}
	
	public void onSomethingReceived(String m)
	{
		if (m.startsWith("READY"))
		{
			isOpponentReady = true;
			if (isOpponentDeckFull() && startingPlayer != 0 && !gameStarted)
			{
				sendReady();
				firePvPGameReadyToStart(opponentDeck);
			}
			else if (!gameStarted)
			{
				sendAgain();
			}
		}
		else if (m.startsWith("LOST"))
		{
			String cardFullName = m.split(" ")[1];
			for (Card card : playerDeck)
			{
				if (card.getFullName().equals(cardFullName))
				{
					fireOpponentChoosedReward(card);
					return;
				}
			}
		}
		else if (m.startsWith("AGAIN"))
		{
			sendMyDeck();
			
			if (ruleElementary) {
				sendElementBoard();
			}
		}
		else if (m.startsWith("ACTION"))
		{
			int index = Integer.parseInt(m.split(" ")[1]);
			String cardFullName = m.split(" ")[2];
			for (Card card : opponentDeck)
			{
				if (card.getFullName().equals(cardFullName))
				{
					Action move = new Action(card, index, 0);
					fireOpponentPlayed(move);
					return;
				}
			}			
		}
		else if (m.startsWith("CARD"))
		{
			int index = Integer.parseInt(m.split(" ")[1]);
			String cardFullName = m.split(" ")[2];
			DatabaseStream dbs = new DatabaseStream(context);
			Card card = dbs.getCard(cardFullName);
			dbs.close();
			opponentDeck[index] = card;
			
			if (isOpponentDeckFull() && startingPlayer != 0 && !gameStarted)
			{
				sendReady();
				if (isOpponentReady) {
					firePvPGameReadyToStart(opponentDeck);
				}
			}
		}
		else if (m.startsWith("ELEMENT"))
		{
			int index = Integer.parseInt(m.split(" ")[1]);
			String element = m.split(" ")[2];
			this.boardElements[index] = element;
			
			if (isOpponentDeckFull() && startingPlayer != 0 && !gameStarted)
			{
				sendReady();
				if (isOpponentReady) {
					firePvPGameReadyToStart(opponentDeck);
				}
			}
		}
	}
	
	public void onOtherPlayerConnected()
	{
		if (isServer)
		{
			if (ruleElementary)
			{
				sendElementBoard();
			}
		}	
		sendMyDeck();	
	}
	
	public String getIpAddress()
	{
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
	        {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) 
	            {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) 
	                	return inetAddress.getHostAddress().toString();
	            }
	        }
	    } 
	    catch (SocketException e) 
	    {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	private boolean isOpponentDeckFull()
	{
		for (int c = 0; c < opponentDeck.length; c++)
		{
			if (opponentDeck[c] == null)
			{
				Log.d("IsOpponentDeckFull", "Hell no :'(");
				return false;
			}
		}

		Log.d("IsOpponentDeckFull", "Yep baby !");
		return true;
	}
	
	public void sendLostCard(Card card)
	{
		Log.d("You lost : " + card.getFullName());
		pvpConnection.send("LOST " + card.getFullName());
	}
	
	private void sendAgain()
	{
		Log.d("Send me all again please");
		pvpConnection.send("AGAIN");
	}
	
	private void sendReady()
	{
		Log.d("I'm Ready !");
		pvpConnection.send("READY");
	}
	
	private void sendMyDeck()
	{
		for (int c = 0; c < playerDeck.length; c++)
		{
			Card card = playerDeck[c];
			pvpConnection.send("CARD " + c + " " + card.getFullName());
		}
	}
	
	private void sendElementBoard()
	{
		for (int c = 0; c < boardElements.length; c++)
		{
			String element = boardElements[c];
			if (element != "")
				pvpConnection.send("ELEMENT " + c + " " + element);
		}
	}
	
	public int getStartingPlayer()
	{
		return startingPlayer;
	}
	
	public void shutdownSocket()
	{
		if (pvp) {
			pvpConnection.close();
		}
	}
	
	public void addEventFiredListener(EventFiredListener rtl)
	{
		listeners.add(rtl);
	}
	
	public void fireOpponentChoosedReward(final Card card)
	{
		for(final EventFiredListener listener : getEventFiredListeners()) 
		{
			Runnable play = new Runnable()
			{
				public void run() 
				{
					listener.eventOpponentChoosedReward(card);
				}			
			};
			context.runOnUiThread(play);
		}
	}
	
	public ArrayList<EventFiredListener> getEventFiredListeners() 
	{
        return listeners;
    }
	
	private void fireSameRuleTriggered()
	{
		for(EventFiredListener listener : getEventFiredListeners()) 
		{
            listener.eventSameTriggered();
		}
	}
	
	private void fireSameWallRuleTriggered()
	{
		for(EventFiredListener listener : getEventFiredListeners()) 
		{
            listener.eventSameWallTriggered();
		}
	}
	
	private void firePlusWallRuleTriggered()
	{
		for(EventFiredListener listener : getEventFiredListeners()) 
		{
            listener.eventPlusWallTriggered();
		}
	}
	
	private void firePlusRuleTriggered()
	{
		for(EventFiredListener listener : getEventFiredListeners()) 
		{
            listener.eventPlusTriggered();
		}
	}
	
	private void fireOpponentPlayed(final Action move)
	{
		for(final EventFiredListener listener : getEventFiredListeners()) 
		{
			Runnable play = new Runnable()
			{
				public void run() 
				{
					listener.eventOpponentPlayed(move);
				}			
			};
			context.runOnUiThread(play);
		}		
	}
	
	private void firePvPGameReadyToStart(Card[] deck)
	{
		gameStarted = true;
		for(EventFiredListener listener : getEventFiredListeners()) 
		{
            listener.eventPvPGameReadyToStart(deck);
		}
	}
	
	private void fireComboRuleTriggered()
	{
		for(EventFiredListener listener : getEventFiredListeners()) 
		{
            listener.eventComboTriggered();
		}
	}

	public int getPlayerScore()
	{
		int player = 0;
		for (Card c : board)
		{
			if (c != null && c.getColor() == PLAYER)
				player++;
		}
		
		if (startingPlayer == OPPONENT) { 
			player++;
		}
		return player;
	}

	public int getOpponentScore()
	{
		int opponent = 0;
		for (Card c : board)
		{
			if (c != null && c.getColor() == OPPONENT) {
				opponent++;
			}
		}
		
		if (startingPlayer == PLAYER) {
			opponent++;
		}
		return opponent;
	}
	
	private void initializeElements()
	{
		String[] listeElements = {"Earth", "Fire", "Wind", "Holy", "Poison", "Thunder", "Water", "Ice"};
		Random r = new Random();
		boolean caseElementaire = false;
		
		for (int i = 0; i < BOARD_SIZE; i++)
		{
			int rand = r.nextInt(100);
			if (rand < 30 || (i == 5 && !caseElementaire))
			{
				caseElementaire = true;
				int element = r.nextInt(listeElements.length);
				boardElements[i] = listeElements[element];
			}
			else 
			{
				boardElements[i] = "";
			}
		}
	}
	
	public String[] getElements()
	{
		return this.boardElements;
	}
	
	public boolean isBotPlayingFirst()
	{
		return startingPlayer == OPPONENT;
	}
	
	public int getNbToursRestants()
	{
		return MAX_ACTIONS - nbActionsPlayed;
	}
	
	public boolean isGameOver()
	{
		return getNbToursRestants() == 0;
	}
	
	public Card[] getBoard()
	{
		return board;
	}
	
	public boolean isCellEmpty(int cell)
	{
		if (cell >= BOARD_SIZE) {
			return false;
		}
		
		return board[cell] == null;
	}
	
	public boolean isPlayerTurn()
	{
		return currentPlayer == PLAYER;
	}
	
	public void playCard(int player, Card card, int cell)
	{
		if (pvp && player == PLAYER)
		{
			pvpConnection.send("ACTION " + cell + " " + card.getFullName());
		}
		
		card.lock();
		board[cell] = card;
    	this.nbActionsPlayed += 1;
    	
    	if (currentPlayer == PLAYER) {
    		currentPlayer = OPPONENT;
    	} else {
			currentPlayer = PLAYER;
    	}
    	
		if (ruleElementary) {
			applyElementaireRule(card, cell);
		}
		
    	if (nbActionsPlayed > 1)
    	{
			if (ruleSame) {
				applySameRule(player, card, cell, false);
			}
			
			applyBasicRule(player, card, cell, false);
			
			if (rulePlus) {
				applyPlusRule(player, card, cell, false);
			}
    	}
	}
	
	private void applyBasicRule(int player, Card card, int cell, boolean combo)
	{
		int swapped = 0;
		if (cell % 3 == 0) 
		{
			Card c = board[cell + 1];
			if (c != null)
			{
				if (c.getColor() != player && card.getRightValue() > c.getLeftValue())
				{
					c.swapColor();
					swapped++;
				}
			}
		}
		else if (cell % 3 == 1)
		{
			Card c = board[cell + 1];
			if (c != null)
			{
				if (c.getColor() != player && card.getRightValue() > c.getLeftValue()) 
				{
					c.swapColor();
					swapped++;
				}
			}
			
			c = board[cell - 1];
			if (c != null) 
			{
				if (c.getColor() != player && card.getLeftValue() > c.getRightValue())
				{
					c.swapColor(); 
					swapped++;
				}
			}
		}
		else
		{
			Card c = board[cell - 1];
			if (c != null)
			{
				if (c.getColor() != player && card.getLeftValue() > c.getRightValue()) 
				{
					c.swapColor(); 
					swapped++;
				}
			}
		}
		
		if (cell / 3 == 0)
		{
			Card c = board[cell + 3];
			if (c != null) 
			{
				if (c.getColor() != player && card.getBottomValue() > c.getTopValue()) 
				{
					c.swapColor();
					swapped++;
				}
			}
		}
		else if (cell / 3 == 1)
		{
			Card c = board[cell + 3];
			if (c != null)
			{
				if (c.getColor() != player && card.getBottomValue() > c.getTopValue()) 
				{
					c.swapColor();
					swapped++;
				}
			}
			
			c = board[cell - 3];
			if (c != null) 
			{
				if (c.getColor() != player && card.getTopValue() > c.getBottomValue())
				{
					c.swapColor(); 
					swapped++;
				}
			}
		}
		else
		{
			Card c = board[cell - 3];
			if (c != null) 
			{
				if (c.getColor() != player && card.getTopValue() > c.getBottomValue())
				{
					c.swapColor();
					swapped++;
				}
			}
		}
		
		if (combo && swapped > 0) {
			fireComboRuleTriggered();
		}
	}
	
	private void applySameRule(int player, Card what, int cell, boolean combo)
	{
		ArrayList<Integer> cards = new ArrayList<Integer>();
		int carteAdverse = 0;
		boolean sameWall = false;
		
		if (cell - 3 >= 0 && board[cell - 3] != null)
		{
			if (board[cell - 3].getBottomValue() == what.getTopValue())
			{
				cards.add(cell - 3);
				if (board[cell - 3].getColor() != player) carteAdverse += 1;
			}
		}
		{
			if (10 == what.getTopValue())
			{
				cards.add(-1);
				sameWall = true;
			}
		}
		if (cell + 3 < this.board.length && board[cell + 3] != null)
		{
			if (board[cell + 3].getTopValue() == what.getBottomValue())
			{
				cards.add(cell + 3);
				if (board[cell + 3].getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell + 3 >= this.board.length && ruleSameWall)
		{
			if (10 == what.getBottomValue())
			{
				cards.add(-1);
				sameWall = true;
			}
		}
		if (cell % 3 <= 1 && board[cell + 1] != null)
		{
			if (board[cell + 1].getLeftValue() == what.getRightValue())
			{
				cards.add(cell + 1);
				if (board[cell + 1].getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell % 3 == 2 && ruleSameWall)
		{
			if (10 == what.getRightValue())
			{
				cards.add(-1);
				sameWall = true;
			}
		}
		if (cell % 3 >= 1 && board[cell - 1] != null)
		{
			if (board[cell - 1].getRightValue() == what.getLeftValue())
			{
				cards.add(cell - 1);
				if (board[cell - 1].getColor() != player) carteAdverse += 1;
			}
		}
		else if (cell % 3 == 0 && ruleSameWall)
		{
			if (10 == what.getLeftValue())
			{
				cards.add(-1);
				sameWall = true;
			}
		}
		
		if (cards.size() >= 2 && carteAdverse >= 1)
		{
			ArrayList<Integer> swapped = new ArrayList<Integer>();
			for (int c : cards)
			{
				if (c != -1)
				{
					Card card = board[c];
					if (card.getColor() != player)
					{
						card.swapColor();
						swapped.add(c);
					}
				}
			}
			
			try
			{ 
				if (!combo)
				{
					if (sameWall)  {
						fireSameWallRuleTriggered();
					}
					else { 
						fireSameRuleTriggered();
					}
				}
				else {
					fireComboRuleTriggered();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			if (ruleCombo)
			{
				for (int c : swapped) 
				{
					Card card = board[c];
					applySameRule(player, card, c, true);
					applyBasicRule(player, card, c, true);
					if (rulePlus) {
						applyPlusRule(player, card, c, true);
					}
				}
			}
		}
	}
	
	private void applyPlusRule(int player, Card what, int cell, boolean combo) {
		int[] sums = new int[4];
		int[] cells = new int[4];
		Card[] cards = new Card[4];
		ArrayList<Integer> swapped = new ArrayList<Integer>();
		boolean plusWall = false;
		
		Card rulePlusWallCard = new Card("", 0, 0, "A", "A", "A", "A", "", 1, null, null, null);
		rulePlusWallCard.setColor(player);
		
		cells[0] = cell - 3;
		cells[1] = cell + 3;
		cells[2] = cell + 1;
		cells[3] = cell - 1;
		
		cards[0] = cell - 3 >= 0 ? board[cells[0]] : (rulePlusWall ? rulePlusWallCard : null);
		cards[1] = cell + 3 < board.length ? board[cells[1]] : (rulePlusWall ? rulePlusWallCard : null);
		cards[2] = cell % 3 <= 1 ? board[cells[2]] : (rulePlusWall ? rulePlusWallCard : null);
		cards[3] = cell % 3 >= 1 ? board[cells[3]] : (rulePlusWall ? rulePlusWallCard : null);
		
		sums[0] = cards[0] != null ? what.getTopValue() + cards[0].getBottomValue() : -1;
		sums[1] = cards[1] != null ? what.getBottomValue() + cards[1].getTopValue() : -1;
		sums[2] = cards[2] != null ? what.getRightValue() + cards[2].getLeftValue() : -1;
		sums[3] = cards[3] != null ? what.getLeftValue() + cards[3].getRightValue() : -1;
		
		for (int i = 0; i < sums.length; i++) {
			for (int j = 0; j < sums.length; j++) {
				if (i != j && sums[i] == sums[j] && sums[i] != -1) {					
					// We need at least an opponent card to trigger plus rule
					boolean atLeastOneOpponentCardCondition = false;
					atLeastOneOpponentCardCondition |= (cards[i] != null && cards[i].getColor() != player);
					atLeastOneOpponentCardCondition |= (cards[j] != null && cards[j].getColor() != player);
					
					// We used plusWall instead of plus
					plusWall |= (cards[i] == rulePlusWallCard || cards[j] == rulePlusWallCard);
					
					if (atLeastOneOpponentCardCondition) {
						if (cards[i] != null && cards[i].getColor() != player)
						{
							cards[i].swapColor();
							swapped.add(i);
						}
						if (cards[j] != null && cards[j].getColor() != player) 
						{
							cards[j].swapColor();
							swapped.add(j);
						}
					}
				}
			}
		}
		
		try
		{
			if (swapped.size() >= 1)
			{
				if (combo) {
					fireComboRuleTriggered();
				}
				else if (plusWall) {
					firePlusWallRuleTriggered();
				}
				else {
					firePlusRuleTriggered();
				}
			}
		}
		catch (Exception e)
		{
			return;
		}
		
		if (ruleCombo)
		{
			for (int i : swapped)
			{
				int c = cells[i];
				Card card = cards[i];
				if (ruleSame) {
					applySameRule(player, card, c, true);
				}
				applyBasicRule(player, card, c, true);
				applyPlusRule(player, card, c, true);
			}
		}
	}
	
	private void applyElementaireRule(Card card, int cell)
	{
		if (boardElements[cell] == null || boardElements[cell] == "") {
			return;
		}
		
		if (boardElements[cell].equals(card.getElement())) {
			card.bonusElementaire();
		}
		else {
			card.malusElementaire();
		}
	}
}
