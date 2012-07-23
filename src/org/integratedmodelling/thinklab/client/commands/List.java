package org.integratedmodelling.thinklab.client.commands;

import java.util.ArrayList;

import org.integratedmodelling.collections.Path;
import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.configuration.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;

import uk.co.flamingpenguin.jewel.cli.Option;

/**
 * A bit of a hybrid command - responds by default remotely or locally according
 * to what listing is requested. For now only "projects" responds locally by
 * default.
 * 
 * @author ferdinando.villa
 *
 */
@Command(id="list")
public class List extends CommandHandler {

	public interface Args extends Arguments {

		@Option(shortName="s", longName="source")
		boolean isSource();

		@Option
		boolean isDependencies();		
		
		@Option(shortName="t", longName="tree")
		boolean isTree();
		
		@Option
		boolean isRemote();

		@Option
		boolean isCanonical();

	}
	
	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		ArrayList<String> argu = new ArrayList<String>();
		Args args = (Args) arguments;

		if (expect(args,0).equals("projects")) {
//
//			ThinklabProject current = session.getCurrentProject();
//
//			for (File f : Configuration.getProjectDirectory().listFiles()) {
//				if (f.isDirectory() && 
//						ThinklabProject.exists(MiscUtilities.getFileName(f.toString()))) {
//					String pname = MiscUtilities.getFileName(f.toString());
//					cl.say(
//							(current != null && current.getId().equals(pname) ? " * " : "   ") + 
//							pname);
//				}
//			}			
//			return Result.ok(session);
			
		} else if (expect(args,0).equals("remotes")) {
			
			for (Object o : Configuration.get().getProperties().keySet()) {
				String pk = o.toString();
				if (pk.startsWith("server.")) {
					String s = Path.getLast(pk, '.');
					String h = Configuration.get().getProperties().getProperty(pk);
					
					cl.say("  " + s + "\t" + h); 
				}
			}
			return Result.ok(session);
		}
		
		/*
		 * no other local options, transfer to remote
		 */
		if (!session.isConnected())
			throw new ThinklabClientException("remote command: not connected to a server");

		argu.add("arg");
		argu.add(expect(args, 0));
		
		if (args.isTree()) {
			argu.add("tree");
			argu.add("true");
		}
		if (args.isCanonical()) {
			argu.add("canonical");
			argu.add("true");
		}
		if (args.isDependencies()) {
			argu.add("dependencies");
			argu.add("true");
		}
		if (args.isSource()) {
			argu.add("source");
			argu.add("true");
		}
		
		if (args.getArguments().size() > 1) {
			argu.add("match");
			argu.add(args.getArguments().get(1));
		}
	
		Result res = session.send("list", true, argu.toArray(new String[argu.size()]));

		boolean isItem = res.get("is-item") != null && res.get("is-item").equals("true");

		for (int i = 0; i < res.size(); i++) {
			String pref = /*res.getResult(i).toString().startsWith(" ") ? "" : "  "*/ "";
			cl.say(pref + res.getResult(i));
		}
		
		if (!isItem)
			cl.say(res.size() + " " + arguments.getArguments().get(0) + " on " 
				+ session.getName());
		
		return Result.ok(session);
	}

	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return Args.class;
	}

}
