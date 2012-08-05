package com.viish.apps.tripletriad.reseau;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.util.Log;

import com.viish.apps.tripletriad.Engine;

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
public class WifiConnection implements iReseau, Runnable
{
	public int timeToAcceptClientConnection;
	private int listeningPort;
	private boolean isHost;
	private String serverIpAdress;
	private Socket socket;
	private ServerSocket ss;
	private ObjectOutputStream oos;
	private ArrayList<Engine> listeners;
	private WifiConnectionReceiver receivingThread;
	
	public WifiConnection(boolean hosting, int port, int ttacc)
	{
		isHost = hosting;
		listeningPort = port;
		timeToAcceptClientConnection = ttacc;
		listeners = new ArrayList<Engine>();
	}
	
	public WifiConnection(boolean hosting, String ip, int port, int ttacc)
	{
		isHost = hosting;
		serverIpAdress = ip;
		listeningPort = port;
		timeToAcceptClientConnection = ttacc;
		listeners = new ArrayList<Engine>();
	}
	
	public void close()
	{
		try 
		{
			if (ss != null) {
				ss.close();
			}
			
			if (oos != null) {
				oos.close();
			}
			
			if (receivingThread != null) {
				receivingThread.close();
			}
			
			if (socket != null) {
				socket.close();
			}
		} 
		catch (Exception e) 
		{
			close();
			e.printStackTrace();
		}
	}
	
	public void addNetworkListener(Engine nl)
	{
		listeners.add(nl);
		Log.d("DEBUG", "Listener Added");
	}
	
	private void initSocketServer() throws Exception
	{
		ss = new ServerSocket(listeningPort);
		ss.setSoTimeout(timeToAcceptClientConnection);
		serverIpAdress = ss.getInetAddress().getHostAddress();
		
		socket = ss.accept();
		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);
		Log.d("DEBUG", "Socket Started");

		OutputStream os = socket.getOutputStream();
		oos = new ObjectOutputStream(os);
		Log.d("DEBUG", "Sender Started");
		
		receivingThread = new WifiConnectionReceiver(this, socket);
		new Thread(receivingThread).start();
		Log.d("DEBUG", "Received Started");
		
		fireOtherPlayerConnected();
	}
	
	private void initSocket() throws Exception
	{
		socket = new Socket(serverIpAdress, listeningPort);
		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);
		Log.d("DEBUG", "Socket Started");

		OutputStream os = socket.getOutputStream();
		oos = new ObjectOutputStream(os);
		Log.d("DEBUG", "Sender Started");
		
		receivingThread = new WifiConnectionReceiver(this, socket);
		new Thread(receivingThread).start();
		Log.d("DEBUG", "Received Started");
		
		fireOtherPlayerConnected();
	}
	
	public void fireSomethingReceived(String msg)
	{
		Log.d("FiringSomethingReceived", msg);
		for (Engine nl : listeners) {
			nl.onSomethingReceived(msg);
		}
	}
	
	public void fireOtherPlayerConnected()
	{
		Log.d("DEBUG", "FiringClientConnected");
		for (Engine nl : listeners) {
			nl.onOtherPlayerConnected();
		}
	}
	
	public void fireErrorOccured()
	{
		Log.d("DEBUG", "fireErrorOccured");
		for (Engine nl : listeners) {
			nl.onErrorOccured();
		}
	}

	public void send(String query) 
	{
		try 
		{
			oos.writeUTF(query);
			oos.flush();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			send(query);
		}
	}

	public void run() 
	{
		try 
		{
			if (isHost)
			{
				initSocketServer();
			} 
			else
			{
				initSocket();
			}
		}		
		catch (Exception e) 
		{
			e.printStackTrace();
			fireErrorOccured();
		}
		while (socket != null && socket.isConnected()) { }
	}
}
