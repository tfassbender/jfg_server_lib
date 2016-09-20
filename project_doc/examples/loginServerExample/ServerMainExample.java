package jfgServerExample.loginServer;

import java.io.IOException;

import net.jfabricationgames.jfgserver.server.JFGLoginServer;

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
		//create the new JFGLoginServer instance that listens on port 4711 (could also be any other port > 1024)
		JFGLoginServer server;
		server = new JFGLoginServer(4711);
		//server = new CustomLoginServer(4711);
		
		//the interpreter is implemented in DefaultLoginServerInterpreter.
		server.setInterpreterFactory(new DefaultLoginServerInterpreter(server));
		
		//now the server just needs to be started and thats it.
		try {
			server.startServer();
			System.out.println("server started");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}