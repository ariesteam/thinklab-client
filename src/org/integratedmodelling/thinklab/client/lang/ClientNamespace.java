package org.integratedmodelling.thinklab.client.lang;

import java.util.Collection;

import org.integratedmodelling.lang.model.LanguageElement;
import org.integratedmodelling.lang.model.Namespace;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;

/**
 * Just a proxy to the abstract parse tree, to implement a purely syntactic
 * language model for the client.
 * 
 * @author Ferd
 *
 */
public class ClientNamespace implements INamespace {

	Namespace _namespace;
	
	public ClientNamespace(Namespace n) {
		this._namespace = n;
	}

	@Override
	public String getNamespace() {
		return this._namespace.getId();
	}

	@Override
	public IOntology getOntology() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IModelObject> getModelObjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLastModification() {
		return this._namespace.getTimeStamp();
	}

	public Namespace getNamespaceBean() {
		return _namespace;
	}

	@Override
	public LanguageElement getLanguageElement() {
		return this._namespace;
	}
	
}
