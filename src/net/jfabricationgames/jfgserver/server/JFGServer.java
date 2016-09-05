package net.jfabricationgames.jfgserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;

public abstract class JFGServer {
	
	protected int port;
	protected List<JFGConnection> connections;
	protected JFGServerInterpreter interpreterFactory;
	
	public JFGServer(int port) {
		this.port = port;
		chooseInterpreter();
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
	
	public abstract void chooseInterpreter();
	
	public void addInterpreter(JFGConnection connection) {
		connection.setInterpreter(interpreterFactory.getInstance());
	}
	
	public void addConnection(JFGConnection connection) {
		connections.add(connection);
	}
	protected void removeConnection(JFGConnection connection) {
		connections.remove(connection);
	}
	
	public List<JFGConnection> getConnections() {
		return connections;
	}
	public void closeConnection(JFGConnection con) {
		connections.remove(con);
	}
	
	public JFGServerInterpreter getInterpreterFactory() {
		return interpreterFactory;
	}
	public void setInterpreterFactory(JFGServerInterpreter interpreterFactory) {
		this.interpreterFactory = interpreterFactory;
	}
}