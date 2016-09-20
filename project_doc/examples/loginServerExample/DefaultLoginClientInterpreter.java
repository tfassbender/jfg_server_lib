package jfgServerExample.loginServer;

import net.jfabricationgames.jfgserver.client.JFGClient;
import net.jfabricationgames.jfgserver.client.JFGClientMessage;
import net.jfabricationgames.jfgserver.interpreter.JFGClientInterpreter;

public class DefaultLoginClientInterpreter implements JFGClientInterpreter {
	
	private static String defaultUsername = "theAnswer";
	private static String defaultPasswd = "42";
	
	@Override
	public void interpreteClientMessage(JFGClientMessage message, JFGClient connection) {
		if (message instanceof LoginFeedback) {
			LoginFeedback loginMessage = (LoginFeedback) message;
			if (loginMessage.isLoginAccepted()) {
				//login accepted; send a welcome message to the server
				System.out.println("Login accepted.");
				DefaultMessage toServer = new DefaultMessage("Hello World.");
				connection.sendMessage(toServer);
			}
			else if (loginMessage.getErrorCode() == LoginFeedback.ERROR_WRONG_LOGIN) {
				System.err.println("Wrong login. I'll try again in 3 seconds.");
				try {
					Thread.sleep(3000);
				}
				catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				//wait for 3 seconds and try again.
				LoginMessage login = new LoginMessage(defaultUsername, defaultPasswd);
				connection.sendMessage(login);
			}
			else if (loginMessage.getErrorCode() == LoginFeedback.ERROR_CANCEL_LOGIN) {
				//too many failed tries; exit program.
				System.err.println("Couldn't establish connection to the server. Shutting down.");
				System.exit(1);
			}
		}
		else if (message instanceof DefaultMessage) {
			DefaultMessage defaultMessage = (DefaultMessage) message;
			//just print the message on the screen.
			System.out.println(defaultMessage.getMessage());
		}
	}
}