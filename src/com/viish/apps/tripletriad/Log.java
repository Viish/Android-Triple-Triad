package com.viish.apps.tripletriad;


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
