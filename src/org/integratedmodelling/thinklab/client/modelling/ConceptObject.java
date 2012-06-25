package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.thinklab.api.modelling.parsing.IConceptDefinition;

public class ConceptObject extends ModelObject implements IConceptDefinition {

	@Override
	public String getName() {
		
		/*
		 * namespace == null only happens in error, but let it through so
		 * we don't get a null pointer exception, and we report the error 
		 * anyway.
		 */
		String ns = "UNDEFINED";
		if (getNamespace() != null)
			ns = getNamespace().getId();
		return ns + ":" + _id;
	}
}
