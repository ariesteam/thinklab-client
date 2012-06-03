package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IContext;
import org.integratedmodelling.thinklab.api.modelling.IState;
import org.integratedmodelling.thinklab.api.modelling.parsing.IClassifyingObserverDefinition;

public class Classification extends Observer implements IClassifyingObserverDefinition {

	IConcept _conceptSpace;
	
	@Override
	public IConcept getConceptSpace() {
		return _conceptSpace;
	}

	@Override
	public void setConceptSpace(IConcept concept) {
		_conceptSpace = concept;
	}

	@Override
	public IState createState(ISemanticObject<?> observable, IContext context)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

}
