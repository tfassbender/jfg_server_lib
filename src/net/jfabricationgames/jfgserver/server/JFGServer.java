package net.jfabricationgames.jfgserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

/**
 * The JFGServer is the base of all other servers.
 * It contains all code needed to create the connections to the clients.
 * 
 * The code needed to react on the clients input is to be implemented as a JFGServerInterpreter.
 * 
 * The server is abstract because an instance of a JFGServer needs to have a JFGServerInterpreter to work correctly.
 */
public abstract class JFGServer {
	
	protected int port;
	protected List<JFGConnection> connections;
	protected JFGServerInterpreter interpreterFactory;
	protected JFGConnection connectionFactory;
	
	private ServerSocket serverSocket;
	private Thread serverThread;
	
	public static final int ERROR_LEVEL_NONE = 1;
	public static final int ERROR_LEVEL_ERROR = 2;
	public static final int ERROR_LEVEL_INFO = 3;
	public static final int ERROR_LEVEL_DEBUG = 4;
	public static final int ERROR_LEVEL_ALL = 5;
	
	protected static int errorLevel = ERROR_LEVEL_ERROR;
	
	/**
	 * Create a new JFGServer listening on a port.
	 * 
	 * @param port
	 * 		The port to listen to.
	 */
	public JFGServer(int port) {
		this.port = port;
		connections = new ArrayList<JFGConnection>();
		connectionFactory = new JFGConnection();//use JFGConnection as default.
		chooseInterpreter();
	}
	
	/**
	 * Create a new ServerSocket on the port of this server.
	 * After the server is started it listens to connections on it's port.
	 * 
	 * All new connections on the socket are accepted and a new JFGConnection instance is created.
	 * 
	 * After the connections are created a new JFGServerInterpreter is added and the connections are added to the known connections.
	 * 
	 * The interpreter that is added is created as instance of the interpreterFactory.
	 * 
	 * @throws IOException
	 * 		An IOException is thrown when the ServerSocket couldn't be created for some reason.
	 */
	public void startServer() throws IOException {
		serverSocket = new ServerSocket(port);
		serverThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					while (true) {
						try {
							Socket connectionSocket = serverSocket.accept();
							JFGConnection connection = connectionFactory.getInstance(JFGServer.this, connectionSocket);
							addInterpreter(connection);
							addConnection(connection);
						}
						catch (IOException ioe) {
							JFGServer.printError(ioe, JFGServer.ERROR_LEVEL_ERROR);
						}
						Thread.sleep(10);
					}
				}
				catch (InterruptedException ie) {
					JFGServer.printError(ie, JFGServer.ERROR_LEVEL_ALL);
				}
			}
		});
		serverThread.start();
	}
	
	/**
	 * Stop the execution of the server.
	 */
	public void stopServer() {
		serverThread.interrupt();
		try {
			serverSocket.close();
		}
		catch (IOException ioe) {
			JFGServer.printError(ioe, JFGServer.ERROR_LEVEL_ERROR);
		}
	}
	
	/**
	 * This abstract method is called after the server is created to ensure that the server has a interpreterFactory.
	 * 
	 * The factory implementation, that is used, is to be created and added using the setInterpreterFactory() method.
	 */
	public abstract void chooseInterpreter();
	
	/**
	 * Set the connection factory to create connections of subclasses of JFGConnection.
	 * 
	 * @param connectionFactory
	 * 		The factory to be used to create new connections (no fields or connections are needed in this factory).
	 */
	public void setConnectionFactory(JFGConnection connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	/**
	 * Add a new instance of the JFGServerInterpreter, created from the factory, to the new connection.
	 * 
	 * @param connection
	 * 		The new connection that needs an interpreter.
	 */
	public void addInterpreter(JFGConnection connection) {
		connection.setInterpreter(interpreterFactory.getInstance());
	}
	
	/**
	 * Add the connection to the list of known connections.
	 * May be overwritten in a subclass if there is something else to be done with the new connections.
	 * 
	 * @param connection
	 * 		The new connection.
	 */
	public void addConnection(JFGConnection connection) {
		connections.add(connection);
	}
	/**
	 * Remove a connection from the list of known connections.
	 * 
	 * @param connection
	 * 		The removed connection.
	 */
	public void removeConnection(JFGConnection connection) {
		connections.remove(connection);
	}
	
	public List<JFGConnection> getConnections() {
		return connections;
	}
	/**
	 * Close the connection and remove it from the list of known connections.
	 * 
	 * @param con
	 * 		The connection to be closed.
	 */
	public void closeConnection(JFGConnection con) {
		con.endConnection();
		connections.remove(con);
	}
	
	/**
	 * Print the stack trace of the error if this error is shown at the current ERROR_LEVEL.
	 * 
	 * @param e
	 * 		The exception that was caught.
	 * 
	 * @param level
	 * 		The level of the exception.
	 */
	public static void printError(Exception e, int level) {
		if (errorLevel >= level && level > ERROR_LEVEL_NONE) {
			e.printStackTrace();
		}
	}
	/**
	 * Print an error message if this error is shown at the current ERROR_LEVEL.
	 * 
	 * @param s
	 * 		The error message.
	 * 
	 * @param level
	 * 		The level of the error.
	 */
	public static void printError(String s, int level) {
		if (errorLevel >= level && level > ERROR_LEVEL_NONE) {
			System.err.println(s);
		}
	}
	
	public static int getErrorLevel() {
		return errorLevel;
	}
	public static void setErrorLevel(int errorLevel) {
		JFGServer.errorLevel = errorLevel;
	}
	
	public JFGServerInterpreter getInterpreterFactory() {
		return interpreterFactory;
	}
	public void setInterpreterFactory(JFGServerInterpreter interpreterFactory) {
		this.interpreterFactory = interpreterFactory;
	}
}