package net.jfabricationgames.jfgserver.server;

import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;

public interface JFGConnectionGroup {
	
	/**
	 * Create a new instance of this JFGConnectionGroup implementation, to group the {@link JFGConnection} in the parameter list.
	 * This new instance is created by the server.
	 *  
	 * @param connections
	 * 		A list of the connections that are grouped in the new instance.
	 * 
	 * @return
	 * 		The new instance of this JFGConnectionGroup implementation.
	 */
	public JFGConnectionGroup getInstance(List<JFGConnection> connections);
	
	/**
	 * Inform the clients of this group that the group was created and can be used now.
	 */
	public void groupStarted();
	
	/**
	 * Send a broadcast message to all connections in this group.
	 * 
	 * @param message
	 * 		The message sent to all connections.
	 */
	public <T extends JFGClientMessage> void sendGroupBroadcast(T message);
	
	/**
	 * Send a broadcast message to all connections in this group except the connection that sent the message.
	 * 
	 * @param message
	 * 		The message sent to all connections.
	 * 
	 * @param fromConnection
	 * 		The original connection that sent the message.
	 */
	public <T extends JFGClientMessage> void sendMessage(T message, JFGConnection fromConnection);
	
	/**
	 * Get a reference of all connections that are in this group.
	 * 
	 * @return
	 * 		All connections of this group.
	 */
	public List<JFGConnection> getConnections();
}