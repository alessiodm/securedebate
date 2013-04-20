package org.alessiodm.securedebate;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.Key;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/*-----------------------------------------------------------------------
 * CLASS clientOutThread
 * ----------------------------------------------------------------------
 * 
 * la classe gestisce l'invio dei messaggi in sala...
 * 
 * ----------------------------------------------------------------------
 * */

class clientOutThread extends Thread
{
	private Socket socket;
	private Key key;
	
	public clientOutThread(Socket s, Key key)
	{
		socket = s;
		this.key = key;
	}
	
	public void run()
	{
		try
		{
			DataOutputStream outSock = new DataOutputStream(socket.getOutputStream());
			InputStream inputstream = System.in;
		    BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
		    
		    while(true){
		    	//System.out.println("INSERISCI UN MESSAGGIO DA INVIARE: ");
		    	String mes = bufferedreader.readLine();
		
		    	byte[] cifrato = Utility.cifra(mes, key);
		    	int len = cifrato.length;
		
		    	outSock.writeInt(len);	
		    	Utility.inviaByteArray(cifrato, outSock);
		    }
		}
		catch(IOException e)
		{
			//TODO
		}
	}
}

/*-----------------------------------------------------------------------
 * CLASS clientInThread
 * ----------------------------------------------------------------------
 * 
 * la classe gestisce i messaggi in arrivo dalla sala...
 * 
 * ----------------------------------------------------------------------
 * */

class clientInThread extends Thread
{
	private Socket socket;
	private Key key;
	
	public clientInThread(Socket s, Key key)
	{
		socket = s;
		this.key = key;
	}
	
	public void run()
	{
		try
		{
			DataInputStream inSock = new DataInputStream(socket.getInputStream());

		    while(true)
		    {		
		    	int len = inSock.readInt();
				byte[] ris = Utility.riceviByteArray(len, inSock);
				//System.out.println("Ricevuto dal server:\n"+ris.toString());
				System.out.println("RICEVUTO: "+Utility.decifra(ris,key));
		    }
		}
		catch(IOException e)
		{
			//TODO
		}
	}
}

/*-----------------------------------------------------------------------
 * CLASS clientInThread
 * ----------------------------------------------------------------------
 * 
 * Gestisce la comunicazione con il MainServer e il MessageDispatcher,
 * inoltre crea i Thread per gestire la comunicazione nella sala...
 * 
 * ----------------------------------------------------------------------
 * */

public class Client extends Thread 
{
	private final int mainServerPort = 4000;
	private final int dispatcherPort = 4001;

