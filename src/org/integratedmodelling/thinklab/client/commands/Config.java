package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.RemoteCommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

import uk.co.flamingpenguin.jewel.cli.Option;

@Command(id="config",description="configure a plugin on the server")
public class Config extends RemoteCommandHandler {

	interface Args extends Arguments {
		@Option
		boolean isKeep();
	}	
	
	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return Args.class;
	}

	@Override
	public Result runRemote(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		Args args = (Args)arguments;
		String keep = args.isKeep() ? "true" : "false";
		return send("config", arguments, "keep", keep);
	}

}
