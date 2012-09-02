package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IState;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.parsing.IValuingObserverDefinition;

public class Value extends Observer implements IValuingObserverDefinition {

	@Override
	public IState createState(ISemanticObject<?> observable, ISubject context) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
