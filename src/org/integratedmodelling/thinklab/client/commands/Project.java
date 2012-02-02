package org.integratedmodelling.thinklab.client.commands;

import java.io.File;

import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.project.ThinklabProject;
import org.integratedmodelling.thinklab.client.shell.CommandLine;
import org.integratedmodelling.thinklab.client.utils.FolderZiper;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;

import uk.co.flamingpenguin.jewel.cli.Option;

@Command(id="project")
public class Project extends CommandHandler {

	interface Args extends CommandHandler.Arguments {
		
		@Option
		public boolean isForce();
		
		@Option
		public boolean isRemote();
	}
	
	@Override
	public Class<? extends Arguments> getArgumentsClass() {
		return Args.class;
	}

	@Override
	public Result execute(Arguments arguments, Session session, CommandLine cl)
			throws ThinklabClientException {

		Args args = (Args)arguments;
				
		ThinklabProject current = session.getCurrentProject();
		String info = "";
		
		String cmd = expect(args,0);

		if (cmd.equals("create")) {
			
			String projectId = expect(args,1);
			session.setCurrentProject(ThinklabProject.create(projectId));
			info = 
				"project " + projectId + " created in " + 
				Configuration.getProjectDirectory();
		
		} else if (cmd.equals("load")) {

			String projectId = expect(args,1);
			session.setCurrentProject(ThinklabProject.load(projectId));
			info = "project " + projectId + " loaded";

		} else if (cmd.equals("info")) {

			ThinklabProject proj = current;
			if (args.getArguments().size() > 1) {
				proj = ThinklabProject.load(expect(args,1));
			}
			checkCurrent(proj);
			
			/*
			 * TODO
			 */
			
		} else if (cmd.equals("list")) {
			
			if (args.isRemote()) {
				
				if (!session.isConnected())
					return Result.fail(session).error("project: not connected to a server");

				Result res = session.send("list", true, "arg", "projects");
				if (res.getStatus() == Result.OK) {
					cl.say(res.size() + " projects on " + session.getName());
					for (int i = 0; i < res.size(); i++) {
						cl.say("   " + res.getResult(i));
					}
				} else {
					return res;
				}
				
			} else {
			
				for (File f : Configuration.getProjectDirectory().listFiles()) {
					if (f.isDirectory() && 
							ThinklabProject.exists(MiscUtilities.getFileName(f.toString()))) {
						String pname = MiscUtilities.getFileName(f.toString());
						cl.say(
								(current != null && current.getId().equals(pname) ? " * " : "   ") + 
								pname);
					}
				}
			}
 			
		} else if (cmd.equals("deploy")) {

			if (!session.isConnected())
				return Result.fail(session).error("project: not connected to a server");

			checkCurrent(current);
			File zip = current.getZipArchive();
			
			cl.append("uploading project... ");
			String handle = session.upload(zip, null);
			cl.say("done");
			
			cl.append("remote deploy... ");
			Result result = session.send("project", false, 
					"cmd", "deploy", 
					"handle", handle, 
					"plugin", current.getId());
		
			if (result.getStatus() == Result.OK) {
				cl.say("done");
				info = "project " + current.getId() + " deployed to " + session.getName();
			} else {
				cl.say("failed");
				return result;
			}
			
		} else if (cmd.equals("undeploy")) {

			if (!session.isConnected())
				return Result.fail(session).error("project: not connected to a server");

			checkCurrent(current);

			cl.append("remote undeploy... ");
			Result result = session.send("project", false, 
					"cmd", "undeploy", 
					"plugin", current.getId());

			if (result.getStatus() == Result.OK) {
				cl.say("done");
				info = "project " + current.getId() + " removed from " + session.getName();
			} else {
				cl.say("failed");
				return result;
			}

		} else if (cmd.equals("import")) {
			
			if (!session.isConnected())
				return Result.fail(session).error("project: not connected to a server");

			String pid = expect(args,1);
			boolean ok = true;
			
			if (ThinklabProject.exists(pid)) {
				if (!args.isForce() && !cl.ask("do you want to overwrite project " +
					pid + 
					" from the local repository? [yes|no] ").equals("yes")) {
					ok = false;
				}
			}
			
			if (ok) {
				Result ret = session.send("project", false, "cmd", "pack", "plugin", pid);
				String handle = (String)ret.get("handle");
				if (handle != null) {
				
					cl.append("downloading... ");
					Pair<File, Integer> zip = session.download(handle, null, null);
					cl.say("done (" + zip.getSecond()/1024 + "k)");
				
					FolderZiper.unzip(zip.getFirst(), Configuration.getProjectDirectory());
				
					session.setCurrentProject(ThinklabProject.load(pid));
					info = "project " + pid + " imported from " + session.getName() + " and loaded";
				
				} else {
					info = "no project retrieved";
				}
			}	
				
		} else if (cmd.equals("remove") || cmd.equals("delete")) {
			
			String pid = null;
			if (current == null) {
				pid = expect(args, 1);
			} else {
				pid = current.getId();
			}
			
			ThinklabProject proj = ThinklabProject.load(pid);
			if (proj == null) {
				return Result.fail(session).error("project " + pid + " does not exist on the client");
			}
				
			if (args.isForce() || cl.ask("do you want to remove project " +
					pid + 
					" from the local repository? [yes|no] ").equals("yes")) {
				
				MiscUtilities.recursiveDelete(proj.getPath());
				cl.say("project " + pid + " removed from disk");				
				if (current != null && current.equals(proj))
					session.setCurrentProject(current = null);
			}
		}
		
		return Result.ok(session).info(info);
	}

	private void checkCurrent(ThinklabProject current) throws ThinklabClientException {
		if (current == null) {
			throw new ThinklabClientException(
					"no current project set: create or load a project first");
		}
	}

}
