package org.integratedmodelling.thinklab.client.knowledge;

import org.integratedmodelling.thinklab.api.knowledge.IAxiom;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;

/**
 * Just a holder for axiom information. 
 * 
 * @author Ferd
 *
 */
public class Axiom implements IAxiom {

	private String _type;
	private Object[] _args;

	//
	// Public static creation members
	//
	
	static public Axiom ClassAssertion(IConcept c) {
		return new Axiom(CLASS_ASSERTION, c);
	}
	
	static public Axiom SubClass(IConcept concept, IConcept ancestor) {
		return new Axiom(SUBCLASS_OF, concept, ancestor);
	}
	
	public Axiom(String type, Object ... args) {
		_type = type;
		_args = args;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof Axiom) {
			return toString().equals(arg0.toString());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		String ret = "<" + _type;
		for (Object o : _args) {
			ret += "," + o.toString();
		}
		return ret + ">";
	}
	
	
	
}
