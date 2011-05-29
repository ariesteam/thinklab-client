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

		if (session == null)
			cl.say("not connected");
		
		return Result.ok(null).info("disconnected from " + session.getServer());
	}

}
