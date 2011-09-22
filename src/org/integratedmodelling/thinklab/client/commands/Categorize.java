package org.integratedmodelling.thinklab.client.commands;

import java.util.ArrayList;

import org.integratedmodelling.thinklab.client.RemoteCommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

import uk.co.flamingpenguin.jewel.cli.Option;

@Command(id="categorize",description="run a categorization model for a concept on the server")
public class Categorize extends RemoteCommandHandler {

	interface Args extends Arguments {

		@Option(longName="dump",shortName="d",description="report results as histogram")
		boolean isDump();

		@Option(longName="visualize",shortName="v",description="build visualization and send URL")
		boolean isVisualize();
		
		@Option(longName="output",shortName="o",description="download results as NetCDF")
		String getOutput();
		boolean isOutput();
		
		@Option(longName="publish",shortName="p",description="publish results as NetCDF into configured publish directory")
		String getPublish();
		boolean isPublish();
	}
	
	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return Args.class;
	}

	@Override
	public Result runRemote(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		Args args = (Args)arguments;
		ArrayList<String> opts = new ArrayList<String>();
		
		if (args.isDump()) {
			opts.add("dump");
			opts.add("true");
		}
		if (args.isVisualize()) {
			opts.add("visualize");
			opts.add("true");
		}
		if (args.isOutput()) {
			opts.add("output");
			opts.add(args.getOutput());
		}
		
		return send(
				"categorize", 
				arguments, 
				opts.size() == 0 ? (String[])null : opts.toArray(new String[opts.size()]));
	}

}
