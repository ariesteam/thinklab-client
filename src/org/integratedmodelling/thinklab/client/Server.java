package org.integratedmodelling.thinklab.client;

import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;

/**
 * Easy interaction with a server.
 * 
 * @author Ferd
 *
 */
public class Server {

	private Session _session;
	int lastLog = 0;

	public Server(Session session) {
		this._session = session;
	}
	
	public Server(String url, String user, String password) throws ThinklabClientException {
		this._session = new Session();
		_session.connect(url, user, password);
	}
	
	public boolean pload(String plugin) throws ThinklabClientException {
		Result result = _session.send("pload", false, "plugin", plugin);
		return result.getStatus() == Result.OK;
	}

	public Task model(String model, String context, String scenario) {
		return null;
	}

	public Task launch(String storyline, String context) {
		return null;
	}

	public Task measure(String concept, String context) {
		return null;
	}
	
	public Task rank(String concept, String context) {
		return null;
	}
	
	public Task classify(String concept, String context) {
		return null;
	}
	
	public Task categorize(String concept, String context) {
		return null;
	}
	
	public Task observe(String concept, String context) {
		return null;
	}

	public boolean deploy(IProject project) {
		return false;
	}

	public boolean list(String what) {
		return false;
	}
	
	public boolean getLogDiff() {
		return false;
	}
	
	public boolean shutdown() {
		return false;
	}
	
	public boolean restart() {
		return false;
	}

	public boolean upgrade(String branch) {
		return false;
	}
	
	

	
}
