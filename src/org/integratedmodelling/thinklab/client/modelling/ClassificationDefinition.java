package org.integratedmodelling.thinklab.client.modelling;

import java.util.List;

import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.modelling.IClassifier;
import org.integratedmodelling.thinklab.api.modelling.IObserver;
import org.integratedmodelling.thinklab.api.modelling.parsing.IClassificationDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IConceptDefinition;

public class ClassificationDefinition implements IClassificationDefinition {

	@Override
	public void setLineNumbers(int startLine, int endLine) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFirstLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLastLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IConcept getConceptSpace() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IConcept classify(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IClassifier> getClassifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IConcept> getConceptOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRank(IConcept concept) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getNumericRange(IConcept concept) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getDistributionBreakpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConceptSpace(IConceptDefinition concept, Type typeHint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addClassifier(IConceptDefinition concept, IClassifier classifier) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean hasZeroRank() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCategorical() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void notifyObserver(IObserver observer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTypeHint(Type type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isContiguousAndFinite() {
		// TODO Auto-generated method stub
		return false;
	}

}
