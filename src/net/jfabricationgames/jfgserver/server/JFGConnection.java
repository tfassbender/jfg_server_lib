package net.jfabricationgames.jfgserver.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

/**
 * A JFGConnection represents the server side end of a connection.
 * For every client there is one JFGConnection instance.
 * 
 * The connection is used to forward the messages from the client to the interpreter and vice versa.
 */
public class JFGConnection implements Runnable {
	
	private JFGServer server;
	private Socket socket;
	
	private ObjectInputStream serverIn;
	private ObjectOutputStream serverOut;
	
	private Thread connection;
	private int sleepTime = 100;
	
	private JFGConnectionGroup group;
	private JFGServerInterpreter interpreter;
	
	/**
	 * Create a new JFGConnection and pass on the server and the connected socket.
	 * The connection doensn't contain a {@link JFGServerInterpreter} and for this reason it's not started directly.
	 * 
	 * The connection is started automatically when the interpreter is set to a non-null value.
	 * 
	 * @param server
	 * 		The {@link JFGServer} instance that accepted the clients request and started the connection.
	 * 
	 * @param socket
	 * 		The socket this connection is connected with.
	 * 
	 * @throws IOException
	 * 		An {@link IOException} is thrown if the in-/out-streams couldn't be created for some reason.
	 */
	public JFGConnection(JFGServer server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;
		serverOut = new ObjectOutputStream(socket.getOutputStream());
		serverIn = new ObjectInputStream(socket.getInputStream());
	}
	/**
	 * Create a new JFGConnection and pass on the server and the connected socket and an interpreter.
	 * Using this constructor the connection is created and directly started.
	 * 
	 * @param server
	 * 		The JFGServer instance that accepted the clients request and started the connection.
	 * 
	 * @param socket
	 * 		The socket this connection is connected with.
	 * 
	 * @param interpreter
	 * 		The {@link JFGServerInterpreter} that interprets the client's input.
	 * 
	 * @throws IOException
	 * 		An {@link IOException} is thrown if the in-/out-streams couldn't be created for some reason.
	 */
	public JFGConnection(JFGServer server, Socket socket, JFGServerInterpreter interpreter) throws IOException {
		this.server = server;
		this.socket = socket;
		this.interpreter = interpreter;
		if (interpreter == null) {
			throw new IllegalArgumentException("Interpreter mussnt be null.");
		}
		serverIn = new ObjectInputStream(socket.getInputStream());
		serverOut = new ObjectOutputStream(socket.getOutputStream());
		startConnection();
	}
	
	/**
	 * The run method from {@link Runnable} to make the connection listen to the clients inputs in a different thread.
	 */
	@Override
	public void run() {
		try {
			while (true) {
				Object clientRequest = serverIn.readObject();
				if (clientRequest instanceof JFGServerMessage) {
					interpreter.interpreteServerMessage((JFGServerMessage) clientRequest, this);
				}
				else {
					JFGServer.printError("JFGConnection: Received object is no JFGServerMessage. Couldn't interprete the message.", JFGServer.ERROR_LEVEL_DEBUG);
				}
				Thread.sleep(sleepTime);
			}
		}
		catch (SocketException | EOFException e) {
			//occurs when the connection is closed by the client and the server tries to read/write from/to the connection.
			JFGServer.printError(e, JFGServer.ERROR_LEVEL_INFO);
		}
		catch (IOException ioe) {
			JFGServer.printError(ioe, JFGServer.ERROR_LEVEL_ERROR);
		}
		catch (InterruptedException ie) {
			JFGServer.printError(ie, JFGServer.ERROR_LEVEL_ALL);
		}
		catch (ClassNotFoundException cnfe) {
			JFGServer.printError(cnfe, JFGServer.ERROR_LEVEL_DEBUG);
		}
	}
	
	/**
	 * Start the connection.
	 * 
	 * @throws IllegalArgumentException
	 * 		An {@link IllegalArgumentException} is thrown if the connection thread is already started.
	 */
	private void startConnection() throws IllegalArgumentException {
		if (connection != null) {
			throw new IllegalArgumentException("The connection thread is already started. Can't start another one.");
		}
		connection = new Thread(this);
		connection.start();
	}
	
	/**
	 * End the connection and close all resources.
	 */
	public void endConnection() {
		connection.interrupt();
		try {
			serverIn.close();
			serverOut.close();
		}
		catch (IOException ioe) {
			JFGServer.printError(ioe, JFGServer.ERROR_LEVEL_DEBUG);
		}
		//Try to close the socket separately
		try {
			socket.close();
		}
		catch (IOException ioe) {
			JFGServer.printError(ioe, JFGServer.ERROR_LEVEL_DEBUG);
		}
		server.removeConnection(this);
	}
	
	/**
	 * Send a message to the client connected to this JFGConnection.
	 * 
	 * @param message
	 * 		The message to send to the client.
	 */
	public void sendMessage(JFGClientMessage message) {
		try {
			serverOut.writeObject(message);
			serverOut.flush();
		}
		catch (IOException ie) {
			JFGServer.printError(ie, JFGServer.ERROR_LEVEL_INFO);
		}
	}
	
	/**
	 * Reset the ouput stream to prevent sending the same reference of an object with a changed state.
	 */
	public void resetOutput() {
		try {
			serverOut.reset();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public JFGConnectionGroup getGroup() {
		return group;
	}
	public void setGroup(JFGConnectionGroup group) {
		this.group = group;
	}
	
	public JFGServerInterpreter getInterpreter() {
		return interpreter;
	}
	/**
	 * Set the interpreter and start the connection.
	 * 
	 * @param interpreter
	 * 		The connections interpreter.
	 */
	public void setInterpreter(JFGServerInterpreter interpreter) {
		this.interpreter = interpreter;
		if (connection == null) {
			startConnection();
		}
	}
	
	public JFGServer getServer() {
		return server;
	}
	public Socket getSocket() {
		return socket;
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