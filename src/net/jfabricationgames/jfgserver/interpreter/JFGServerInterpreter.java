package net.jfabricationgames.jfgserver.interpreter;

import net.jfabricationgames.jfgserver.client.JFGConnection;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;

public interface JFGServerInterpreter {

	public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection);
}