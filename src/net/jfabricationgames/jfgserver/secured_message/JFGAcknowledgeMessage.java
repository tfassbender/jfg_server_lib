package net.jfabricationgames.jfgserver.secured_message;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;

public class JFGAcknowledgeMessage implements JFGClientMessage, JFGServerMessage {
	
	private static final long serialVersionUID = 8900952077551743235L;
	
	private int acknoledgingMessageId;
	
	public JFGAcknowledgeMessage(int acknoledgingMessageId) {
		this.acknoledgingMessageId = acknoledgingMessageId;
	}
	
	public int getAcknoledgingMessageId() {
		return acknoledgingMessageId;
	}
	public void setAcknoledgingMessageId(int acknoledgingMessageId) {
		this.acknoledgingMessageId = acknoledgingMessageId;
	}
}