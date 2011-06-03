package org.integratedmodelling.thinklab.client.commands;

import java.io.File;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="put")
public class Upload extends CommandHandler {

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		if (!session.isConnected())
			throw new ThinklabClientException("upload: not connected to a server");
		
		File file = new File(expect(arguments, 0));
		if (!file.exists())
			throw new ThinklabClientException("upload: file " + file + " does not exist");
			
		String ss = session.upload(file, null);
		
		return Result.ok(session).setResult(ss);
	}

}
