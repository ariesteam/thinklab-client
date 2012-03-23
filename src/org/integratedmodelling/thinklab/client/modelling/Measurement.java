package org.integratedmodelling.thinklab.client.modelling;

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
	
}
