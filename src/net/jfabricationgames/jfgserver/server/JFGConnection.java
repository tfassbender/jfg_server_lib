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
	
	protected JFGServer server;
	private Socket socket;
	
	protected ObjectInputStream serverIn;
	protected ObjectOutputStream serverOut;
	
	private Thread connection;
	protected int sleepTime = 100;
	
	protected JFGConnectionGroup group;
	protected JFGServerInterpreter interpreter;
	
	private static boolean resetBeforeSending = false;
	
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
	 * Create a new JFGConnection from another connection by cloning it.
	 * 
	 * @param connection
	 * 		The connection that is cloned.
	 */
	public JFGConnection(JFGConnection connection) {
		this.server = connection.server;
		this.socket = connection.socket;
		this.serverIn = connection.serverIn;
		this.serverOut = connection.serverOut;
		this.connection = connection.connection;
		this.sleepTime = connection.sleepTime;
		this.group = connection.group;
		this.interpreter = connection.interpreter;
	}
	
	/**
	 * Create a new JFGConnection without a server, a socket or an interpreter to be used as factory.
	 */
	public JFGConnection() {
		
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
					receiveMessage((JFGServerMessage) clientRequest);
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
	 * Create a new instance of the JFGConnection as factory instance for the server.
	 * 
	 * @param server
	 * 		The server that created the JFGConnection.
	 * 
	 * @param socket
	 * 		The socket the JFGConnection is connected to.
	 * 
	 * @return
	 * 		The new created instance of the JFGConnection.
	 * 
	 * @throws IOException
	 * 		An IOException is thrown if the in-/out-streams couldn't be created for some reason.
	 */
	public JFGConnection getInstance(JFGServer server, Socket socket) throws IOException {
		return new JFGConnection(server, socket);
	}
	
	/**
	 * End the old connection and start a new connection with the socket and streams given as parameter.
	 * 
	 * This method is primary used by the JFGSecureLoginServer to re-login a user to the existing JFGConnection.
	 * 
	 * @param socket
	 * 		The new socket.
	 * 
	 * @param in
	 * 		The new ObjectInputStream.
	 * 
	 * @param out
	 * 		The new ObjectOutputStream.
	 */
	public void restart(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
		endConnection(false);
		this.socket = socket;
		this.serverIn = in;
		this.serverOut = out;
		startConnection();
		System.out.println("connection restarted " + Thread.currentThread());
	}
	
	/**
	 * Receive a message that was sent to the socket of this server.
	 * 
	 * @param message
	 * 		The message that was sent.
	 */
	public void receiveMessage(JFGServerMessage message) {
		interpreter.interpreteServerMessage(message, this);
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
	 * End the connection and close all resources and remove the connection from the server.
	 */
	public void endConnection() {
		endConnection(true);
	}
	/**
	 * End the connection and close all resources.
	 * 
	 * @param removeConnection
	 * 		Optionally remove the connection from the server.
	 */
	public void endConnection(boolean removeConnection) {
		if (connection != null) {
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
			if (removeConnection) {
				server.removeConnection(this);				
			}
			connection = null;
		}
	}
	
	/**
	 * Stop the connection by interrupting the thread WITHOUT closing the resources.
	 */
	public void stopConnection() {
		if (connection != null) {
			connection.interrupt();
			server.removeConnection(this);
			connection = null;
		}
	}
	
	/**
	 * Send a message to the client connected to this JFGConnection.
	 * 
	 * @param message
	 * 		The message to send to the client.
	 */
	public void sendMessage(JFGClientMessage message) {
		try {
			if (resetBeforeSending) {
				resetOutput();
			}
			serverOut.writeObject(message);
			serverOut.flush();
		}
		catch (IOException ie) {
			JFGServer.printError(ie, JFGServer.ERROR_LEVEL_INFO);
		}
	}
	/**
	 * Send a message to the client connected to this JFGConnection using the writeUnshared method.
	 * 
	 * @param message
	 * 		The message to send to the client.
	 */
	public void sendMessageUnshared(JFGClientMessage message) {
		try {
			serverOut.writeUnshared(message);
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
			JFGServer.printError(e, JFGServer.ERROR_LEVEL_INFO);
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
	
	/**
	 * Indicates whether the output is reseted before every sent message.
	 * The output is only reseted before a normal write. Not an unshared write.
	 * 
	 * @return
	 * 		Returns true if the output is set to reset before sending.
	 */
	public static boolean isResetBeforeSending() {
		return resetBeforeSending;
	}
	/**
	 * Set to reset the stream before sending any new objects on all connections.
	 * 
	 * @param resetBeforeSending
	 * 		Set the reset on or off.
	 */
	public static void setResetBeforeSending(boolean resetBeforeSending) {
		JFGConnection.resetBeforeSending = resetBeforeSending;
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
	
	public ObjectInputStream getInputStream() {
		return serverIn;
	}
	public ObjectOutputStream getOutputStream() {
		return serverOut;
	}
}