package jfgServerExample.groupServer;

import java.io.IOException;

import net.jfabricationgames.jfgserver.server.JFGGroupServer;

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
		//create the new JFGGroupServer instance that listens on port 4711 (could also be any other port > 1024)
		JFGGroupServer server = new JFGGroupServer(4711, 3);
		//the second argument (3) is the group size that needs to be reached until a group is created. 
		//the interpreter is chosen to be a default implementation from the group server, which is enough for a simple example.
		//the server will now just broadcast everything that comes in to the sending client after the group was created.
		
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