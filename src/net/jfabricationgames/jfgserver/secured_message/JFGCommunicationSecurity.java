package net.jfabricationgames.jfgserver.secured_message;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;

public class JFGCommunicationSecurity {
	
	private Thread ackTimerThread;
	
	private JFGSecureMessageClient client;
	private JFGSecureMessageConnection connection;
	
	private Map<Integer, Serializable> securedMessages;
	private Map<Integer, Integer> messageTimers;
	
	private Map<Integer, Serializable> receivedMessages;
	private Map<Integer, Integer> receivedMessageTimer;
	
	/**
	 * The time in seconds till a secured message is resent if no ACK arrived.
	 */
	public static final int RESENT_MESSAGE = 5;
	/**
	 * The time in seconds a received message is stored to prevent interpreting resent messages a second time.
	 */
	public static final int STORE_RECEIVED_MESSAGES = 60;
	
	public JFGCommunicationSecurity(JFGSecureMessageClient client) {
		this.client = client;
		init();
	}
	public JFGCommunicationSecurity(JFGSecureMessageConnection connection) {
		this.connection = connection;
		init();
	}
	
	private void init() {
		securedMessages = new HashMap<Integer, Serializable>();
		messageTimers = new HashMap<Integer, Integer>();
		receivedMessages = new HashMap<Integer, Serializable>();
		receivedMessageTimer = new HashMap<Integer, Integer>();
		ackTimerThread = createAckTimerThread();
		ackTimerThread.start();
	}
	
	/**
	 * Secure the message by checking if the receiver sends an ACK-Message back.
	 * If no ACK arrives after 'RESENT_MESSAGE' seconds the message is re-send.
	 * 
	 * @param message
	 * 		The message that is secured. The message needs to implement SecurableMessage to be secured.
	 * 
	 * @throws JFGSecureCommunicationException
	 * 		A JFGSecureCommunicationException is thrown when the message that is to be secured doesn't implement SecurableMessage.
	 */
	public void secureMessage(Serializable message) {
		if (message instanceof JFGSecurableMessage) {
			JFGSecurableMessage msg = (JFGSecurableMessage) message;
			securedMessages.put(msg.getMessageId(), message);
			messageTimers.put(msg.getMessageId(), 0);
			//System.out.println("Sending secured message (id: " + msg.getMessageId() + ")");
		}
		else if (!(message instanceof JFGAcknowledgeMessage)) {
			throw new JFGSecureCommunicationException("The message sent can't be secured because it doesn't implement SecurableMessage.");
		}
	}
	/**
	 * Receive an ACK-Message from the receiver of a message sent.
	 * 
	 * @param ackMessage
	 * 		The acknowledge message containing the acknowledgement id.
	 */
	public void receiveAcknoledgeMessage(JFGAcknowledgeMessage ackMessage) {
		int ackId = ackMessage.getAcknoledgingMessageId();
		//synchronize using this object to be sure to not get a concurrent modification in the timer thread
		synchronized (this) {
			messageTimers.remove(ackId);
			securedMessages.remove(ackId);
		}
		//System.out.println("Acknowledgement received (id: " + ackId + ")");
	}
	
	/**
	 * Check if a message that was received was already received a previous time (resent because the ACK was lost).
	 * The messages received are stored for 'STORE_RECEIVED_MESSAGES' seconds.
	 * 
	 * @param message
	 * 		The received message.
	 * 
	 * @return
	 * 		Returns true if the message is already known.
	 */
	public boolean isResentMessage(Serializable message) {
		if (message instanceof JFGSecurableMessage) {
			JFGSecurableMessage msg = (JFGSecurableMessage) message;
			Serializable received = receivedMessages.get(msg.getMessageId()); 
			if (received == null) {
				return false;
			}
			else {
				return message.equals(received);
			}
		}
		else {
			//the message was unsecured
			return false;
		}
	}
	
	/**
	 * Send the acknowledge message to a received message back to the sending instance.
	 *  
	 * @param message
	 * 		The received message.
	 */
	public void sendAcknowledge(Serializable message) {
		if (message instanceof JFGSecurableMessage) {
			JFGSecurableMessage msg = (JFGSecurableMessage) message;
			JFGAcknowledgeMessage ackMessage = new JFGAcknowledgeMessage(msg.getMessageId());
			if (client != null) {
				client.sendMessage(ackMessage);
			}
			else {
				connection.sendMessage(ackMessage);
			}
			//System.out.println("Sending acknowledgement (id: " + msg.getMessageId() + ")");
		}
	}
	
	/**
	 * Send a message that didn't reach the destination a second time.
	 * 
	 * @param messageId
	 * 		The id of the message that was lost.
	 */
	private void resendMessage(int messageId) {
		if (client != null) {
			try {
				client.resetOutput();
				client.sendMessage((JFGServerMessage) securedMessages.get(messageId));
			}
			catch (ClassCastException cce) {
				throw new JFGSecureCommunicationException("Couldn't resent a message.", cce);
			}
		}
		else {
			try {
				connection.resetOutput();
				connection.sendMessage((JFGClientMessage) securedMessages.get(messageId));
			}
			catch (ClassCastException cce) {
				throw new JFGSecureCommunicationException("Couldn't resent a message.", cce);
			}
		}
	}
	
	private Thread createAckTimerThread() {
		Thread ackTimerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						synchronized (JFGCommunicationSecurity.this) {
							for (int messageId : messageTimers.keySet()) {
								int timer = messageTimers.get(messageId);
								if (timer < 5) {
									messageTimers.put(messageId, timer+1);								
								}
								else {
									//re-send the message and reset the timer 
									messageTimers.put(messageId, 0);
									resendMessage(messageId);
								}
							}
							Integer removedMessage = null;
							for (int messageId : receivedMessageTimer.keySet()) {
								int timer = messageTimers.get(messageId);
								if (timer >= STORE_RECEIVED_MESSAGES) {
									removedMessage = messageId;
								}
								else {
									messageTimers.put(messageId, timer+1);
								}
							}
							if (removedMessage != null) {
								receivedMessageTimer.remove(removedMessage);
								receivedMessages.remove(removedMessage);
							}
						}
						Thread.sleep(1000);
					}
				}
				catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		});
		return ackTimerThread;
	}
}