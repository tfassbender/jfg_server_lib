package jfgServerExample.groupServer;

import net.jfabricationgames.jfgserver.client.JFGClient;
import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGClientInterpreter;

public class GroupClientInterpreter implements JFGClientInterpreter {

	private boolean isEnabled = false;
	
	@Override
	public void interpreteClientMessage(JFGClientMessage message, JFGClient connection) {
		//react on the message you received from the server here.
		if (message instanceof BasicMessage) {
			BasicMessage basic = (BasicMessage) message;
			//just print the data on the terminal for this test
			System.out.println(basic.getMessage());
			System.out.println(basic.getFirstInt());
		}
		else {
			if (!isEnabled) {
				isEnabled = true;
				//the group was created so you may now send your message.
				//send a random number between 41 and 43 so every client sends another message (probably)
				BasicMessage answer = new BasicMessage("The Answer is:", (int) (Math.random()*3 + 41));
				connection.sendMessage(answer);
			}			
		}
	}
	
	public boolean isEnabled() {
		return isEnabled;
	}
}
