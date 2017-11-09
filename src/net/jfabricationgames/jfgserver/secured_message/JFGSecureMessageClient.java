package net.jfabricationgames.jfgserver.secured_message;

import java.io.EOFException;
import java.io.IOException;
import java.io.StreamCorruptedException;

import net.jfabricationgames.jfgserver.client.JFGClient;
import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGClientInterpreter;

public class JFGSecureMessageClient extends JFGClient {
	
	private JFGCommunicationSecurity communicationSecurity;
	private JFGSecureMessageOrder messageOrder;
	
	private String reloginPassword;
	
	/**
	 * Create a new JFGSecureMessageClient connected to a host on a port and add an interpreter to the client.
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
	public JFGSecureMessageClient(String host, int port, JFGClientInterpreter clientInterpreter) {
		super(host, port, clientInterpreter);
		communicationSecurity = new JFGCommunicationSecurity(this);
		messageOrder = new JFGSecureMessageOrder();
	}
	/**
	 * Create a new JFGSecureMessageClient connected to a host on a port.
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
	public JFGSecureMessageClient(String host, int port) {
		super(host, port);
		communicationSecurity = new JFGCommunicationSecurity(this);
		messageOrder = new JFGSecureMessageOrder();
	}
	/**
	 * Create a new JFGSecureMessageClient using the default host and port (if set).
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
	public JFGSecureMessageClient(JFGClientInterpreter clientInterpreter) throws IllegalArgumentException {
		super(clientInterpreter);
		communicationSecurity = new JFGCommunicationSecurity(this);
		messageOrder = new JFGSecureMessageOrder();
	}
	/**
	 * Create a new JFGSecureMessageClient using the default host and port (if set).
	 * The client is started directly because the interpreter is already known.
	 * 
	 * @throws IllegalArgumentException
	 * 		An {@link IllegalArgumentException} is thrown if defaultHost and defaultPort are not set correctly.
	 */
	public JFGSecureMessageClient() throws IllegalArgumentException{
		super();
		communicationSecurity = new JFGCommunicationSecurity(this);
		messageOrder = new JFGSecureMessageOrder();
	}
	/**
	 * Create a new JFGSecureMessageClient by cloning another secure message client.
	 * 
	 * @param client
	 * 		The secure client that is cloned.
	 */
	public JFGSecureMessageClient(JFGSecureMessageClient client) {
		super(client);
		communicationSecurity = client.communicationSecurity;
		messageOrder = new JFGSecureMessageOrder();
	}
	
	/**
	 * Send a secured message to the server and check it's arrival.
	 * The CommunicationSecurity checks that the message arrives.
	 * 
	 * @param message
	 * 		The message send to the server.
	 */
	@Override
	public void sendMessage(JFGServerMessage message) {
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
	protected void resendMessage(JFGServerMessage message) {
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
	public void sendMessageUnshared(JFGServerMessage message) {
		messageOrder.addSendCount(message);
		communicationSecurity.secureMessage(message);
		synchronized (this) {
			super.sendMessageUnshared(message);			
		}
	}
	
	/**
	 * Overrides the run method from JFGClient to not stop reading when an exception occurs.
	 */
	@Override
	public void run() {
		try {
			while (true) {
				try {
					Object clientRequest = clientIn.readObject();
					if (clientRequest instanceof CorruptedMessage) {
						System.out.println("currupted message received");
						if (reloginPassword != null) {
							System.out.println("relogin called");
							relogin();
						}
					}
					if (clientRequest instanceof JFGClientMessage) {
						receiveMessage((JFGClientMessage) clientRequest);
					}
					else {
						System.err.println("JFGClient: Received object is no JFGClientMessage. Couldn't interprete the message.");
					}
				}
				catch (StreamCorruptedException sce) {
					sce.printStackTrace();
					//re-login if the server supports it
					if (reloginPassword != null) {
						//relogin();
						break;
					}
				}
				catch (EOFException eofe) {
					//eofe.printStackTrace();
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
				}
				Thread.sleep(sleepTime);
			}
		}
		catch (InterruptedException ie) {
			//ie.printStackTrace();
		}
		catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
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
	public void receiveMessage(JFGClientMessage message) {
		if (message instanceof JFGAcknowledgeMessage) {
			communicationSecurity.receiveAcknoledgeMessage((JFGAcknowledgeMessage) message);
		}
		else if (message instanceof JFGReloginMessage) {
			System.out.println("relogin message received");
			JFGReloginMessage reloginMessage = ((JFGReloginMessage) message);
			messageOrder.isInOrder(message);//call to set the count in the message order
			if (reloginMessage.getType() == JFGReloginMessage.ReloginMessageType.SEND_RELOGIN_PASSWORD) {
				reloginPassword = reloginMessage.getReloginPassword();
				System.out.println("relogin password received: " + reloginPassword);
			}
			else if (reloginMessage.getType() == JFGReloginMessage.ReloginMessageType.SERVER_RELOGIN_REQUEST) {
				System.out.println("relogin called");
				relogin();
			}
			communicationSecurity.sendAcknowledge(message);
		}
		else {
			if (!communicationSecurity.isResentMessage(message)) {
				if (messageOrder.isInOrder(message)) {//checks and buffers if false
					super.receiveMessage(message);
					JFGSecurableMessage bufferedMessage;
					while ((bufferedMessage = messageOrder.getNextBufferedMessage()) != null) {
						//receive all buffered messages
						super.receiveMessage((JFGClientMessage) bufferedMessage);
					}
				}
			}
			communicationSecurity.sendAcknowledge(message);
		}
	}
	
	/**
	 * Re-login this client to the server when the connection broke.
	 */
	private void relogin() {
		//close the connection and start a new one in a new thread
		//close it in a synchronized block to ensure that there is no more data send while restarting
		Thread restartThread = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (JFGSecureMessageClient.this) {
					System.out.println("closing connection");
					closeConnection();
					System.out.println("restarting connection");
					startClient();
					try {
						Thread.sleep(1000);
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					System.out.println("sending client relogin request");
					JFGReloginMessage reloginMessage = new JFGReloginMessage(JFGReloginMessage.ReloginMessageType.CLIENT_RELOGIN_REQUEST);
					reloginMessage.setReloginPassword(reloginPassword);
					sendMessage(reloginMessage);
				}
			}
		});
		restartThread.start();
	}
}