package org.integratedmodelling.thinklab.client.modelling;

import java.util.ArrayList;
import java.util.List;

import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IObserver;
import org.integratedmodelling.thinklab.api.modelling.IState;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.parsing.IConditionalObserverDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IExpressionDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IObserverDefinition;

public class ConditionalObserver extends Observer implements IConditionalObserverDefinition {

	ArrayList<Pair<IObserver,IExpression>> _observers;
	
	@Override
	public void addObserver(IExpressionDefinition expression, IObserverDefinition observer) {
		_observers.add(new Pair<IObserver, IExpression>((IObserver)observer, (IExpression)expression));
	}
	
	@Override
	public List<Pair<IObserver,IExpression>> getObservers() {
		return _observers;
	}

	@Override
	public IState createState(ISemanticObject<?> observable, ISubject context)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}
}
