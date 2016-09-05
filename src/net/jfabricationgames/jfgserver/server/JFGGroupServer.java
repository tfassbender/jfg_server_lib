package net.jfabricationgames.jfgserver.server;

import java.util.ArrayList;
import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

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
	
	public JFGConnectionGroup getConnectionGroup() {
		return connectionGroup;
	}
	public void setConnectionGroup(JFGConnectionGroup connectionGroup) {
		this.connectionGroup = connectionGroup;
	}
}