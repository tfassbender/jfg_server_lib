package net.jfabricationgames.jfgserver.server;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;

public class DefaultJFGConnectionGroup implements JFGConnectionGroup {

	@Override
	public <T extends JFGClientMessage> void sendGroupBroadcast(T message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends JFGClientMessage> void sendMessage(T message, JFGConnection connection) {
		// TODO Auto-generated method stub
		
	}
	
}