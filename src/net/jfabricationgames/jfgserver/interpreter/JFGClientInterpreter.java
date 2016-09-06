package net.jfabricationgames.jfgserver.interpreter;

import net.jfabricationgames.jfgserver.client.JFGClient;
import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.server.JFGServer;

public interface JFGClientInterpreter {
	
	/**
	 * Interpret the messages that are sent from the {@link JFGServer} to the {@link JFGClient} of this interpreter and react on the requests.
	 * 
	 * @param message
	 * 		The message the server sent.
	 * 
	 * @param connection
	 * 		The client that received the message.
	 */
	public void interpreteClientMessage(JFGClientMessage message, JFGClient connection);
}