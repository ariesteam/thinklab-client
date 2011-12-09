package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.knowledge.IValue;
import org.integratedmodelling.thinklab.api.knowledge.factories.IKnowledgeManager;
import org.integratedmodelling.thinklab.api.knowledge.storage.IKBox;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.project.IProjectFactory;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;

public class ProjectFactory implements IProjectFactory {

	private static ProjectFactory _this = null;
	private boolean _initialized;
	
	ArrayList<IProject> _projects = new ArrayList<IProject>();
	
	public static ProjectFactory get() {
		if (_this == null)
			_this = new ProjectFactory();
		return _this;
	}
	
	public ProjectFactory() {		
	}

	public synchronized void loadProjects() {

		_projects.clear();
		
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

	public static void refreshOntology(URL url, String fileBaseName, boolean b) {
		// TODO Auto-generated method stub
		
	}

	public static IOntology requireOntology(String fileBaseName) {
		// TODO Auto-generated method stub
		return null;
	}

}
