package org.integratedmodelling.thinklab.client.modelling;

import java.io.File;
import java.util.Collection;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.storage.IKBox;
import org.integratedmodelling.thinklab.api.modelling.IAgentModel;
import org.integratedmodelling.thinklab.api.modelling.IAnnotation;
import org.integratedmodelling.thinklab.api.modelling.IModel;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.IScenario;
import org.integratedmodelling.thinklab.api.modelling.factories.IModelManager;
import org.integratedmodelling.thinklab.api.modelling.observation.IContext;
import org.integratedmodelling.thinklab.api.runtime.ISession;

/**
 * A model manager that can parse the Thinklab language and build a model map, without actually being 
 * capable of running the model objects.
 * 
 * @author Ferd
 *
 */
public class ModelManager implements IModelManager {

	private static ModelManager _this = null;
	
	public static ModelManager get() {
		if (_this == null) {
			_this = new ModelManager();
		}
		return _this;
	}
	
	@Override
	public IAgentModel getAgentModel(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAnnotation getAnnotation(String arg0) {
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

	@Override
	public INamespace getNamespace(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<INamespace> getNamespaces() {
		// TODO Auto-generated method stub
		return null;
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
	public INamespace loadFile(String arg0) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
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
	public Collection<INamespace> load(File sourceFolder) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

}
