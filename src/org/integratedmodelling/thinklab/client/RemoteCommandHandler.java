package org.integratedmodelling.thinklab.client;

import java.io.File;

import org.integratedmodelling.thinklab.client.Session.RemoteCommand;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;
import org.integratedmodelling.thinklab.client.utils.Pair;

import uk.co.flamingpenguin.jewel.cli.Option;

public abstract class RemoteCommandHandler extends CommandHandler {
	
	Session _session = null;
	
	public interface Arguments extends CommandHandler.Arguments {
		
		@Option(description="return immediately, don't wait for completion of long-running command")
		boolean isAsync();
	}
	
	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return Arguments.class;
	}

	protected boolean _asynchronous = false;
	
	/**
	 * Use this rather than session.send() to call commands on the server. Automatically sets
	 * async flag and validates command against declaration reported by server. Unfortunately we 
	 * cannot know the options passed in advance, so you need to pass them.
	 * 
	 * @param command
	 * @param arguments
	 * @return
	 * @throws ThinklabClientException
	 */
	protected Result send(String command, Arguments arguments, String ... options) throws ThinklabClientException {

		RemoteCommand rc = _session.getRemoteCommandDeclaration(command);
		
		if (rc == null)
			throw new ThinklabClientException("command " + rc + " unknown to server " + _session.getName());
		
		if (rc.args.length < arguments.getArguments().size()) {
			throw new ThinklabClientException("remote command " + rc + " has less arguments than passed " 
						+ arguments.getArguments().size());
		}
		
		String[] args =
			new String[(arguments.getArguments().size() * 2) + 
			           (options == null ? 0 : options.length)];
		
		/*
		 * pair arguments with argument names; validate options against declaration
		 */
		int i = 0, a = 0;
		for (String s : arguments.getArguments()) {
			args[i++] = rc.args[a++];
			args[i++] = s;
		}
		if (options != null) {
			for (String s : options) {

				/*
				 * validate options against declaration
				 */
				boolean found = false;
				if (i % 2 == 0) {
					for (String ss : rc.opts)
						if (ss.equals(s)) {
							found = true;
							break;
						}
					if (!found) {
						throw new ThinklabClientException("command " + command
								+ " does not admit passed option " + s);
					}
				}

				args[i++] = s;
			}
		}
		
		return _session.send(command, _asynchronous, args);
	}
	
	public abstract Result runRemote(Arguments arguments, Session session, CommandLine cl)
		throws ThinklabClientException;
	
	@Override
	public final Result execute(CommandHandler.Arguments arguments, Session session, CommandLine cl) throws ThinklabClientException {
		
		if (session == null)
			throw new ThinklabClientException("remote command: not connected to a server");
		
		
		Arguments args = (Arguments)arguments;
		
		_asynchronous = args.isAsync();
		_session = session;

		Result result = runRemote(args, session, cl);
		
		/*
		 * postprocess result; if anything needs to be downloaded, download
		 */
		if (result.getStatus() == Result.OK) {
			
			for (Pair<String,String> df : result.getDownloads()) {
				cl.say("downloading result file " + df.getFirst());
				session.download(df.getSecond(), new File(df.getFirst()), null);
			}
		}
		
		return result.setSession(session);
	}

}
