package org.integratedmodelling.thinklab.client.modelling;

import java.util.ArrayList;

import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.modelling.IModel;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.modelling.parsing.IPropertyDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.ISubjectGenerator;

public class SubjectGenerator extends ModelObject implements ISubjectGenerator {

	IList observable;
	ArrayList<IFunctionCall> ogens = new ArrayList<IFunctionCall>();
	ArrayList<Pair<IPropertyDefinition, IModel>> odeps = 
			new ArrayList<Pair<IPropertyDefinition,IModel>>();
	
	
	@Override
	public ISubject createSubject() {
		return null;
	}

	@Override
	public void setObservable(IList odef) {
		observable = odef;
	}

	@Override
	public void addObservationGeneratorFunction(IFunctionCall ff) {
		/*
		 * TODO validation of return types
		 */
		ogens.add(ff);
	}

	@Override
	public void addModelDependency(IPropertyDefinition property, IModel observer) {
		/*
		 * TODO vaidation
		 */
		odeps.add(new Pair<IPropertyDefinition, IModel>(property, observer));
	}

}
