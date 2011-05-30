package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.RemoteCommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="pload")
public class Pload extends RemoteCommandHandler {

	@Override
	public Result runRemote(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {
		return send("pload", arguments);
	}

}
