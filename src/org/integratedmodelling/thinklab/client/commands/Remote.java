package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;

@Command(id="remote")
public class Remote extends CommandHandler {

	@Override
	public Result execute(Arguments arg, Session session) throws ThinklabClientException {
		
		String cmd = expect(arg, 0);
		String op  = expect(arg, 1);
		
		if ("add".equals(cmd)) {

			String url = expect(arg, 2);
			Configuration.getProperties().setProperty("server." + op, url);
			Configuration.saveProperties();
			
		} else if ("remove".equals(cmd)) {

			Configuration.getProperties().remove("server." + op);
			Configuration.saveProperties();
			
		}
		
		return null;
	}

}