package org.integratedmodelling.thinklab.client.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.utils.FolderZiper;

public class ThinklabProject {
	
	String _id = null;
	Properties _properties = null;
	
	public ThinklabProject(String pluginId) {		
		_id = pluginId;
	}
	
	/**
	 * Get the content of THINKLAB-INF/thinklab.properties if the plugin contains that
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
					"THINKLAB-INF" +
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

	public static ThinklabProject getProject(String id, String[] dependencies) 
		throws ThinklabClientException {
		
		ThinklabProject ret = new ThinklabProject(id);

		if (!ret.exists()) {
			ret.create(dependencies);
		} else {
			ret.load();
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
					File.separator + _id + File.separator + "THINKLAB-INF");
		
		return f.exists();
	}
	
	
	public static boolean exists(String id) {
		
		File f = 
			new File(Configuration.getProjectDirectory() + 
					File.separator + id + File.separator + "THINKLAB-INF");
		
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
				"<plugin id=\"__ID__\" version=\"0.8.1.20110428103733\">\r\n" + 
				"   <requires>\r\n".replaceAll("__ID__", _id));

			for (String s : dependencies) {
				out.print(
					"	     <import exported=\"false\" match=\"compatible\" " +
					"optional=\"false\" plugin-id=\"__ID__\" reverse-lookup=\"true\"/>\r\n".replaceAll("__ID__", s));
			}
			
			out.print(
				"   </requires>\r\n" + 
				"</plugin>");
		
			fout.close();
			
			File td = new File(pluginDir + File.separator + "THINKLAB-INF");
			td.mkdirs();
			this._properties = new Properties();
			
			/*
			 * insert default loaders
			 */
			
			saveProperties();
			
		} catch (IOException e) {
			throw new ThinklabClientException(e);
		}
	}

	private void saveProperties() throws ThinklabClientException {
		
		File td = 
			new File(Configuration.getProjectDirectory(_id) +
				File.separator + "THINKLAB-INF" +
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