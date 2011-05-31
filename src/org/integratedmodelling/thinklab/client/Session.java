package org.integratedmodelling.thinklab.client;

import java.io.File;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.listeners.ProgressListener;
import org.integratedmodelling.thinklab.client.utils.Escape;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;
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

	private int delay = 4000;
	
	public interface Listener {
		
		public void onStart(String command, String ... arguments);
		public void onFinish(String command, Result ret, String ... arguments);
		public void onWait(String command, String ... arguments);
	}
	
	public class RemoteCommand {

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
	
	public void scanCommands() throws ThinklabClientException {

		_commands.clear();
		
		Result cmds = send("getCommands", false);
		
		for (int i = 0; i < cmds.resultSize(); i++) {
			String id = (String)cmds.getResult(i,0);
			String ds = (String)cmds.getResult(i,1);
			JSONArray args = (JSONArray)cmds.getResult(i,2);
			JSONArray opts = (JSONArray)cmds.getResult(i,3);
			
			_commands.put(id, new RemoteCommand(id, ds, args, opts));
		}		
		
	}
	
	public RemoteCommand getRemoteCommandDeclaration(String command) {
		return _commands.get(command);
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
		scanCommands();
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
	 * 
	 * @param command
	 * @param asynchronous if this is true, we don't wait for completion of long-running commands and just 
	 * 					   return the task ID. Default is to wait (polling the server until completion).
	 * @param arguments
	 * @return
	 * @throws ThinklabClientException
	 */
	public Result send(
			String command, boolean asynchronous,
			String ... arguments) throws ThinklabClientException {
		return send(command, asynchronous, null, arguments);
	}
	
	/**
	 * Send a command and if requested, wait for completion. Return result or task ID.
	 * 
	 * @param command
	 * @param asynchronous if this is true, we don't wait for completion of long-running commands and just 
	 * 					   return the task ID. Default is to wait (polling the server until completion).
	 * @param listener a Listener to be notified of start, finish and wait.
	 * @param arguments
	 * @return
	 * @throws ThinklabClientException
	 */
	public Result send(
			String command, boolean asynchronous, Listener listener,
			String ... arguments) throws ThinklabClientException {
		
		Result ret = null;
		
		if (listener != null)
			listener.onStart(command, arguments);

		boolean waiting = false;
		do {			
			ret = sendInternal(command, arguments);
			if ( (waiting = (!asynchronous && ret != null && ret.getStatus() == Result.WAIT))) {
				try {
					Thread.sleep(delay);
					if (listener != null)
						listener.onWait(command, arguments);
					command = "check";
					arguments = new String[]{"taskid", ret.get("taskid").toString()}; 
				} catch (InterruptedException e) {
					throw new ThinklabClientException(e);
				}
			}
		} while (waiting);

		if (listener != null)
			listener.onFinish(command, ret, arguments);

		return ret;
	}

	private Result sendInternal(String command, String ... arguments) throws ThinklabClientException {
		
		String url = _server + "/" + command + "?session=" + _id;
		
		if (arguments != null && arguments.length > 0) {
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
			
			/*
			 * anything not JSON means a 404 or other error
			 */
			if (!rep.getMediaType().equals(MediaType.APPLICATION_JSON))
				throw new ThinklabClientException("server failure: not a Thinklab REST service");
			
			JSONObject js = new JSONObject(rep.getText());
			return new Result(js);
			
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
	}
	
	/**
	 * Download file from server specified by given handle. If fname is not null,
	 * save to given file name, overwriting any existing ones without warning. Otherwise
	 * save to default download directory using temp file name and subdirectory corresponding
	 * to session id. Extension is taken from
	 * handle. If fname is given w/o extension and handle has an extension, set the 
	 * extension of the downloaded file to that in the handle.
	 * 
	 * @param handle
	 * @param fname
	 * @return the final file name downloaded
	 */
	public File download(String handle, File fname, ProgressListener listener) throws ThinklabClientException {

		String hext = MiscUtilities.getFileExtension(handle);
		String fext = fname == null ? "" : MiscUtilities.getFileExtension(fname.toString());
		
		if (fname == null) {
			// download to session subdir to minimize conflicts
			File dpath = 
				new File(
						Configuration.getDownloadPath()+ 
						File.separator + 
						MiscUtilities.getFilePath(handle));
			dpath.mkdirs();
			fname = new File(
					dpath + 
					File.separator + MiscUtilities.getFileName(handle));
		} else {
			// conflicts are assumed taken care of upstream when fname is passed
			String path = MiscUtilities.getFilePath(fname.toString());
			if (path.isEmpty())
				fname = new File(
					Configuration.getDownloadPath()+ 
					File.separator + fname);
				
		}
		
		if (fext.isEmpty() && !hext.isEmpty())
			fname = new File(fname + "." + fext);
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(
					_server + "/send?session=" + _id + 
					"&handle=" + Escape.forURL(handle));
			HttpResponse response = client.execute(get);
			
			if (response.getStatusLine().getStatusCode() != 200)
				throw new ThinklabClientException(
						"upload of " + fname + " failed: status code = " + 
						response.getStatusLine().getStatusCode());
			
			MiscUtilities.saveStreamToFile(response.getEntity().getContent(), fname);
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		return fname;
	}
	
	/**
	 * Upload file to server; return handle that server will use to locate the
	 * file. 
	 * 
	 * @param fname
	 * @return
	 * @throws ThinklabClientException
	 */
	public String upload(File fname, ProgressListener listener) throws ThinklabClientException {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(_server + "/receive?session=" + _id);

		FileBody uploadFilePart = new FileBody(fname);
		MultipartEntity reqEntity = new MultipartEntity();
		reqEntity.addPart("upload-file", uploadFilePart);
		httpPost.setEntity(reqEntity);
		
		try {
			
			HttpResponse response = httpclient.execute(httpPost);
			
			if (response.getStatusLine().getStatusCode() != 200)
				throw new ThinklabClientException(
						"upload of " + fname + " failed: status code = " + 
						response.getStatusLine().getStatusCode());
			
			String rep = MiscUtilities.convertStreamToString(
					response.getEntity().getContent());
			
			Result js = new Result(new JSONObject(rep));
			if (js.getStatus() != Result.OK)
				throw new ThinklabClientException("transfer failed");
			
			return js.getResult().toString();
			
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
