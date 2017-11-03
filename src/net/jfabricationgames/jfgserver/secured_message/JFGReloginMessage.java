package net.jfabricationgames.jfgserver.secured_message;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;

public class JFGReloginMessage implements JFGClientMessage, JFGServerMessage, JFGSecurableMessage {
	
	private static final long serialVersionUID = -690310463597863214L;
	
	private int sendCount;
	
	private ReloginMessageType type;
	
	private String reloginPassword;
	
	private int securingId;
	
	public enum ReloginMessageType {
		SEND_RELOGIN_PASSWORD,//send the password for re-login from the server to the client 
		CLIENT_RELOGIN_REQUEST,//request a re-login (sent from client to server)
		SERVER_RELOGIN_REQUEST;//request the client to re-login to the server (sent from server to client because the server can't establish the connection)
	}
	
	public JFGReloginMessage(ReloginMessageType type) {
		this.type = type;
		securingId = (int) (Math.random() * Integer.MAX_VALUE);
	}
	
	@Override
	public int getMessageId() {
		return securingId;
	}
	
	public ReloginMessageType getType() {
		return type;
	}
	public void setType(ReloginMessageType type) {
		this.type = type;
	}
	
	public String getReloginPassword() {
		return reloginPassword;
	}
	public void setReloginPassword(String reloginPassword) {
		this.reloginPassword = reloginPassword;
	}
	
	@Override
	public int getSendCount() {
		return sendCount;
	}
	@Override
	public void setSendCount(int count) {
		sendCount = count;
	}
}