	public Client()
	{
		System.setProperty("javax.net.ssl.trustStore", "cert/client_trust_ca_certs.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "clientts");
	}
	
	public void run()
	{
		try
		{
			String [] se = new String[1];
			se[0] = "SSL_RSA_WITH_RC4_128_SHA";
			
			InetAddress mainServerAddr = InetAddress.getByName("localhost");
			
		    SSLContext ctx = SSLContext.getInstance("TLS");
		    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		    KeyStore ks = KeyStore.getInstance("JKS");

		    ks.load(new FileInputStream("cert/client_certs.jks"), "clientks".toCharArray());
		    kmf.init(ks, "clientk".toCharArray());
		    ctx.init(kmf.getKeyManagers(), null, null);

		    SSLSocketFactory factory = ctx.getSocketFactory();
		    
			SSLSocket ssocket = (SSLSocket) factory.createSocket(mainServerAddr, mainServerPort);
			ssocket.setEnabledCipherSuites(se);	
			//ssocket.setSoTimeout(30000);
			
			/***** PROTOCOLLO DI COMUNICAZIONE CON IL MAIN SERVER **************************/
			DataInputStream inSock = new DataInputStream(ssocket.getInputStream());
			DataOutputStream outSock = new DataOutputStream(ssocket.getOutputStream());

			InputStream inputstream = System.in;
		    BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
		    String richiesta;
		    
		    System.out.println("Inserisci la modalita' (crea || partecipa):");
		    richiesta = bufferedreader.readLine();
		    
		    if (richiesta.equals("crea"))
		    	System.out.println("Inserisci il nome della sala da creare:");
		    else if (richiesta.equals("partecipa"))
		    	System.out.println("Inserisci il nome della sala in cui entrare");
		    else
		    {
		    	System.out.println("ERRORE!!! Termino...");
		    	return;
		    }
		    outSock.writeUTF(richiesta);
		    String nomeSala = bufferedreader.readLine();
		    outSock.writeUTF(nomeSala);
		    
		    String risposta = inSock.readUTF();
		    if (risposta.equals("NO"))
		    {
		    	System.out.println("ERRORE: sala non utilizzabile...\nTermino...");
		    	return;
		    }
		    
		    ObjectInputStream keyInSock = new ObjectInputStream (ssocket.getInputStream());
			Key key = (Key) keyInSock.readObject();
			System.out.println("CHIAVE RICEVUTA: "+key.toString());
			
			//bufferedreader.close();
			ssocket.close();
			/********* FINE PROCEDURA CON IL MAIN SERVER *********************************/
			
			/***************** CONTATTO IL MESSAGE_DISPATCHER ***************************/
			
			//------ CREAZIONE SOCKET CONNESSA --------------------------
			
			Socket socket = null;
			InetAddress dispatcherAddr = InetAddress.getByName("localhost");
			
			try{ // Creiamo la socket
				socket = new Socket(dispatcherAddr, dispatcherPort);
	   	   		inSock = new DataInputStream(socket.getInputStream());
	   	   		outSock = new DataOutputStream(socket.getOutputStream());
	   	  	}
	   	   	catch(SocketException eS){
	   	   		System.out.println("Errore nella creazione della socket:");
				eS.printStackTrace();
				System.exit(1);
	   	   	}
	   	   	catch(IOException eIO){
	   	   		System.out.println("Errore nella creazione di stream:");
	    	  		eIO.printStackTrace();
	    	  		System.exit(1);
	   	   	}
	   	   	//-------------------------------------------------------------
	   	   	
	   	   	//--------- COMUNICAZIONE -------------------------------------
			try{
				//Bisognerebbe ricevere la risposta cifrata, ma er velocit�
				//la mando in chiaro...
				outSock.writeUTF(nomeSala);
				//System.out.println("INVIATA NOME SALA: "+nomeSala);
				
				int len = inSock.readInt();
				//System.out.println("RICEVUTO LEN: "+len);
				byte[] nonceCifrato = Utility.riceviByteArray(len, inSock);
				int nonce = Integer.parseInt(Utility.decifra(nonceCifrato, key));
				System.out.println("RICEVUTO NONCE: " + nonce);		
				nonce += 3;				
				nonceCifrato = Utility.cifra("" + nonce, key);
				len = nonceCifrato.length;
				outSock.writeInt(len);
				//System.out.println("INVIATA LUNGHEZZA NONCE: "+len);
				Utility.inviaByteArray(nonceCifrato, outSock);
				//System.out.println("INVIATO CIFRATO...in attesa di risposta...");
				
				//Bisognerebbe ricevere la risposta cifrata, ma er velocit�
				//la mando in chiaro...
				risposta = inSock.readUTF();
				System.out.println(risposta);
			}
			catch(IOException e){
				System.out.println("Errore nella comunicazione!!!");
				e.printStackTrace();
				System.exit(1);
			}
			//------------------------------------------------------------
	   	   	   	   	
			/********** FINE CONTATTO MESSAGE DISPATCHER ********************************/
			
	   	   	/*********** GENERAZIONE THREADS INVIO-RICEZIONE ****************************/
	   	   	if (risposta.equals("OK"))
	   	   	{
	   	   		new clientInThread(socket, key).start();
	   	   		new clientOutThread(socket, key).start();
	   	   	}
	   	   	else
	   	   		System.out.println("Autorizzazione negata...\nTerminazione...");
	   	  	/****************************************************************************/
		}
	   	catch(Exception e){
	   		e.printStackTrace();
	    	return;
	   	}
	}
	
	/*private String[] getCipherSuites(SSLSocket ss)
	{
		String[] scs = ss.getSupportedCipherSuites();
		String[] ascs = new String[scs.length];
		
		int ascsl = 0;
		
		for (int i=0; i<scs.length; i++) 
			if (scs[i].indexOf("_anon_") > 0)
				ascs[ascsl++] = scs[i];

		String[] oecs = ss.getEnabledCipherSuites();
		String[] necs = new String[oecs.length + ascsl];
		
		System.arraycopy(oecs, 0, necs, 0, oecs.length);
		System.arraycopy(ascs, 0, necs, oecs.length, ascsl);
		
		return necs;
	}*/
	
	public static void main(String[] args)
	{
		Client c = new Client();
		c.start();
	}
}
