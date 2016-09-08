package jfgServerExample.basicExample;

import java.io.IOException;

import net.jfabricationgames.jfgserver.server.JFGEchoServer;

public class ServerMainExample {
	
	/**
	 * The server side of the execution starts here. 
	 */
	public static void main(String[] args) {
		new ServerMainExample();
	}
	
	/**
	 * Create the ServerMainExample that contains the server class and function.
	 */
	public ServerMainExample() {
		//create the new JFGEchoServer instance that listens on port 4711 (could also be any other port > 1024)
		JFGEchoServer server = new JFGEchoServer(4711);
		//the interpreter is chosen to be a default implementation from the echo server, which is enough for a simple echo server
		//the server will now just resent everything that comes in to the sending client.
		
		//messages sent to the echo server will only be echoed if they implement JFGServerMessage AND JFGClientMessage.
		//if you need to echo the messages in another way you need to create your own implementation of JFGServerInterpreter
		//and add it to the server:
		server.setInterpreterFactory(new BasicServerInterpreter());
		
		//anyways creating a new interpreter and adding it to the echo server would not really fit the sense of the echo server
		//because that overwrites the only implementation of this class...
		
		//if you need to create your own interpreter for just echoing messages you could also create your own implementation
		//of the JFGServer class. But changing the echo server would be okay too.
		
		//after the server has the right interpreter it just needs to be started:
		try {
			server.startServer();
			System.out.println("server started");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}