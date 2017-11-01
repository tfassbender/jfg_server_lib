package net.jfabricationgames.jfgserver.secured_message;

import java.util.HashMap;
import java.util.Map;

import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;
import net.jfabricationgames.jfgserver.secured_message.JFGReloginMessage.ReloginMessageType;
import net.jfabricationgames.jfgserver.server.JFGConnection;
import net.jfabricationgames.jfgserver.server.JFGLoginServer;
import net.jfabricationgames.jfgserver.server.JFGServer;

/**
 * This server is not more secured to login than the normal login server.
 * The secured extension is that if the connection breaks because of a StreamCorruptedException it can be restored.
 * 
 * All other functions are just like the JFGLoginServer.
 * 
 * It has to be used with an interpreter that can handle the reconnect functions (like the JFGDatabaseSecureLoginInterpreter) to work properly.
 */
public class JFGSecureLoginServer extends JFGLoginServer {
	
	private Map<String, JFGConnection> reloginPasswordConnections;

	public JFGSecureLoginServer(int port) {
		super(port);
		reloginPasswordConnections = new HashMap<String, JFGConnection>();
	}

	@Override
	public void chooseInterpreter() {
		setInterpreterFactory(new DefaultSecureLoginInterpreter(this));
	}
	
	@Override
	public void acceptLogin(JFGConnection connection) throws IllegalArgumentException {
		super.acceptLogin(connection);
		JFGReloginMessage reloginMessage = new JFGReloginMessage(ReloginMessageType.SEND_RELOGIN_PASSWORD);
		String reloginPassword = generatePassword();
		reloginMessage.setReloginPassword(reloginPassword);
		connection.sendMessage(reloginMessage);
		reloginPasswordConnections.put(reloginPassword, connection);
	}
	
	@Override
	public void removeConnection(JFGConnection connection) {
		super.removeConnection(connection);
		String pass = "";
		for (String reloginPassword : reloginPasswordConnections.keySet()) {
			if (connection.equals(reloginPasswordConnections.get(reloginPassword))) {
				pass = reloginPassword;
			}
		}
		reloginPasswordConnections.remove(pass);
	}
	
	/**
	 * Re-login a connection that has sent a re-login password.
	 *  
	 * @param connection
	 * 		The connection that sent the re-login password.
	 * 
	 * @param password
	 * 		The re-login password.
	 */
	public void relogin(JFGConnection connection, String password) {
		JFGConnection existing = reloginPasswordConnections.get(password);
		if (existing != null) {
			removeWaiting(connection);
			existing.restart(connection.getSocket(), connection.getInputStream(), connection.getOutputStream());
		}
	}
	
	/**
	 * A default implementation of the server interpreter that just accepts every login and sends broadcasts.
	 */
	private class DefaultSecureLoginInterpreter implements JFGServerInterpreter {
		
		private JFGSecureLoginServer server;
		
		public DefaultSecureLoginInterpreter(JFGSecureLoginServer server) {
			this.server = server;
		}
		
		@Override
		public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
			if (server.isLoggedIn(connection)) {
				if (message instanceof JFGClientMessage) {
					sendBroadcast((JFGClientMessage) message);
				}
				else {
					JFGServer.printError("JFGSecureLoginServer: Couldn't repeat. Message doesn't implement JFGClientMessage", JFGServer.ERROR_LEVEL_DEBUG);
				}
			}
			else {
				if (message instanceof JFGReloginMessage) {
					//re-login the user (if the password is right)
					relogin(connection, ((JFGReloginMessage) message).getReloginPassword());
				}
				else {
					//just accept every new login
					server.acceptLogin(connection);					
				}
			}
		}
		
		@Override
		public JFGServerInterpreter getInstance() {
			return new DefaultSecureLoginInterpreter(server);
		}
	}
	
	/**
	 * Generate an alpha-numerical password for the re-login.
	 */
	private String generatePassword() {
		final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		StringBuilder password = new StringBuilder();
		for (int i = 0; i < 25; i++) {//generate a 25 characters password
			password.append(chars.charAt((int) (Math.random()*chars.length())));
		}
		return password.toString();
	}
}