package org.integratedmodelling.thinklab.client.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ShellCommand {

	public static class Result {
		public int exitCode;
		public String output;
		public String error;
		public Exception exp;
	}
	
	static class StreamGobbler extends Thread {
		
	    InputStream is;
	    String type;
	    OutputStream os;
	 
	    String output = "";
	    
	    StreamGobbler(InputStream is, String type)
	    {
	        this(is, type, null);
	    }

	    StreamGobbler(InputStream is, String type, OutputStream redirect)
	    {
	        this.is = is;
	        this.type = type;
	        this.os = redirect;
	    }
	    
			/** creates readers to handle the text created by the external program
			 */		
	    public void run() {
	        
	    	try {
	            PrintWriter pw = null;
	            if (os != null)
	                pw = new PrintWriter(os);
	                
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null)
	            {
	            	output += line + "\n";
	            }
	            if (pw != null)
	                pw.flush();
	        } catch (IOException ioe) {
	        	throw new RuntimeException(ioe);
	        }
	    }
	}
	
	public static Result exec(String[] cmds, boolean waitForResult, File dir)
	{
	    Result result = new Result();
	    result.output = "";
	    try
	    {
	        Process process = null;
	        if(cmds.length > 1)
	            process=Runtime.getRuntime().exec(cmds, null, dir);
	        else
	            process=Runtime.getRuntime().exec(cmds[0], null, dir);
	        if (waitForResult)
	        {
	            StreamGobbler errordataReader = new StreamGobbler(process
	                    .getErrorStream(), "ERROR");

	            StreamGobbler outputdataReader = new StreamGobbler(process
	                    .getInputStream(), "OUTPUT");

	            errordataReader.start();
	            outputdataReader.start();

	            int exitVal = process.waitFor();
	            errordataReader.join();
	            outputdataReader.join();
	            result.exitCode = exitVal;
	            result.output = outputdataReader.output;
	            result.error = errordataReader.output;
	        }
	    }
	    catch (Exception exp)
	    {
	        result.exp = exp;
	        result.exitCode = -1;
	    }
	    return result;
	}
}
