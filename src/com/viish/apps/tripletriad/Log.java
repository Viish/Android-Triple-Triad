package com.viish.apps.tripletriad;

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
public class Log 
{
	private static final String TAG = "Triple Triad";
	
	public static void d(Object...objects) 
	{
			android.util.Log.d(TAG, toString(objects));
	}
	
	public static void d(Throwable t, Object...objects) 
	{
			android.util.Log.d(TAG, toString(objects), t);
	}
	
	public static void w(Object...objects) 
	{
			android.util.Log.w(TAG, toString(objects));
	}
	
	public static void w(Throwable t, Object...objects) 
	{
			android.util.Log.w(TAG, toString(objects), t);
	}
	
	public static void e(Object...objects) 
	{
			android.util.Log.e(TAG, toString(objects));
	}
	
	public static void e(Throwable t, Object...objects) 
	{
			android.util.Log.e(TAG, toString(objects), t);
	}
	
	private static String toString(Object...objects) 
	{
		StringBuilder sb = new StringBuilder();
		for (Object o : objects) 
		{
			sb.append(o);
		}
		return sb.toString();
	}
}
