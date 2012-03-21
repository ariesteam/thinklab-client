package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;

import org.integratedmodelling.thinklab.api.lang.parsing.IMetadataDefinition;

public class Metadata extends LanguageElement implements IMetadataDefinition {

	HashMap<String, Object> data = new HashMap<String, Object>();
	
	public void put(String id, Object value) {
		data.put(id,value);
	}

	public Collection<String> getKeys() {
		return data.keySet();
	}
	
	public Object getValue(String key) {
		return data.get(key);
	}

	@Override
	public Object get(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dump(PrintStream out) {
		// TODO Auto-generated method stub
		
	}
	
}
