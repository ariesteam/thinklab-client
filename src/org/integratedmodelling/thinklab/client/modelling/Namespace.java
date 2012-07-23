package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.thinklab.api.knowledge.IAxiom;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.modelling.IContext;
import org.integratedmodelling.thinklab.api.modelling.IExtent;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.parsing.IModelObjectDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.INamespaceDefinition;
import org.integratedmodelling.thinklab.api.project.IProject;

/**
 * Beans to incarnate the model expressed in any of the Thinklab modeling languages. All languages should just
 * worry about generating these, and leave their operationalization to implementations.
 * 
 * @author Ferd
 *
 */
public class Namespace extends LanguageElement implements INamespaceDefinition {

	public ArrayList<IAxiom> axioms = new ArrayList<IAxiom>();
	public HashSet<IAxiom> axiomCatalog = new HashSet<IAxiom>();
	ArrayList<INamespace> importedNamespaces = new ArrayList<INamespace>();
	ArrayList<IModelObject> modelObjects = new ArrayList<IModelObject>();
	ArrayList<IModelObject> _knowledge = new ArrayList<IModelObject>();
	public HashSet<String> _names = new HashSet<String>();
	
	ArrayList<Pair<String, Integer>> _errors = new ArrayList<Pair<String,Integer>>();
	ArrayList<Pair<String, Integer>> _warnings = new ArrayList<Pair<String,Integer>>();
	
	String _id;
	long timeStamp;
	IProject project;
	private String _resourceUrl;
	String _trainingKbox = null;
	String _storageKbox = null;
	String _lookupKbox = null;
	String _expressionLanguage = null;
	
	public Namespace() {}
	public Namespace(String id) { setId(id); }
	
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public List<IModelObject> getModelObjects() {
		return modelObjects;
	}
	
	public Collection<IAxiom> getAxioms() {
		return axioms;
	}
	
	public Collection<IModelObject> getKnowledge() {
		return _knowledge;
	}
	
	/**
	 * Add an axiom. Tolerant to duplicated axioms.
	 * @param axiom
	 */
	public void addAxiom(IAxiom axiom) {
		if (!axiomCatalog.contains(axiom)) {
			axiomCatalog.add(axiom);
			axioms.add(axiom);
		}
	}
	
	/**
	 * Model objects get in the model object array unless they're concepts or properties defined
	 * by inference. In that case they end up in the knowledge array  but only when the namespace
	 * comes from a model resource.
	 * 
	 * @param mo
	 */
	@Override
	public void addModelObject(IModelObjectDefinition o) {
		
		IModelObject mo = (IModelObject) o;
		
		if (mo.getId() != null) {
			_names.add(mo.getId());
		}
		
		/*
		 * FIXME this should disappear
		 */
		if ( (_resourceUrl != null && 
				(_resourceUrl.toString().endsWith(".tql") || _resourceUrl.toString().endsWith(".clj") )) &&				
				(mo instanceof ConceptObject || mo instanceof PropertyObject) && 
				mo.getFirstLineNumber() == 0 && mo.getLastLineNumber() == 0) {
			_knowledge.add(mo);
		} else {
			modelObjects.add(mo);			
		}
	}
	
	@Override
	public void dump(PrintStream out) {
		
		// TODO dump all axioms
		for (IAxiom a : axioms) {
			out.println(a);
		}
		
		// TODO dump all model objects
		for (IModelObject m : modelObjects) {
			((ModelObject)m).dump(out);
		}
	}
	
	/**
	 * Linear search should be OK, but we can certainly add a hashtable at some point.
	 * 
	 * @param object
	 * @return
	 */
	public IModelObject getModelObject(String object) {
		
		if (!_names.contains(object))
			return null;
		
		for (IModelObject mo : modelObjects) {
			if (mo.getId().equals(object))
				return mo;
		}
		return null;
	}
	
	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	/**
	 * Synchronize axioms with ConceptObjects and PropertyObjects. Only
	 * parses axioms -> object and not the other way around as we assume
	 * that parsers will generate axioms from model objects.
	 * 
	 * TODO very incomplete!
	 * 
	 */
	public void synchronizeKnowledge() {

		System.out.println("syncing namespace " + getId() + ": " + axioms.size() + " axioms");
		
		for (IAxiom axiom : axioms) {
			
			if (axiom.is(IAxiom.CLASS_ASSERTION)) {
				if (!_names.contains(axiom.getArgument(0))) {
					ConceptObject co = new ConceptObject();
					co.setId(axiom.getArgument(0).toString());
					co.setNamespace(this);
					addModelObject(co);
				}
			}
		}
	}
	
	@Override
	public void setResourceUrl(String resourceUrl) {
		_resourceUrl = resourceUrl;
	}
	
	@Override
	public void addImportedNamespace(INamespace namespace) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public IConcept getConcept(String s) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public IProperty getProperty(String s) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setId(String id) {
		_id = id;
	}

	@Override
	public String getId() {
		return _id;
	}
	
	@Override
	public String getResourceUrl() {
		// TODO Auto-generated method stub
		return _resourceUrl;
	}
	
	@Override
	public void setStorageKbox(String kboxUri) {
		_storageKbox = kboxUri;
	}

	@Override
	public void setTrainingKbox(String kboxUri) {
		_trainingKbox = kboxUri;
	}
	
	@Override
	public String getStorageKbox() {
		return _storageKbox;
	}
	
	@Override
	public String getTrainingKbox() {
		return _trainingKbox;
	}
	
	@Override
	public void setLookupKbox(String kboxUri) {
		_lookupKbox = kboxUri;
	}
	
	@Override
	public String getLookupKbox() {
		return _lookupKbox;
	}
	@Override
	public void setExpressionLanguage(String language) {
		_expressionLanguage = language;
	}

	@Override
	public String getExpressionLanguage() {
		return _expressionLanguage;
	}
	
	public void addError(String error, int lineNumber) {
		_errors.add(new Pair<String, Integer>(error, lineNumber));
	}
	
	public boolean hasErrors() {
		return _errors.size() > 0;
	}
	
	public void addWarning(String warning, int lineNumber) {	
		_warnings.add(new Pair<String, Integer>(warning, lineNumber));
	}
	
	@Override
	public IContext getCoverage() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addCoveredExtent(IExtent extent) {
		// TODO Auto-generated method stub
		
	}
}
