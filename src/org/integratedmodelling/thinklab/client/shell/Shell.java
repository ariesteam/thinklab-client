/**
 * Shell.java
 * ----------------------------------------------------------------------------------
 * 
 * Copyright (C) 2008 www.integratedmodelling.org
 * Created: Jan 17, 2008
 *
 * ----------------------------------------------------------------------------------
 * This file is part of Thinklab.
 * 
 * Thinklab is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Thinklab is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with the software; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * ----------------------------------------------------------------------------------
 * 
 * @copyright 2008 www.integratedmodelling.org
 * @author    Ferdinando Villa (fvilla@uvm.edu)
 * @author    Ioannis N. Athanasiadis (ioannis@athanasiadis.info)
 * @date      Jan 17, 2008
 * @license   http://www.gnu.org/licenses/gpl.txt GNU General Public License v3
 * @link      http://www.integratedmodelling.org
 **/
package org.integratedmodelling.thinklab.client.shell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.integratedmodelling.collections.Path;
import org.integratedmodelling.thinklab.client.CommandManager;
import org.integratedmodelling.thinklab.client.Result;
import org.integratedmodelling.thinklab.client.Session;
import org.integratedmodelling.thinklab.client.configuration.Configuration;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.integratedmodelling.thinklab.client.utils.ShellCommand;

import bsh.util.JConsole;

/**
 * A simple command-line driven interface, using the graphical BeanShell console.
 * 
 * Knows a few standard commands:
 * 
 *   connect <server> [<user> <password>]
 *   disconnect/reconnect
 *   quit/exit
 *   plugin create|load|unload|deploy|config|list
 *   server {add|remove|test} <name> <url>
 *   tasks 
 *     task ID - command 
 *   check <task>
 *   
 *   examples:
 *   
 *   > server add tlk http://ecoinformatics.uvm.edu/tlk-rest
 *   > server update tlk <branch>
 *      ....
 *   > connect tlk ken password
 *   
 *   tlk> plugin config directory <dir>
 *   tlk> plugin create|select org.integratedmodelling.aries.san-pedro
 *   tlk> plugin config add-dependency ....
 *   tlk> plugin load huginn-ken
 *   tlk> model --async -o file.nc model context
 *   	[task id 2003]
 *   tlk> model -o file.nc model context
 *     .....
 *     execution terminated
 *     downloading file.nc
 *   tlk> check 2003
 *   	task 2003 finished 23sec ago
 *      downloading file.nc
 *   tlk> status
 *   	memory available: xxxx
 *   	uptime: 2h 32s
 *      ...
 *   tlk> disconnect
 *   > plugin deploy org.integratedmodelling.aries.san-pedro aries-server
 *   > exit
 *    
 *   anything else is sent to the server as is.
 * 
 * @author Ferdinando Villa
 */
public class Shell implements CommandLine {
	
	private static final String RUNTIME_PROPERTY_PREFIX = "runtime.path.";
	private static ArrayList<String> _shell =
		new ArrayList<String>();
	
	JConsole console = null;
	File historyFile = null;
	
	Font inputFont = new Font("Courier", Font.BOLD, 12);
	Font outputFont = new Font("Courier", Font.PLAIN, 12);
	Session currentSession = new Session();
	
	protected String prompt() {
		return currentSession.isConnected() ?
				currentSession.getName() + ">" :
				">";
	}
	
	/* (non-Javadoc)
	 * @see org.integratedmodelling.thinklab.client.shell.CommandCock#ask(java.lang.String)
	 */
	@Override
	public String ask(String prompt) throws ThinklabClientException {
		console.print(prompt);
		String ret = readLine(console.getInputStream()).trim();
		
		/*
		 * the stupid console returns a ; for any empty command, which is weird - this should be
		 * removed and the console should be fixed.
		 */
		return ret == null ? null : ((ret.trim().isEmpty() || ret.equals(";")) ? null : ret.trim());
	}

	/* (non-Javadoc)
	 * @see org.integratedmodelling.thinklab.client.shell.CommandCock#ask(java.lang.String, java.lang.String)
	 */
	@Override
	public String ask(String prompt, String defaultresponse) throws ThinklabClientException {
		String ret = ask(prompt);
		return ret == null ? defaultresponse : ret;
	}
	
	/* (non-Javadoc)
	 * @see org.integratedmodelling.thinklab.client.shell.CommandCock#say(java.lang.String)
	 */
	@Override
	public void say(String text) {
		console.println(text);
	}
	
	/* (non-Javadoc)
	 * @see org.integratedmodelling.thinklab.client.shell.CommandCock#ask()
	 */
	@Override
	public String ask() throws ThinklabClientException {
		return ask(null);
	}
	
	public static void initialize() {

		/*
		 * register all commands
		 */
		CommandManager.initialize();

		/*
		 * compile a list of executables we want to support in the shell
		 */
		for (Object o : Configuration.get().getProperties().keySet()) {
			if (o.toString().startsWith(RUNTIME_PROPERTY_PREFIX)) {
				
				String exec = Path.getLast(o.toString(), '.');
				_shell.add(exec);
				
			}
		}
	}
	
