package org.integratedmodelling.thinklab.client.modelling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.lang.SemanticType;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.modelling.IExtent;
import org.integratedmodelling.thinklab.api.modelling.IObservingObject;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.modelling.parsing.IObservingObjectDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IPropertyDefinition;
import org.integratedmodelling.thinklab.common.owl.KnowledgeManager;
import org.integratedmodelling.thinklab.common.utils.CamelCase;

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
		private boolean _distribute;
		private Object  _contextModel;
		
		Dependency() {}
		
		Dependency(Object cmodel, String formalName, IProperty property, boolean required, boolean distribute) {
			this._cmodel = cmodel;
			this._formalName = formalName;
			this._property = property;
			this._optional = required;
			this._distribute = distribute;
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

		@Override
		public boolean isDistributed() {
			return _distribute;
		}

		@Override
		public Object getContextModel() {
			return _contextModel;
		}

		@Override
		public Object getWhereCondition() {
			return null;
		}
		
	}
	
	ArrayList<IDependency> _dependencies = new ArrayList<IDependency>();
	ArrayList<ISemanticObject<?>> _observables = new ArrayList<ISemanticObject<?>>();
	ArrayList<String> _observableNames = new ArrayList<String>();
	
	String _observableCName;
	
	@Override
	public void addObservable(IList instance, String formalName) {
		try {
			if (_observableCName == null)
				_observableCName = instance.first().toString();
			_observables.add(KnowledgeManager.get().entify(instance));
			if (formalName == null) {
				formalName = CamelCase.toLowerCase(new SemanticType(_observableCName).getLocalName(), '-');
			}
			_observableNames.add(formalName);
		} catch (ThinklabException e) {
			throw new ThinklabRuntimeException(e);
		}
	}
	
	@Override
	public String getObservableConceptName() {
		return _observableCName;
	}

	@Override
	public void addDependency(Object cmodel, String formalName, IPropertyDefinition property, boolean optional, boolean distribute, 
			Object contextGenerator, 
			Object whereCondition) {
//		_dependencies.add(new Dependency(cmodel, formalName, 
//				(property == null ? null : KnowledgeManager.get().getProperty(property.getName())), 
//				optional, distribute));
	}

	@Override
	public List<IDependency> getDependencies() {
		return _dependencies;
	}

	/* (non-Javadoc)
	 * @see org.integratedmodelling.thinklab.api.modelling.IObservingObject#hasActionsFor(org.integratedmodelling.thinklab.api.knowledge.IConcept, org.integratedmodelling.thinklab.api.knowledge.IConcept)
	 */
	@Override
	public boolean hasActionsFor(IConcept observable, IConcept domainConcept) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.integratedmodelling.thinklab.api.modelling.parsing.IObservingObjectDefinition#addAction(org.integratedmodelling.thinklab.api.knowledge.IConcept, java.lang.String, org.integratedmodelling.thinklab.api.modelling.parsing.IExpressionDefinition, org.integratedmodelling.thinklab.api.modelling.parsing.IExpressionDefinition, boolean)
	 */
	@Override
	public void addAction(String target, int type, IExpression action, IExpression condition, IConcept[] domains) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IConcept addExtentConstraintFunction(IFunctionCall extentGenerator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IExtent> getExtentConstraints() {
		// TODO Auto-generated method stub
		return null;
	}

}
