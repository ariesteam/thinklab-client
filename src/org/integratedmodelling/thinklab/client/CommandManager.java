package org.integratedmodelling.thinklab.client;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.integratedmodelling.thinklab.client.CommandHandler.Arguments;
import org.integratedmodelling.thinklab.client.annotations.Command;
import org.integratedmodelling.thinklab.client.commands.Categorize;
import org.integratedmodelling.thinklab.client.commands.Cd;
import org.integratedmodelling.thinklab.client.commands.Check;
import org.integratedmodelling.thinklab.client.commands.Config;
import org.integratedmodelling.thinklab.client.commands.Connect;
import org.integratedmodelling.thinklab.client.commands.Coverage;
import org.integratedmodelling.thinklab.client.commands.Disconnect;
import org.integratedmodelling.thinklab.client.commands.Download;
import org.integratedmodelling.thinklab.client.commands.Gazetteer;
import org.integratedmodelling.thinklab.client.commands.Help;
import org.integratedmodelling.thinklab.client.commands.List;
import org.integratedmodelling.thinklab.client.commands.Measure;
import org.integratedmodelling.thinklab.client.commands.Mkdir;
import org.integratedmodelling.thinklab.client.commands.Model;
import org.integratedmodelling.thinklab.client.commands.Observe;
import org.integratedmodelling.thinklab.client.commands.Pload;
import org.integratedmodelling.thinklab.client.commands.Project;
import org.integratedmodelling.thinklab.client.commands.Pwd;
import org.integratedmodelling.thinklab.client.commands.Rank;
import org.integratedmodelling.thinklab.client.commands.Remote;
import org.integratedmodelling.thinklab.client.commands.Run;
import org.integratedmodelling.thinklab.client.commands.Shutdown;
import org.integratedmodelling.thinklab.client.commands.Status;
import org.integratedmodelling.thinklab.client.commands.Storyline;
import org.integratedmodelling.thinklab.client.commands.Upload;
import org.integratedmodelling.thinklab.client.commands.User;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.shell.CommandLine;
import org.integratedmodelling.thinklab.client.shell.Shell;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;
import org.integratedmodelling.thinklab.client.utils.ShellCommand;
import org.integratedmodelling.thinklab.client.utils.StringUtils;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class CommandManager {
		
	static HashMap<String, Class<? extends CommandHandler>> _commands = 
		new HashMap<String, Class<? extends CommandHandler>>();
	
	public static void registerCommand(String id, Class<? extends CommandHandler> cls) {
		_commands.put(id, cls);
	}
	
	public static void initialize()  {
		
		for (Class<?> cls : 
				MiscUtilities.findSubclasses(
						CommandHandler.class, 
						"org.integratedmodelling.thinklab.client.commands", 
						Shell.class.getClassLoader())) {	
			
			/*
			 * lookup annotation, ensure we can use the class
			 */
			if (cls.isInterface() || Modifier.isAbstract(cls.getModifiers()))
				continue;
			
			/*
			 * lookup implemented concept
			 */
			for (Annotation annotation : cls.getAnnotations()) {
				if (annotation instanceof Command) {
					registerCommand(((Command)annotation).id(), (Class<? extends CommandHandler>) cls);
				}
			}
			
		}

		/*
		 * checks if automatic registration worked; if not, manually register commands.
		 * This should disappear when the class discovery works in jar files.
		 * 
		 * TODO if this needs to stay, it should use a specific register function that
		 * only takes the class as an argument, and uses the annotation for the rest.
		 */
		if (_commands.size() == 0) {
			
			registerCommand("categorize", Categorize.class);
			registerCommand("cd", Cd.class);
			registerCommand("check", Check.class);
			registerCommand("config", Config.class);
			registerCommand("connect", Connect.class);
			registerCommand("coverage", Coverage.class);
			registerCommand("disconnect", Disconnect.class);
			registerCommand("get", Download.class);
			registerCommand("gazetteer", Gazetteer.class);
			registerCommand("help", Help.class);
			registerCommand("list", List.class);
			registerCommand("measure", Measure.class);
			registerCommand("mkdir", Mkdir.class);
			registerCommand("model", Model.class);
			registerCommand("observe", Observe.class);
			registerCommand("pload", Pload.class);
			registerCommand("project", Project.class);
			registerCommand("pwd", Pwd.class);
			registerCommand("rank", Rank.class);
			registerCommand("remote", Remote.class);
			registerCommand("run", Run.class);
			registerCommand("shutdown", Shutdown.class);
			registerCommand("status", Status.class);
			registerCommand("storyline", Storyline.class);
			registerCommand("put", Upload.class);
			registerCommand("user", User.class);

		}
		
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

			/*
			 * turn over to OS
			 */
			ShellCommand.Result res = 
				ShellCommand.exec(args, true, 
						new File(session.getCurrentDirectory(false)));

			cl.append(res.getOutput());
			
			ret = res.exitCode == 0 ? Result.ok(session) : Result.fail(session);
		}
		
		
		return ret;
	}
}
