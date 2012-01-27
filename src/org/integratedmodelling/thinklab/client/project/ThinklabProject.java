package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabIOException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.modelling.ModelManager;
import org.integratedmodelling.thinklab.client.utils.FolderZiper;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;

public class ThinklabProject implements IProject {
	
	String _id = null;
	Properties _properties = null;
	private ArrayList<INamespace> namespaces = new ArrayList<INamespace>();
	
	public ThinklabProject(String pluginId) {		
		_id = pluginId;
	}
	
	public ThinklabProject(File dir) throws ThinklabClientException {		
		_id = MiscUtilities.getFileName(dir.toString());
		load();
	}
	
	/**
	 * Get the content of META-INF/thinklab.properties if the plugin contains that
	 * directory, or null if it doesn't. Can be used to check if a plugin is a 
	 * thinklab plugin based on the null return value.
	 * 
	 * TODO move to client library and load the library in the server package
	 * 
	 * @param plugin
	 * @return
	 * @throws ThinklabIOException
	 */
	public static Properties getPluginProperties(String plugin) throws ThinklabClientException {
		
			Properties ret = null;
			File pfile = 
				new File(
					Configuration.getProjectDirectory(plugin) + 
					File.separator + 
					"META-INF" +
					File.separator + 
					"thinklab.properties");
			
			if (pfile.exists()) {
				try {
					ret = new Properties();
					ret.load(new FileInputStream(pfile));
				} catch (Exception e) {
					throw new ThinklabClientException(e);
				}
			}
			
			return ret;
		}
	
	public static ThinklabProject create(String id) throws ThinklabClientException {
	
		ThinklabProject ret = new ThinklabProject(id);

		if (ret.exists()) {
			throw new ThinklabClientException("project " + id + " already exists");
		}
		ret.create((String[])null);
		return ret;	
	}
	
