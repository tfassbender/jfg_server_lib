package net.jfabricationgames.jfgserver.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import net.jfabricationgames.jfgserver.interpreter.JFGClientInterpreter;

/**
 * The JFGClient is used to create a connection to the server and send messages to the server or other connected clients.
 */
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
	
	/**
	 * Create a new JFGClient connected to a host on a port and add an interpreter to the client.
	 * The client is started directly because the interpreter is already known.
	 * 
	 * @param host
	 * 		The host to connect to.
	 * 
	 * @param port
	 * 		The port to connect to.
	 * 
	 * @param clientInterpreter
	 * 		The {@link JFGClientInterpreter} that interprets the messages coming from the server.
	 */
	public JFGClient(String host, int port, JFGClientInterpreter clientInterpreter) {
		this.host = host;
		this.port = port;
		this.clientInterpreter = clientInterpreter;
		startClient();
	}
	/**
	 * Create a new JFGClient connected to a host on a port.
	 * The client is NOT started directly because it doesn't have an interpreter.
	 * 
	 * When the interpreter is added the client is started automatically.
	 * 
	 * @param host
	 * 		The host to connect to.
	 * 
	 * @param port
	 * 		The port to connect to.
	 */
	public JFGClient(String host, int port) {
		this.host = host;
		this.port = port;
	}
	/**
	 * Create a new JFGCClient using the default host and port (if set).
	 * The client is NOT started directly because it doesn't have an interpreter.
	 * 
	 * When the interpreter is added the client is started automatically.
	 * 
	 * @param clientInterpreter
	 * 		The {@link JFGClientInterpreter} that interprets the messages coming from the server.
	 * 
	 * @throws IllegalArgumentException
	 * 		An {@link IllegalArgumentException} is thrown if defaultHost and defaultPort are not set correctly.
	 */
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
	/**
	 * Create a new JFGClient using the default host and port (if set).
	 * The client is started directly because the interpreter is already known.
	 * 
	 * @throws IllegalArgumentException
	 * 		An {@link IllegalArgumentException} is thrown if defaultHost and defaultPort are not set correctly.
	 */
	public JFGClient() throws IllegalArgumentException {
		if (defaultHost != null && !defaultHost.equals("") && defaultPort > 0) {
			this.host = defaultHost;
			this.port = defaultPort;
		}
		else {
			throw new IllegalArgumentException("Can't create a client on the current default connection. (" + defaultHost + " : " + defaultPort + ")");
		}
	}
	
	/**
	 * The run method from {@link Runnable} to read the incoming messages from the server and pass them on to the interpreter.
	 */
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
		catch (SocketException se) {
			//occurs when the connection is closed and the thread tries to read.
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
	
	/**
	 * Start the client's connection thread.
	 * 
	 * @throws IllegalArgumentException
	 * 		An {@link IllegalArgumentException} is thrown if the client is already started.
	 */
	private void startClient() throws IllegalArgumentException {
		if (clientInterpreter == null) {
			throw new IllegalArgumentException("Client can't be started without a JFGClientInterpreter.");
		}
		if (connection != null) {
			throw new IllegalArgumentException("The client Thread is already started. Can't start another on.");
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
	/**
	 * Create the socket and the streams.
	 */
	private void createClient() throws IOException {
		socket = new Socket(host, port);
		clientOut = new ObjectOutputStream(socket.getOutputStream());
		clientIn = new ObjectInputStream(socket.getInputStream());
	}
	
	/**
	 * Send a message to the server.
	 * 
	 * @param message
	 * 		The message send to the server.
	 */
	public void sendMessage(JFGServerMessage message) {
		try {
			clientOut.writeObject(message);
			clientOut.flush();
		}
		catch (IOException ie) {
			ie.printStackTrace();
		}
	}
	
	/**
	 * Reset the ouput stream to prevent sending the same reference of an object with a changed state.
	 */
	public void resetOutput() {
		try {
			clientOut.reset();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Close the streams and end the connection.
	 */
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
	
	/**
	 * Set the default values for the connection hosts and ports.
	 * 
	 * @param host
	 * 		The new default host.
	 * 
	 * @param port
	 * 		The new default port.
	 */
	public static void setDefaultConnection(String host, int port) {
		defaultHost = host;
		defaultPort = port;
	}
	
	public JFGClientInterpreter getClientInterpreter() {
		return clientInterpreter;
	}
	/**
	 * Set the client interpreter and start the connection thread.
	 * 
	 * @param clientInterpreter
	 * 		The new client interpreter.
	 */
	public void setClientInterpreter(JFGClientInterpreter clientInterpreter) {
		this.clientInterpreter = clientInterpreter;
		if (connection == null) {
			startClient();
		}
	}
	
	public int getSleepTime() {
		return sleepTime;
	}
	/**
	 * Set the time to sleep, for the thread, between two reads.
	 * 
	 * @param sleepTime
	 * 		The sleep time in milliseconds.
	 */
	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
}