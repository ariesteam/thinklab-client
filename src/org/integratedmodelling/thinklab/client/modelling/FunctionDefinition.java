package org.integratedmodelling.thinklab.client.modelling;

import java.util.Map;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.project.IProject;

public class FunctionDefinition extends ModelObject implements IFunctionCall {

	String _type;
	Map<String, Object> _parameters;
	IProject _project;
	
	@Override
	public void set(String type, Map<String, Object> parms) {
		_type = type;
		_parameters  = parms;
	}

	@Override
	public Map<String, Object> getParameters() {
		return _parameters;
	}

	@Override
	public String getId() {
		return _type;
	}

	@Override
	public void setProject(IProject project) {
		_project = project;
	}

	@Override
	public Object call() throws ThinklabException {
		// TODO use server, throw warnings etc
		return null;
	}
	
}
