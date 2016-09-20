package jfgServerExample.loginServer;

import net.jfabricationgames.jfgserver.client.JFGServerMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGServerInterpreter;
import net.jfabricationgames.jfgserver.server.JFGConnection;
import net.jfabricationgames.jfgserver.server.JFGLoginServer;

public class DefaultLoginServerInterpreter implements JFGServerInterpreter {
	
	private JFGLoginServer server;
	
	private int failedLogins;
	private int loginTries = 3;
	
	private String onlyUser = "theAnswer";
	private String onlyPassword = "42";
	
	private static LoginFeedback loginAccepted = new LoginFeedback(true);
	private static LoginFeedback loginWrong = new LoginFeedback(false, LoginFeedback.ERROR_WRONG_LOGIN, "Wrong username or password");
	private static LoginFeedback loginDenied = new LoginFeedback(false, LoginFeedback.ERROR_CANCEL_LOGIN, "To many failed logins. Connection ended.");
	
	public DefaultLoginServerInterpreter(JFGLoginServer server) {
		this.server = server;
	}
	
	@Override
	public void interpreteServerMessage(JFGServerMessage message, JFGConnection connection) {
		if (message instanceof LoginMessage) {
			//login messages
			if (!server.isLoggedIn(connection)) {
				LoginMessage loginMessage = (LoginMessage) message;
				if (loginMessage.getUser().equals(onlyUser) && loginMessage.getPassword().equals(onlyPassword)) {
					//accept the login
					server.acceptLogin(connection);
					connection.sendMessage(loginAccepted);
				}
				else {
					//deny login
					failedLogins++;
					if (loginTries != -1 && failedLogins > loginTries) {
						//to many failed logins
						connection.sendMessage(loginDenied);
						server.denyLogin(connection);
					}
					else {
						connection.sendMessage(loginWrong);
					}
				}
			}
			else {
				System.err.println("DefaultLoginServerInterpreter: User is already logged in.");
			}
		}
		else if (message instanceof DefaultMessage) {
			//normal messages
			DefaultMessage textMessage = (DefaultMessage) message;
			//find the number of the connected user and send a broadcast of his message.
			int user = connection.getServer().getConnections().indexOf(connection);
			//just broadcast the messages to all logged in users.
			textMessage.setMessage("User " + user + ": " + textMessage.getMessage());
			server.sendBroadcast(textMessage);
		}
		else {
			//unknown message type
			System.err.println("The type of the message is unknown");
		}
	}
	
	@Override
	public JFGServerInterpreter getInstance() {
		return new DefaultLoginServerInterpreter(server);
	}
}