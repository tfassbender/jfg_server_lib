package net.jfabricationgames.jfgserver.secured_message;

public class JFGSecureCommunicationException extends RuntimeException {

	private static final long serialVersionUID = -4052048133982634565L;
	
	public JFGSecureCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}
	public JFGSecureCommunicationException(String message) {
		super(message);
	}
}