	/**
	 * Create a namespace in the first sourcefolder.
	 * @param p 
	 * 
	 * @param ns
	 * @return file path relative to project directory
	 * @throws ThinklabException
	 */
	public String createNamespace(IProject p, String ns) throws ThinklabException {
				
		File ret = new File(getSourceFolders().iterator().next() + File.separator + 
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
		
		return getSourceFolderNames().iterator().next() + File.separator + 
				ns.replace('.', File.separatorChar) + ".tql";
	}
	
	public static ThinklabProject load(String id) throws ThinklabClientException {
	
		ThinklabProject ret = new ThinklabProject(id);

		if (!ret.exists()) {
			throw new ThinklabClientException("project " + id + " does not exist");
		}
		ret.load();
		return ret;	
	}
	
	/**
	 * Re-read all resources.
	 * 
	 * @throws ThinklabClientException
	 */
	public void refresh() throws ThinklabClientException {
		namespaces.clear();
		load();
	}

	private void create(String[] dependencies) throws ThinklabClientException {

		/*
		 * TODO these need to make sense. Probably the modelling plugin should
		 * just contain dependencies for the commonly used ones, such as
		 * space and time, and all other deps should be other project chosen
		 * from the workspace.
		 */
		if (dependencies == null) {
			String dps = Configuration.getProperties().getProperty(
					"default.project.dependencies",
					"org.integratedmodelling.thinklab.core");
			
			dependencies = dps.split(",");
		}
		
		File plugdir = Configuration.getProjectDirectory(_id);
		createManifest(plugdir, dependencies);
		
	}
	
	public String getId() {
		return _id;
	}
	
	public boolean exists() {
		File f = 
			new File(Configuration.getProjectDirectory() + 
					File.separator + _id + File.separator + "META-INF");
		
		return f.exists();
	}
	
	public static boolean exists(File dir) {
		
		File f = 
			new File(dir + File.separator + "META-INF");
		return f.exists();
	}
	
	public static boolean exists(String id) {
		
		File f = 
			new File(Configuration.getProjectDirectory() + 
					File.separator + id + File.separator + "META-INF" + File.separator + "thinklab.properties");
		
		return f.exists();
	}
	
	public void createManifest(File pluginDir, String[] dependencies) throws ThinklabClientException {

		try {
			FileOutputStream fout = 
				new FileOutputStream(new File(pluginDir + File.separator + "plugin.xml"));
			PrintStream out = new PrintStream(fout);
			out.print( 
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n" + 
				"<!DOCTYPE plugin PUBLIC \"-//JPF//Java Plug-in Manifest 1.0\" \"http://jpf.sourceforge.net/plugin_1_0.dtd\">\r\n" + 
				"<plugin id=\"__ID__\" version=\"0.8.1.20110428103733\">\r\n".replaceAll("__ID__", _id) + 
				"   <requires>\r\n");

			for (String s : dependencies) {
				out.print(
					"	     <import exported=\"false\" match=\"compatible\" " +
					"optional=\"false\" plugin-id=\"__ID__\" reverse-lookup=\"true\"/>\r\n".replaceAll("__ID__", s));
			}
			
			out.print(
				"   </requires>\r\n" + 
				"</plugin>");
		
			fout.close();
			
			File td = new File(pluginDir + File.separator + "META-INF");
			td.mkdirs();
			this._properties = new Properties();

			
			/*
			 * create the default source folder 
			 * TODO also create an empty plugin configuration file.
			 */
			this._properties.put(IProject.SOURCE_FOLDER_PROPERTY, "src");
			new File(pluginDir + File.separator + "src").mkdirs();
			
			saveProperties();
			
		} catch (IOException e) {
			throw new ThinklabClientException(e);
		}
	}

	private void saveProperties() throws ThinklabClientException {
		
		File td = 
			new File(Configuration.getProjectDirectory(_id) +
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

	public File getPath() {
		return Configuration.getProjectDirectory(_id);
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof ThinklabProject && ((ThinklabProject)o).getId().equals(_id);
	}

	@Override
	public int hashCode() {
		return _id.hashCode();
	}

	@Override
	public Properties getProperties() {
		if (_properties == null) {
			try {
				_properties = getPluginProperties(getId());
			} catch (ThinklabClientException e) {
				throw new ThinklabRuntimeException(e);
			}
		}
		return _properties;
	}

	@Override
	public Collection<INamespace> getNamespaces() {
		return namespaces;
	}

	@Override
	public Collection<File> getSourceFolders() {
		String[] folders = getProperties().getProperty(IProject.SOURCE_FOLDER_PROPERTY, "src").split(",");
		ArrayList<File> ret = new ArrayList<File>();
		for (String f : folders) {
			ret.add(new File(getPath() + File.separator + f));
		} 
		return ret;
	}
	
	public Collection<String> getSourceFolderNames() {
		String[] folders = getProperties().getProperty(IProject.SOURCE_FOLDER_PROPERTY, "src").split(",");
		ArrayList<String> ret = new ArrayList<String>();
		for (String f : folders) {
			ret.add(f);
		} 
		return ret;
	}

	@Override
	public String getOntologyNamespacePrefix() {
		return getProperties().getProperty(
				IProject.ONTOLOGY_NAMESPACE_PREFIX_PROPERTY, "http://www.integratedmodelling.org/ns");
	}

	public Collection<INamespace> load()
			throws ThinklabClientException {
	
		_properties = getPluginProperties(_id);

		namespaces = new ArrayList<INamespace>();
		HashSet<File> read = new HashSet<File>();
		
		for (File dir : this.getSourceFolders()) {
		
			if (!dir.isDirectory() || !dir.canRead()) {
				throw new ThinklabClientException("source directory " + dir + " is unreadable");
			}	 
		
			loadInternal(dir, read, namespaces, "", this);
		}
		
		return namespaces;
	}

	private void loadInternal(File f, HashSet<File> read, ArrayList<INamespace> ret, String path,
			IProject project) throws ThinklabClientException {

		if (f. isDirectory()) {
			
			String pth = path + "." + MiscUtilities.getFileBaseName(f.toString());

			for (File fl : f.listFiles()) {
				loadInternal(fl, read, ret, pth, project);
			}
			
		} else if (f.toString().endsWith(".owl")) {
			try {
				ProjectFactory.refreshOntology(
						f.toURI().toURL(), 
						MiscUtilities.getFileBaseName(f.toString()), false);
			} catch (MalformedURLException e) {
				throw new ThinklabClientException(e);
			}
			
			/*
			 * TODO validate ontology URL vs. namespace path
			 */
			IOntology o = 
					ProjectFactory.requireOntology(
							MiscUtilities.getFileBaseName(f.toString()));
			String uri = 
					project.getOntologyNamespacePrefix() + "/" + path.replaceAll(".", "/") + 
					MiscUtilities.getFileBaseName(f.toString());
					
			if (!o.getURI().equals(uri)) {
				throw new ThinklabClientException(
						"illegal ontology namespace in " + f + 
						": file path requires " + uri + ", " +
						o.getURI() + " found");
			}
			
			/*
			 * TODO add namespace and project to ontology metadata
			 */
			
			/*
			 * TODO if auto sync is requested and configured, upload newer ontologies 
			 * to location matching URI
			 */
			
		} else if (f.toString().endsWith(".tql") || f.toString().endsWith(".clj")) {

			INamespace ns;
			try {
				ns = ModelManager.get().loadFile(f.toString(), this);
			} catch (ThinklabException e) {
				throw new ThinklabClientException(e);
			}

			/*
			 * validate namespace vs. file path
			 */
			if (!ns.getNamespace().equals(path))
				throw new ThinklabClientException(
						"illegal namespace declaration in " + f + 
						": file path requires " + path + ", " +
						ns.getNamespace() + " found");
					
			ret.add(ns);
		}
		
	}
	
}
