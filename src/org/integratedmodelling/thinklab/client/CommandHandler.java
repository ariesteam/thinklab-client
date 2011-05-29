package org.integratedmodelling.thinklab.client;

import java.util.List;

import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;

import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

public abstract class CommandHandler {
		
	public static interface Arguments {
		
		@Unparsed
		List<String> getArguments();
		
		@Option(helpRequest = true) 
		boolean getHelp();
	}
	
	public String expect(Arguments args, int n) throws ThinklabClientException {
		if (args.getArguments().size() < n-1) {
			throw new ThinklabClientException("not enough arguments passed to command");
		}
		return args.getArguments().get(n);
	}
	
	/**
	 * This one will get a command using the appropriate interface. 
	 * Unfortunately we will need to cast it until I find a smarter way.
	 * @param session 
	 * 
	 * @param command
	 * @return
	 * @throws ThinklabClientException 
	 */
	public abstract Result execute(Arguments arguments, Session session) throws ThinklabClientException;
	
	/**
	 * Must return an inner interface annotated with jewelCli annotations for
	 * argument and options declaration. Extend it with an inner interface to
	 * implement options.
	 * 
	 * @return
	 */
	public Class<? extends Arguments> getArgumentsClass() {
		return Arguments.class;
	}
}
