package jfgServerExample.groupServer;

import java.util.ArrayList;
import java.util.List;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;

/**
 * A BasicMessage is a very simple example for a class that implements JFGClientMessage and JFGServerMessage.
 * Because of this two interfaces the objects of this class can be sent to a JFGServer or a JFGClient.
 * 
 * The object is sent through an object stream. The data don't need to be parsed but can be just sent.
 */
public class BasicMessage implements JFGClientMessage, JFGServerMessage {
	
	/**
	 * The serialVersionUID should be created for every class that implements JFGClientMessage or JFGServerMessage because
	 * they are sub-interfaces of java.lang.Serializable.
	 */
	private static final long serialVersionUID = -3606392259884980324L;
	
	private String message;
	
	private List<Integer> integers;
	
	/**
	 * Create a new BasicMessage.
	 */
	public BasicMessage(String message, int integer) {
		this.message = message;
		integers = new ArrayList<Integer>();
		integers.add(integer);
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getFirstInt() {
		return integers.get(0);
	}
}