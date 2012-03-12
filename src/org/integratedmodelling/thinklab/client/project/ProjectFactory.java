package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.project.IProjectFactory;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;

public class ProjectFactory implements IProjectFactory {

	private static ProjectFactory _this = null;
	private boolean _initialized;
	
	ArrayList<IProject> _projects = new ArrayList<IProject>();
	
	// set of loaded resources to speed up checking for loaded projects
	HashSet<File> _projectFiles = new HashSet<File>();
	
	public static ProjectFactory get() {
		if (_this == null)
			_this = new ProjectFactory();
		return _this;
	}
	
	protected ProjectFactory() {		
	}

	public synchronized void loadProjects() {

		_projects.clear();
		_projectFiles.clear();
		
		/*
		 * load all projects in configured directory
		 */
		for (File f : Configuration.getProjectDirectory().listFiles()) {
			
			// already loaded as a dependency
			if (_projectFiles.contains(f))
				continue;

			if (ThinklabProject.exists(f)) {
					
				try {
					ThinklabProject proj = new ThinklabProject(f);

					if (proj.hasErrors()) {
						/*
						 * FIXME
						 * do something
						 */
						System.out.println("project " + proj.getId() + " has errors");
						for (String s : proj.getErrors()) {
							System.out.println("\t" + s);
						}
					}
					_projects.add(proj);
					_projectFiles.add(f);
				} catch (ThinklabException e) {
					// TODO warn project could not be loaded
				}
			}
		}
	}
	
	public void initialize() {
		
		if (!_initialized) {
			loadProjects();
		}
		_initialized = true;
		
	}
	
	@Override
	public IProject createProject(String arg0) throws ThinklabException {
		IProject ret = ThinklabProject.create(arg0);
		loadProjects();
		return ret;	
	}

	@Override
	public void deleteProject(String arg0) throws ThinklabException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IProject getProject(String arg0, boolean attemptLoading) {
		
		IProject ret = null;
		
		for (IProject p : _projects) {
			if (p.getId().equals(arg0)) {
				return p;
			}
		}
		
		if (attemptLoading) {
			/*
			 * see if we have it in the project path and it's not loaded already. This will be
			 * called by dependencies
			 */
			File f = Configuration.getProjectDirectory(arg0);
				
			if (ThinklabProject.exists(f)) {
				try {
					_projects.add(ret = new ThinklabProject(f));
					_projectFiles.add(f);
				} catch (ThinklabException e) {
					// TODO warn project could not be loaded
				}
			}
		}				
				
		return ret;
	}
	
	
	@Override
	public Collection<IProject> getProjects() {
		return _projects;
	}

	public static void refreshOntology(URL url, String fileBaseName, boolean b) {
		// TODO Auto-generated method stub
		
	}

	public static IOntology requireOntology(String fileBaseName) {
		// TODO Auto-generated method stub
		return null;
	}

}
