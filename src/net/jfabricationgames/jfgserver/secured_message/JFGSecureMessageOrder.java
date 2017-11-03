package net.jfabricationgames.jfgserver.secured_message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to make sure that the messages received are in the right order and didn't overtake an earlier message.
 */
public class JFGSecureMessageOrder {
	
	private Map<Integer, JFGSecurableMessage> messageBuffer;
	
	private int lastReceived;
	
	private int sendCount;
	
	public JFGSecureMessageOrder() {
		messageBuffer = new HashMap<Integer, JFGSecurableMessage>();
		lastReceived = 0;
		sendCount = 0;
	}
	
	/**
	 * Check whether the received message was in the right order.
	 */
	public boolean isInOrder(Serializable message) {
		if (message instanceof JFGSecurableMessage) {
			JFGSecurableMessage securableMessage = ((JFGSecurableMessage) message);
			if (securableMessage.getSendCount() == lastReceived+1) {
				lastReceived++;
				return true;
			}
			else if (securableMessage.getSendCount() <= lastReceived) {
				//also accept messages that don't use the send count if their count is lower that the current expected.
				return true;
			}
			else {
				messageBuffer.put(securableMessage.getSendCount(), securableMessage);
				return false;
			}
		}
		else {
			throw new JFGSecureCommunicationException("The message sent can't be secured because it doesn't implement JFGSecurableMessage.");
		}
	}
	
	/**
	 * Get the next buffered message or null if there is no such message.
	 */
	public JFGSecurableMessage getNextBufferedMessage() {
		JFGSecurableMessage message = messageBuffer.get(lastReceived+1);
		if (message != null) {
			lastReceived++;
			messageBuffer.remove(lastReceived);
			return message;
		}
		else {
			return null;
		}
	}
	
	/**
	 * Add the next send count to a message.
	 */
	public void addSendCount(Serializable message) {
		if (message instanceof JFGSecurableMessage) {
			sendCount++;
			((JFGSecurableMessage) message).setSendCount(sendCount);
		}
		else if (!(message instanceof JFGAcknowledgeMessage)) {
			throw new JFGSecureCommunicationException("The message sent can't be secured because it doesn't implement JFGSecurableMessage.");
		}
	}
}