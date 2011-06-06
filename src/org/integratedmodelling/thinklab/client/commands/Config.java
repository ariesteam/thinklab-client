package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

import uk.co.flamingpenguin.jewel.cli.Option;

@Command(id="config",description="configure a plugin on the server")
public class Config extends CommandHandler {

	interface Args extends Arguments {
		@Option
		boolean isKeep();
		
		@Option
		boolean isRemote();
	}	
	
	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return Args.class;
	}

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {
		
		Args args = (Args)arguments;
		if (args.isRemote()) {
			
			if (!session.isConnected())
				throw new ThinklabClientException("remote command: not connected to a server");
			
			String keep = args.isKeep() ? "true" : "false";
			return session.send("config", true, expect(args,0), expect(args,1), "keep", keep);
		}
			
		String var = expect(args, 0);
		String val = expect(args, 1);
		Configuration.getProperties().setProperty(var, val);

		return Result.ok(session);
	}

}
