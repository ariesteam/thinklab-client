package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabIOException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
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
	private Collection<INamespace> namespaces;
	
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
	 * 
	 * @param ns
	 * @return
	 * @throws ThinklabException
	 */
	public File createNamespace(String ns) throws ThinklabException {
		
		File ret = new File(getSourceFolders().iterator().next() + File.separator + 
							ns.replaceAll(".", File.separator) + ".tql");
		
		File dir = new File(MiscUtilities.getFilePath(ret.toString()));
		
		
		try {
			dir.mkdirs();
			PrintWriter out = new PrintWriter(ret);
			out.println("namespace " + ns + ";\n");
			out.close();
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		
		return ret;
	}
	
	public static ThinklabProject load(String id) throws ThinklabClientException {
	
		ThinklabProject ret = new ThinklabProject(id);

		if (!ret.exists()) {
			throw new ThinklabClientException("project " + id + " does not exist");
		}
		ret.load();
		return ret;	
	}
	
	private void load() throws ThinklabClientException {
		
		_properties = getPluginProperties(_id);
		try {
			this.namespaces = ModelManager.get().load(this);
		} catch (ThinklabException e) {
			throw new ThinklabClientException(e.getMessage());
		}
	}

	private void create(String[] dependencies) throws ThinklabClientException {

		if (dependencies == null) {
			String dps = Configuration.getProperties().getProperty(
					"default.project.dependencies",
					"org.integratedmodelling.thinklab.core" +
						",org.integratedmodelling.thinklab.modelling" +
						",org.integratedmodelling.thinklab.metadata" +
						",org.integratedmodelling.thinklab.sql" +
						",org.integratedmodelling.aries.core");
			
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
			 * insert directories for the default loaders. Not really necessary, but 
			 * nice as a mnemonic if you ever look at the dir.
			 */
			new File(pluginDir + File.separator + "agents").mkdirs();
			new File(pluginDir + File.separator + "annotations").mkdirs();
			new File(pluginDir + File.separator + "config").mkdirs();
			new File(pluginDir + File.separator + "contexts").mkdirs();
			new File(pluginDir + File.separator + "models").mkdirs();
			new File(pluginDir + File.separator + "ontologies").mkdirs();
			new File(pluginDir + File.separator + "scenarios").mkdirs();
			
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


	@Override
	public String getOntologyNamespacePrefix() {
		return getProperties().getProperty(
				IProject.ONTOLOGY_NAMESPACE_PREFIX_PROPERTY, "http://www.integratedmodelling.org/ns");
	}

	
	
}
