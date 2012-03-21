package org.integratedmodelling.thinklab.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.collections.Triple;
import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.plugin.IThinklabPlugin;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.listeners.ProgressListener;
import org.integratedmodelling.thinklab.client.modelling.ModelManager;
import org.integratedmodelling.thinklab.client.project.ThinklabProject;
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
 * No actual state is kept on the server (it's REST, baby) so no need to disconnect at the
 * end, but a disconnect() method is provided so that the same session can be reused or made
 * local at will.
 * 
 * @author ferdinando.villa
 *
 */
public class Session {

	public static final int DEFAULT_SERVER_PORT = 8182;
	
	private String _server = "http://127.0.0.1:8182";
	private String _id = null;
	private String _name = null;
	private boolean _connected = false;
	private String _user = null;

	private ArrayList<INamespace> _systemNamespaces = 
			new ArrayList<INamespace>();
	
	private int delay = 4000;
	private String _curDir = File.separator;
	private File _currentDirectory = Configuration.getProjectDirectory();
	
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
		
		for (int i = 0; i < cmds.size(); i++) {
			String id = (String)cmds.getResult(i,0);
			String ds = (String)cmds.getResult(i,1);
			JSONArray args = (JSONArray)cmds.getResult(i,2);
			JSONArray opts = (JSONArray)cmds.getResult(i,3);
			
			_commands.put(id, new RemoteCommand(id, ds, args, opts));
		}		
		
	}
	
	public boolean isConnected() {
		return _connected;
	}
	
	public RemoteCommand getRemoteCommandDeclaration(String command) {
		return _commands.get(command);
	}
	
	private HashMap<String, RemoteCommand> _commands = 
		new HashMap<String, Session.RemoteCommand>();
	private ThinklabProject _currentProject;
	
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
	
	public void connect(String url, String name, String user, String password) throws ThinklabClientException {

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
		this._connected = true;
		this._user = user;
	}
	
	public Session() {
	}

	public void connect(String url, String user, String password) throws ThinklabClientException {

		_server = url;
		_name = "default";

		initialize();

		/*
		 * get an authenticated session, establish ID
		 */
		Result auth = send("auth", false, "user", user, "password", password);
		this._id = auth.get("session").toString();
		this._connected = true;
		this._user = user;
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
		Result ret = null;
		
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
			ret = new Result(js);
			ret.setSession(this);
			
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		
		return ret;
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
	 * @return the final file name downloaded and its size
	 */
	public Pair<File, Integer> download(String handle, File fname, ProgressListener listener) throws ThinklabClientException {

		String hext = MiscUtilities.getFileExtension(handle);
		String fext = fname == null ? "" : MiscUtilities.getFileExtension(fname.toString());
		int bytes = 0;
		
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
			
			bytes = MiscUtilities.saveStreamToFile(response.getEntity().getContent(), fname);
			
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		return new Pair<File, Integer>(fname, bytes);
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

	public void setCurrentProject(ThinklabProject project) throws ThinklabClientException {
		this._currentProject = project;
		/*
		 * reset current dir
		 */
		_currentDirectory = Configuration.getProjectDirectory();
		setCurrentDirectory(project.getId());
	}

	public ThinklabProject getCurrentProject() {
		return this._currentProject;
	}

	public String getCurrentUser() {
		return _user;
	}
	
	public Collection<INamespace> getSystemNamespaces() throws ThinklabClientException {
		
		_systemNamespaces.clear();
		
		Result res = this.send("capabilities", false);
		/*
		 * all non-project namespaces, i.e. all ontologies in system plugins, unless
		 * already loaded and not modified.
		 */
		JSONArray ontologies = (JSONArray)res.get("ontologies");
		for (int i = 0; i < ontologies.length(); i++) {
			try {
				JSONArray jo = (JSONArray)(ontologies.get(i));
				String id = jo.getString(0);
				String url = _server + "/resource?ontology=" + id;
				long lastm = Long.parseLong(jo.getString(2));		
				
				/*
				 * attempt to load namespace from server unless we have it
				 * and its last modification date was >=
				 */
				INamespace nns = ModelManager.get().getNamespace(id);
				if (nns == null || (nns != null && nns.getTimeStamp() < lastm)) {
					nns = ModelManager.get().loadNamespace(id, url, "owl");
				}
				
				if (nns != null) {
					_systemNamespaces.add(nns);					
				}
			} catch (JSONException e) {
				// christ
			} catch (ThinklabException e) {
				throw new ThinklabClientException(e);
			}
		}
		
		return _systemNamespaces;
	}
	
	public HashMap<String, Object> getFullStatus() throws ThinklabClientException {
		
		HashMap<String, Object> ret = new HashMap<String, Object>();

		ret.put("user", this.getCurrentUser());
		
		Result res = this.send("capabilities", false);
		
		/*
		 * report all vars
		 */
 		ret.put("boot.time", new Date(Long.parseLong(res.get("boot.time").toString())));
		ret.put("current.time", new Date(Long.parseLong(res.get("current.time").toString())));
		
		ret.put("memory.total", Long.parseLong(res.get("memory.total").toString())/(1024L*1024L));
		ret.put("memory.free", Long.parseLong(res.get("memory.free").toString())/(1024L*1024L));
		
		/*
		 * plugins status
		 */
		ArrayList<Triple<String, String, Boolean>> pls = new ArrayList<Triple<String,String,Boolean>>();
		JSONArray plugins = (JSONArray) res.get("plugins");
		if (plugins != null) {
			for (int i = 0; i < plugins.length(); i++) {
				try {
					JSONObject plug = plugins.getJSONObject(i);
				
					String pid  = plug.getString("id");
					String ver  = plug.getString("version");
					Boolean act = plug.getString("active").equals("true");
				
					pls.add(new Triple<String, String, Boolean>(pid, ver, act));
				
				} catch (JSONException e) {
					// christ
				}
			}	
		}

		/*
		 * sort by plugin ID
		 */
		Collections.sort(pls, new Comparator<Triple<String, String, Boolean>>() {
			@Override
			public int compare(Triple<String, String, Boolean> arg0,
					Triple<String, String, Boolean> arg1) {
				return arg0.getFirst().compareTo(arg1.getFirst());
			}
		});
		ret.put("plugins", pls);
		
		
		/*
		 * tasks status
		 */
		
		/*
		 * namespaces and error status
		 */
		
		/*
		 * 
		 */
		
		/*
		 * log diff since last call
		 */
		
		
		return ret;
	}

	
	public void disconnect() {

		this._server = null;
		this._name = null;
		this._user = null;
		this._connected = false;
		this._id = null;
		this._systemNamespaces.clear();
	}

	public void setCurrentDirectory(String dir) throws ThinklabClientException {
		
		if (dir == null) {
			this._currentDirectory = 
				_currentProject == null ?
					Configuration.getProjectDirectory() :
					Configuration.getProjectDirectory(_currentProject.getId());
			this._curDir = File.separator;
			return;
		}
		
		File dr = new File(this._currentDirectory, dir);
		if (! (dr.exists() && dr.isDirectory())) {
			throw new ThinklabClientException("directory " + dir + " does not exist");
		}
		this._currentDirectory = dr;
		this._curDir = MiscUtilities.getFileName(dr.toString());
	}
	
	/**
	 * Return current directory for display or for use
	 * @param b if true, return the display name (starting at $THINKLAB_PROJECT_DIR);
	 * otherwise, return the actual full path.
	 * 
	 * @return
	 */
	public String getCurrentDirectory(boolean display) {
		return display ? _curDir : _currentDirectory.toString();
	}

	/**
	 * Deploy a project to the connected server
	 * 
	 * @param project
	 * @return
	 * @throws ThinklabClientException
	 */
	public boolean deploy(ThinklabProject project) throws ThinklabClientException {

		if (!isConnected())
			throw new ThinklabClientException("project: not connected to a server");

		/*
		 * deploy all dependencies first
		 */
		for (IThinklabPlugin p : project.getPrerequisites()) {
			deploy((ThinklabProject)p);
		}
		
		File zip = project.getZipArchive();
		String handle = upload(zip, null);
		Result result = send("project", false, 
				"cmd", "deploy", 
				"handle", handle, 
				"plugin", project.getId());
	
		return result.getStatus() == Result.OK;
	}
}
