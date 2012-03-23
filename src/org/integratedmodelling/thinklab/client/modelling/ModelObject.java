package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;

import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.parsing.IMetadataDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IModelObjectDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.INamespaceDefinition;

public class ModelObject extends LanguageElement implements IModelObject, IModelObjectDefinition {
	
	String     _id;
	INamespace _namespace;
	IMetadata  _metadata;
	
	@Override
	public void dump(PrintStream out) {
	}
	
	@Override
	public String getName() {
		return getNamespace().getId() + "/" + _id;
	}
	
	@Override
	public IMetadata getMetadata() {
		return _metadata;
	}
	
	@Override
	public void setNamespace(INamespaceDefinition namespace) {
		_namespace = (INamespace) namespace;
	}
	

	
	@Override
	public void setMetadata(IMetadataDefinition metadata) {
		_metadata = (IMetadata) metadata;
	}
	
	@Override
	public INamespace getNamespace() {
		return _namespace;
	}

	@Override
	public String getId() {
		return _id;
	}


	@Override
	public void setId(String id) {
		_id = id;
	}

}
