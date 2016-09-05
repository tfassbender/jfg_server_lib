package net.jfabricationgames.jfgserver.server;

import java.util.ArrayList;
import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

public class JFGLoginServer extends JFGServer {
	
	private List<JFGConnectionGroup> groups;
	
	private JFGConnectionGroup connectionGroup;
	
	private List<JFGConnection> waitingConnections;

	public JFGLoginServer(int port) {
		super(port);
		groups = new ArrayList<JFGConnectionGroup>();
		connectionGroup = new DefaultJFGConnectionGroup();
	}

	@Override
	public void chooseInterpreter() {
		setInterpreterFactory(new DefaultLoginInterpreter(this));
	}
	
	@Override
	public void addConnection(JFGConnection connection) {
		waitingConnections.add(connection);
	}
	
	private class DefaultLoginInterpreter implements JFGServerInterpreter {
		
		private JFGLoginServer server;
		
		public DefaultLoginInterpreter(JFGLoginServer server) {
			this.server = server;
		}
		
		@Override
		public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
			if (server.isLoggedIn(connection)) {
				if (message instanceof JFGClientMessage) {
					sendBroadcast((JFGClientMessage) message);
				}
				else {
					System.err.println("JFGLoginServer: Couldn't repeat. Message doesn't implement JFGClientMessage");
				}
			}
			else {
				//just accept every login
				server.acceptLogin(connection);
			}
		}
		
		@Override
		public JFGServerInterpreter getInstance() {
			return new DefaultLoginInterpreter(server);
		}
	}
	
	public void acceptLogin(JFGConnection connection) throws IllegalArgumentException {
		if (waitingConnections.remove(connection)) {
			connections.add(connection);
		}
		else {
			throw new IllegalArgumentException("The connection is not in the waiting list.");
		}
	}
	
	public void denyLogin(JFGConnection connection) throws IllegalArgumentException {
		if (waitingConnections.remove(connection)) {
			connection.endConnection();
		}
		else {
			throw new IllegalArgumentException("The connection is not in the waiting list.");
		}
	}
	
	public boolean isLoggedIn(JFGConnection connection) {
		return connections.contains(connection);
	}
	
	public void createGroup(List<JFGConnection> connections) {
		JFGConnectionGroup group = connectionGroup.getInstance(connections);
		groups.add(group);
	}
	
	public List<JFGConnectionGroup> getGroups(JFGConnection connection) {
		List<JFGConnectionGroup> groups = new ArrayList<JFGConnectionGroup>(this.groups.size());
		for (JFGConnectionGroup g : groups) {
			for (JFGConnection c : g.getConnections()) {
				if (c.equals(connection)) {
					groups.add(g);
				}
			}
		}
		return groups;
	}
	
	public void sendBroadcast(JFGClientMessage message) {
		for (JFGConnection con : connections) {
			con.sendMessage(message);
		}
	}
}