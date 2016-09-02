package net.jfabricationgames.jfgserver.interpreter;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGConnection;

public interface JFGClientInterpreter {
	
	public void interpreteClientMessage(JFGClientMessage message, JFGConnection connection);
}