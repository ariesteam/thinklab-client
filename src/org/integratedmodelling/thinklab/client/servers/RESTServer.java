package org.integratedmodelling.thinklab.client.servers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.thinklab.api.lang.IPrototype;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.IServer;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.modelling.Metadata;
import org.integratedmodelling.thinklab.client.project.ThinklabProject;
import org.integratedmodelling.thinklab.client.utils.FolderZiper;

public class RESTServer implements IServer {

	Metadata _metadata = new Metadata();
	
	/*
	 * this is to flag situations when the server can access the same filesystem
	 * as the client, preventing the need to transfer files.
	 */
	protected boolean _isLocal = false;
	
	protected final CResult OK_RESULT = new CResult(OK, null, null, null, null);
	private Session _session;
	protected String _url; 
	protected String _user = null;
	protected String _password = null;
	
	protected ArrayList<IPrototype> _functions;
	protected ArrayList<IPrototype> _commands;
	
	public RESTServer(String url, String user, String password) {
		_url = url;
		_user = user;
		_password = password;
	}
	
	protected Session getSession() throws ThinklabClientException {
		
		if (_session == null) {
			_session = new Session();
			_session.connect(_url, _user, _password);
			
		}
		return _session;
	}
	
	
	protected CResult error(Exception e) {
		return new CResult(ERROR, null, null, null, e);
	}
	

	@Override
	public IMetadata getMetadata() {
		return _metadata;
	}
	
	@Override
	public Result executeStatement(String s) {
		
		org.integratedmodelling.thinklab.client.Result r;
		try {
			r = getSession().send(
					"execute-statement", false, "statement", s, "isLocal", 
					_isLocal ? "true" : "false");
		} catch (ThinklabClientException e) {
			return error(e);
		}
		
		CResult ret = new CResult(r.getStatus(), s, r.getResult(), null, null);
		
		return ret;
	}

	@Override
	public Result executeCommand(String command) {

		return OK_RESULT;
	}

	@Override
	public long executeStatementAsynchronous(String s) {

		org.integratedmodelling.thinklab.client.Result r;
		try {
			r = getSession().send("execute-statement", true, "statement", s,
					_isLocal ? "true" : "false");
		} catch (ThinklabClientException e) {
			return -1L;
		}
		
		
		return r.getTaskID();
	}

	@Override
	public long executeCommandAsynchronous(String command) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getStatus(long handle) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Result getTaskResult(long handle, boolean dispose) {
		// TODO get capabilities and test connection
		return null;
	}

	@Override
	public Result boot() {

		try {
			org.integratedmodelling.thinklab.client.Result r = 
					getSession().send("capabilities", false);
			
			if (r.getStatus() != IServer.OK) {
				return error(new ThinklabClientException("cannot parse server response"));
			}
			
			/*
			 * parse capabilities into server metadata
			 */
			parseCapabilities(r);
		
		} catch (ThinklabClientException e) {
			return error(e);
		}
		return OK_RESULT;
	}

	protected void parseCapabilities(
			org.integratedmodelling.thinklab.client.Result r) {
		// TODO Auto-generated method stub
		System.out.println(r);
	}

	@Override
	public Result shutdown() {
		
		if (_session != null) {
			_session.disconnect();
			_session = null;
		}
		return OK_RESULT;
	}

	@Override
	public boolean isActive() {
		return _session != null && _session.isConnected();
	}

	@Override
	public Result deploy(IProject p) {

		try {
			if (!getSession().deploy((ThinklabProject)p))
				return error(new ThinklabClientException("failed to deploy project " + p.getId()));
		} catch (ThinklabClientException e) {
			return error(e);
		}
		return OK_RESULT;
	}

	@Override
	public Result undeploy(IProject p) {
		try {
			if (!getSession().undeploy((ThinklabProject)p))
				return error(new ThinklabClientException("failed to deploy project " + p.getId()));
		} catch (ThinklabClientException e) {
			return error(e);
		}
		return OK_RESULT;
	}

	@Override
	public List<IPrototype> getFunctionPrototypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IPrototype> getCommandPrototypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getSupportedLanguages() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result exportCoreKnowledge(File file) {

		try {

			Session s = getSession();
			org.integratedmodelling.thinklab.client.Result ret = s.send("extract-knowledge", false);

			String handle = (String)ret.get("handle");
			if (handle != null) {
				Pair<File, Integer> zip = s.download(handle, null, null);
				FolderZiper.unzip(zip.getFirst(), file);
			}
			
		} catch (ThinklabClientException e) {
			return new CResult(ERROR, null, null, null, e);
		}
		
		return OK_RESULT;
	}
	// ------------------------------------------------------------------------------------------------------------
	
	public static class CResult implements Result {

		private int _s;
		private String _c;
		private Object _r;
		private String _o;
		private Throwable _e;

		public CResult(int status, String command, Object result, String output, Throwable exception) {
			_s = status; 
			_c = command;
			_r = result;
			_o = output;
			_e = exception;
		}
		
		@Override
		public int getStatus() {
			return _s;
		}

		@Override
		public String getCommand() {
			return _c;
		}

		public Object getResult() {
			return _r;
		}
		
		@Override
		public String getOutput() {
			return _o;
		}

		@Override
		public Throwable getException() {
			return _e;
		}
		
	}


}
