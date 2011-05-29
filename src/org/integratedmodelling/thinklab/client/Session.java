package org.integratedmodelling.thinklab.client;

import java.util.HashMap;

import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.utils.Escape;
import org.json.JSONArray;
import org.json.JSONException;
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

	class RemoteCommand {

		public String id;
		public String ds;
		public String[] args;
		public String[] opts;

		String[] convert(JSONArray js) throws ThinklabClientException {
			if (js == null)
				return null;
			String[] ret = new String[js.length()];
			for (int i = 0; i < js.length(); i++) {
				try {
					ret[i] = js.getString(i);
				} catch (JSONException e) {
					throw new ThinklabClientException(e);
				}
			}
			return ret;
		}
		
		public RemoteCommand(String id, String ds, JSONArray args,
				JSONArray opts) throws ThinklabClientException {
			this.id = id;
			this.ds = ds;
			this.args = convert(args);
			this.opts = convert(opts);
		}
		
	}
	
	private HashMap<String, RemoteCommand> _commands = 
		new HashMap<String, Session.RemoteCommand>();
	
	private void initialize() throws ThinklabClientException {
	
		/*
		 * send ping, disconnect if offline
		 */
		Result rs = send("", false);
		
		/*
		 * get capabilities, store in session
		 */
		Result cmds = send("getCommands", false);
		
		for (int i = 0; i < cmds.resultSize(); i++) {
			String id = (String)cmds.getResult(i,0);
			String ds = (String)cmds.getResult(i,1);
			JSONArray args = (JSONArray)cmds.getResult(i,2);
			JSONArray opts = (JSONArray)cmds.getResult(i,3);
			
			_commands.put(id, new RemoteCommand(id, ds, args, opts));
		}		
	}
	
	public Session(String url, String name, String user, String password) throws ThinklabClientException {

		_server = url;
		_name = name;
		
		initialize();

		/*
		 * get an unauthenticated session, establish ID
		 */
		Result auth = 
			user == null ? 
				send("auth", false) :
				send("auth", false, "user", user, "password", password);
				
		if (auth.getStatus() != Result.OK)
			throw new ThinklabClientException(auth.print());
		
		this._id = auth.get("session").toString();
	}

	public Session(String url, String user, String password) throws ThinklabClientException {

		initialize();

		/*
		 * get an authenticated session, establish ID
		 */
		Result auth = send("auth", false, "user", user, "password", password);
		this._id = auth.get("session").toString();
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
		
		String url = _server + "/" + command + "?session=" + _id;
		
		if (arguments != null && arguments.length > 0) {
			url += "?";
			for (int i = 0; i < arguments.length; i++)
				url += 
					"&" + 
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
	
	public String getName() {
		return _name;
	}

	public String getServer() {
		return _server;
	}
}
