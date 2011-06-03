package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.RemoteCommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

@Command(id="list")
public class List extends RemoteCommandHandler {

	@Override
	public Result runRemote(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		Result res = session.send("list", true, "arg", expect(arguments,0));
		cl.say(res.size() + " " + arguments.getArguments().get(0) + " on " 
				+ session.getName());
		for (int i = 0; i < res.size(); i++) {
			cl.say("   " + res.getResult(i));
		}
		return Result.ok(session);
	}

}
