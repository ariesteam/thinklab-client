package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.modelling.IContext;
import org.integratedmodelling.thinklab.api.modelling.IDataSource;
import org.integratedmodelling.thinklab.api.modelling.IObserver;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IObservationDefinition;

/**
 * An observation pairs a datasource with an observer. The datasource may provide
 * additional context for it even if a context hasn't been specified.
 * 
 * @author Ferd
 *
 */
public class Observation extends ModelObject implements IObservationDefinition {

	IList       _observable;
	IDataSource _datasource;
	IContext    _context;
	IObserver   _observer;
	Object      _inlineState;
	
	@Override
	public void setObservable(IList semantics) {
		_observable = semantics;
	}

	@Override
	public IList getObservable() {
		return _observable;
	}

	@Override
	public IDataSource getDataSource() {
		return _datasource;
	}

	@Override
	public IContext getContext() {
		return _context;
	}

	@Override
	public void setObserver(IObserver observer) {
		_observer = observer;
	}

	@Override
	public void setInlineState(Object state) {
		_inlineState = state;
	}

	@Override
	public void setDataSource(IDataSource datasource) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDatasourceGeneratorFunction(IFunctionDefinition function) {
		// TODO Auto-generated method stub
		
	}

}
