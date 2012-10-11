package org.integratedmodelling.thinklab.client.modelling;

import java.util.Map;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.lang.IExpressionLanguageAdapter;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.project.IProject;

public class DummyLanguageAdapter implements IExpressionLanguageAdapter {

	class DummyExpression implements IExpression {

		String _code;
		
		DummyExpression(String code) {
			_code = code;
		}
		
		@Override
		public Object eval(Map<String, Object> parameters)
				throws ThinklabException {
			return null;
		}

		@Override
		public void setProjectContext(IProject project) {
		}
		
	}
	
	@Override
	public IExpression getTrueExpression() {
		return new DummyExpression("true");
	}

	@Override
	public IExpression getExpression(String expression, IConcept... domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IExpression compileFunctionCall(IFunctionCall processFunctionCall) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IExpression compileObject(Object processLiteral) {
		// TODO Auto-generated method stub
		return null;
	}

}
