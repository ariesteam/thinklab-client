package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;

import org.integratedmodelling.thinklab.api.modelling.parsing.ILanguageDefinition;

public abstract class LanguageElement implements ILanguageDefinition {
	
	int        _lastLineNumber = 0;
	int        _firstLineNumber = 0;
	
	public abstract void dump(PrintStream out);
	
	@Override
	public void setLineNumbers(int startLine, int endLine) {
		_firstLineNumber = startLine;
		_lastLineNumber  = endLine;
	}
	
	@Override
	public int getFirstLineNumber() {
		return _firstLineNumber;
	}
	
	@Override
	public int getLastLineNumber() {
		return _lastLineNumber;
	}
}
