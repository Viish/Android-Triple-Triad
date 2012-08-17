package com.viish.apps.tripletriad;

import java.util.EventListener;

import com.viish.apps.tripletriad.cards.Card;
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
public interface EventFiredListener extends EventListener
{
	void eventSameWallTriggered();
	void eventPlusWallTriggered();
	void eventSameTriggered();
	void eventPlusTriggered();
	void eventComboTriggered();
	void eventOpponentPlayed(Action move);
	void eventPvPGameReadyToStart(Card[] opponentDeck);
	void eventOpponentChoosedReward(Card card);
}
