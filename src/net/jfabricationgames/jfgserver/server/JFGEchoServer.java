package net.jfabricationgames.jfgserver.server;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

/**
 * A simple implementation of an echo server that just sends back everything it receives.
 */
public class JFGEchoServer extends JFGServer {
	
	/**
	 * Create a new EchoServer on a port.
	 * 
	 * @param port
	 * 		The port number the server is listening to.
	 */
	public JFGEchoServer(int port) {
		super(port);
	}
	
	@Override
	public void chooseInterpreter() {
		setInterpreterFactory(new EchoInterpreter());
	}
	
	/**
	 * A simple implementation of a server interpreter for an echo server.
	 */
	private class EchoInterpreter implements JFGServerInterpreter {
		
		/**
		 * Interpreter the message from the client.
		 * Just send the same message back if possible.
		 * 
		 * @param message
		 * 		The message from the client.
		 * 
		 * @param connection
		 * 		The connection that received the message.
		 */
		@Override
		public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
			if (message instanceof JFGClientMessage) {
				connection.sendMessage((JFGClientMessage) message);
			}
			else {
				System.err.println("JFGEchoServer: Couldn't repeat. Message doesn't implement JFGClientMessage");
			}
		}
		
		@Override
		public JFGServerInterpreter getInstance() {
			return new EchoInterpreter();
		}
	}
}