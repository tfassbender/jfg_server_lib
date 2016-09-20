package jfgServerExample.loginServer;

import java.util.ArrayList;
import java.util.List;

import net.jfabricationgames.jfgserver.server.DefaultJFGConnectionGroup;
import net.jfabricationgames.jfgserver.server.JFGConnection;
import net.jfabricationgames.jfgserver.server.JFGConnectionGroup;
import net.jfabricationgames.jfgserver.server.JFGLoginServer;

/**
 * 	This implementation of a custom JFGLoginServer shows how to overwrite the default implementation to customize you server.
 * 	
 *	In this customized implementation a group of users is created when there are three users loged in.
 */
public class CustomLoginServer extends JFGLoginServer {
	
	private List<JFGConnection> groupConnections;
	
	public CustomLoginServer(int port) {
		super(port);
		//set the group factory of the server to a new group.
		setGroupFactory(new DefaultJFGConnectionGroup());
		//the DefaultJFGConnectionGroup is the same as used in JFGLoginServer. It's just changed for the example.
		groupConnections = new ArrayList<JFGConnection>(3);
	}
	
	@Override
	public void addConnection(JFGConnection connection) {
		super.addConnection(connection);
		synchronized (this) {
			//if there are three connections logged in create a group of them.
			groupConnections.add(connection);
			if (groupConnections.size() == 3) {
				JFGConnectionGroup group = createGroup(groupConnections);
				//Send a message to the connections to inform them that they're grouped.
				DefaultMessage groupMessage = new DefaultMessage("__GroupCreated__");
				for (JFGConnection con : group.getConnections()) {
					con.sendMessage(groupMessage);
				}
			}
		}
	}
}