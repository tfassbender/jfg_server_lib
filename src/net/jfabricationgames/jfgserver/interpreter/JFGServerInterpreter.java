package net.jfabricationgames.jfgserver.interpreter;

import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.server.JFGConnection;

public interface JFGServerInterpreter {

	public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection);
}