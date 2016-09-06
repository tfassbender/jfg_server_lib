package net.jfabricationgames.jfgserver.server;

import java.util.ArrayList;
import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

/**
 * This server is used to accept all connections and group them into variable sized groups.
 * If there are enough connections a group is created directly.
 * 
 * The groups used can be changed by changing the groupFactory using the setGroupFactory() method.
 */
public class JFGGroupServer extends JFGServer {
	
	private int groupSize;
	private List<JFGConnectionGroup> groups;
	
	private List<JFGConnection> tmpGroup;
	
	private JFGConnectionGroup connectionGroup;

	public JFGGroupServer(int port, int groupSize) {
		super(port);
		this.groupSize = groupSize;
		groups = new ArrayList<JFGConnectionGroup>();
		tmpGroup = new ArrayList<JFGConnection>(groupSize);
		connectionGroup = new DefaultJFGConnectionGroup();
	}
	
	@Override
	public void chooseInterpreter() {
		setInterpreterFactory(new DefaultGroupInterpreter());
	}
	
	/**
	 * Add the connection to the known connections and create a group of the connections.
	 * The group is created directly when there are enough connections for a group.
	 */
	@Override
	public void addConnection(JFGConnection connection) {
		super.addConnection(connection);
		tmpGroup.add(connection);
		if (tmpGroup.size() == groupSize) {
			JFGConnectionGroup group = connectionGroup.getInstance(tmpGroup);
			groups.add(group);
			tmpGroup = new ArrayList<JFGConnection>(groupSize);
		}
	}
	
	/**
	 * A simple implementation of a group server interpreter.
	 */
	private class DefaultGroupInterpreter implements JFGServerInterpreter {
		
		@Override
		public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
			if (message instanceof JFGClientMessage) {
				JFGConnectionGroup group = getGroup(connection);
				if (group != null) {
					group.sendGroupBroadcast((JFGClientMessage) message);
				}
				else {
					System.err.println("JFGGroupServer: Couldn't send broadcast. The connection has no group.");
				}
			}
			else {
				System.err.println("JFGGroupServer: Couldn't repeat. Message doesn't implement JFGClientMessage.");
			}
		}

		@Override
		public JFGServerInterpreter getInstance() {
			return new DefaultGroupInterpreter();
		}
	}
	
	/**
	 * Find the group of a JFGConnecion.
	 * 
	 * @param connection
	 * 		The connection which's group is searched.
	 * 
	 * @return
	 * 		The group of connection.
	 */
	public JFGConnectionGroup getGroup(JFGConnection connection) {
		JFGConnectionGroup group = null;
		for (JFGConnectionGroup g : groups) {
			for (JFGConnection c : g.getConnections()) {
				if (c.equals(connection)) {
					group = g;
				}
			}
		}
		return group;
	}
	
	/**
	 * Get the current JFGConnectionGroup that is used as the factory for all created groups.
	 * 
	 * @return
	 * 		The current group factory.
	 */
	public JFGConnectionGroup getGroupFactory() {
		return connectionGroup;
	}
	/**
	 * Set the group factory to a JFGConnectionGroup to create new groups.
	 * The new groups are created by the getInstance() method of the JFGConnecionGroup implementation.
	 * 
	 * @param connectionGroup
	 * 		The new factory.
	 */
	public void setGroupFactory(JFGConnectionGroup connectionGroup) {
		this.connectionGroup = connectionGroup;
	}
}