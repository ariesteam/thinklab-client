package org.integratedmodelling.thinklab.client;

import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.utils.Escape;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * A session is opened on the client any time the connect command is issued and completes
 * successfully. It can be used to send commands to the server and holds local info on
 * authentication.
 * 
 * @author ferdinando.villa
 *
 */
public class Session {

	private String _server = "http://127.0.0.1:8182";
	private String _id = null;
	private String _name = null;
	
	private void initialize() throws ThinklabClientException {
	
		/*
		 * send ping, disconnect if offline
		 */
		
		/*
		 * get capabilities, store in session
		 */
		
	}
	
	public Session(String url, String name) throws ThinklabClientException {

		_server = url;
		_name = name;
		initialize();

		/*
		 * get an unauthenticated session, establish ID
		 */
	}

	public Session(String url, String user, String password) throws ThinklabClientException {

		initialize();

		/*
		 * get an authenticated session, establish ID
		 */
	}

	/**
	 * Send a command and if requested, wait for completion. Return result or task ID.
	 * NOTE: this only uses get.
	 * 
	 * @param command
	 * @param synchronous if this is true, we don't wait for completion of long-running commands and just 
	 * 					  return the task ID. Default is to wait (polling the server until completion).
	 * @param arguments
	 * @return
	 * @throws ThinklabClientException
	 */
	public Result send(
			String command, boolean synchronous, 
			String ... arguments) throws ThinklabClientException {
		
		String url = _server + "/rest/" + command + "&session=" + _id;
		
		if (arguments != null) {
			url += "?";
			for (int i = 0; i < arguments.length; i++)
				url += 
					Escape.forURL(arguments[i]) + 
					"=" +
					Escape.forURL(arguments[++i]);
		}
		
		try {
			ClientResource cr = new ClientResource(url);
			Representation rep = cr.get(MediaType.APPLICATION_JSON);
			JSONObject js = new JSONObject(rep.getText());
			return new Result(js);
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
	}
	
}
