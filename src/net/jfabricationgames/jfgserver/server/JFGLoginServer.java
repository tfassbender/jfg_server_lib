package net.jfabricationgames.jfgserver.server;

import java.util.ArrayList;
import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

/**
 * The JFGLoginServer is the most advanced (default) implementation of a {@link JFGServer}.
 * 
 * The default version of the {@link JFGServerInterpreter} just accepts every connection on the first received message.
 * In a real implementation the user data should be checked.
 * 
 * The server is also able to create and manage groups of connections.
 */
public class JFGLoginServer extends JFGServer {
	
	private List<JFGConnectionGroup> groups;
	
	private JFGConnectionGroup groupFactory;
	
	private List<JFGConnection> waitingConnections;

	public JFGLoginServer(int port) {
		super(port);
		groups = new ArrayList<JFGConnectionGroup>();
		groupFactory = new DefaultJFGConnectionGroup();
	}

	@Override
	public void chooseInterpreter() {
		setInterpreterFactory(new DefaultLoginInterpreter(this));
	}
	
	@Override
	public void addConnection(JFGConnection connection) {
		waitingConnections.add(connection);
	}
	
	/**
	 * A default implementation of the server interpreter that just accepts every login and sends broadcasts.
	 */
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
	
	/**
	 * Accept the login of a {@link JFGConnection} and move it from the pending list to the list of active connections.
	 * 
	 * @param connection
	 * 		The connection that is accepted.
	 * 
	 * @throws IllegalArgumentException
	 * 		An {@link IllegalArgumentException} is thrown if the connection is not in the pending list.
	 */
	public void acceptLogin(JFGConnection connection) throws IllegalArgumentException {
		if (waitingConnections.remove(connection)) {
			connections.add(connection);
		}
		else {
			throw new IllegalArgumentException("The connection is not in the waiting list.");
		}
	}
	
	/**
	 * Deny the login of a {@link JFGConnection} and close it's connection.
	 * 
	 * @param connection
	 * 		The connection that is denied.
	 * 
	 * @throws IllegalArgumentException
	 * 		An {@link IllegalArgumentException} is thrown if the connection is not in the pending list.
	 */
	public void denyLogin(JFGConnection connection) throws IllegalArgumentException {
		if (waitingConnections.remove(connection)) {
			connection.endConnection();
		}
		else {
			throw new IllegalArgumentException("The connection is not in the waiting list.");
		}
	}
	
	/**
	 * Check whether a connection is logged in on the server.
	 * 
	 * @param connection
	 * 		The connection to check.
	 * 
	 * @return
	 * 		True if the connection is listed as active connection.
	 */
	public boolean isLoggedIn(JFGConnection connection) {
		return connections.contains(connection);
	}
	
	/**
	 * Create a {@link JFGConnectionGroup} of a list of active connections and manage that group.
	 * 
	 * @param connections
	 * 		The connections that are added to a group.
	 */
	public JFGConnectionGroup createGroup(List<JFGConnection> connections) {
		JFGConnectionGroup group = groupFactory.getInstance(connections);
		groups.add(group);
		return group;
	}
	
	/**
	 * A list of all groups a connection is contained in.
	 * 
	 * @param connection
	 * 		The connection which's groups are searched.
	 * 
	 * @return
	 * 		A list of the connections groups.
	 */
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
	
	/**
	 * Send a broadcast message to all active connections.
	 * 
	 * @param message
	 * 		The message to be send.
	 */
	public void sendBroadcast(JFGClientMessage message) {
		for (JFGConnection con : connections) {
			con.sendMessage(message);
		}
	}
	
	public JFGConnectionGroup getGroupFactory() {
		return groupFactory;
	}
	public void setGroupFactory(JFGConnectionGroup groupFactory) {
		this.groupFactory = groupFactory;
	}
}