package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

import uk.co.flamingpenguin.jewel.cli.Option;

@Command(id="restart")
public class Restart extends CommandHandler {

	interface RArgs extends Arguments {
		
		@Option(description="update server to given branch after restart")
		String getUpdate();
		
		boolean isUpdate();
	}
	
	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return RArgs.class;
	}

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		RArgs args = (RArgs) arguments;
		String hook = "restart";
		String parm = null;
		
		if (args.isUpdate()) {
			
			String s = 
				cl.ask("please confirm attempt to update remote " 
						+ session.getName() + " to branch " + 
						args.getUpdate() + " [yes|no] ");

			if (s != null && s.equals("yes")) {
				cl.say("  Inserting update hook and shutting down server.");
				hook = "update";
				parm = args.getUpdate();
			} else {
				cl.say("restart aborted");
				return null;
			}
		}

		/*
		 * TODO send exit command with appropriate hook and parameter if required.
		 */
		
		/*
		 * TODO cleanup before disconnect
		 */
		
		return Result.ok(null).info("disconnected from " + session.getServer());
	}

}
