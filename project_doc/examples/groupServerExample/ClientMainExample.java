package jfgServerExample.groupServer;

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
		//create a new JFGClient that connects to the group server from the example in groupServer.ServerMainExample
		//String host = "jfabricationgames.ddns.net";
		String host = "localhost";
		int port = 4711;//the port needs to be the same as set in the server to correctly connect.
		GroupClientInterpreter interpreter = new GroupClientInterpreter();
		JFGClient client = new JFGClient(host, port, interpreter);
		System.out.println("client started");
		//using the JFGClient(String, int, JFGClientInterpreter) constructor the client is created and started directly.
		//by using another constructor without the JFGClientInterpreter the client would not be started until it gets
		//a client interpreter by the JFGClient.setClientInterpreter(JFGClientInterpreter) method.
		
		//after the client is created and started you have to wait until the server created a group.
		//the group is created as soon as there are enough connections (3 in this example).
		//when the group is created it sends a message to the client telling him that the group was created and he may now 
		//start to send what ever he wants.
		//This message is send by the default implementation in the group server but is to be implemented by you if you 
		//create your own server interpreter
		
		
		//If you start all the clients from one computer and they try to use the same socket on the same port it probably will
		//cause the program to throw an exception because the data couldn't read all objects on the same socket correctly.
		//So in most cases if you start all the connections on one computer a java.io.StreamCorruptedException or a 
		//java.io.EOFException will occur, causing the program to crash.
		
		
		//the message in this example is not send here but from the GroupClientInterpreter when the group is created.
		
		
		//after the client was created you first have to wait until the group is created to send and receive the messages:
		while (!interpreter.isEnabled()) {
			try {
				//wait for the group to be created.
				Thread.sleep(100);
			}
			catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		
		//after sending the message it takes a short time for the server to get the message and react to it.
		//so if you want to receive a message you need to wait.
		try {
			//one second should be more than enough.
			Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
			ie.printStackTrace();
		}
		
		//after the messages are sent and the connection to the server is no longer needed (probably when closing the program)
		//the client should be closed:
		client.closeConnection();
		
		//depending on your implementation you could also first send a message to the server that informs the server that this
		//connection is closed now. But the server also realizes that the connection was closed and removes the connection from
		//it's list of active connections.
	}
}