package net.jfabricationgames.jfgserver.server;

import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;

public interface JFGConnectionGroup {
	
	public JFGConnectionGroup getInstance(List<JFGConnection> connections);
	
	public <T extends JFGClientMessage> void sendGroupBroadcast(T message);
	
	public <T extends JFGClientMessage> void sendMessage(T message, JFGConnection fromConnection);
	
	public List<JFGConnection> getConnections();
}