package org.integratedmodelling.thinklab.client.servers;

import java.io.File;
import java.util.List;

import org.integratedmodelling.thinklab.api.lang.IPrototype;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.IServer;
import org.integratedmodelling.thinklab.client.modelling.Metadata;
import org.integratedmodelling.thinklab.client.utils.JVMLauncher;

/**
 * Boots a server on the local machine and gives specialized access to it 
 * using the filesystem for transfers instead of REST.
 * 
 * @author Ferd
 *
 */
public class EmbeddedServer implements IServer {

	JVMLauncher _jvm = new JVMLauncher();
	Metadata _metadata = new Metadata();
	
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
		// TODO Auto-generated method stub
		return null;
	}

}
