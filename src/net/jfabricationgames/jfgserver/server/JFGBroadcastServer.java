package net.jfabricationgames.jfgserver.server;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

/**
 * A simple implementation of a broadcast server to send broadcast messages to every known {@link JFGConnection}.
 */
public class JFGBroadcastServer extends JFGServer {
	
	/**
	 * Create a new BroadcastServer on a port.
	 * 
	 * @param port
	 * 		The port number the server is listening to.
	 */
	public JFGBroadcastServer(int port) {
		super(port);
	}
	
	@Override
	public void chooseInterpreter() {
		setInterpreterFactory(new BroadcastInterpreter());
	}
	
	/**
	 * Send a broadcast message (message to all known connections).
	 * 
	 * @param message
	 * 		The message to be sent.
	 */
	public void sendBroadcast(JFGClientMessage message) {
		for (JFGConnection con : connections) {
			con.sendMessage(message);
		}
	}
	
	/**
	 * A simple implementation of a broadcast interpreter.
	 */
	private class BroadcastInterpreter implements JFGServerInterpreter {
		
		@Override
		public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
			if (message instanceof JFGClientMessage) {
				sendBroadcast((JFGClientMessage) message);
			}
			else {
				System.err.println("JFGBroadcastServer: Couldn't repeat. Message doesn't implement JFGClientMessage");
			}
		}
		
		@Override
		public JFGServerInterpreter getInstance() {
			return new BroadcastInterpreter();
		}
	}
}