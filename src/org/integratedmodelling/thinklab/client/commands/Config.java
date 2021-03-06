package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.configuration.Configuration;
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
			return session.send("config", true, 
					"plugin", expect(args,0), 
					"variable", expect(args,1), 
					"value", expect(args,2), 
					"keep", keep);
		}
			
		if (args.getArguments() == null || args.getArguments().size() == 0) {
			
			/*
			 * print configuration
			 */
			for (Object s : Configuration.get().getProperties().keySet()) {
				
				String k = (String)s;
				String p = Configuration.get().getProperties().getProperty(k);
				
				cl.say("  " + k + "\t" + p);
			}
			
			return Result.ok(session);
		}
		
		String var = expect(args, 0);
		String val = expect(args, 1);
		Configuration.get().getProperties().setProperty(var, val);

		return Result.ok(session);
	}

}
