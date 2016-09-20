package jfgServerExample.loginServer;

import net.jfabricationgames.jfgserver.client.JFGClient;

public class ClientMainExample {
	
	/**
	 * The client side of the implementation starts here.
	 * 
	 * The connection doesn't need to be set up directly after starting the program.
	 * You can do this any time you want.
	 */
	public static void main(String[] args) {
		new ClientMainExample();
	}
	
	/**
	 * Create the ClientMainExample that contains the client side connection to the server.
	 */
	public ClientMainExample() {
		//create a new JFGClient that connects to the login server from the example in loginServer.ServerMainExample
		//String host = "jfabricationgames.ddns.net";
		String host = "localhost";
		int port = 4711;//the port needs to be the same as set in the server to correctly connect.
		DefaultLoginClientInterpreter interpreter = new DefaultLoginClientInterpreter();
		JFGClient client = new JFGClient(host, port, interpreter);
		System.out.println("client started\n");
		//using the JFGClient(String, int, JFGClientInterpreter) constructor the client is created and started directly.
		//by using another constructor without the JFGClientInterpreter the client would not be started until it gets
		//a client interpreter by the JFGClient.setClientInterpreter(JFGClientInterpreter) method.
		
		//after the client is created it needs to login
		LoginMessage login;
		if (Math.random() > 0.5) {
			//login using the right user and password.
			login = new LoginMessage("theAnswer", "42");
		}
		else {
			//login using the wrong user and password to demonstrate this case.
			login = new LoginMessage("wrongAnswer", "43");
		}
		client.sendMessage(login);
		
		//after sending the message it takes a short time for the server to get the message and react to it.
		//so if you want to receive a message you need to wait.
		try {
			//wait for 10 seconds to receive messages from possible connected users.
			Thread.sleep(10000);
		}
		catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		System.out.println("\nClient closed");
		
		//after the messages are sent and the connection to the server is no longer needed (probably when closing the program)
		//the client should be closed:
		client.closeConnection();
		
		//depending on your implementation you could also first send a message to the server that informs the server that this
		//connection is closed now. But the server also realizes that the connection was closed and removes the connection from
		//it's list of active connections.
	}
}