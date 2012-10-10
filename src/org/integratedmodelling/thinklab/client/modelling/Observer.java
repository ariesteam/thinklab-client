package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabUnsupportedOperationException;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.modelling.IAccessor;
import org.integratedmodelling.thinklab.api.modelling.IObserver;
import org.integratedmodelling.thinklab.api.modelling.IScale;
import org.integratedmodelling.thinklab.api.modelling.IScenario;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.modelling.parsing.IObserverDefinition;

public abstract class Observer extends ObservingObject implements IObserverDefinition {

	IObserver _mediated = null;
	IAccessor _accessor = null;

	/**
	 * Add one observer with an optional conditional expression to contextualize the model to use. Creation
	 * of conditional observers if more than one observer is added or there are conditions is
	 * handled transparently.
	 * 
	 * @param observer
	 * @param expression
	 */
	@Override
	public void addMediatedObserver(IObserverDefinition odef, IExpression edef) {
		
		IObserver observer = (IObserver)odef;
		IExpression expression = (IExpression)edef;
		
		if (_mediated == null && expression == null) {
			_mediated = observer;
		} else {
			if (_mediated == null) {
				_mediated = new ConditionalObserver();
			} else if (	!(_mediated instanceof ConditionalObserver)) {
				ConditionalObserver obs = new ConditionalObserver();
				obs.addObserver(null, (IObserverDefinition) _mediated);
				_mediated = obs;
			}
			((ConditionalObserver)_mediated).addObserver(edef, odef);
		}
	}
	
	public Object getObservedObject() {
		return _mediated;
	}


	@Override
	public void setAccessorGeneratorFunction(IFunctionCall function) {
	}

	@Override
	public IAccessor getAccessor(IScale context) {
		return _accessor;
	}

	@Override
	public IObserver train(ISubject context) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("models cannot be trained at the client side");
	}

	@Override
	public IObserver applyScenario(IScenario scenario) throws ThinklabException {
		throw new ThinklabUnsupportedOperationException("scenarios cannot be applied at the client side");
	}

	@Override
	public IObserver getMediatedObserver() {
		return _mediated;
	}
	
	@Override
	public IList getFinalObservable() {

		return _mediated == null ? 
					_observables.get(0).getSemantics() : 
					((Observer)_mediated).getFinalObservable();
	}

	@Override
	public boolean isTrivialMediation(IObserver observer2) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
