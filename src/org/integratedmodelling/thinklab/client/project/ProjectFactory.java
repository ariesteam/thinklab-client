package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.project.IProjectFactory;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;

public class ProjectFactory implements IProjectFactory {

	private static ProjectFactory _this = null;
	
	ArrayList<IProject> _projects = new ArrayList<IProject>();
	
	public static ProjectFactory get() {
		if (_this == null)
			_this = new ProjectFactory();
		return _this;
	}
	
	public ProjectFactory() {
		
		/*
		 * load all projects in configured directory
		 */
		for (File f : Configuration.getProjectDirectory().listFiles()) {
			
			if (ThinklabProject.exists(f)) {
				try {
					_projects.add(new ThinklabProject(f));
				} catch (ThinklabClientException e) {
					// TODO warn project could not be loaded
				}
			}
		}
	}
	
	@Override
	public IProject createProject(String arg0) throws ThinklabException {
		return ThinklabProject.create(arg0);
	}

	@Override
	public void deleteProject(String arg0) throws ThinklabException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IProject getProject(String arg0) {
		for (IProject p : _projects)
			if (p.getId().equals(arg0))
				return p;
		return null;
	}

	@Override
	public Collection<IProject> getProjects() {
		return _projects;
	}

}
