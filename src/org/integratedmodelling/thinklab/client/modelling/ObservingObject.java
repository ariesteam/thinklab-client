package org.integratedmodelling.thinklab.client.modelling;

import java.util.ArrayList;
import java.util.List;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.modelling.IObservingObject;
import org.integratedmodelling.thinklab.api.modelling.parsing.IObservingObjectDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IPropertyDefinition;
import org.integratedmodelling.thinklab.client.knowledge.KnowledgeManager;
import org.integratedmodelling.thinklab.client.knowledge.Property;

/**
 * Models and Observers. They both have observables, which are complicated enough to handle
 * to deserve being handled once.
 * 
 * @author Ferd
 *
 */
public abstract class ObservingObject extends ModelObject implements IObservingObject, IObservingObjectDefinition {
	
	public static class Dependency implements IDependency {

		private Object _cmodel;
		private String _formalName;
		private IProperty _property;
		private boolean _optional;

		Dependency() {}
		
		Dependency(Object cmodel, String formalName, IProperty property, boolean required) {
			this._cmodel = cmodel;
			this._formalName = formalName;
			this._property = property;
			this._optional = required;
		}
		
		@Override
		public Object getObservable() {
			return _cmodel;
		}

		@Override
		public String getFormalName() {
			return _formalName;
		}

		@Override
		public boolean isOptional() {
			return _optional;
		}

		@Override
		public IProperty getProperty() {
			return _property;
		}
		
	}
	
	ArrayList<IDependency> _dependencies = new ArrayList<IDependency>();
	ArrayList<ISemanticObject<?>> _observables = new ArrayList<ISemanticObject<?>>();

	String _observableCName;
	
	@Override
	public void addObservable(IList instance, String formalName) {
		try {
			_observableCName = instance.first().toString();
			_observables.add(KnowledgeManager.get().entify(instance));
		} catch (ThinklabException e) {
			throw new ThinklabRuntimeException(e);
		}
	}
	
	@Override
	public String getObservableConceptName() {
		return _observableCName;
	}


	@Override
	public void addDependency(Object cmodel, String formalName, IPropertyDefinition property, boolean optional) {
		_dependencies.add(new Dependency(cmodel, formalName, 
				(property == null ? null : new Property(property.getNamespace().getId(), property.getId())), 
				optional));
	}

	@Override
	public List<IDependency> getDependencies() {
		return _dependencies;
	}
	

}
