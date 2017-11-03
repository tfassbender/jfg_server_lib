package net.jfabricationgames.jfgserver.secured_message;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;
import net.jfabricationgames.jfgserver.server.JFGConnection;
import net.jfabricationgames.jfgserver.server.JFGServer;

public class JFGSecureMessageConnection extends JFGConnection {
	
	private JFGCommunicationSecurity communicationSecurity;
	private JFGSecureMessageOrder messageOrder;
	
	/**
	 * Create a new JFGSecureMessageConnection and pass on the server and the connected socket.
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
	public JFGSecureMessageConnection(JFGServer server, Socket socket) throws IOException {
		super(server, socket);
		communicationSecurity = new JFGCommunicationSecurity(this);
		messageOrder = new JFGSecureMessageOrder();
	}
	/**
	 * Create a new JFGSecureMessageConnection and pass on the server and the connected socket and an interpreter.
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
	public JFGSecureMessageConnection(JFGServer server, Socket socket, JFGServerInterpreter interpreter) throws IOException {
		super(server, socket, interpreter);
		communicationSecurity = new JFGCommunicationSecurity(this);
		messageOrder = new JFGSecureMessageOrder();
	}
	
	/**
	 * Create a new JFGSecureMessageConnection from another connection by cloning it.
	 * 
	 * @param connection
	 * 		The connection that is cloned.
	 */
	public JFGSecureMessageConnection(JFGConnection connection) {
		super(connection);
		communicationSecurity = new JFGCommunicationSecurity(this);
		messageOrder = new JFGSecureMessageOrder();
	}
	
	/**
	 * Create a new JFGSecureMessageConnection from another JFGSecureMessageConnection by cloning it.
	 * 
	 * @param connection
	 * 		The secure connection that is cloned.
	 */
	public JFGSecureMessageConnection(JFGSecureMessageConnection connection) {
		super(connection);
		communicationSecurity = connection.communicationSecurity;
		messageOrder = new JFGSecureMessageOrder();
	}
	
	/**
	 * Create a new empty JFGSecureMessageConnection as factory for the server. 
	 */
	public JFGSecureMessageConnection() {
		super();
	}
	
	/**
	 * The run method from {@link Runnable} to make the connection listen to the clients inputs in a different thread.
	 * 
	 * Overrides the run method from JFGConnection to not stop reading when an exception occurs.
	 */
	@Override
	public void run() {
		try {
			while (true) {
				try {
					Object clientRequest = serverIn.readObject();
					if (clientRequest instanceof JFGServerMessage) {
						receiveMessage((JFGServerMessage) clientRequest);
					}
					else {
						JFGServer.printError("JFGConnection: Received object is no JFGServerMessage. Couldn't interprete the message.", JFGServer.ERROR_LEVEL_DEBUG);
					}
				}
				catch (StreamCorruptedException sce) {
					sce.printStackTrace();
					relogin();
				}
				catch (SocketException | EOFException e) {
					//occurs when the connection is closed by the client and the server tries to read/write from/to the connection.
					JFGServer.printError(e, JFGServer.ERROR_LEVEL_INFO);
				}
				catch (IOException ioe) {
					JFGServer.printError(ioe, JFGServer.ERROR_LEVEL_ERROR);
				}				
				Thread.sleep(sleepTime);
			}
		}
		catch (InterruptedException ie) {
			JFGServer.printError(ie, JFGServer.ERROR_LEVEL_ALL);
		}
		catch (ClassNotFoundException cnfe) {
			JFGServer.printError(cnfe, JFGServer.ERROR_LEVEL_DEBUG);
		}
	}
	
	/**
	 * Create a new instance of the JFGSecureMessageConnection from the factory.
	 * 
	 * Override this method to change the factory function of the server.
	 */
	@Override
	public JFGConnection getInstance(JFGServer server, Socket socket) throws IOException {
		return new JFGSecureMessageConnection(server, socket);
	}

	/**
	 * Send a secured message to the server and check it's arrival.
	 * The CommunicationSecurity checks that the message arrives.
	 * 
	 * @param message
	 * 		The message send to the server.
	 */
	@Override
	public void sendMessage(JFGClientMessage message) {
		messageOrder.addSendCount(message);
		communicationSecurity.secureMessage(message);
		synchronized (this) {
			super.sendMessage(message);			
		}
	}
	/**
	 * Re-send a message from the communication security.
	 * No new message order or security needed. 
	 */
	protected void resendMessage(JFGClientMessage message) {
		synchronized (this) {
			super.sendMessage(message);			
		}
	}
	/**
	 * Send a message to the server connected to this JFGClient using the writeUnshared method.
	 * The CommunicationSecurity checks that the message arrives.
	 * 
	 * @param message
	 * 		The message to send to the server.
	 */
	@Override
	public void sendMessageUnshared(JFGClientMessage message) {
		messageOrder.addSendCount(message);
		communicationSecurity.secureMessage(message);
		synchronized (this) {
			super.sendMessageUnshared(message);			
		}
	}
	
	/**
	 * Receive a message that was send to the client's socket.
	 * 
	 * If it's an ACK-Message it's passed on to the communication security.
	 * Otherwise it's passed on to the interpreter. 
	 * 
	 * @param message
	 * 		The message that was received.
	 */
	@Override
	public void receiveMessage(JFGServerMessage message) {
		if (message instanceof JFGAcknowledgeMessage) {
			communicationSecurity.receiveAcknoledgeMessage((JFGAcknowledgeMessage) message);
		}
		else {
			if (!communicationSecurity.isResentMessage(message)) {
				if (messageOrder.isInOrder(message)) {//checks and buffers if false
					super.receiveMessage(message);
					JFGSecurableMessage bufferedMessage;
					while ((bufferedMessage = messageOrder.getNextBufferedMessage()) != null) {
						//receive all buffered messages
						super.receiveMessage((JFGServerMessage) bufferedMessage);
					}
				}
			}
			communicationSecurity.sendAcknowledge(message);
		}
	}
	
	/**
	 * Send a request to the client to re-login into the server.
	 */
	private void relogin() {
		JFGReloginMessage reloginMessage = new JFGReloginMessage(JFGReloginMessage.ReloginMessageType.SERVER_RELOGIN_REQUEST);
		sendMessage(reloginMessage);
	}
}