	public class ConsolePanel extends JFrame {

		private static final long serialVersionUID = -1303258585100820402L;

		public ConsolePanel() {
		    super("Thinklab client");
		    Container content = getContentPane();
		    content.setBackground(Color.lightGray);
		    JPanel controlArea = new JPanel(new GridLayout(2, 1));
		    content.add(controlArea, BorderLayout.EAST);
		    console = new JConsole();
		    console.setFont(outputFont);
		    // Preferred height is irrelevant, since using WEST region
		    console.setPreferredSize(new Dimension(600, 400));
		    console.setBorder(BorderFactory.createLineBorder (Color.lightGray, 2));
		    console.setBackground(Color.white);
		    content.add(console, BorderLayout.WEST);
		    pack();
		    setVisible(true);
		  }
		}


	private boolean error;
	
	public Shell() throws ThinklabClientException {
		
		historyFile = 
			new File(
				Configuration.get().getWorkspace("client_config") + 
				File.separator + 
				".history");
	}
	
	public  void printStatusMessage() {
		
		console.println("ThinkLab client shell v" + Configuration.get().getVersion());
		console.println();
		
		console.println("Enter \'help\' for a list of commands; \'exit\' quits");
		console.println();
	}

	
	public static String readLine(InputStream stream) throws ThinklabClientException {
		String ret = null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		try {
			ret = reader.readLine();
		} catch (IOException e) {
			throw new ThinklabClientException(e);
		}
		return ret;
	}
	
	public void startConsole() throws Exception {
				
		ConsolePanel jpanels = new ConsolePanel();
		
		/*
		 * read history if any
		 */
		List<?> lines = null;
		try {
			lines = FileUtils.readLines(historyFile);
		} catch (IOException e) {
			// no problem
		}
		
		if (lines != null) {
			for (Object line : lines) {
				console.addToHistory(line.toString());
			}
		}
		
		
		/* greet user */
		printStatusMessage();

		String input = "";
		
		/* define commands from user input */
		while (true) {
			
			console.print(prompt() + " ");
			console.setStyle(inputFont);
			
			input = readLine(console.getInputStream()).trim();
			
			console.setStyle(outputFont);
			
			String shellc = null;
			String shs = null;
			
			for (String s : _shell) {
				if (input.startsWith(s)) {
					shs = s;
					shellc = Configuration.get().getProperties().
						getProperty(RUNTIME_PROPERTY_PREFIX + s);
				}
			}
			
			if ("exit".equals(input)) {
				System.exit(0);
				break;
				
			} else if (shellc != null) {
				
				/*
				 * remove cmd, substitute with path
				 */
				input = input.substring(shs.length()).trim();
				String[] argz = input.split("\\ ");
				String[] args = new String[argz.length + 1];
				for (int i = 0; i < args.length; i++) {
					args[i] = 
						(i == 0) ?
							shellc:
							argz[i - 1];
				}
				
				ShellCommand.Result res = 
					ShellCommand.exec(args, true, 
							new File(currentSession.getCurrentDirectory(false)));
				
				shs = null;
				shellc = null;
				
				console.print(res.error + res.output);
				
			} else if (input.startsWith("!")) {
				
				String ss = input.substring(1);
				for (int i = console.getHistory().size(); i > 0; i--) {
					String s = console.getHistory().get(i-1);
					if (s.startsWith(ss)) {
						console.println(s);
						execute(s);
						break;
					}
				}
				
			} else if (!("".equals(input)) && /* WTF? */!input.equals(";")) {
				
				execute(input);
				
				// TODO see if we want to exclude commands that created errors.
				if (/*!error*/true) {
			          BufferedWriter bw = null;
				      try {
				        	 bw = new BufferedWriter(
				        			  new FileWriter(historyFile, true));
				          bw.write(input.trim());
				          bw.newLine();
				          bw.flush();
				       } catch (IOException ioe) {
				       } finally {
				 	 if (bw != null) 
				 		 try {
				 			 bw.close();
				 	 	} catch (IOException ioe2) {
				 	 	}
				    }
				}
			}
		}
	}

	private void execute(String input) {

		
		try {
			
			Result result = CommandManager.execute(input, currentSession, this);
			
			this.error = false;
			
            if (result != null) {
            	String ret = result.print();
            	if (!ret.isEmpty())
            		console.println(result.print());
            	
            	currentSession = result.getSession();
            }
            console.getOut().flush();

		} catch (Exception e) {
			
			e.printStackTrace();
			this.error = true;
			console.setStyle(Color.red);
			console.println("  " + e.getMessage());
			console.setStyle(Color.black);
		}

        /*
         *  give it a little rest to help the output show entirely before the prompt
         *  is printed again.
         */
		try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
		}
	}
	
	public static void main(String[] args) throws Exception {
		initialize();
		Shell shell = new Shell();
		shell.startConsole();
	}

	@Override
	public void append(String string) {
		console.print(string);
	}
}
