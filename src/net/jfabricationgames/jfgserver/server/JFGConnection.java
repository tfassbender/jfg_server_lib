package net.jfabricationgames.jfgserver.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

public class JFGConnection implements Runnable {
	
	private JFGServer server;
	private Socket socket;
	
	private ObjectInputStream serverIn;
	private ObjectOutputStream serverOut;
	
	private Thread connection;
	
	private JFGConnectionGroup group;
	private JFGServerInterpreter interpreter; 
	
	public JFGConnection(JFGServer server, Socket socket) throws IOException {
		this.server = server;
		this.socket = socket;
		serverIn = new ObjectInputStream(socket.getInputStream());
		serverOut = new ObjectOutputStream(socket.getOutputStream());
	}
	public JFGConnection(JFGServer server, Socket socket, JFGServerInterpreter interpreter) throws IOException {
		this.server = server;
		this.socket = socket;
		this.interpreter = interpreter;
		if (interpreter == null) {
			throw new IllegalArgumentException("Interpreter mussnt be null.");
		}
		serverIn = new ObjectInputStream(socket.getInputStream());
		serverOut = new ObjectOutputStream(socket.getOutputStream());
		startConnection();
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				Object clientRequest = serverIn.readObject();
				if (clientRequest instanceof JFGServerMessage) {
					interpreter.interpreteServerMessage((JFGServerMessage) clientRequest, this);
				}
				else {
					System.err.println("JFGConnection: Received object is no JFGServerMessage. Couldn't interprete the message.");
				}
				Thread.sleep(100);
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		catch (InterruptedException ie) {
			//ie.printStackTrace();
		}
		catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	
	private void startConnection() {
		connection = new Thread(this);
		connection.start();
	}
	
	public <T extends JFGClientMessage> void sendMessage(T message) {
		try {
			serverOut.writeObject(message);
			serverOut.flush();
		}
		catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	protected JFGConnectionGroup getGroup() {
		return group;
	}
	protected void setGroup(JFGConnectionGroup group) {
		this.group = group;
	}
	
	public JFGServerInterpreter getInterpreter() {
		return interpreter;
	}
	public void setInterpreter(JFGServerInterpreter interpreter) {
		this.interpreter = interpreter;
		if (connection == null) {
			startConnection();
		}
	}
	
	public JFGServer getServer() {
		return server;
	}
	public Socket getSocket() {
		return socket;
	}
}