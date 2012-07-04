package org.integratedmodelling.thinklab.client.utils;

import java.io.File;
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;

public class JVMLauncher {

	PrintStream _ostream = System.out;
	PrintStream _estream = System.err;
	int _logLevel = Project.MSG_INFO;
	File _baseDir = new File(System.getProperty("user.dir"));
	Path _classPath = null;
	
	Java _javaTask = new Java();
	Project _project = null;
	
	
	public void addToClasspath(Object classpathFile) {
		if (_classPath == null) {
			_classPath = _javaTask.createClasspath();
		}
		if (classpathFile instanceof File) {
			File f = (File)classpathFile;
			if (f.isDirectory()) {
			}
		}

		/*
		 * TODO finish
		 */
	}
	
	public void launch(String classname) {

        _project = new Project();
        _project.setBaseDir(_baseDir);
        _project.init();
        DefaultLogger logger = new DefaultLogger();
        _project.addBuildListener(logger);
//        logger.setOutputPrintStream(_ostream);
//        logger.setErrorPrintStream(_estream);
//        logger.setMessageOutputLevel(_logLevel);
//        System.setOut(new PrintStream(new DemuxOutputStream(_project, false)));
//        System.setErr(new PrintStream(new DemuxOutputStream(_project, true)));
        _project.fireBuildStarted();

        try {
//                Echo echo = new Echo();
//                echo.setTaskName("Echo");
//                echo.setProject(_project);
//                echo.init();
//                echo.setMessage("Launching Some Class");
//                echo.execute();

                _javaTask.setTaskName("runjava");
                _javaTask.setProject(_project);
                _javaTask.setFork(true);
                _javaTask.setSpawn(true);
                _javaTask.setFailonerror(false);
                _javaTask.setClassname(classname);
                if (_classPath != null)	{
                	_javaTask.setClasspath(_classPath);
                }
                _javaTask.init();
                int ret = _javaTask.executeJava();

        } catch (BuildException e) {
    			_project.fireBuildFinished(e);
    			_project = null;
        }

	}
	
	public boolean isRunning() {
		return _project != null;
	}
	
	public void shutdown() {
		if (_project != null)
			_project.fireBuildFinished(null);
		_project = null;
	}
}