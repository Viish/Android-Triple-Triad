package com.viish.apps.tripletriad;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
public class DatabaseStream 
{
	private static final int STARTING_GILS = 100;
	private SQLiteConnector connector;
	private SQLiteDatabase stream;
	private Context context;
	
	public DatabaseStream(Context c)
	{
		context = c;
		connector = new SQLiteConnector(context, "TripleTriad", 10);
		stream = connector.getWritableDatabase();
	}
	
	public void reinitCards()
	{
		stream.delete("MyCards", null, null);
		initCards();
	}
	
	public void unlockAllCards()
	{
		for (Card c : getAllCards())
		{
			nouvelleCarte(c.getFullName());
		}
	}
	
	public Card getCard(Cursor cursor) {
		int nameColumn = cursor.getColumnIndex("Name");
		int episodeColumn = cursor.getColumnIndex("Episode");
		int levelColumn = cursor.getColumnIndex("Level");
		int topColumn = cursor.getColumnIndex("TopValue");
		int leftColumn = cursor.getColumnIndex("LeftValue");
		int botColumn = cursor.getColumnIndex("BotValue");
		int rightColumn = cursor.getColumnIndex("RightValue");
		int elementColumn = cursor.getColumnIndex("Element");
		
		String name = cursor.getString(nameColumn);
		int episode = cursor.getInt(episodeColumn);
		int level = cursor.getInt(levelColumn);
		String top = cursor.getString(topColumn);
		String left = cursor.getString(leftColumn);
		String bot = cursor.getString(botColumn);
		String right = cursor.getString(rightColumn);
		String element = cursor.getString(elementColumn);
		
		String fullName = "ff" + episode + "/" + name;
		Bitmap blue = null, red = null, back = null;
		try 
		{
			blue = BitmapFactory.decodeStream(context.getResources().getAssets().open(fullName + ".1.png"));
			red = BitmapFactory.decodeStream(context.getResources().getAssets().open(fullName + ".2.png"));
			back = BitmapFactory.decodeStream(context.getResources().getAssets().open("back.png"));	
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		Card card = new Card(name, level, episode, top, left, bot, right, element, 1, blue, red, back);
		return card;
	}
	
	public Card getCard(String cardFullName)
	{
		Card card = null;
		String name = cardFullName.split("/")[1];
		String episodeName = cardFullName.split("/")[0];
		int episode = Integer.parseInt(episodeName.split("ff")[1]);
		
		Cursor result = stream.query("Cards", null, "Name LIKE \"" + name + "\" AND Episode LIKE " + episode, null, null, null, null);
		if (result != null && result.move(1)) {
			card = getCard(result);
		}
		result.close();
		
		return card;
	}
	
	public void close()
	{
		connector.close();
		stream.close();
	}

	public void nouvelleCarte(String cardFullName) 
	{
		Cursor result = stream.query("MyCards", null, "CardName LIKE \"" + cardFullName + "\"", null, null, null, "CardName ASC");
		
		if (result != null && result.move(1)) // Si on en a deja un exemplaire, on augmente de 1 la valeur Number
		{
			int number = 0;
			int numberColumn = result.getColumnIndex("Number");
			number = result.getInt(numberColumn); // Valeur anterieure
			number += 1; // Nouvelle valeur
			
			ContentValues cv = new ContentValues();
			cv.put("CardName", cardFullName);
			cv.put("Number", number);
			
			stream.update("MyCards", cv, "CardName LIKE \"" + cardFullName + "\"", null); // Mise a jour de la table
			
			result.close();
			
		}
		else // Premier exemplaire, on l'ajoute dans MyCards
		{
			ContentValues cv = new ContentValues();
			cv.put("CardName", cardFullName);
			cv.put("Number", 1);
			stream.insert("MyCards", null, cv);
			
			if (result != null)
			{
				result.close();
			}
		}
	}
	
	public Card getRandomCard(int level)
	{
		Card card = null;
		Cursor result = stream.query("Cards", null, "Level LIKE " + level + " AND Episode NOT LIKE 0", null, null, null, "Name ASC");
		int randomInt = new Random().nextInt(result.getCount());
		if (result != null && result.move(randomInt)) {
			card = getCard(result);
		}
		result.close();
		
	    return card;
	}

	public ArrayList<Card> getXRandomCards(String cond, int howMuch)
	{
		ArrayList<Card> cards = new ArrayList<Card>();
		
		Cursor result = stream.query("Cards", null, cond, null, null, null, "Name ASC");
		if (result != null)
		{
			int[] randoms = new int[howMuch];
	    	Random random = new Random();
	    	for (int i = 0; i < howMuch; i++)
	    	{
	    		boolean existeDeja = false;
	    		int rand = random.nextInt(result.getCount() - 1) + 1;
	    		
	    		for (int j = 0; j < i; j++)
	    		{
	    			if (randoms[j] == rand) existeDeja = true;
	    		}
	    		
	    		if (existeDeja) i -= 1;
	    		else randoms[i] = rand;
	    	}
	    	Arrays.sort(randoms);
    		
    		for (int i = 0; i < howMuch; i++)
    		{
    			result.move(randoms[i]);
    			Card card = getCard(result);
				cards.add(card);
    			result.moveToFirst();
    		}
    		
    		result.close();
		}
		
		return cards;
	}
	
	public ArrayList<Card> getXRandomMyCards(int howMuch)
	{
		ArrayList<Card> cards = new ArrayList<Card>();
		
		Cursor result = stream.query("MyCards", null, null, null, null, null, "Name ASC");
		if (result != null)
		{
			int[] randoms = new int[howMuch];
	    	Random random = new Random();
	    	for (int i = 0; i < howMuch; i++)
	    	{
	    		boolean existeDeja = false;
	    		int rand = random.nextInt(result.getCount() - 1) + 1;
	    		
	    		for (int j = 0; j < i; j++)
	    		{
	    			if (randoms[j] == rand) existeDeja = true;
	    		}
	    		
	    		if (existeDeja) i -= 1;
	    		else randoms[i] = rand;
	    	}
	    	Arrays.sort(randoms);
    		
    		for (int i = 0; i < howMuch; i++)
    		{
    			result.move(randoms[i]);
    			Card card = getCard(result);
				cards.add(card);
    			result.moveToFirst();
    		}
    		
    		result.close();
		}
		
		return cards;
	}
	
	public ArrayList<Card> getAllCards(int filterByLevel)
	{
		ArrayList<Card> cards = new ArrayList<Card>();
		
		String selection = null;
		if (filterByLevel > -1) {
			selection = "Level LIKE " + filterByLevel;
		}
		
		Cursor result = stream.query("Cards", null, selection, null, null, null, "Level ASC");
		if (result != null)
		{
			while (result.move(1))
			{
				Card card = getCard(result);
				cards.add(card);
			}
		}
		result.close();
		
		return cards;
	}
	
	public ArrayList<Card> getAllCards()
	{
		return getAllCards(-1);
	}

	public ArrayList<Card> getMyCards() {
		return getMyCards(-1);
	}
	
	public ArrayList<Card> getMyCards(int filterByLevel) 
	{
		ArrayList<Card> cards = new ArrayList<Card>();
		
		Cursor result = stream.query("MyCards", null, null, null, null, null, "CardName DESC");
		if (result != null && result.move(1))
		{
			do
			{
				int cardNameColumn = result.getColumnIndex("CardName");
				int howMuchColumn = result.getColumnIndex("Number");
				
				String fullName = result.getString(cardNameColumn);
				int howMuch = result.getInt(howMuchColumn);
				String cardName = fullName.split("/")[1];
				int episode = Integer.parseInt(fullName.split("/")[0].split("ff")[1]);
				
				String selection = "";
				if (filterByLevel > -1) {
					selection = " AND Level LIKE " + filterByLevel;
				}
				
				Cursor result2 = stream.query("Cards", null, "Name LIKE \"" + cardName + "\" AND Episode LIKE " + episode + selection, null, null, null, "Name ASC");
				if (result2 != null && result2.move(1))
				{
					Card card = getCard(result2);
					card.setNumber(howMuch);
	    			cards.add(card);	    
	    			result2.close();	    			
				}
				if (result2 != null)
				{
	    			result2.close();
				}
			}
			while (result.move(1));
		}
		if (result != null)
		{
			result.close();
		}
		
		return cards;
	}

	public void supprimerCarte(String cardFullName) 
	{
		Cursor result = stream.query("MyCards", null, "CardName LIKE \"" + cardFullName + "\"", null, null, null, "CardName ASC");
		
		if (result != null && result.move(1)) // Si on en a deja un exemplaire, on diminue de 1 la valeur Number, si Number tombe a zero, on supprime toute l'entree
		{
			int number = 0;
			int numberColumn = result.getColumnIndex("Number");
			number = result.getInt(numberColumn); // Valeur anterieure
			number -= 1; // Nouvelle valeur
			
			if (number > 0)
			{
				ContentValues cv = new ContentValues();
				cv.put("CardName", cardFullName);
				cv.put("Number", number);
				
				stream.update("MyCards", cv, "CardName LIKE \"" + cardFullName + "\"", null); // Mise a jour de la table
				
				result.close();
			}
			else
			{
				stream.delete("MyCards", "CardName LIKE \"" + cardFullName + "\"", null); // Suppresion de la ligne
				
				result.close();
			}
		}
	}
	
	public int getGils()
	{
		SharedPreferences lastSettings = context.getSharedPreferences("TripleTriad", Context.MODE_PRIVATE);
		int gils = lastSettings.getInt("Gils", 0);
		return gils;
	}
	
	public void setGils(int gils)
	{
		SharedPreferences lastSettings = context.getSharedPreferences("TripleTriad", Context.MODE_PRIVATE);
		Editor editor = lastSettings.edit();
		editor.putInt("Gils", gils);
		editor.apply();
	}
	
	public void initCards() 
	{
		for (int i = 1; i <= 10; i++)
		{
			int howMuch = 0;
			if (i >= 7 && i <= 10) howMuch = 1;
			else if (i >= 5 && i <= 6) howMuch = 2;
			else if (i >= 2 && i<= 4) howMuch = 3;
			else howMuch = 4;
			
			ArrayList<Card> cards = new ArrayList<Card>();
			cards = getXRandomCards("Level LIKE " + i + " AND Episode NOT LIKE 0", howMuch);
			
			for (Card c : cards)
			{
				nouvelleCarte(c.getFullName());
			}
		}
		
		SharedPreferences lastSettings = context.getSharedPreferences("TripleTriad", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = lastSettings.edit();
		editor.putBoolean("FirstTime", false);
		editor.putInt("Gils", STARTING_GILS); // 100 Gils au depart
		editor.commit();
	}

	public void unlockAllDissidiaCards() 
	{
		Cursor result = stream.query("Cards", null, "Episode LIKE 0", null, null, null, null);
		
		if (result != null)
		{
			while (result.move(1))
	    	{
	    		int nameColumn = result.getColumnIndex("Name");
				String name = result.getString(nameColumn);
				
				nouvelleCarte("ff0/" + name);
	    	}
			result.close();
		}
	}
}

class SQLiteConnector extends SQLiteOpenHelper 
{
	public SQLiteConnector(Context context, String databaseName, int databaseVersion)
	{
		super(context, databaseName, null, databaseVersion);
	}
	
	public void onCreate(SQLiteDatabase db) 
	{
		createDBs(db, false);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS Cards");
		createDBs(db, true);
	}
	
	private void createDBs(SQLiteDatabase db, boolean upgrade)
	{
		if (!upgrade) 
		{
			db.execSQL("CREATE TABLE MyCards (CardName TEXT NOT NULL, Number INTEGER NOT NULL, PRIMARY KEY (CardName))");
		}
		
		db.execSQL("CREATE TABLE Cards (Name TEXT NOT NULL, Episode INTEGER NOT NULL, Level INTEGER NOT NULL, TopValue TEXT NOT NULL, LeftValue TEXT NOT NULL, BotValue TEXT NOT NULL, RightValue TEXT NOT NULL, Element TEXT NOT NULL, PRIMARY KEY (Name, Episode))");
		db.execSQL("INSERT INTO Cards VALUES (\"Levrikon\", 7, 1, \"1\", \"4\", \"2\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Mu\", 7, 1, \"3\", \"2\", \"2\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"SwordDance\", 7, 1, \"5\", \"2\", \"3\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Mandragora\", 7, 1, \"1\", \"2\", \"6\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Formula\", 7, 1, \"1\", \"5\", \"2\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Kalm\", 7, 1, \"4\", \"4\", \"2\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"1stRay\", 7, 1, \"1\", \"1\", \"4\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Cockatrice\", 7, 1, \"2\", \"5\", \"3\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"DorkyFace\", 7, 1, \"2\", \"1\", \"6\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"DevilRide\", 7, 1, \"4\", \"1\", \"5\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MP\", 7, 1, \"2\", \"1\", \"3\", \"6\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Aphide\", 8, 1, \"2\", \"4\", \"4\", \"1\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Bogomile\", 8, 1, \"1\", \"5\", \"1\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Carnidea\", 8, 1, \"2\", \"1\", \"6\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Diodon\", 8, 1, \"3\", \"1\", \"2\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Elastos\", 8, 1, \"1\", \"1\", \"4\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Fungus\", 8, 1, \"5\", \"3\", \"1\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Gallus\", 8, 1, \"2\", \"6\", \"1\", \"2\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Incube\", 8, 1, \"2\", \"5\", \"1\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Larva\", 8, 1, \"4\", \"3\", \"4\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Nocturnus\", 8, 1, \"6\", \"2\", \"1\", \"1\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Aeon\", 10, 1, \"1\", \"2\", \"2\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Piranha\", 10, 1, \"1\", \"1\", \"4\", \"3\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Garm\", 10, 1, \"3\", \"3\", \"4\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Nebiros\", 10, 1, \"5\", \"2\", \"1\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Fungus\", 10, 1, \"1\", \"3\", \"1\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"FlambosEau\", 10, 1, \"1\", \"2\", \"4\", \"4\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MachineAlbhed\", 10, 1, \"5\", \"4\", \"2\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Alcione\", 10, 1, \"4\", \"4\", \"2\", \"2\", \"Wind\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ElementaireBlanc\", 10, 1, \"5\", \"1\", \"2\", \"2\", \"Ice\")");
		db.execSQL("INSERT INTO Cards VALUES (\"FlambosFoudre\", 10, 1, \"5\", \"5\", \"1\", \"1\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ElementaireDore\", 10, 1, \"3\", \"2\", \"3\", \"4\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ElementaireRouge\", 10, 1, \"4\", \"2\", \"1\", \"6\", \"Fire\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"AttackSquad\", 7, 2, \"7\", \"2\", \"1\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Bomb\", 7, 2, \"7\", \"4\", \"1\", \"1\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Custom\", 7, 2, \"4\", \"2\", \"6\", \"6\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Grangalan\", 7, 2, \"5\", \"4\", \"3\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Grunt\", 7, 2, \"6\", \"2\", \"4\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Snow\", 7, 2, \"4\", \"4\", \"5\", \"2\", \"Ice\")");
		db.execSQL("INSERT INTO Cards VALUES (\"TouchMe\", 7, 2, \"5\", \"3\", \"5\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ArkDragon\", 7, 2, \"2\", \"5\", \"3\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"UnderLizard\", 7, 2, \"5\", \"2\", \"5\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"BlackBat\", 7, 2, \"4\", \"4\", \"5\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Deenglow\", 7, 2, \"3\", \"2\", \"2\", \"7\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Arcanada\", 8, 2, \"5\", \"5\", \"3\", \"1\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Feng\", 8, 2, \"4\", \"2\", \"5\", \"4\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Formicide\", 8, 2, \"5\", \"2\", \"5\", \"2\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Hera\", 8, 2, \"3\", \"7\", \"1\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Koatl\", 8, 2, \"3\", \"3\", \"5\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Licorne\", 8, 2, \"5\", \"4\", \"3\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Malaku\", 8, 2, \"5\", \"5\", \"2\", \"3\", \"Wind\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Orchida\", 8, 2, \"7\", \"1\", \"3\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Schizoid\", 8, 2, \"6\", \"3\", \"2\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Selek\", 8, 2, \"5\", \"3\", \"5\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Xilopode\", 8, 2, \"6\", \"3\", \"1\", \"4\", \"Ice\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Malbernardo\", 10, 3, \"6\", \"6\", \"3\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"FlambosFeu\", 10, 3, \"4\", \"5\", \"5\", \"3\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Kusarik\", 10, 3, \"4\", \"4\", \"5\", \"5\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Thytan\", 10, 3, \"5\", \"5\", \"5\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Chimera\", 10, 3, \"5\", \"5\", \"4\", \"4\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"VerDesSables\", 10, 3, \"5\", \"6\", \"2\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Anacondar\", 10, 3, \"4\", \"4\", \"4\", \"4\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Larva\", 10, 3, \"5\", \"2\", \"6\", \"3\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Cougar\", 10, 3, \"6\", \"6\", \"2\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Spectre\", 10, 3, \"1\", \"7\", \"6\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Adamanthart\", 10, 3, \"6\", \"6\", \"4\", \"2\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Behemot\", 10, 3, \"7\", \"7\", \"1\", \"3\", \"Thunder\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Zem\", 7, 3, \"3\", \"6\", \"6\", \"2\", \"Wind\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Jemnezmy\", 7, 3, \"6\", \"3\", \"6\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ArmoredGolem\", 7, 3, \"5\", \"5\", \"3\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Cactuar\", 7, 3, \"7\", \"5\", \"1\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"DeathDealer\", 7, 3, \"2\", \"7\", \"5\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"CrazySaw\", 7, 3, \"3\", \"5\", \"3\", \"6\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Hochu\", 7, 3, \"5\", \"6\", \"4\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MightyGrunt\", 7, 3, \"7\", \"4\", \"2\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"RouletteCannon\", 7, 3, \"2\", \"6\", \"6\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"DesertSahagin\", 7, 3, \"6\", \"3\", \"4\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Goblin\", 7, 3, \"7\", \"2\", \"5\", \"3\", \"Earth\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Adephage\", 8, 3, \"3\", \"5\", \"5\", \"5\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Barbarian\", 8, 3, \"5\", \"4\", \"6\", \"2\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Berseker\", 8, 3, \"4\", \"2\", \"7\", \"4\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Celebis\", 8, 3, \"7\", \"5\", \"3\", \"2\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Pampa\", 8, 3, \"6\", \"3\", \"6\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Phantom\", 8, 3, \"7\", \"3\", \"1\", \"5\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Satyrux\", 8, 3, \"7\", \"3\", \"5\", \"1\", \"Ice\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Scavenger\", 8, 3, \"6\", \"6\", \"3\", \"1\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Tomberry\", 8, 3, \"3\", \"4\", \"4\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Trogiidae\", 8, 3, \"5\", \"3\", \"3\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Weevil\", 8, 3, \"6\", \"2\", \"3\", \"6\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Sahagin\", 10, 2, \"7\", \"1\", \"2\", \"2\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Octopus\", 10, 2, \"6\", \"3\", \"3\", \"2\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Epej\", 10, 2, \"5\", \"6\", \"1\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Galkimasera\", 10, 2, \"6\", \"3\", \"4\", \"1\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Arhiman\", 10, 2, \"4\", \"3\", \"3\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ElementaireBleu\", 10, 2, \"6\", \"1\", \"2\", \"5\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Echenis\", 10, 2, \"5\", \"5\", \"1\", \"3\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Balsamine\", 10, 2, \"4\", \"3\", \"4\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Aquelous\", 10, 2, \"5\", \"3\", \"5\", \"1\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Bicorne\", 10, 2, \"5\", \"5\", \"2\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"FlambosGlace\", 10, 2, \"6\", \"2\", \"6\", \"2\", \"Ice\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Piros\", 10, 2, \"7\", \"2\", \"4\", \"2\", \"Fire\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Solid1st\", 7, 4, \"3\", \"3\", \"6\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Solid2nd\", 7, 4, \"7\", \"2\", \"6\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Solid3rd\", 7, 4, \"2\", \"7\", \"1\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"BatteryCap\", 7, 4, \"4\", \"4\", \"7\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Unknown1\", 7, 4, \"6\", \"6\", \"2\", \"2\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MidgarZolom\", 7, 4, \"3\", \"7\", \"6\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MagicPot\", 7, 4, \"1\", \"6\", \"4\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"HammerBlaster\", 7, 4, \"7\", \"3\", \"2\", \"5\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Doorbull\", 7, 4, \"4\", \"4\", \"4\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"GasDucter\", 7, 4, \"7\", \"3\", \"3\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Foulander\", 7, 4, \"6\", \"3\", \"6\", \"3\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Ao\", 8, 4, \"4\", \"6\", \"5\", \"5\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Cariatide\", 8, 4, \"6\", \"5\", \"4\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Draconus\", 8, 4, \"3\", \"6\", \"3\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Eiffel\", 8, 4, \"2\", \"7\", \"6\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Marsupial\", 8, 4, \"7\", \"4\", \"4\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Moloch\", 8, 4, \"6\", \"3\", \"7\", \"2\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Polyphage\", 8, 4, \"7\", \"3\", \"4\", \"5\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Succube\", 8, 4, \"2\", \"3\", \"6\", \"7\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Tikal\", 8, 4, \"1\", \"7\", \"4\", \"6\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"TRex\", 8, 4, \"4\", \"7\", \"2\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Wendigo\", 8, 4, \"7\", \"6\", \"1\", \"3\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Zauras\", 10, 4, \"6\", \"7\", \"3\", \"2\", \"Wind\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Zuu\", 10, 4, \"6\", \"5\", \"4\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Pampa\", 10, 4, \"5\", \"5\", \"5\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ashura\", 10, 4, \"4\", \"6\", \"4\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Haruna\", 10, 4, \"7\", \"3\", \"6\", \"3\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Mandragore\", 10, 4, \"6\", \"6\", \"2\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Exoray\", 10, 4, \"5\", \"7\", \"3\", \"3\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"FlambosObscur\", 10, 4, \"6\", \"6\", \"5\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ElementaireObscur\", 10, 4, \"7\", \"7\", \"3\", \"4\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"DefenderZero\", 10, 4, \"5\", \"5\", \"4\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"UltraMight\", 10, 4, \"6\", \"5\", \"5\", \"5\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Boundfat\", 7, 5, \"5\", \"6\", \"6\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"IronGiant\", 7, 5, \"3\", \"6\", \"5\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"LandWorm\", 7, 5, \"7\", \"5\", \"5\", \"4\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Malboro\", 7, 5, \"3\", \"1\", \"2\", \"A\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Gargoyle\", 7, 5, \"6\", \"3\", \"5\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Allemagne\", 7, 5, \"5\", \"4\", \"7\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MasterTomberry\", 7, 5, \"7\", \"3\", \"7\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Unknown2\", 7, 5, \"6\", \"2\", \"7\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Unknown3\", 7, 5, \"4\", \"6\", \"7\", \"4\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"DeathClaw\", 7, 5, \"6\", \"2\", \"6\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"GhostShip\", 7, 5, \"7\", \"4\", \"7\", \"2\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Chimaira\", 8, 5, \"7\", \"3\", \"5\", \"6\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ekarissor\", 8, 5, \"6\", \"5\", \"6\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Griffon\", 8, 5, \"7\", \"4\", \"7\", \"2\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Kanibal\", 8, 5, \"3\", \"7\", \"5\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"KyokoK\", 8, 5, \"3\", \"1\", \"2\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Pikasso\", 8, 5, \"5\", \"4\", \"7\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Protesis\", 8, 5, \"6\", \"7\", \"6\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Sulfor\", 8, 5, \"5\", \"6\", \"7\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"TomberrySr\", 8, 5, \"4\", \"4\", \"7\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"WedgeBiggs\", 8, 5, \"6\", \"7\", \"2\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Xylomid\", 8, 5, \"7\", \"2\", \"4\", \"7\", \"Poison\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"PotMagique\", 10, 5, \"A\", \"1\", \"7\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Granada\", 10, 5, \"5\", \"3\", \"5\", \"6\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Lumbrikus\", 10, 5, \"5\", \"6\", \"5\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Tomberry\", 10, 5, \"6\", \"7\", \"3\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Barbatos\", 10, 5, \"5\", \"6\", \"5\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Xylomid\", 10, 5, \"7\", \"7\", \"4\", \"3\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Monolithe\", 10, 5, \"7\", \"5\", \"4\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Varna\", 10, 5, \"2\", \"6\", \"6\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ElementaireNoir\", 10, 5, \"7\", \"7\", \"1\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MasterIaguaro\", 10, 5, \"6\", \"7\", \"4\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ChimeraBrain\", 10, 5, \"3\", \"4\", \"7\", \"5\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"KingBehemoth\", 10, 5, \"7\", \"5\", \"6\", \"5\", \"Thunder\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"AirBuster\", 7, 6, \"8\", \"4\", \"8\", \"2\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"APS\", 7, 6, \"7\", \"8\", \"3\", \"4\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"CarryArmor\", 7, 6, \"7\", \"2\", \"5\", \"8\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"HelleticHojo\", 7, 6, \"2\", \"8\", \"8\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"BottomSwell\", 7, 6, \"5\", \"8\", \"7\", \"2\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"HundredGunner\", 7, 6, \"6\", \"8\", \"4\", \"5\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"JenovaLife\", 7, 6, \"4\", \"8\", \"5\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Palmer\", 7, 6, \"4\", \"8\", \"1\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"GuardScorpion\", 7, 6, \"3\", \"8\", \"8\", \"2\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MateriaKeeper\", 7, 6, \"4\", \"6\", \"8\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MotorBall\", 7, 6, \"8\", \"7\", \"7\", \"2\", \"Thunder\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Cyanide\", 8, 6, \"1\", \"8\", \"4\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ecorche\", 8, 6, \"1\", \"3\", \"8\", \"8\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Flotix\", 8, 6, \"4\", \"6\", \"5\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"FujinRaijin\", 8, 6, \"2\", \"4\", \"8\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Goliath\", 8, 6, \"4\", \"3\", \"7\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Hornet\", 8, 6, \"6\", \"5\", \"4\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Iguanor\", 8, 6, \"8\", \"2\", \"8\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Krystal\", 8, 6, \"7\", \"1\", \"8\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Lugus\", 8, 6, \"7\", \"5\", \"8\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Shumi\", 8, 6, \"6\", \"4\", \"8\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Sulfura\", 8, 6, \"7\", \"4\", \"3\", \"8\", \"Wind\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"ScagliaEmuzu\", 10, 6, \"8\", \"2\", \"6\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Thros\", 10, 6, \"8\", \"8\", \"1\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ScagliaEkyu\", 10, 6, \"8\", \"7\", \"1\", \"5\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"LanceurAlbhed\", 10, 6, \"6\", \"8\", \"3\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ScagliaGunai\", 10, 6, \"8\", \"2\", \"8\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ChocoboEater\", 10, 6, \"2\", \"8\", \"8\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ScagliaGhy\", 10, 6, \"8\", \"5\", \"7\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"CannoniereAlbhed\", 10, 6, \"7\", \"7\", \"6\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Sferogestore\", 10, 6, \"6\", \"8\", \"4\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"EfrayZombie\", 10, 6, \"8\", \"6\", \"3\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"NeoSeymour\", 10, 6, \"8\", \"3\", \"5\", \"7\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"DemonsGate\", 7, 7, \"8\", \"8\", \"3\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"GiNattak\", 7, 7, \"7\", \"2\", \"6\", \"8\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ProudClod\", 7, 7, \"5\", \"8\", \"5\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"BioHojo\", 7, 7, \"7\", \"4\", \"7\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"UltimaWeapon\", 7, 7, \"6\", \"8\", \"7\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"EmeraldWeapon\", 7, 7, \"6\", \"8\", \"4\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"DiamondWeapon\", 7, 7, \"8\", \"3\", \"8\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"RubyWeapon\", 7, 7, \"6\", \"8\", \"4\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"JenovaSynthesis\", 7, 7, \"5\", \"2\", \"8\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"BizarroSephiroth\", 7, 7, \"5\", \"8\", \"6\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"SaferSephiroth\", 7, 7, \"8\", \"2\", \"8\", \"8\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Acarnan\", 8, 7, \"8\", \"4\", \"5\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Acron\", 8, 7, \"8\", \"8\", \"5\", \"2\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Agamemnon\", 8, 7, \"5\", \"8\", \"6\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Alienator\", 8, 7, \"8\", \"8\", \"4\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Anakronox\", 8, 7, \"8\", \"3\", \"7\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Attila\", 8, 7, \"6\", \"7\", \"4\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Fabryce\", 8, 7, \"1\", \"7\", \"7\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Mithra\", 8, 7, \"8\", \"8\", \"5\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Monarch\", 8, 7, \"7\", \"8\", \"2\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Omniborg\", 8, 7, \"5\", \"5\", \"8\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"PampaSr\", 8, 7, \"8\", \"4\", \"4\", \"8\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Wendigo\", 10, 7, \"8\", \"2\", \"8\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"YenkeBiran\", 10, 7, \"8\", \"4\", \"8\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Josguein\", 10, 7, \"4\", \"7\", \"5\", \"8\", \"?\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Efray\", 10, 7, \"8\", \"4\", \"4\", \"8\", \"?\")");
		db.execSQL("INSERT INTO Cards VALUES (\"GardienDesLimbes\", 10, 7, \"7\", \"7\", \"7\", \"3\", \"Holy\")");
		db.execSQL("INSERT INTO Cards VALUES (\"GardienCeleste\", 10, 7, \"7\", \"7\", \"4\", \"7\", \"Holy\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Yunalesca\", 10, 7, \"8\", \"7\", \"5\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"UltimaArma\", 10, 7, \"8\", \"7\", \"6\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"YunalescaFinale\", 10, 7, \"8\", \"6\", \"7\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"SeymourOmega\", 10, 7, \"8\", \"7\", \"7\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"OmegaArma\", 10, 7, \"8\", \"8\", \"1\", \"8\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Reno\", 7, 8, \"7\", \"6\", \"5\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Rude\", 7, 8, \"3\", \"9\", \"9\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Elena\", 7, 8, \"1\", \"9\", \"5\", \"9\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Tseng\", 7, 8, \"6\", \"9\", \"8\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Rufus\", 7, 8, \"9\", \"4\", \"4\", \"9\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"CocoMog\", 7, 8, \"3\", \"9\", \"4\", \"9\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"GroChocobo\", 7, 8, \"8\", \"2\", \"8\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Rahmu\", 7, 8, \"9\", \"9\", \"2\", \"4\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Titan\", 7, 8, \"6\", \"8\", \"3\", \"9\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Shiva\", 7, 8, \"9\", \"4\", \"7\", \"4\", \"Ice\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ifrit\", 7, 8, \"2\", \"8\", \"9\", \"7\", \"Fire\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Angel\", 8, 8, \"9\", \"3\", \"7\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Chicobo\", 8, 8, \"9\", \"4\", \"8\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Gilgamesh\", 8, 8, \"3\", \"6\", \"9\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Golgotha\", 8, 8, \"2\", \"4\", \"9\", \"9\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"GroChocobo\", 8, 8, \"4\", \"9\", \"8\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ifrit\", 8, 8, \"9\", \"8\", \"2\", \"6\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Minimog\", 8, 8, \"9\", \"2\", \"9\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ondine\", 8, 8, \"8\", \"2\", \"6\", \"9\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Shiva\", 8, 8, \"6\", \"9\", \"4\", \"7\", \"Ice\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Tauros\", 8, 8, \"5\", \"9\", \"9\", \"1\", \"Earth\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Taurux\", 8, 8, \"9\", \"9\", \"2\", \"5\", \"earth\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Valefore\", 10, 8, \"9\", \"1\", \"8\", \"5\", \"Wind\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ifrit\", 10, 8, \"9\", \"2\", \"8\", \"6\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ixion\", 10, 8, \"7\", \"9\", \"2\", \"6\", \"Thunder\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Shiva\", 10, 8, \"9\", \"4\", \"7\", \"6\", \"Ice\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Bahamut\", 10, 8, \"A\", \"8\", \"5\", \"2\", \"Holy\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Yojimbo\", 10, 8, \"1\", \"9\", \"9\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Anima\", 10, 8, \"A\", \"9\", \"1\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Anabella\", 10, 8, \"9\", \"3\", \"3\", \"9\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Maria\", 10, 8, \"A\", \"2\", \"A\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Samantha\", 10, 8, \"3\", \"3\", \"A\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Sin\", 10, 8, \"9\", \"A\", \"3\", \"4\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Kjata\", 7, 9, \"A\", \"3\", \"9\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Hades\", 7, 9, \"4\", \"A\", \"3\", \"8\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Leviathan\", 7, 9, \"5\", \"2\", \"A\", \"9\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Odin\", 7, 9, \"2\", \"6\", \"A\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Phoenix\", 7, 9, \"5\", \"A\", \"4\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Typhon\", 7, 9, \"6\", \"9\", \"2\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Alexander\", 7, 9, \"6\", \"6\", \"4\", \"A\", \"Holy\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Bahamut\", 7, 9, \"7\", \"2\", \"7\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"NeoBahamut\", 7, 9, \"A\", \"9\", \"3\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"BahamutZero\", 7, 9, \"8\", \"2\", \"A\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"KnightsOfRound\", 7, 9, \"A\", \"4\", \"A\", \"4\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Ahuri\", 8, 9, \"8\", \"4\", \"A\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Alexander\", 8, 9, \"9\", \"2\", \"4\", \"A\", \"Holy\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Bahamut\", 8, 9, \"A\", \"6\", \"2\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Cerberes\", 8, 9, \"7\", \"A\", \"6\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"HellTrain\", 8, 9, \"3\", \"A\", \"A\", \"1\", \"Poison\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Leviathan\", 8, 9, \"7\", \"7\", \"1\", \"A\", \"Water\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Nosferatu\", 8, 9, \"5\", \"3\", \"8\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Odin\", 8, 9, \"8\", \"5\", \"3\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Orbital\", 8, 9, \"4\", \"A\", \"9\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Phenix\", 8, 9, \"7\", \"A\", \"7\", \"2\", \"Fire\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Zephyr\", 8, 9, \"A\", \"7\", \"7\", \"1\", \"Wind\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"BraskaUltimeChimere\", 10, 9, \"3\", \"3\", \"A\", \"9\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ValeforePurgateur\", 10, 9, \"9\", \"9\", \"5\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"IfritPurgateur\", 10, 9, \"A\", \"6\", \"2\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"IxionPurgateur\", 10, 9, \"7\", \"6\", \"A\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ShivaPurgatrice\", 10, 9, \"9\", \"9\", \"3\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"YojimboPurgateur\", 10, 9, \"3\", \"A\", \"3\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"AnimaPurgatrice\", 10, 9, \"A\", \"9\", \"1\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"AnabellaPurgatrice\", 10, 9, \"A\", \"5\", \"2\", \"9\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"MariaPurgatrice\", 10, 9, \"3\", \"4\", \"A\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"SamanthaPurgatrice\", 10, 9, \"A\", \"6\", \"A\", \"1\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"BahamutPurgateur\", 10, 9, \"A\", \"9\", \"6\", \"1\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Cloud\", 7, 10, \"A\", \"6\", \"8\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Vincent\", 7, 10, \"2\", \"9\", \"7\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Barret\", 7, 10, \"A\", \"2\", \"A\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Yuffie\", 7, 10, \"8\", \"A\", \"8\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Sephiroth\", 7, 10, \"6\", \"A\", \"6\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"CaitSith\", 7, 10, \"8\", \"5\", \"5\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Cid\", 7, 10, \"9\", \"A\", \"6\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Tifa\", 7, 10, \"8\", \"6\", \"A\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Aeris\", 7, 10, \"8\", \"6\", \"A\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"RedXIII\", 7, 10, \"8\", \"8\", \"5\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Zack\", 7, 10, \"A\", \"5\", \"2\", \"A\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Edea\", 8, 10, \"A\", \"3\", \"3\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Irvine\", 8, 10, \"2\", \"A\", \"9\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Kiros\", 8, 10, \"6\", \"A\", \"6\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Laguna\", 8, 10, \"5\", \"9\", \"3\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Linoa\", 8, 10, \"4\", \"A\", \"2\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Quistis\", 8, 10, \"9\", \"2\", \"A\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Seifer\", 8, 10, \"6\", \"4\", \"A\", \"9\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Selphie\", 8, 10, \"A\", \"4\", \"6\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Squall\", 8, 10, \"A\", \"9\", \"6\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Ward\", 8, 10, \"A\", \"8\", \"2\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Zell\", 8, 10, \"8\", \"6\", \"A\", \"5\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Kimhari\", 10, 10, \"A\", \"8\", \"5\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Rikku\", 10, 10, \"1\", \"9\", \"A\", \"7\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Lulu\", 10, 10, \"6\", \"A\", \"4\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Auron\", 10, 10, \"A\", \"6\", \"6\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Wakka\", 10, 10, \"5\", \"A\", \"3\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Yuna\", 10, 10, \"5\", \"A\", \"A\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Seymour\", 10, 10, \"6\", \"4\", \"9\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Tidus\", 10, 10, \"A\", \"5\", \"9\", \"5\", \"\")");
		
		db.execSQL("INSERT INTO Cards VALUES (\"Artemis\", 0, 10, \"4\", \"A\", \"A\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Bartz\", 0, 10, \"3\", \"9\", \"5\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"CecilDark\", 0, 10, \"A\", \"2\", \"5\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"CecilPaladin\", 0, 10, \"A\", \"4\", \"3\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Cloud\", 0, 10, \"A\", \"9\", \"8\", \"2\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"CloudOfDarkness\", 0, 10, \"8\", \"4\", \"A\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Exdeath\", 0, 10, \"9\", \"A\", \"4\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Frioniel\", 0, 10, \"A\", \"5\", \"3\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Garland\", 0, 10, \"A\", \"6\", \"8\", \"6\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Djidan\", 0, 10, \"A\", \"7\", \"8\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Gabranth\", 0, 10, \"A\", \"1\", \"A\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Golbez\", 0, 10, \"2\", \"6\", \"9\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Jecht\", 0, 10, \"5\", \"A\", \"A\", \"5\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Kuja\", 0, 10, \"5\", \"1\", \"A\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"OnionKnight\", 0, 10, \"6\", \"4\", \"9\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Sephiroth\", 0, 10, \"A\", \"4\", \"4\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Shantotto\", 0, 10, \"8\", \"5\", \"A\", \"4\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Squall\", 0, 10, \"5\", \"3\", \"A\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Terra\", 0, 10, \"6\", \"A\", \"A\", \"3\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Tidus\", 0, 10, \"A\", \"7\", \"2\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"WarriorOfLight\", 0, 10, \"A\", \"2\", \"A\", \"8\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"ImperatorPalamencia\", 0, 10, \"A\", \"1\", \"3\", \"A\", \"\")");
		db.execSQL("INSERT INTO Cards VALUES (\"Kefka\", 0, 10, \"9\", \"8\", \"A\", \"1\", \"\")");
	}
	
	public void close()
	{
		super.close();
	}
}
