package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;

import org.integratedmodelling.thinklab.api.modelling.parsing.IUnitDefinition;

public class UnitDefinition extends LanguageElement implements IUnitDefinition {

	String _expression;
	
	@Override
	public void setExpression(String expression) {
		_expression = expression;
	}

	@Override
	public void dump(PrintStream out) {
		
	}

}
