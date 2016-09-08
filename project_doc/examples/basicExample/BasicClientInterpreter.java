package jfgServerExample.basicExample;

import net.jfabricationgames.jfgserver.client.JFGClient;
import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGClientInterpreter;

public class BasicClientInterpreter implements JFGClientInterpreter {
	
	@Override
	public void interpreteClientMessage(JFGClientMessage message, JFGClient connection) {
		//react on the message you received from the server here.
		if (message instanceof BasicMessage) {
			BasicMessage basic = (BasicMessage) message;
			//just print the data on the terminal for this test
			System.out.println(basic.getMessage());
			System.out.println(basic.getFirstInt());
		}
	}
}
