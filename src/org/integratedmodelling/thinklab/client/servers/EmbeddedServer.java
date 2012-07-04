package org.integratedmodelling.thinklab.client.servers;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.utils.NetUtilities;

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
	private ExecuteWatchdog _watchdog;
	
	private static final String LOCAL_URL = "http://127.0.0.1/8182/rest";
	
	/*
	 * default timeout 30s until we decide that the server didn't start up properly.
	 */
	private static final long TIMEOUT = 30000;
	
	public EmbeddedServer() {
		super(LOCAL_URL, null, null);
		_instDir = System.getenv("THINKLAB_HOME");
		if (_instDir == null)
			throw new ThinklabRuntimeException("THINKLAB_HOME not defined: cannot establish a path to Thinklab");
	}

	@Override
	public Result boot() {
		
		/*
		 * see if a server was started before we boot.
		 */
		if (!NetUtilities.portAvailable(8182)) {
			return OK_RESULT;
		}
		
		/*
		 * start it with the script and wait until port becomes available.
		 */
		String line = "run-server.bat";
		CommandLine cmdLine = CommandLine.parse(line);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(new File(_instDir));
		try {
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
		
		_watchdog = executor.getWatchdog();

		long timeout = 0;
		do {
			try {
				Thread.sleep(500);
				timeout += 500;
			} catch (InterruptedException e) {
			}
		} while (NetUtilities.portAvailable(8182) || timeout > TIMEOUT);
		
		if (NetUtilities.portAvailable(8182)) {
			_watchdog = null;
			_running = false;
			return error(null);
		}
		
		return OK_RESULT;
	}

	@Override
	public Result shutdown() {

		if (_watchdog != null && _running) {
			_watchdog.destroyProcess();
		}
		_running = false;
		
		return OK_RESULT;
	}

	@Override
	public boolean isActive() {
		// TODO
		return false;
	}


}
