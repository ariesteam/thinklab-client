package org.integratedmodelling.thinklab.client.modelling;

import java.util.Set;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IScale;
import org.integratedmodelling.thinklab.api.modelling.IState;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.parsing.ICategorizingObserverDefinition;

public class Categorization extends Observer implements ICategorizingObserverDefinition {

	Set<String> _dictionary;
	
	@Override
	public void setDictionary(Set<String> dictionary) {
		_dictionary = dictionary;
	}

	@Override
	public Set<String> getDictionary() {
		return _dictionary;
	}

	@Override
	public IState createState(ISemanticObject<?> observable, ISubject context)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

}
