package net.jfabricationgames.jfgserver.interpreter;

import net.jfabricationgames.jfgserver.client.JFGClient;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.server.JFGConnection;
import net.jfabricationgames.jfgserver.server.JFGServer;

public interface JFGServerInterpreter {
	
	/**
	 * Interpret the messages that are sent from the {@link JFGClient} to the {@link JFGServer} of this interpreter and react on the clients requests.
	 * 
	 * @param message
	 * 		The message the client sent.
	 * 
	 * @param connection
	 * 		The connection that sent the message.
	 */
	public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection);
	
	/**
	 * Create a new independent instance of this class to be added into a new {@link JFGConnection}.
	 * 
	 * @return
	 * 		The new instance.
	 */
	public JFGServerInterpreter getInstance();
}