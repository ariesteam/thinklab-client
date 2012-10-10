package org.integratedmodelling.thinklab.client.modelling;

import java.util.List;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IAccessor;
import org.integratedmodelling.thinklab.api.modelling.IDataSource;
import org.integratedmodelling.thinklab.api.modelling.IExtent;
import org.integratedmodelling.thinklab.api.modelling.IObserver;
import org.integratedmodelling.thinklab.api.modelling.IScale;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.modelling.parsing.IModelDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IObserverDefinition;

public class Model extends ObservingObject implements IModelDefinition {

	IObserver _observer;
	IAccessor _accessor = null;
	
	@Override
	public void addObserver(IObserverDefinition odef, IExpression edef) {
		
		IObserver observer = (IObserver)odef;
		IExpression expression = (IExpression)edef;
		
		if (_observer == null && expression == null) {
			_observer = observer;
		} else {
			if (_observer == null) {
				_observer = new ConditionalObserver();
			} else if (	!(_observer instanceof ConditionalObserver)) {
				ConditionalObserver obs = new ConditionalObserver();
				obs.addObserver(null, (IObserverDefinition) _observer);
				_observer = obs;
			}
			((ConditionalObserver)_observer).addObserver(edef, odef);
		}
	}

	@Override
	public IObserver getObserver() {
		return _observer;
	}

	@Override
	public List<ISemanticObject<?>> getObservables() {
		return _observables;
	}
	
	@Override
	public void setDataSource(IDataSource datasource) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObservableFunction(IFunctionCall function, String formalName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInlineState(Object state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IScale getCoverage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCoveredExtent(IExtent extent) throws ThinklabException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IDataSource getDatasource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasActionsFor(IConcept observable, IConcept domainConcept) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IAccessor getAccessor(IScale context) {
		return _observer == null ? 
					_accessor : 
					_observer.getAccessor(context);
	}

	@Override
	public void setAccessorGeneratorFunction(IFunctionCall function) {
		// TODO Auto-generated method stub
		
	}


}
