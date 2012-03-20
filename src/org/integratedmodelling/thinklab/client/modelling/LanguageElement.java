package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;

import org.integratedmodelling.thinklab.api.lang.parsing.ILanguageDefinition;

public abstract class LanguageElement implements ILanguageDefinition {
	
	public abstract void dump(PrintStream out);
}
