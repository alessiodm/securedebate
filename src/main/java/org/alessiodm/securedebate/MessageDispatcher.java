package org.alessiodm.securedebate;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.Vector;
import java.util.Random;
import java.security.*;

class Ascoltatore implements DebateListener
{
	private Socket socket;
	private Key key;
	
	public Ascoltatore(Socket clientSocket, Key key){
		socket = clientSocket;
		this.key = key;
	}
	
	public void arrivedMessage(String mes)
	{
		try{
			DataOutputStream outSock = new DataOutputStream(socket.getOutputStream());
			
			byte[] cifrato = Utility.cifra(mes, key);
	    	int len = cifrato.length;
	
	    	outSock.writeInt(len);	
	    	Utility.inviaByteArray(cifrato, outSock);
		}
		catch(IOException e){
			System.out.println("ASCOLTATORE: Errore nella comunicazione!!!");
			e.printStackTrace();
			return;
		}
	}
}

// Classe che implementa il thread del servitore parallelo
class Server_Thread extends Thread{
	private Socket socket;
	private DebateServer dbServer;
	private Ascoltatore a;
	
	public Server_Thread(Socket clientSocket, DebateServer db, Ascoltatore a){
		socket = clientSocket;
		dbServer = db;
		this.a = a;
	}
	
	public void run(){
		DataInputStream inSock;

		//--------- COMUNICAZIONE -------------------------------------
		try{
			inSock = new DataInputStream(socket.getInputStream());
			
			while(true){
				int len = inSock.readInt();
				byte[] ris = Utility.riceviByteArray(len, inSock);
				//System.out.println("Ricevuto dal server:\n"+ris.toString());
				dbServer.addMessage(Utility.decifra(ris, dbServer.getKey()));
			}
		}
		catch(IOException e){
			System.out.println("SERVER_THREAD: Errore nella comunicazione!!!");
			e.printStackTrace();
			dbServer.removeDebateListener(a);
			return;
		}
	}
}


public class MessageDispatcher extends Thread
{
	private Vector<DebateServer> dbServers = new Vector<DebateServer>();
	
	public MessageDispatcher(){ super();}
	
	public boolean esisteSala(String nome)
	{
		Iterator<DebateServer> i = dbServers.iterator();
		
		while (i.hasNext())
		{
			DebateServer ds = (DebateServer) i.next();
			if (ds.getNomeSala().equals(nome))
				return true;
		}
		
		return false;
	}
	
	public void aggiungiSala(String nome, Key key)
	{
		dbServers.add(new DebateServer(key, nome));
	}
	
	public Key getKeySala(String nomeSala)
	{
		Iterator<DebateServer> i = dbServers.iterator();
		
		while (i.hasNext())
		{
			DebateServer ds = (DebateServer) i.next();
			if (ds.getNomeSala().equals(nomeSala))
				return ds.getKey();
		}
		
		return null;
	}
	
	public int getIndexDebateServer(String nome)
	{
		for (int i=0; i<dbServers.size(); i++)
			if (dbServers.get(i).getNomeSala().equals(nome))
				return i;

		return -1;
	}
	
	public void run()
	{
		//Variabili per la comunicazione connessa
		int myPort;
		ServerSocket serverSocket = null;
		Socket clientSocket = null;

		//-------- SETTAGGIO SERVERSOCKET--------------------------------------
 		try{
			myPort = 4001;
			if (myPort < 1024) {
				System.out.println("Porta non consentita!!!\nTerminazione...");
				System.exit(1);
			}
			serverSocket = new ServerSocket(myPort);
			serverSocket.setReuseAddress(true);
		}
		catch(NumberFormatException ne){
			System.out.println("Porta server non corretta!!!\nTerminazione...");
			System.exit(1);
		}
		/*catch(SocketException eS){
    			System.out.println("Errore settaggio opzioni ServerSocket!");
    			eS.printStackTrace();
		}*/
		catch(IOException eIO){
			System.out.println("Errore nella creazione della ServerSocket!");
			eIO.printStackTrace();
			System.exit(1);
		}
    		//---------------------------------------------------------------------
    		
		System.out.println("MESSAGE DISPATCHER: Avviamento demone...");

    		while(true){ //Daemon
    			try{
    				clientSocket = serverSocket.accept(); //accetto una connessione
					System.out.println("MESSAGE DISPATCHER: Ricevuta richiesta di connessione...");
    			}
    			/*catch(SocketException eS){
    				System.out.println("Errore settaggio opzioni clientSocket!");
    				eS.printStackTrace();
    			}*/
    			catch(IOException eIO){
    				System.out.println("MESSAGE DISPATCHER: Errore nella creazione della clientSocket!");
    				eIO.printStackTrace();
    				System.exit(1);
    			}
    		
    			try{
    				DataInputStream inSock;
    				DataOutputStream outSock;
    				
    				inSock = new DataInputStream(clientSocket.getInputStream());
    				outSock = new DataOutputStream(clientSocket.getOutputStream());
    				
    				//Protocollo di autorizzazione...
    				// - Ricevo il nome della sala
    				String nomeSala = inSock.readUTF();
    				System.out.println("MD: ricevuto nome sala -> "+ nomeSala);
    				// - Recupero la chiave e invio un nonce cifrato
    				Key key = getKeySala(nomeSala);
    				int nonce = new Random().nextInt();
    				byte[] nonceCifrato = Utility.cifra("" + nonce, key);
    				int len = nonceCifrato.length;
    				outSock.writeInt(len);
    				Utility.inviaByteArray(nonceCifrato, outSock);
    				
    				// - Ricevo la risposta
    				len = inSock.readInt();
    				System.out.println("MD: ricevuta lunghezza risp. -> "+ len);
    				byte[] risposta = Utility.riceviByteArray(len, inSock);
    				
    				// - Controllo
    				String verifica = Utility.decifra(risposta, key);
    				System.out.println("MD: ricevuta risposta -> "+ verifica);
    				
    				if (verifica.equals("" + (nonce + 3)))
    				{	
    					//Invio la risposta; dovrebbe essere cifrata ma per semplicit�
    					//la mando in chiaro...
        				outSock.writeUTF("OK");
        				
    					//Lancio il thread che gestisce il servizio
    					DebateServer dbs = dbServers.get(getIndexDebateServer(nomeSala));
        				Ascoltatore a = new Ascoltatore(clientSocket, dbs.getKey());
        				dbs.addDebateListener(a);
        				new Server_Thread(clientSocket, dbs, a).start();
        				System.out.println("MESSAGE DISPATCHER: Lanciato thread per gestire la connessione...");
    				}
    				else
    					System.out.println("MESSAGE DISPATCHER: CLIENT NON AUTORIZZATO");
    			}
    			catch(Exception e){
    				System.out.println("MESSAGE DISPATCHER: Errore nella creazione thread!!!");
    				e.printStackTrace();
    				continue;
    			}
    		}//while
	}//run
}


