package com.viish.apps.tripletriad;

import java.util.EventListener;

import com.viish.apps.tripletriad.cards.Card;
import com.viish.apps.tripletriad.robots.Action;

public interface EventFiredListener extends EventListener
{
	void eventSameWallTriggered();
	void eventSameTriggered();
	void eventPlusTriggered();
	void eventComboTriggered();
	void eventOpponentPlayed(Action move);
	void eventPvPGameReadyToStart(Card[] opponentDeck);
	void eventOpponentChoosedReward(Card card);
}
