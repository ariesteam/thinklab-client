package org.integratedmodelling.thinklab.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Configuration {

	static Properties _properties = null;
	
	public static File getConfigPath() {
		File ret = new File(System.getProperty("user.home") + File.separator + ".thinklab");
		ret.mkdirs();
		return ret;
	}

	public static File getDownloadPath() {
		File ret = new File(getConfigPath() + File.separator + "downloads"); 
		ret.mkdirs();
		return ret;
	}
	
	public static File getTemporaryPath() {
		File ret = new File(getConfigPath() + File.separator + "tmp"); 
		ret.mkdirs();
		return ret;
	}

	public static String getVersion() {
		return "1.0alpha";
	}

	public static Properties getProperties() {
		
		if (_properties == null) {
			_properties = new Properties();
			File f = new File(getConfigPath() + File.separator + "client.properties");
			if (f.exists())	{
				try {
					_properties.load(new FileInputStream(f));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return _properties;
	}
	
	public static void saveProperties() {
		File f = new File(getConfigPath() + File.separator + "client.properties");
		try {
			getProperties().store(new FileOutputStream(f), null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static File getProjectDirectory() {

		String configured = getProperties().getProperty("project.directory");
		File ret = null;
		if (configured == null) {
			ret = new File(getConfigPath() + File.separator + "projects"); 
		} else {
			ret = new File(configured);
		}
		ret.mkdirs();
		return ret;

	}
	
	public static File getProjectDirectory(String plugin) {
		File ret = new File(getProjectDirectory() + File.separator + plugin);
		ret.mkdirs();
		return ret;
	}
}
