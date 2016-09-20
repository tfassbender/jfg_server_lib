package jfgServerExample.loginServer;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;

public class DefaultMessage implements JFGServerMessage, JFGClientMessage {
	
	private static final long serialVersionUID = -5225852701398192785L;
	
	private String message;
	
	public DefaultMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}