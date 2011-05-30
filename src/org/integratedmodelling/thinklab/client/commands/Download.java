package org.integratedmodelling.thinklab.client.commands;

import java.io.File;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="get")
public class Download extends CommandHandler {

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		if (session == null)
			throw new ThinklabClientException("download: not connected to a server");
		
		String handle = expect(arguments, 0);			
		File dest = null;
		if (arguments.getArguments().size() > 1)
			dest = new File(expect(arguments,1));
		
		File ss = session.download(handle, dest, null);
		
		return Result.ok(session).setResult(ss.toString());
	}

}
