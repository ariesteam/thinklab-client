package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IClassification;
import org.integratedmodelling.thinklab.api.modelling.IState;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.parsing.IClassifyingObserverDefinition;

public class Classification extends Observer implements IClassifyingObserverDefinition {

	IClassification _classification;

	@Override
	public IState createState(ISemanticObject<?> observable, ISubject context)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public IClassification getClassification() {
		return _classification;
	}


	@Override
	public void setClassification(IClassification classification) {
		_classification = classification;
	}

}
