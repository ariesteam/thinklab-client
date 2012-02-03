package org.integratedmodelling.thinklab.client;

import java.net.URL;

import org.integratedmodelling.exceptions.ThinklabRuntimeException;

public class ServerConfiguration {

	private String name;
	private String url;
	private String user = null;
	private String password = null;
	
	public ServerConfiguration(String name, String url) {

		this.name = name;
		this.url = url;
		
		checkPassword();
	}
	
	public ServerConfiguration(String name, String url, String user, String password) {

		this(name, url);
		
		this.user = user;
		this.password = password;
		
		if (user == null && password == null) {
			checkPassword();
		}
	}
		
	private void checkPassword() {
			
		/*
		 * take user from URL instead of processing it at server side.
		 */
		try {
			URL serv = new URL(url);
			String ss = serv.getUserInfo();
			if (ss != null && !ss.isEmpty()) {
				
				String[] ui = ss.split("\\:");
			
				if (ui.length > 0)
					user = ui[0];
				if (ui.length > 1)
					password = ui[1];
			}
		} catch (Exception e) {
			throw new ThinklabRuntimeException(e);
		}
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getUser() {
		return user;
	}
	
	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}
}
