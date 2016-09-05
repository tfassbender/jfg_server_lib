package net.jfabricationgames.jfgserver.server;

import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;

public class DefaultJFGConnectionGroup implements JFGConnectionGroup {
	
	private List<JFGConnection> connections;
	
	public DefaultJFGConnectionGroup() {
		
	}
	private DefaultJFGConnectionGroup(List<JFGConnection> connections) {
		this.connections = connections;
	}
	
	@Override
	public JFGConnectionGroup getInstance(List<JFGConnection> connections) {
		DefaultJFGConnectionGroup group = new DefaultJFGConnectionGroup(connections);
		return group;
	}
	
	@Override
	public <T extends JFGClientMessage> void sendGroupBroadcast(T message) {
		for (JFGConnection con : connections) {
			con.sendMessage(message);
		}
	}
	
	@Override
	public <T extends JFGClientMessage> void sendMessage(T message, JFGConnection fromConnection) {
		for (JFGConnection con : connections) {
			if (!con.equals(fromConnection)) {
				con.sendMessage(message);
			}
		}
	}
	
	@Override
	public List<JFGConnection> getConnections() {
		return connections;
	}
}