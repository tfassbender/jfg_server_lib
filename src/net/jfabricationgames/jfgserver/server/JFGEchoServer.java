package net.jfabricationgames.jfgserver.server;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

public class JFGEchoServer extends JFGServer {

	public JFGEchoServer(int port) {
		super(port);
	}
	
	@Override
	public void chooseInterpreter() {
		setInterpreterFactory(new EchoInterpreter());
	}
	
	@Override
	public void addInterpreter(JFGConnection connection) {
		connection.setInterpreter(new EchoInterpreter());
	}
	
	private class EchoInterpreter implements JFGServerInterpreter {
		
		@Override
		public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
			if (message instanceof JFGClientMessage) {
				connection.sendMessage((JFGClientMessage) message);
			}
			else {
				System.err.println("JFGEchoServer: Couldn't repeat. Message doesn't implement JFGClientMessage");
			}
		}

		@Override
		public JFGServerInterpreter getInstance() {
			return new EchoInterpreter();
		}
	}
}