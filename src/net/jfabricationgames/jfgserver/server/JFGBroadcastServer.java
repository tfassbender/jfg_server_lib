package net.jfabricationgames.jfgserver.server;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

public class JFGBroadcastServer extends JFGServer {

	public JFGBroadcastServer(int port) {
		super(port);
	}
	
	@Override
	public void chooseInterpreter() {
		setInterpreterFactory(new BroadcastInterpreter());
	}
	
	public void sendBroadcast(JFGClientMessage message) {
		for (JFGConnection con : connections) {
			con.sendMessage(message);
		}
	}
	
	private class BroadcastInterpreter implements JFGServerInterpreter {

		@Override
		public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
			if (message instanceof JFGClientMessage) {
				sendBroadcast((JFGClientMessage) message);
			}
			else {
				System.err.println("JFGBroadcastServer: Couldn't repeat. Message doesn't implement JFGClientMessage");
			}
		}

		@Override
		public JFGServerInterpreter getInstance() {
			return new BroadcastInterpreter();
		}
	}
}