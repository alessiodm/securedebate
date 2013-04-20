package org.alessiodm.securedebate;

import java.io.*;
import java.net.Socket;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.crypto.*;

public class MainServer extends Thread
{
	private final int port = 4000;
	private Socket clientSocket = null;
	private SSLServerSocket ss = null;
	
	private MessageDispatcher md = null;
	
	public MainServer(MessageDispatcher md/*Parametri come nomi e pwds...*/)
	{
		this.md = md;
		
		try
		{			
			char[] pwdTS = "serverts".toCharArray();
			char[] pwdKS = "serverks".toCharArray();
			char[] pwdKey = "serverk".toCharArray();
			
			String [] enabledCiphers = new String[1];
			enabledCiphers[0] = "SSL_RSA_WITH_RC4_128_SHA";
			
			SSLContext ctx = SSLContext.getInstance("SSL", "SunJSSE");
			KeyStore ks = KeyStore.getInstance("JKS");
			KeyStore ts = KeyStore.getInstance("JKS");		
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509", "SunJSSE");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
			
			ks.load(new FileInputStream("cert/server_certs.jks"), pwdKS);
			ts.load(new FileInputStream("cert/server_trust_certs.jks"), pwdTS);	
			kmf.init(ks, pwdKey);
			tmf.init(ts);
			
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			SSLServerSocketFactory f = ctx.getServerSocketFactory();
			ss = (SSLServerSocket) f.createServerSocket(port);
			//ss.setEnabledCipherSuites(getCipherSuites(ss));
			
			ss.setEnabledCipherSuites(enabledCiphers);
			ss.setNeedClientAuth(true);
			ss.setReuseAddress(true);
		}
		catch(NoSuchAlgorithmException e)
		{ e.printStackTrace(); System.exit(1); }
		catch(NoSuchProviderException e)
		{ e.printStackTrace(); System.exit(1); }
		catch(KeyStoreException e)
		{ e.printStackTrace(); System.exit(1); }
		catch(IOException e)
		{ e.printStackTrace(); System.exit(1); }
		catch(KeyManagementException e)
		{ e.printStackTrace(); System.exit(1); }
		catch(UnrecoverableKeyException e)
		{ e.printStackTrace(); System.exit(1); }
		catch(CertificateException e)
		{ e.printStackTrace(); System.exit(1); }
	}
	
	// IMPLEMENTAZIONE SERVER SEQUENZIALE...
	public void run()
	{	
		while (true) // Daemon
		{
			try{
				System.out.println("MAIN SERVER: in attesa di richieste...");
    				
				clientSocket = ss.accept(); //accetto una connessione
    			//clientSocket.setSoTimeout(30000);				
				System.out.println("MAIN SERVER: Ricevuta richiesta di connessione...");
					
				DataInputStream inSock = new DataInputStream(clientSocket.getInputStream());
    			DataOutputStream outSock = new DataOutputStream(clientSocket.getOutputStream());

			    String richiesta = inSock.readUTF();
			    String nomeSala = inSock.readUTF();
			    
			    KeyGenerator keyGen = KeyGenerator.getInstance("TripleDES");
				keyGen.init(168);
				Key key = null;
				
				if (richiesta.equals("crea"))
				{
					// - Controllo che la sala non esista gi�...
					if (md.esisteSala(nomeSala))
					{
						System.out.println("MAIN_SERVER: Sala esistente!!!");
						outSock.writeUTF("NO");
						continue;
					}
					
					// - Generazione della chiave
					key = keyGen.generateKey();				
					md.aggiungiSala(nomeSala, key);
				}
				else if (richiesta.equals("partecipa"))
				{
					if (md.esisteSala(nomeSala))
						key = md.getKeySala(nomeSala);
					else
					{
						System.out.println("MAIN_SERVER: La sala non esiste!!!");
						outSock.writeUTF("NO");
						continue;
					}
				}
				else
					System.exit(1);
				
				outSock.writeUTF("OK");
				ObjectOutputStream keyOutSock = new ObjectOutputStream (clientSocket.getOutputStream());
				keyOutSock.writeObject(key);
				System.out.println("MAIN SERVER: CHIAVE SCAMBIATA: "+key.toString());
				

				clientSocket.close();	
    		}
    		catch(IOException eIO){
    			System.out.println("IO Error: Fine comunicazione...");
    			eIO.printStackTrace();
    			//return;
    		}
    		catch(Exception e)
    		{
    			System.out.println("Error: Fine comunicazione...");
    			e.printStackTrace();
    			//return;
    		}
		}
	}
	
	/*private String[] getCipherSuites(SSLServerSocket ss)
	{
		String[] scs = ss.getSupportedCipherSuites();
		String[] ascs = new String[scs.length];
		
		int ascsl = 0;
		
		for (int i=0; i<scs.length; i++) 
			if (scs[i].indexOf("_anon_") > 0) //anon -> non richiede autenticazione
				ascs[ascsl++] = scs[i];

		String[] oecs = ss.getEnabledCipherSuites();
		String[] necs = new String[oecs.length + ascsl];
		
		System.arraycopy(oecs, 0, necs, 0, oecs.length);
		System.arraycopy(ascs, 0, necs, oecs.length, ascsl);
		
		return necs;
	}*/
	
	public static void main(String[] args)
	{
		MessageDispatcher md = new MessageDispatcher();
		MainServer ms = new MainServer(md);
		
		md.start();
		ms.start();
	}
}
