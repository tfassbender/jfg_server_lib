package net.jfabricationgames.jfgserver.interpreter;

import net.jfabricationgames.jfgserver.client.JFGClient;
import net.jfabricationgames.jfgserver.client.JFGClientMessage;

public interface JFGClientInterpreter {
	
	public void interpreteClientMessage(JFGClientMessage message, JFGClient connection);
}