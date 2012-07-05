package org.integratedmodelling.thinklab.client.servers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.integratedmodelling.thinklab.api.lang.IPrototype;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.IServer;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.modelling.Metadata;

public class RESTServer implements IServer {

	Metadata _metadata = new Metadata();
	
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result executeCommand(String command) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long executeStatementAsynchronous(String s) {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result boot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result shutdown() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Result authenticate(Object... authInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result deploy(IProject p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result undeploy(IProject p) {
		// TODO Auto-generated method stub
		return null;
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
			s.send("extract-knowledge", false);

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
