package net.jfabricationgames.jfgserver.server;

import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;

/**
 * A simple implementation of a {@link JFGConnectionGroup} used as default in the {@link JFGGroupServer}.
 */
public class DefaultJFGConnectionGroup implements JFGConnectionGroup {
	
	private List<JFGConnection> connections;
	
	/**
	 * Create a new DefaultJFGConnectionGroup to use as a factory.
	 * This group doesn't contain any connections and is only used for the factory in the server.
	 * All instances containing {@link JFGConnection}s are to be created using the {@link JFGConnectionGroup#getInstance(List) getInstance()} method.
	 */
	public DefaultJFGConnectionGroup() {
		
	}
	public DefaultJFGConnectionGroup(List<JFGConnection> connections) {
		this.connections = connections;
		groupStarted();
	}
	
	@Override
	public JFGConnectionGroup getInstance(List<JFGConnection> connections) {
		DefaultJFGConnectionGroup group = new DefaultJFGConnectionGroup(connections);
		return group;
	}
	
	@Override
	public void groupStarted() {
		DefaultJFGMessage message = new DefaultJFGMessage();
		message.setMessage("Group Started");
		sendGroupBroadcast(message);
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