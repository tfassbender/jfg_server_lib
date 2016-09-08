package jfgServerExample.basicExample;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;
import net.jfabricationgames.jfgserver.server.JFGConnection;

public class BasicServerInterpreter implements JFGServerInterpreter {
	
	public BasicServerInterpreter() {
		
	}
	
	@Override
	public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
		//do something that echoes the received messages here
		if (message instanceof JFGClientMessage) {
			connection.sendMessage((JFGClientMessage) message);
			System.out.println("Echoed message.");
		}
		else {
			System.err.println("Can't echo the message. It doesn't implement JFGClientMessage.");
		}
	}
	
	@Override
	public JFGServerInterpreter getInstance() {
		//return a new independent instance of this class here
		return new BasicServerInterpreter();
	}
}