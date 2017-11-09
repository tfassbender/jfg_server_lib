package net.jfabricationgames.jfgserver.secured_message;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;

/**
 * Simulate a StreamCorruptedException with this message.
 */
public class CorruptedMessage implements JFGServerMessage, JFGClientMessage, JFGSecurableMessage {
	
	private static final long serialVersionUID = 1189926860487939707L;

	@Override
	public int getMessageId() {
		return 0;
	}

	@Override
	public int getSendCount() {
		return 0;
	}

	@Override
	public void setSendCount(int count) {
		
	}
}