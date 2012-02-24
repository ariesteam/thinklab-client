package org.integratedmodelling.thinklab.client.modelling;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabValidationException;
import org.integratedmodelling.lang.model.Namespace;
import org.integratedmodelling.thinklab.api.knowledge.storage.IKBox;
import org.integratedmodelling.thinklab.api.lang.IModelParser;
import org.integratedmodelling.thinklab.api.modelling.IAgentModel;
import org.integratedmodelling.thinklab.api.modelling.IContext;
import org.integratedmodelling.thinklab.api.modelling.IModel;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.IScenario;
import org.integratedmodelling.thinklab.api.modelling.factories.IModelManager;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.ISession;
import org.integratedmodelling.thinklab.client.lang.ClientNamespace;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;

/**
 * A model manager that can parse the Thinklab language and build a model map, without actually being 
 * capable of running the model objects.
 * 
 * @author Ferd
 *
 */
public class ModelManager implements IModelManager {

	private static ModelManager _this = null;
	
	HashMap<String, IModelParser> interpreters = new HashMap<String, IModelParser>();
	HashMap<String, INamespace> namespaces = new HashMap<String, INamespace>();
	
	public static ModelManager get() {
		if (_this == null) {
			_this = new ModelManager();
		}
		return _this;
	}
	
	public void addInterpreter(String extension, IModelParser modelParser) {
		interpreters.put(extension, modelParser);
	}
	
	@Override
	public IAgentModel getAgentModel(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IScenario> getApplicableScenarios(IModel arg0,
			IContext arg1, boolean arg2) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContext getCoverage(IModel arg0, IKBox arg1, ISession arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IModelObject> getDependencies(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModel getModel(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModelObject getModelObject(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Override this to return a new subclass of Resolver to intercept
	 * errors and warnings.
	 * 
	 * @param project
	 * @return
	 */
	public Resolver createResolver(IProject project) {
		return new Resolver(project);
	}

	@Override
	public INamespace getNamespace(String arg0) {
		return namespaces.get(arg0);
	}

	@Override
	public Collection<INamespace> getNamespaces() {

		ArrayList<INamespace> ret = new ArrayList<INamespace>();
		for (INamespace n : namespaces.values()) {
			ret.add(n);
		}
		
		/*
		 * sort namespace list alphabetically
		 */
		Collections.sort(ret, new Comparator<INamespace>() {
			@Override
			public int compare(INamespace arg0, INamespace arg1) {
				return arg0.getNamespace().compareTo(arg1.getNamespace());
			}
		});
		
		return ret;
		
	}

	@Override
	public IScenario getScenario(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSource(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INamespace loadFile(String file, IProject project) throws ThinklabException {
		
		String extension = MiscUtilities.getFileExtension(file);
		IModelParser parser = interpreters.get(extension);
		INamespace ret = null;
		
		if (parser == null) {
			throw new ThinklabValidationException("don't know how to parse a " + extension + " model file");
		}
		
		Namespace ns = parser.parse(file, createResolver(project));
		ns.setProject(project);
		ns.setSourceFile(new File(file));
		ns.synchronizeKnowledge();
		
		ret = new ClientNamespace(ns);
		
		namespaces.put(ret.getNamespace(), ret);
		
		return ret;
	}

	@Override
	public void releaseNamespace(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public IContext run(IModel arg0, IKBox arg1, ISession arg2, IContext arg3)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<INamespace> load(IProject project)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canParseExtension(String fileExtension) {
		return interpreters.containsKey(fileExtension);
	}

	public INamespace loadNamespace(String namespaceId, String resource, String resourceType)
			throws ThinklabException {
		
		String extension = resourceType;
		if (extension == null)
			extension = MiscUtilities.getFileExtension(resource);
		
		IModelParser parser = interpreters.get(extension);
		INamespace ret = null;
		
		if (parser == null) {
			throw new ThinklabValidationException("don't know how to parse a " + extension + " resource");
		}
		
		Namespace ns = parser.parse(resource, createResolver(null));

		if (ns != null) {
			ns.setId(namespaceId);
			ns.synchronizeKnowledge();
			ret = new ClientNamespace(ns);
			namespaces.put(ret.getNamespace(), ret);
		}
		
		return ret;
	}

}
