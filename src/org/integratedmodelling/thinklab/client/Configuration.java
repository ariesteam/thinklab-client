package org.integratedmodelling.thinklab.client;

import java.io.File;

import org.omg.CORBA.Environment;

public class Configuration {

	public static File getConfigPath() {
		// TODO Auto-generated method stub
		return new File(System.getProperty("user.home") + File.separator + ".thinklab");
	}

	public static String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

}
