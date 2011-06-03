package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="disconnect")
public class Disconnect extends CommandHandler {

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		if (!session.isConnected())
			cl.say("not connected");
		
		/*
		 * TODO use --keep option to save session for later reconnect, or cleanup
		 */
		String serv = session.getServer();
		
		session.disconnect();
		
		return Result.ok(session).info("disconnected from " + serv);
	}

}
