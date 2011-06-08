package org.integratedmodelling.thinklab.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.integratedmodelling.thinklab.client.CommandHandler.Arguments;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;
import org.integratedmodelling.thinklab.client.utils.StringUtils;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class CommandManager {
		
	static HashMap<String, Class<? extends CommandHandler>> _commands = 
		new HashMap<String, Class<? extends CommandHandler>>();
	
	public static void registerCommand(String id, Class<? extends CommandHandler> cls) {
		_commands.put(id, cls);
	}
	
	public static Result execute(String s, Session session, CommandLine cl) throws ThinklabClientException {
		
		Collection<String> sss = StringUtils.tokenize(s);
		String[] ss = sss.toArray(new String[sss.size()]);
		
		Result ret = null;
		
		Class<? extends CommandHandler> cmdcl = _commands.get(ss[0]);
		String[] args = Arrays.copyOfRange(ss, 1, ss.length);
		
		if (cmdcl != null) {
			
			try {
				CommandHandler cmd = cmdcl.newInstance();
				final Object command = CliFactory.parseArguments(cmd.getArgumentsClass(), args);
				ret = cmd.execute((Arguments) command, session, cl);
			} catch (Exception e) {
				
				if (e instanceof ArgumentValidationException) {
					ret = Result.ok(session).info(e.getMessage());
				} else {
					throw new ThinklabClientException(e);
				}
			}
		} else {

			throw new ThinklabClientException("command " + s + " unknown");
		}
		
		
		return ret;
	}
}
