package org.integratedmodelling.thinklab.client.modelling;

import java.util.List;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabUnsupportedOperationException;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IContext;
import org.integratedmodelling.thinklab.api.modelling.IDataSource;
import org.integratedmodelling.thinklab.api.modelling.IExtent;
import org.integratedmodelling.thinklab.api.modelling.IObservation;
import org.integratedmodelling.thinklab.api.modelling.IObserver;
import org.integratedmodelling.thinklab.api.modelling.parsing.IExpressionDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.modelling.parsing.IModelDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IObserverDefinition;

public class Model extends ObservingObject implements IModelDefinition {

	IObserver _observer;
	
	@Override
	public void addObserver(IObserverDefinition odef, IExpressionDefinition edef) {
		
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
	public IObservation observe(IContext context)
			throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("models cannot be contextualized at the client side");
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
	public IContext getCoverage() {
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


}
