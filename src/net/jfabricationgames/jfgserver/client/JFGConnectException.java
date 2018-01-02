package net.jfabricationgames.jfgserver.client;

public class JFGConnectException extends RuntimeException {
	
	private static final long serialVersionUID = -6463184132087809018L;
	
	public JFGConnectException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	public JFGConnectException(String message) {
		super(message);
	}
}