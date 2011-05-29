package org.integratedmodelling.thinklab.client.commands;

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
		
		/*
		 * establish session with server
		 */
		session = new Session(server, remote);
		
		return Result.ok(session).info("connected to " + session.getServer());
	}

}
