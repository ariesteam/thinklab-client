package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;

import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.modelling.parsing.IMetadataDefinition;

public class Metadata extends LanguageElement implements IMetadataDefinition {

	HashMap<String, Object> _data = new HashMap<String, Object>();
	
	@Override
	public void put(String id, Object value) {
		if (value != null && id != null)
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

	@Override
	public void merge(IMetadata md) {
		_data.putAll(((Metadata)md)._data);
	}

	@Override
	public String getString(String field) {
		return get(field).toString();
	}

	@Override
	public Integer getInt(String field) {
		return get(field) == null ? null : Integer.parseInt(get(field).toString());
	}

	@Override
	public Long getLong(String field) {
		return get(field) == null ? null : Long.parseLong(get(field).toString());
	}

	@Override
	public Double getDouble(String field) {
		return get(field) == null ? null : Double.parseDouble(get(field).toString());
	}

	@Override
	public Float getFloat(String field) {
		return get(field) == null ? null : Float.parseFloat(get(field).toString());
	}

	@Override
	public Boolean getBoolean(String field) {
		return get(field) == null ? null : Boolean.parseBoolean(get(field).toString());
	}

	@Override
	public IConcept getConcept(String field) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getString(String field, String def) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInt(String field, int def) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getLong(String field, long def) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDouble(String field, double def) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFloat(String field, float def) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getBoolean(String field, boolean def) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IConcept getConcept(String field, IConcept def) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Object> getValues() {
		// TODO Auto-generated method stub
		return null;
	}
}
