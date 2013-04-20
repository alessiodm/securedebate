package org.alessiodm.securedebate;

//import javax.crypto.*;
//import javax.net.ssl.*;

import java.security.*;
import java.util.*;

public class DebateServer
{
	private final Key key;
	private final String nomeSala;
	private Vector<DebateListener> debateListeners = new Vector<DebateListener>();
	String session = "";
	
	public DebateServer(Key key, String name)
	{
		this.key = key;
		this.nomeSala = name;
	}
	
	public void addDebateListener(DebateListener dl)
	{
		if (!debateListeners.contains(dl))
			debateListeners.add(dl);
	}
	
	public void removeDebateListener(DebateListener dl)
	{
		if (debateListeners.contains(dl))
			debateListeners.remove(dl);
	}
	
	public void addMessage(String mes/*, DebateListener sender*/)
	{
		session += mes+"\n";
		signalDebateListeners(mes);
	}
	
	public String getNomeSala()
	{
		return nomeSala;
	}
	
	public Key getKey()
	{
		return key;
	}
	
	protected void signalDebateListeners(String mes)
	{
		Iterator<DebateListener> i = debateListeners.iterator();
		
		while (i.hasNext())
		{
			DebateListener dl = (DebateListener) i.next();
			dl.arrivedMessage(mes);
		}
	}
}
