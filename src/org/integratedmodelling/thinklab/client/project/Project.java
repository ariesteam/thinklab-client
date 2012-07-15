package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.integratedmodelling.common.HashableObject;
import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.factories.IProjectManager;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.modelling.ModelManager;
import org.integratedmodelling.thinklab.client.utils.CamelCase;
import org.integratedmodelling.thinklab.client.utils.FolderZiper;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;

public class Project extends HashableObject implements IProject {

	String _id = null;
	File _path;

	Properties _properties = null;
	private ArrayList<INamespace> _namespaces = new ArrayList<INamespace>();
	private String[] _dependencies;
	private boolean _loaded = false;
	
	IProjectManager _manager;
	
	/*
	 * these 2 are in sync
	 */
	private ArrayList<File> _resourcesInError = new ArrayList<File>();
	private ArrayList<String> _errors = new ArrayList<String>();
	
	public Project(File path, IProjectManager manager) {
		
		_path = path;
		_manager = manager;
		_id = MiscUtilities.getFileName(path.toString());
		_properties = getProperties();
		String pp = getProperties().getProperty(IProject.PREREQUISITES_PROPERTY, "");
		_dependencies = 
			pp.isEmpty() ? new String[0] :
			getProperties().getProperty(IProject.PREREQUISITES_PROPERTY, "").split(",");
	}
	
	@Override
	public String getId() {
		return _id;
	}


	@Override
	public void load() throws ThinklabException {

		loadDependencies();		
		_namespaces = new ArrayList<INamespace>();
		HashSet<File> read = new HashSet<File>();
		loadInternal(new File(_path + File.separator + this.getSourceDirectory()), read, _namespaces, "", this);
		_loaded = true;
	}

	private void loadDependencies() throws ThinklabClientException {

		for (String dep : _dependencies) {
			IProject p = ProjectFactory.get().getProject(dep, true);
			if (p == null)
				throw new ThinklabClientException(_id + ": cannot load prerequisite project " + dep);
		}
	}

	private void loadInternal(File f, HashSet<File> read, ArrayList<INamespace> ret, String path,
			IProject project) throws ThinklabClientException {

		String pth = 
				path == null ? 
					"" : 
					(path + (path.isEmpty() ? "" : ".") + CamelCase.toLowerCase(MiscUtilities.getFileBaseName(f.toString()), '-'));
		
		if (f. isDirectory()) {
		
			for (File fl : f.listFiles()) {
				loadInternal(fl, read, ret, pth, project);
			}
			
		} else if (ModelManager.get().canParseExtension(MiscUtilities.getFileExtension(f.toString()))) {

			INamespace ns;
			try {
				ns = ModelManager.get().loadFile(f.toString(), pth, this);
				if (ns != null) 
					ret.add(ns);
			} catch (ThinklabException e) {
				_resourcesInError.add(f);
				_errors.add(e.getMessage());
			}
		}
	}

	@Override
	public void unload() throws ThinklabException {
		
		for (INamespace ns : _namespaces) {
			ModelManager.get().releaseNamespace(ns.getId());
		}
		
		_namespaces.clear();
		_resourcesInError.clear();
		_loaded = false;
	}

	@Override
	public File findResource(String resource) {

		File ff = 
			new File(
				_path + File.separator + getSourceDirectory() + 
				File.separator + resource);
		
		if (ff.exists()) {
			return ff;
		}
		return null;
	}
	
	@Override
	public File findResourceForNamespace(String namespace, String extension) {

		String fp = namespace.replace('.', File.separatorChar);
		File ff = new File(_path + File.separator + getSourceDirectory() + File.separator + fp + "." + extension);
		if (ff.exists()) {
			return ff;
		}
			
		return null;
	}

	@Override
	public boolean isLoaded() {
		return _loaded;
	}

	@Override
	public Properties getProperties() {
		
		if (_properties == null) {

			_properties = new Properties();
			try {
				File pfile = 
					new File(
						_path + 
						File.separator + 
						"META-INF" +
						File.separator + 
						"thinklab.properties");
				
				if (pfile.exists()) {
					try {
						_properties.load(new FileInputStream(pfile));
					} catch (Exception e) {
						throw new ThinklabClientException(e);
					}
				}
					
			} catch (ThinklabClientException e) {
				throw new ThinklabRuntimeException(e);
			}
		}
		return _properties;
	}
	
	@Override
	public File getWorkspace() {
		return _path;
	}

	@Override
	public File getWorkspace(String subspace) {
		File ret = new File(_path + File.separator + subspace);
		ret.mkdirs();
		return ret;
	}

	@Override
	public File getScratchArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getScratchArea(String subArea) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getTempArea(String subArea) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getLoadPath() {
		return _path;
	}

	@Override
	public File getLoadPath(String subArea) {
		File ret = new File(_path + File.separator + subArea);
		return ret.exists() ? ret : null;
	}

	@Override
	public Collection<INamespace> getNamespaces() {
		return _namespaces;
	}

	@Override
	public String getSourceDirectory() {
		return getProperties().getProperty(IProject.SOURCE_FOLDER_PROPERTY, "src");
	}
	
	@Override
	public String getOntologyNamespacePrefix() {
		return getProperties().getProperty(
				IProject.ONTOLOGY_NAMESPACE_PREFIX_PROPERTY, "http://www.integratedmodelling.org/ns");
	}

	@Override
	public void addDependency(String plugin, boolean reload)
			throws ThinklabException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<IProject> getPrerequisites() {
		
		ArrayList<IProject> ret = new ArrayList<IProject>();

		for (String s : _dependencies) {
			IProject p = _manager.getProject(s);
			if (p != null) {
				ret.add(p);
			}
		}
		
		return ret;
	}

	@Override
	public long getLastModificationTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	// NON-API
	
	private void saveProperties() throws ThinklabClientException {
		
		File td = 
			new File(_path +
				File.separator + "META-INF" +
				File.separator + "thinklab.properties");
		
		try {
			_properties.store(new FileOutputStream(td), null);
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		
	}

	public File getZipArchive() throws ThinklabClientException {
		
		File ret = null;
		try {
			ret = File.createTempFile("tpr", ".zip", Configuration.getTemporaryPath());
			FolderZiper.zipFolder(
				Configuration.getProjectDirectory(_id).toString(), 
				ret.toString());
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		return ret;
	}

}
