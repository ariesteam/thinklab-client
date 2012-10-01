package org.integratedmodelling.thinklab.client.modelling;

import java.util.Set;

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

}
