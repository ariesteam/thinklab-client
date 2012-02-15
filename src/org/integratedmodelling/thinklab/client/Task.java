package org.integratedmodelling.thinklab.client;

/**
 * A remote server task. Returned by all asynchronous operations on Server.
 * 
 * @author Ferd
 *
 */
public class Task {

	long   _id;
	String _command;
	Server _server;
	
	Task(Server server, long id, String command) {
		this._id = id;
		this._command = command;
		this._server = server;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 * @Override
	 */
	public String toString() {
		return "";
	}
	
	public long getId() {
		return _id;
	}
	
	boolean isFinished() {
		/*
		 * TODO
		 * check
		 */
		return false;
	}
}
