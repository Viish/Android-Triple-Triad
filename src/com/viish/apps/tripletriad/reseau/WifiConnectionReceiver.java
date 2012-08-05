package com.viish.apps.tripletriad.reseau;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import android.util.Log;

public class WifiConnectionReceiver implements Runnable
{
	private WifiConnection wc;
	private ObjectInputStream ois;
	private Socket socket;
	
	public WifiConnectionReceiver(WifiConnection connection, Socket s) throws Exception
	{
		wc = connection;
		socket = s;
		InputStream is = s.getInputStream();
		ois = new ObjectInputStream(is);
	}
	
	public void close()
	{
			try 
			{
				if (socket != null)
					socket.close();
				
				if (ois != null)
					ois.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
	}
	
	public void run() 
	{
		while (ois != null && socket.isConnected())
		{
			String messageReceived;
			try 
			{
				messageReceived = ois.readUTF();
				Log.d("SomethingReceived", messageReceived);
				wc.fireSomethingReceived(messageReceived);
			} 
			catch (EOFException e) 
			{
				e.printStackTrace();
				close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
