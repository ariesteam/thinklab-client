package org.integratedmodelling.thinklab.client.commands;

import java.io.File;

import org.integratedmodelling.thinklab.client.CommandHandler;
import org.integratedmodelling.thinklab.client.Configuration;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.project.ThinklabProject;
import org.integratedmodelling.thinklab.client.shell.CommandLine;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;

@Command(id="project")
public class Project extends CommandHandler {

	interface Args extends CommandHandler.Arguments {
		
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
			
			for (File f : Configuration.getProjectDirectory().listFiles()) {
				if (f.isDirectory() && 
					ThinklabProject.exists(MiscUtilities.getFileName(f.toString()))) {
					String pname = MiscUtilities.getFileName(f.toString());
					cl.say(
						(current != null && current.getId().equals(pname) ? " * " : "   ") + 
						pname);
				}
			}
 			
		} else if (cmd.equals("deploy")) {
			
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
			cl.say("done");
		
			info = "project " + current.getId() + " deployed to " + session.getName();

			
		} else if (cmd.equals("undeploy")) {

			checkCurrent(current);

			cl.append("remote undeploy... ");
			Result result = session.send("project", false, 
					"cmd", "undeploy", 
					"plugin", current.getId());
			cl.say("done");

			info = "project " + current.getId() + " removed from " + session.getName();

		} else if (cmd.equals("import")) {
			
			/*
			 * TODO import project from server; ask for confirmation/save/rename if
			 * already existing.
			 */
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
