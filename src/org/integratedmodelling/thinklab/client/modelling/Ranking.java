package org.integratedmodelling.thinklab.client.modelling;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.lang.RankingScale;
import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
import org.integratedmodelling.thinklab.api.modelling.IClassification;
import org.integratedmodelling.thinklab.api.modelling.IContext;
import org.integratedmodelling.thinklab.api.modelling.IState;
import org.integratedmodelling.thinklab.api.modelling.parsing.IRankingObserverDefinition;

public class Ranking extends Observer implements IRankingObserverDefinition {
	
	int _type = RANKING;
	RankingScale _scale = new RankingScale();

	@Override
	public int getType() {
		return _type;
	}


	@Override
	public void setType(int type) {
		_type = type;
	}

	@Override
	public void setScale(Number from, Number to) {
		_scale = new RankingScale(from, to);
	}

	@Override
	public IState createState(ISemanticObject<?> observable, IContext context)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RankingScale getScale() {
		return _scale;
	}


	@Override
	public void setDiscretization(IClassification classification) {
		// TODO Auto-generated method stub
		
	}

}
