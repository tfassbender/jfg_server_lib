package net.jfabricationgames.jfgserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public abstract class JFGServer {
	
	protected int port;
	protected List<JFGConnection> connections;
	
	public JFGServer(int port) {
		this.port = port;
	}
	
	public void startServer() throws IOException {
		ServerSocket welcomeSocket = new ServerSocket(port);
		try {
			while (true) {
				Socket connectionSocket = welcomeSocket.accept();
				JFGConnection connection = new JFGConnection(this, connectionSocket);
				addInterpreter(connection);
				addConnection(connection);
			}
		}
		catch (Exception e) {
			welcomeSocket.close();
		}
	}
	
	public abstract void addInterpreter(JFGConnection connection);
	
	public void addConnection(JFGConnection connection) {
		connections.add(connection);
	}
	
	public List<JFGConnection> getConnections() {
		return connections;
	}
	public void closeConnection(JFGConnection con) {
		connections.remove(con);
	}
}