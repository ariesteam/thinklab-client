package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IClassification;
import org.integratedmodelling.thinklab.api.modelling.IState;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.IUnit;
import org.integratedmodelling.thinklab.api.modelling.parsing.IMeasuringObserverDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IUnitDefinition;

public class Measurement extends Observer implements IMeasuringObserverDefinition {

	IUnit _unit;

	public IUnit getUnit() {
		return _unit;
	}

	@Override
	public void setUnit(IUnitDefinition unit) {

		/*
		 * TODO produce unit
		 */
		
	}

	@Override
	public IState createState(ISemanticObject<?> observable, ISubject context)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDiscretization(IClassification classification) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IClassification getDiscretization() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
