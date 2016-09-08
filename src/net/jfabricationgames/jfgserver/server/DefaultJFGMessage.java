package net.jfabricationgames.jfgserver.server;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;

/**
 * A default implementation of a message to be send to the server or client.
 */
public class DefaultJFGMessage implements JFGClientMessage, JFGServerMessage {
	
	private static final long serialVersionUID = 1911908950367832283L;
	
	private String message;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}