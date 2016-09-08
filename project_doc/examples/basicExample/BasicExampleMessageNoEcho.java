package jfgServerExample.basicExample;

import net.jfabricationgames.jfgserver.client.JFGServerMessage;

/**
 * This class is used as an example for a message that can be sent to the server, but can't be echoed because it doesn't 
 * implement JFGClientMessage.
 */
public class BasicExampleMessageNoEcho implements JFGServerMessage {
	
	private static final long serialVersionUID = 6781435771156169802L;
	
	//just a not used field. Only to send something to the server.
	@SuppressWarnings("unused")
	private String foo = "A message that can't be interpreted";
}