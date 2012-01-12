package org.integratedmodelling.thinklab.client.knowledge;

import org.integratedmodelling.thinklab.api.knowledge.IAxiom;

/**
 * Just a holder for axiom information. 
 * 
 * @author Ferd
 *
 */
public class Axiom implements IAxiom {

	String   type;
	Object[] arguments;
	
	public Axiom(String type, Object ... args) {
		this.type = type;
		this.arguments = args;
	}

	public String getType() {
		return type;
	}

	public Object[] getArguments() {
		return arguments;
	}
	
	
}
