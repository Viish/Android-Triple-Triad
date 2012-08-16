package com.viish.apps.tripletriad.robots;

import com.viish.apps.tripletriad.Log;
import com.viish.apps.tripletriad.cards.Card;

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
public class BotHard implements iBot
{
	public static final int MAX = 10;
	public static final int MIN = -10;
	
	private int ME, HIM;
	private int profondeurMax;
	private Card[] myDeck, hisDeck, board;
	private String[] elements;
	private boolean elementaire, identique, plus, memeMur, combo;
	
	public BotHard(int me, int him, Card[] myDeck, Card[] hisDeck, Card[] board, String[] elements, boolean identique, boolean plus, boolean memeMur, boolean combo, boolean elementaire)
	{	
		ME = me;
		HIM = him;
		profondeurMax = 2;
		
		this.myDeck = myDeck;
		this.hisDeck = hisDeck;
		this.board = board;
		this.elements = elements;
		this.identique = identique;
		this.plus = plus;
		this.memeMur = memeMur;
		this.combo = combo;
		
		this.elementaire = elementaire;
	}

	@Override
	public Action nextMove() {
		Card carteAJouer = null;
		int caseOuJouer = -1;
		
		int cardOnBoard = 0;
		for (int i = 0; i < board.length; i++) {
			if (board[i] != null) {
				cardOnBoard++;
			}
		}
		if (cardOnBoard < 3) {
			profondeurMax = 1;
		} else if (cardOnBoard < 5) {
			profondeurMax = 2;
		} else {
			profondeurMax = 3;
		}
		
		int gainMax = MIN;
		int gain = 0;
		
		for (int c = 0; c < myDeck.length; c++) { 
			if (myDeck[c] != null) {
				if (!myDeck[c].isPlayed()) {
					for (int slot = 0; slot < board.length; slot++) {
						if (board[slot] == null) { // For each empty space on the board
							Card[] copyBoard = clone(board);
							Card card = myDeck[c];
							card.lock();
							
							simulate(ME, card, slot, copyBoard);
							gain = min(copyBoard, profondeurMax);
							
							if (gain > gainMax) {
								gainMax = gain;
								carteAJouer = card;
								caseOuJouer = slot;
							}
	
							card.unlock();
						}
					}
				}
			}
		}

		return new Action(carteAJouer, caseOuJouer, gain);
	}
	
	private void simulate(int player, Card card, int slot, Card[] board) {
		MinMaxEngine engine = new MinMaxEngine(board, elements, identique, plus, memeMur, combo, elementaire);
		engine.playCard(player, card, slot);
	}
	
	private int min(Card[] board, int profondeur) {
		if (profondeur == 0 || isBoardFull(board)) {
			return eval(board);
		}
		
		int gainMin = MAX;
		int gain;
		
		for (int c = 0; c < hisDeck.length; c++) { 
			if (hisDeck[c] != null) {
				if (!hisDeck[c].isPlayed()) {
					// For each card I have left
					for (int slot = 0; slot < board.length; slot++) {
						if (board[slot] == null) { // For each empty space on the board
							Card[] copyBoard = clone(board);
							Card card = hisDeck[c];
							card.lock();
							
							simulate(HIM, card, slot, copyBoard);
							gain = max(copyBoard, profondeur - 1);
							
							if (gain < gainMin) {
								gainMin = gain;
							}
	
							card.unlock();
						}
					}
				}
			}
		}
		
		return gainMin;
	}
	
	private int max(Card[] board, int profondeur) {
		if (profondeur == 0 || isBoardFull(board)) {
			return eval(board);
		}
		
		int gainMax = MIN;
		int gain;
		
		for (int c = 0; c < myDeck.length; c++) { 
			if (myDeck[c] != null) {
				if (!myDeck[c].isPlayed()) {
					for (int slot = 0; slot < board.length; slot++) {
						if (board[slot] == null) { // For each empty space on the board
							Card[] copyBoard = clone(board);
							Card card = myDeck[c];
							card.lock();
							
							simulate(ME, card, slot, copyBoard);
							gain = min(copyBoard, profondeur - 1);
							
							if (gain > gainMax) {
								gainMax = gain;
							}
	
							card.unlock();
						}
					}
				}
			}
		}
		
		return gainMax;
	}
	
	private int eval(Card[] board) {
		int eval = 0;
		for (int c = 0; c < board.length; c++) {
			if (board[c] != null) {
				eval += board[c].getCardView().getColor() == ME ? 1 : 0;
			}
		}
		return eval;
	}
	
	private boolean isBoardFull(Card[] board) {
		for (int c = 0; c < board.length; c++) {
			if (board[c] == null) {
				return false;
			}
		}
		return true;
	}
	
	private Card[] clone(Card[] original) {
		Card[] copy = new Card[original.length];
		for (int c = 0; c < original.length; c++) {
			copy[c] = original[c] == null ? null : original[c].clone();
		}
		return copy;
	}

	@Override
	public int getPlayerValue() {
		return ME;
	}
}
