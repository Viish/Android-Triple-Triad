package com.viish.apps.tripletriad.reseau;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import android.util.Log;

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
				if (socket != null) {
					socket.close();
				}
				
				if (ois != null) {
					ois.close();
				}
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
