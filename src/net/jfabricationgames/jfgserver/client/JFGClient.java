package net.jfabricationgames.jfgserver.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import net.jfabricationgames.jfgserver.interpreter.JFGClientInterpreter;

public class JFGClient implements Runnable {
	
	private ObjectInputStream clientIn;
	private ObjectOutputStream clientOut;
	
	private Socket socket;
	
	private JFGClientInterpreter clientInterpreter;
	
	private Thread connection;
	private int sleepTime;
	
	private String host;
	private int port;
	
	private static String defaultHost = "jfabricationgames.ddns.net";
	private static int defaultPort = -1;
	
	public JFGClient(String host, int port, JFGClientInterpreter clientInterpreter) {
		this.host = host;
		this.port = port;
		this.clientInterpreter = clientInterpreter;
		startClient();
	}
	public JFGClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	public JFGClient(JFGClientInterpreter clientInterpreter) throws IllegalArgumentException {
		if (defaultHost != null && !defaultHost.equals("") && defaultPort > 0) {
			this.host = defaultHost;
			this.port = defaultPort;
			this.clientInterpreter = clientInterpreter;
			startClient();
		}
		else {
			throw new IllegalArgumentException("Can't create a client on the current default connection. (" + defaultHost + " : " + defaultPort + ")");
		}
	}
	public JFGClient() {
		if (defaultHost != null && !defaultHost.equals("") && defaultPort > 0) {
			this.host = defaultHost;
			this.port = defaultPort;
		}
		else {
			throw new IllegalArgumentException("Can't create a client on the current default connection. (" + defaultHost + " : " + defaultPort + ")");
		}
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				Object clientRequest = clientIn.readObject();
				if (clientRequest instanceof JFGClientMessage) {
					clientInterpreter.interpreteClientMessage((JFGClientMessage) clientRequest, this);
				}
				else {
					System.err.println("JFGClient: Received object is no JFGClientMessage. Couldn't interprete the message.");
				}
				Thread.sleep(sleepTime);
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		catch (InterruptedException ie) {
			//ie.printStackTrace();
		}
		catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	
	public void startClient() throws IllegalArgumentException {
		if (clientInterpreter == null) {
			throw new IllegalArgumentException("Client can't be started without a JFGClientInterpreter.");
		}
		try {
			createClient();
			connection = new Thread(this);
			connection.start();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	private void createClient() throws IOException {
		socket = new Socket(host, port);
		clientIn = new ObjectInputStream(socket.getInputStream());
		clientOut = new ObjectOutputStream(socket.getOutputStream());
	}
	
	public void sendMessage(JFGClientMessage message) {
		try {
			clientOut.writeObject(message);
			clientOut.flush();
		}
		catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
	public void closeConnection() {
		connection.interrupt();
		try {
			clientIn.close();
			clientOut.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		//Try to close the socket separately
		try {
			socket.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static void setDefaultConnection(String host, int port) {
		defaultHost = host;
		defaultPort = port;
	}
	
	public JFGClientInterpreter getClientInterpreter() {
		return clientInterpreter;
	}
	public void setClientInterpreter(JFGClientInterpreter clientInterpreter) {
		this.clientInterpreter = clientInterpreter;
	}
	
	public int getSleepTime() {
		return sleepTime;
	}
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
}