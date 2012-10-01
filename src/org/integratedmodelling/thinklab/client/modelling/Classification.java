package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.thinklab.api.modelling.IClassification;
import org.integratedmodelling.thinklab.api.modelling.parsing.IClassifyingObserverDefinition;

public class Classification extends Observer implements IClassifyingObserverDefinition {

	IClassification _classification;

	@Override
	public IClassification getClassification() {
		return _classification;
	}


	@Override
	public void setClassification(IClassification classification) {
		_classification = classification;
	}

}
