package org.integratedmodelling.thinklab.client.commands;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;
import org.integratedmodelling.thinklab.client.utils.Path;

@Command(id="remote")
public class Remote extends CommandHandler {

	@Override
	public Result execute(Arguments arg, Session session, CommandLine cl) throws ThinklabClientException {
		
		String cmd = expect(arg, 0);
		Result ret = null;
		
		if ("add".equals(cmd)) {

			String op  = expect(arg, 1);
			String url = expect(arg, 2);

			Configuration.getProperties().setProperty("server." + op, url);
			Configuration.saveProperties();
			
		} else if ("remove".equals(cmd)) {

			String op  = expect(arg, 1);
			Configuration.getProperties().remove("server." + op);
			Configuration.saveProperties();
			
		} else if ("list".equals(cmd)) {
			
			for (Object o : Configuration.getProperties().keySet()) {
				String pk = o.toString();
				if (pk.startsWith("server.")) {
					String s = Path.getLast(pk, '.');
					String h = Configuration.getProperties().getProperty(pk);
					
					cl.say("  " + s + "\t" + h); 
				}
			}
		}
		
		return ret;
	}

}