package org.integratedmodelling.thinklab.client.commands;

import java.util.Date;

import org.integratedmodelling.thinklab.client.RemoteCommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

import uk.co.flamingpenguin.jewel.cli.Option;

@Command(id="status")
public class Status extends RemoteCommandHandler {

	public interface Args extends Arguments {
		
		@Option
		public int getLog();
		public boolean isLog();
	}	
	
	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return Args.class;
	}

	@Override
	public Result runRemote(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {
		
		Result ret = session.send("", false);

		/*
		 * report all vars
		 */
		Date up = new Date(Long.parseLong(ret.get("boot.time").toString()));
		Date lt = new Date(Long.parseLong(ret.get("local.time").toString()));
		
		cl.say("   Running version " +
				ret.get("thinklab.version") + 
				" [" + ret.get("thinklab.branch") + "]");

		cl.say("   Since: " + up + " (local time: " + lt + ")");
		
		cl.say("   CPUs: " + ret.get("processors"));
		
		cl.say("  " + ret.get("memory.free") + " free memory out of " + ret.get("memory.max") + 
				" (total " + ret.get("memory.total") + ")");
		
		
		return ret;
	}

}
