package org.integratedmodelling.thinklab.client.commands;

import java.io.File;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="mkdir")
public class Mkdir extends CommandHandler {

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {
		
		File f = 
			new File(session.getCurrentDirectory(false) + File.separator + expect(arguments, 0));
		f.mkdirs();
		
		return Result.ok(session);
	}

}
