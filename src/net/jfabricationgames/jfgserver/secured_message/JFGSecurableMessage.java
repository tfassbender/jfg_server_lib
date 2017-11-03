package net.jfabricationgames.jfgserver.secured_message;

public interface JFGSecurableMessage {
	
	/**
	 * Get the id of the message that is sent back within an JFGAcknowledgeMessage when the message was received.
	 * 
	 * The id should be unique for every message (e.g. by using a big random)
	 */
	public int getMessageId();
	
	/**
	 * Get a send count from a message.
	 * 
	 * The count is set and read by the JFGSecureMessage framework.
	 * The implementing class only has to store this count.
	 */
	public int getSendCount();
	/**
	 * Set the send count of a message.
	 * 
	 * The count is set and read by the JFGSecureMessage framework.
	 * The implementing class only has to store this count.
	 */
	public void setSendCount(int count);
}