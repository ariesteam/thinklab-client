package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.factories.IProjectManager;
import org.integratedmodelling.thinklab.api.project.IProject;

public class ProjectManager implements IProjectManager {

	HashMap<String, IProject> _projects = new HashMap<String, IProject>();
	
	@Override
	public IProject getProject(String projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IProject> getProjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadAllProjects() throws ThinklabException {
		// TODO Auto-generated method stub

	}

	@Override
	public IProject deployProject(String pluginId, String resourceId)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void undeployProject(String projectId) throws ThinklabException {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] registerProject(File... projectDir) throws ThinklabException {
		
		String[] ret = new String[projectDir.length];
		
		for (int i = 0; i < projectDir.length; i++) {
			IProject p = new Project(projectDir[i], this);
			ret[i] = p.getId();
			_projects.put(p.getId(), p);
		}
		
		return ret;
		
	}

	@Override
	public void unregisterProject(String projectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshProject(String projectId) throws ThinklabException {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerProjectDirectory(File projectDirectory) {
		// TODO Auto-generated method stub

	}

}
