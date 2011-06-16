package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="help",description="list available commands")
public class Help extends CommandHandler {

	interface Args extends Arguments {
	}	
	
	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return Args.class;
	}

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {
		
		Args args = (Args)arguments;
			
		if (args.getArguments() == null || args.getArguments().size() == 0) {
			
			/*
			 * list all commands
			 */
			
			return Result.ok(session);
		}

		/* list single command */
		String cmd = expect(args, 0);

		return Result.ok(session);
	}

}
