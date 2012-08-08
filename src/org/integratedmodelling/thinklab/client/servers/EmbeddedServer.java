package org.integratedmodelling.thinklab.client.servers;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.IServer;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.utils.NetUtilities;
import org.integratedmodelling.thinklab.common.configuration.Configuration;
import org.integratedmodelling.thinklab.common.owl.KnowledgeManager;

/**
 * Boots a server on the local machine and gives specialized access to it 
 * using the filesystem for transfers instead of REST.
 * 
 * @author Ferd
 *
 */
public class EmbeddedServer extends RESTServer {

	String   _instDir;
	Session  _session;
	boolean _running = false;
	CResult _err = null;
	
	private static final String LOCAL_URL = "http://127.0.0.1:8182/rest";
	
	/*
	 * default timeout 1min until we decide that the server didn't start up properly.
	 */
	private static final long TIMEOUT = 60000;
	
	public EmbeddedServer() {
		super(LOCAL_URL, null, null);
		_isLocal = true;
		_instDir = System.getProperty("thinklab.inst");
		if (_instDir == null) {
			// server is useless. Do not say anything.
		}
		
		File f = Configuration.get().getWorkspace(IServer.KNOWLEDGE_STORAGE_AREA);
		if (f.exists() && new File(f + File.separator + "thinklab.owl").exists()) {
			try {
				((KnowledgeManager)_km).loadKnowledge(f);
			} catch (ThinklabException e) {
				throw new ThinklabRuntimeException(e);
			}
		}
		
		if (!NetUtilities.portAvailable(8182)) {
			_running = true;
		}
	}

	
	
	public static boolean isAvailable() {
		/*
		 * lookup thinklab installation
		 */
		String idir = System.getProperty("thinklab.inst");
		return idir != null && 
			new File(idir + File.separator + "bin").exists();
		
	}
	
	
	@Override
	public Result boot() {
	
		if (_instDir == null)
			return error(
				new ThinklabClientException("no property 'thinklab.inst' defined: cannot access a local Thinklab installation"));
		
		/*
		 * see if a server was started before we boot. CHECK - we
		 * should check that it's actually a thinklab server on the
		 * port, although that may be overkill at this point.
		 */
		if (_running) {
			return OK_RESULT;
		}
		
		/*
		 * start it with the script and wait until port becomes available.
		 */
		String line = _instDir + File.separator + "run-server.bat";
		CommandLine cmdLine = CommandLine.parse(line);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(new File(_instDir));
		try {
			_running = true;
			executor.execute(cmdLine, new ExecuteResultHandler() {
				
				@Override
				public void onProcessFailed(ExecuteException arg0) {
					_running = false;
					_err = error(arg0);
				}
				
				@Override
				public void onProcessComplete(int arg0) {
					_running = false;
				}
			});
		} catch (Exception e) {
			return error(e);
		}
		
		if (!_running) {
			/*
			 * failed
			 */
			return 
				_err == null ? 
					error(new ThinklabException("launch of embedded Thinklab server failed")) :
					_err;
		}
		
		/*
		 * wait until server is active before giving control to client
		 */
		long timeout = 0;
		do {
			try {
				Thread.sleep(500);
				timeout += 500;
			} catch (Exception e) {
			}
		} while (NetUtilities.portAvailable(8182) && timeout < TIMEOUT);
		
		if (NetUtilities.portAvailable(8182)) {
			_running = false;
			return error(null);
		}

		/*
		 * read up knowledge from server location
		 */
		try { 
			((KnowledgeManager)_km).loadKnowledge(Configuration.get().getWorkspace("knowledge"));
		} catch (ThinklabException e) {
			return error(e);
		}
		
		/*
		 * parse capabilities and set metadata
		 */
		return super.boot();		
	}

	@Override
	public Result shutdown() {

		/*
		 * delete session
		 */
		super.shutdown();

		/*
		 * TODO so far haven't found a way to destroy the process. Server will
		 * remain running, this just kills the session. Behavior is proper, but
		 * no restart facility is available for now.
		 */
		_running = false;
		
		return OK_RESULT;
	}

	@Override
	public boolean isActive() {
		return _running;
	}

	@Override
	public Result deploy(IProject p) {

		String dirs = "";
		
		try {
			for (IProject proj : p.getPrerequisites()) {
				dirs += (dirs.isEmpty() ? "" : ",") + proj.getLoadPath();
			}
		} catch (ThinklabException e) {
			return error(e);
		}
		
		System.out.println("sending deps " + dirs);
		
		try {
			Result res =
				getSession().send(
							"project", false, 
							"cmd", "register", 
							"directory", dirs, 
							"plugin", p.getId());
			if (res.getStatus() == OK) {
				res = getSession().send(
						"project", false, 
						"cmd", "load", 
						"plugin", p.getId());
			}
			return res;
		} catch (Exception e) {
			/**
			 * FIXME remove - debug
			 */
			e.printStackTrace();
			return error(e);
		}
	}


}
