package org.integratedmodelling.thinklab.client.commands;

import java.net.URL;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="connect")
public class Connect extends CommandHandler {

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		String remote = expect(arguments, 0);
		
		String server = null;
		if (remote.startsWith("htt") && remote.contains("://"))
			server = remote;
		else 
			server = Configuration.getProperties().getProperty("server." + remote);
		
		if (server == null)
			throw new ThinklabClientException("server " + remote + " is unknown");
		
		String user = null;
		String password = null;

		/*
		 * take user from URL instead of processing it at server side.
		 */
		try {
			URL serv = new URL(server);
			String ss = serv.getUserInfo();
			if (ss != null && !ss.isEmpty()) {
				
				String[] ui = ss.split("\\:");
			
				if (ui.length > 0)
					user = ui[0];
				if (ui.length > 1)
					password = ui[1];
			}
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
		
		/*
		 * any user/password indicated explicitly supersedes the URL
		 */
		if (arguments.getArguments().size() >= 2) {
			user = expect(arguments, 1);
		}

		if (arguments.getArguments().size() >= 3) {
			password = expect(arguments, 2);
		} else if (user != null && password == null){
			password = cl.ask("password for " + user + ": ");
		}
		
		/*
		 * establish session with server
		 */
		session = new Session(server, remote, user, password);
		
		return Result.ok(session).info(
				"connected to " + session.getServer() + "\n" +
				(user == null ? "anonymous" : user) + " session established");
	}

}
