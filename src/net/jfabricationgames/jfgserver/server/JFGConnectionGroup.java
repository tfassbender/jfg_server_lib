package net.jfabricationgames.jfgserver.server;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;

public interface JFGConnectionGroup {
	
	public <T extends JFGClientMessage> void sendGroupBroadcast(T message);
	
	public <T extends JFGClientMessage> void sendMessage(T message, JFGConnection connection);
}