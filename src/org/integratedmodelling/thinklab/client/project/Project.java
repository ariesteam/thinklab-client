package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.integratedmodelling.common.HashableObject;
import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.factories.IProjectManager;
import org.integratedmodelling.thinklab.api.lang.IResolver;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.client.configuration.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.modelling.ModelManager;
import org.integratedmodelling.thinklab.client.modelling.Namespace;
import org.integratedmodelling.thinklab.client.utils.CamelCase;
import org.integratedmodelling.thinklab.client.utils.FolderZiper;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;

public class Project extends HashableObject implements IProject {
	
	String _id = null;
	File _path;

	int _refcount = 0;
	
	Properties _properties = null;
	private ArrayList<INamespace> _namespaces = new ArrayList<INamespace>();
	private String[] _dependencies;
	private boolean _loaded = false;
	
	ProjectManager _manager;
	
	/*
	 * these 2 are in sync
	 */
	private ArrayList<File> _resourcesInError = new ArrayList<File>();
	private ArrayList<String> _errors = new ArrayList<String>();

	
	public Project(File path, IProjectManager manager) {
		
		_path = path;
		_manager = (ProjectManager) manager;
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

	public void load(IResolver resolver, Set<File> context) throws ThinklabException {

		if (isLoaded()/* && isDirty() */)
			unload();
		
//		_refcount ++;
		
		/*
		 * if we haven't been unloaded, we didn't need to so we don't need
		 * loading, either.
		 */
//		if (isLoaded())
//			return;
		
		for (IProject p : _manager.computeDependencies(this)) {
			if (p.equals(this))
				continue;
			IResolver r = resolver.getImportResolver(p);
			((Project)p).load(r, context);
		}
		
		_namespaces = new ArrayList<INamespace>();
		
		File srcDir = new File(_path + File.separator + this.getSourceDirectory());
		for (File fl : srcDir.listFiles()) {
			loadInternal(fl, context, _namespaces, "", this, resolver);
		}
		
		_loaded = true;
	}
	
	private void loadInternal(File f, Set<File> context, ArrayList<INamespace> ret, String path,
			IProject project, IResolver resolver) throws ThinklabClientException {

		String pth = 
				path == null ? 
					"" : 
					(path + (path.isEmpty() ? "" : ".") + CamelCase.toLowerCase(MiscUtilities.getFileBaseName(f.toString()), '-'));
						
		if (f. isDirectory()) {

			for (File fl : f.listFiles()) {
				loadInternal(fl, context, ret, pth, project, resolver);
			}
			
		} else if (ModelManager.get().canParseExtension(MiscUtilities.getFileExtension(f.toString()))) {

			if (context.contains(f))
				return;
			/*
			 * already imported by someone else
			 */
			if (ModelManager.get().getNamespace(pth) != null)
				return;
			
			INamespace ns;
			try {
				ns = ModelManager.get().loadFile(f.toString(), pth, this, resolver);
				if (ns != null) {
					ret.add(ns);
					/*
					 * this is necessary to publish the
					 * namespace if it is not created by a parser that uses
					 * the resolver.
					 */
					ModelManager.get().notifyNamespace(ns);
				} else {
					/*
					 * should not happen anymore
					 */
					System.out.println("NAH namespace is null.");
				}
			} catch (ThinklabException e) {
				System.out.println(pth + " has errors: " + e.getMessage());
				_resourcesInError.add(f);
				_errors.add(e.getMessage());
			}
		}
	}

	public void unload() throws ThinklabException {
		
		if (!isLoaded())
			return;

		/*
		 * unload dependents in reverse order of dependency.
		 */
		List<IProject> deps = _manager.computeDependencies(this);
		for (int i = deps.size() - 1; i >= 0; i--) {
			IProject p = deps.get(i);
			if (p.equals(this))
				continue;
			((Project)p).unload();
		}
		
//		_refcount --;
//		
//		if (_refcount == 0) {
		
			for (INamespace ns : _namespaces) {
				ModelManager.get().releaseNamespace(ns.getId());
			}
		
			_namespaces.clear();
			_resourcesInError.clear();
			_loaded = false;
//		}
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
	public File findResourceForNamespace(String namespace) {

		String fp = namespace.replace('.', File.separatorChar);
		for (String extension : new String[]{"tql", "owl"}) {
			File ff = new File(_path + File.separator + getSourceDirectory() + File.separator + fp + "." + extension);
			if (ff.exists()) {
				return ff;
			}
		}
			
		return null;
	}

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
	public List<IProject> getPrerequisites() throws ThinklabException {
		return _manager.computeDependencies(this);
	}

	@Override
	public long getLastModificationTime() {

		long lastmod = 0L;
		
		for (File f : FileUtils.listFiles(_path, new String[]{}, true)) {
			if (f.lastModified() > lastmod)
				lastmod = f.lastModified();
		}
		
		return lastmod;		
	}

	// NON-API

	public void addDependency(String plugin) throws ThinklabException {
	
		String pp = getProperties().getProperty(IProject.PREREQUISITES_PROPERTY, "");
		String[] deps = 
			pp.isEmpty() ? new String[0] :
			getProperties().getProperty(IProject.PREREQUISITES_PROPERTY, "").split(",");
		
		String dps = "";
		for (String s : deps) {
			if (s.equals(plugin))
				return;
			dps += (dps.isEmpty()? "" : ",") + s;
		}
		
		dps += (dps.isEmpty()? "" : ",") + plugin;
		getProperties().setProperty(IProject.PREREQUISITES_PROPERTY, dps);
		deps = dps.split(",");

		saveProperties();
		
	}
	
	public void createManifest(String[] dependencies) throws ThinklabException {
			
		File td = new File(_path + File.separator + "META-INF");
		td.mkdirs();
		
		new File(_path + File.separator + getSourceDirectory()).mkdirs();
			
		if (dependencies != null && dependencies.length > 0) {
			for (String d : dependencies)
				addDependency(d);
		} else {
			saveProperties();
		}
	}
	
	private void saveProperties() throws ThinklabException {
		
		File td = 
			new File(_path +
				File.separator + "META-INF" +
				File.separator + "thinklab.properties");
		
		try {
			getProperties().store(new FileOutputStream(td), null);
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		
	}

	public File getZipArchive() throws ThinklabException {
		
		File ret = null;
		try {
			ret = File.createTempFile("tpr", ".zip", Configuration.get().getTempArea("pack"));
			FolderZiper.zipFolder(
				Configuration.get().getProjectDirectory(_id).toString(), 
				ret.toString());
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		return ret;
	}

	public String createNamespace(IProject p, String ns) throws ThinklabException {
		
		File ret = new File(_path + File.separator + getSourceDirectory() + File.separator + 
				ns.replace('.', File.separatorChar) + ".tql");
		File dir = new File(MiscUtilities.getFilePath(ret.toString()));

		try {
			dir.mkdirs();
			PrintWriter out = new PrintWriter(ret);
			out.println("namespace " + ns + ";\n");
			out.close();
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}

		return getSourceDirectory() + File.separator + 
				ns.replace('.', File.separatorChar) + ".tql";
	}
	
	String[] getPrerequisiteIds() {
		return _dependencies;
	}

	@Override
	public boolean providesNamespace(String namespaceId) {
		return findResourceForNamespace(namespaceId) != null;
	}

	/*
	 * imported namespaces are not seen directly by loadInternal but need to be
	 * reported here by the resolver.
	 */
	public void notifyNamespace(Namespace ns) {
		for (INamespace n : _namespaces) {
			if (n.getId().equals(ns.getId()))
				return;
		}
		_namespaces.add(ns);
	}

	@Override
	public List<String> getUserResourceFolders() {

		ArrayList<String> ret = new ArrayList<String>();
		
		for (File f : _path.listFiles()) {
			if (f.isDirectory() &&
				!f.equals(new File(_path + File.separator + getSourceDirectory())) &&
				!ProjectManager.get().isManagedDirectory(
						MiscUtilities.getFileName(f.toString()), this)) {
				ret.add(MiscUtilities.getFileBaseName(f.toString()));
			}
		}
		
		return ret;
	}

	@Override
	public boolean hasErrors() {
		
		for (INamespace n : _namespaces) {
			if (n.hasErrors())
				return true;
		}
		return false;
	}
	
	@Override
	public boolean hasWarnings() {
		
		for (INamespace n : _namespaces) {
			if (n.hasWarnings())
				return true;
		}
		return false;
	}
}
