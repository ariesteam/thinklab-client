package org.integratedmodelling.thinklab.client.modelling;

import java.util.ArrayList;

import org.integratedmodelling.collections.Triple;
import org.integratedmodelling.thinklab.api.lang.IList;
import org.integratedmodelling.thinklab.api.modelling.IModel;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.modelling.parsing.IPropertyDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.ISubjectGenerator;

public class SubjectGenerator extends ModelObject implements ISubjectGenerator {

	IList observable;
	ArrayList<IFunctionCall> ogens = new ArrayList<IFunctionCall>();
	ArrayList<Triple<IPropertyDefinition, IModel, Boolean>> odeps = 
			new ArrayList<Triple<IPropertyDefinition,IModel, Boolean>>();
	
	
	@Override
	public ISubject observe(INamespace namespace) {
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
	public void addModelDependency(IPropertyDefinition property, IModel observer, boolean propagate) {
		odeps.add(new Triple<IPropertyDefinition, IModel, Boolean>(property, observer, propagate));
	}


//	public void initialize() throws ThinklabException {
//				/*
//		 * check that 'propagate' status only applies to object properties
//		 */
//		for (Triple<IPropertyDefinition, IModel, Boolean> mm : odeps) {
//			IProperty p = Thinklab.p(mm.getFirst().getName());
//			if (mm.getThird() && !p.isObjectProperty())
//				throw new ThinklabValidationException(p + ": multiple agents can only be the target of object properties");
//			
//			DepModel dp = new DepModel();
//			dp.property = p;
//			dp.model = mm.getSecond();
//			dp.propagate = mm.getThird();
//		
//			mdeps.add(dp);
//		}
//	}
	
}
