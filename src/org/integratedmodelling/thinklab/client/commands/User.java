package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.RemoteCommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="user")
public class User extends RemoteCommandHandler {

	@Override
	public Result runRemote(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {
		
		String arg = expect(arguments, 0);
		
		if (arg.equals("add")) {
			
		} else if (arg.equals("remove")) {
			
		} else if (arg.equals("passwd")) {
			
		} else if (arg.equals("role")) {
			
		} else if (arg.equals("set")) {
			
		} else if (arg.equals("unset")) {
			
		}
		
		return null;
	}

}
