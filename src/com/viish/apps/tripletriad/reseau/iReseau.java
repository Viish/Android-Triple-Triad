package com.viish.apps.tripletriad.reseau;

import com.viish.apps.tripletriad.Engine;

public interface iReseau 
{
	public void send(String s);
	public void fireSomethingReceived(String msg);
	public void fireOtherPlayerConnected();
	public void fireErrorOccured();
	public void addNetworkListener(Engine nl);
}
