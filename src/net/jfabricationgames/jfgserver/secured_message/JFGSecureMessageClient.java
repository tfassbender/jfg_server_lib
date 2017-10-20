package net.jfabricationgames.jfgserver.secured_message;

import java.io.IOException;

import net.jfabricationgames.jfgserver.client.JFGClient;
import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGClientInterpreter;

public class JFGSecureMessageClient extends JFGClient {
	
	private JFGCommunicationSecurity communicationSecurity;
	
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
		communicationSecurity.secureMessage(message);
		super.sendMessage(message);
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
		communicationSecurity.secureMessage(message);
		super.sendMessageUnshared(message);
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
					if (clientRequest instanceof JFGClientMessage) {
						receiveMessage((JFGClientMessage) clientRequest);
					}
					else {
						System.err.println("JFGClient: Received object is no JFGClientMessage. Couldn't interprete the message.");
					}
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
		else {
			if (!communicationSecurity.isResentMessage(message)) {
				super.receiveMessage(message);
			}
			communicationSecurity.sendAcknowledge(message);
		}
	}
}