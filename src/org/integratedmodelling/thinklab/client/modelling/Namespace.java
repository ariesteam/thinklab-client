package org.integratedmodelling.thinklab.client.modelling;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.collections.Triple;
import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabRuntimeException;
import org.integratedmodelling.thinklab.api.knowledge.IAxiom;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IOntology;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.modelling.IExtent;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.IScale;
import org.integratedmodelling.thinklab.api.modelling.parsing.IConceptDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IModelObjectDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.INamespaceDefinition;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.client.project.ProjectManager;
import org.integratedmodelling.thinklab.common.owl.KnowledgeManager;

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
	ArrayList<String> _trainingNamespaces = new ArrayList<String>();
	ArrayList<String> _lookupNamespaces = new ArrayList<String>();
	String _expressionLanguage = null;
	private IConceptDefinition _agentType;
	IOntology ontology = null;
	int lastAxiom = 0;
	
	Map<String, Object> _symbolTable = new HashMap<String, Object>();
	
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
	
	@Override
	public Map<String, Object> getSymbolTable() {
		return _symbolTable;
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
		modelObjects.add(mo);			
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
	 * Create the namespace's ontology from any accumulated axioms. If no axioms were
	 * in the file, the ontology will be null to prevent OWLAPI overhead.
	 */
	public void synchronizeKnowledge() {

		if (ontology == null && axioms.size() > 0) {
			
			KnowledgeManager km = null;
			if (ProjectManager.get().getCurrentServer() != null) {
				km = (KnowledgeManager) ProjectManager.get().getCurrentServer().getKnowledgeManager();
				ontology = km.requireOntology(_id, project.getOntologyNamespacePrefix());
			}
			if (ontology != null) {
				try {
					Collection<IAxiom> ax = new ArrayList<IAxiom>();
					for (int i = lastAxiom; i < axioms.size(); i++)
						ax.add(axioms.get(i));
					ontology.define(ax);
					lastAxiom = axioms.size();
				} catch (ThinklabException e) {
					throw new ThinklabRuntimeException(e);
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
	}
	
	@Override
	public IConcept getConcept(String s) {
		return ontology == null ? null : ontology.getConcept(s);
	}
	
	@Override
	public IProperty getProperty(String s) {
		return ontology == null ? null : ontology.getProperty(s);
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
		return _resourceUrl;
	}
	
	@Override
	public void setExpressionLanguage(String language) {
		_expressionLanguage = language;
	}

	@Override
	public String getExpressionLanguage() {
		return _expressionLanguage;
	}
		
	@Override
	public boolean hasErrors() {
		return _errors.size() > 0;
	}
	
	@Override
	public IScale getCoverage() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addCoveredExtent(IExtent extent) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setAgentConcept(IConceptDefinition agentConcept) {
		this._agentType = agentConcept;
	}
	
	@Override
	public void addWarning(String warning, int lineNumber) {	
		_warnings.add(new Pair<String, Integer>(warning, lineNumber));
	}
	
	@Override
	public void addError(int errorCode, String errorMessage, int lineNumber) {
		this._errors.add(new Pair<String, Integer>(errorMessage, lineNumber));
	}
	
	@Override
	public Collection<Triple<Integer, String, Integer>> getErrors() {

		List<Triple<Integer, String, Integer>> ret = new ArrayList<Triple<Integer,String,Integer>>();
		for (Pair<String, Integer> e : _errors) {
			ret.add(new Triple<Integer, String, Integer>(0, e.getFirst(), e.getSecond()));
		}
		return ret;
	}

	@Override
	public Collection<Pair<String, Integer>> getWarnings() {
		return _warnings;
	}

	@Override
	public IOntology getOntology() {
		return ontology;
	}

	@Override
	public boolean hasWarnings() {
		return _warnings.size() > 0;
	}
	
	@Override
	public List<String> getTrainingNamespaces() {
		return _trainingNamespaces;
	}
	
	@Override
	public List<String> getLookupNamespaces() {
		return _lookupNamespaces;
	}
	
	@Override
	public void addLookupNamespace(String tns) {
		_lookupNamespaces.add(tns);
	}
	
	@Override
	public void addTrainingNamespace(String tns) {
		_trainingNamespaces.add(tns);
	}
	
	@Override
	public void setOntology(IOntology iOntology) {
		ontology = iOntology;
	}
}
