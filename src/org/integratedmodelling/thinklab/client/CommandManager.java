package org.integratedmodelling.thinklab.client;

import java.util.Arrays;
import java.util.HashMap;

import org.integratedmodelling.thinklab.client.CommandHandler.Arguments;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class CommandManager {
		
	static HashMap<String, Class<? extends CommandHandler>> _commands = 
		new HashMap<String, Class<? extends CommandHandler>>();
	
	public static void registerCommand(String id, Class<? extends CommandHandler> cls) {
		_commands.put(id, cls);
	}
	
	public static Result execute(String s, Session session) throws ThinklabClientException {
		
		String[] ss = s.trim().split("\\ ");
		Result ret = null;
		
		Class<? extends CommandHandler> cmdcl = _commands.get(ss[0]);
		String[] args = Arrays.copyOfRange(ss, 1, ss.length);
		
		if (cmdcl != null) {
			
			try {
				CommandHandler cmd = cmdcl.newInstance();
				final Object command = CliFactory.parseArguments(cmd.getArgumentsClass(), args);
				ret = cmd.execute((Arguments) command, session);
			} catch (Exception e) {
				
				if (e instanceof ArgumentValidationException) {
					ret = Result.info(e.getMessage());
				} else {
					throw new ThinklabClientException(e);
				}
			}
		} else {
			/*
			 * send to REST server connected if any
			 */
		}
		return ret;
	}
}
