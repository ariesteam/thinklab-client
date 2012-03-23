package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;

import org.integratedmodelling.thinklab.api.modelling.parsing.IMetadataDefinition;

public class Metadata extends LanguageElement implements IMetadataDefinition {

	HashMap<String, Object> _data = new HashMap<String, Object>();
	
	@Override
	public void put(String id, Object value) {
		_data.put(id,value);
	}

	@Override
	public Collection<String> getKeys() {
		return _data.keySet();
	}

	@Override
	public Object get(String string) {
		return _data.get(string);
	}

	@Override
	public void dump(PrintStream out) {
		// TODO Auto-generated method stub
		
	}
	
}
