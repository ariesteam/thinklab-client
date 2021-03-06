//package org.integratedmodelling.thinklab.client.knowledge;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//
//import org.integratedmodelling.exceptions.ThinklabException;
//import org.integratedmodelling.lang.SemanticType;
//import org.integratedmodelling.thinklab.api.factories.IKnowledgeManager;
//import org.integratedmodelling.thinklab.api.knowledge.IConcept;
//import org.integratedmodelling.thinklab.api.knowledge.IOntology;
//import org.integratedmodelling.thinklab.api.knowledge.IProperty;
//import org.integratedmodelling.thinklab.api.knowledge.ISemanticObject;
//import org.integratedmodelling.thinklab.api.knowledge.kbox.IKbox;
//import org.integratedmodelling.thinklab.api.lang.IList;
//import org.integratedmodelling.thinklab.common.owl.OWL;
//
///**
// * Knowledge manager for client library, which will not allow any operation but will create
// * blindly any concept and ontology that it's asked to produce. Used to parse models where all concepts
// * defined are expected to be created. Don't use improperly.
// * 
// * TODO implement all the concept, property, ontology and reasoning functions using OWLAPI 2.0 and 
// * use as delegate for the KM in thinklab. We need reasoning in the client if we want any meaningful
// * way to organize concepts.
// * 
// * @author Ferd
// *
// */
//public class KnowledgeManager implements IKnowledgeManager {
//
//	private static KnowledgeManager _this;
//	
//	HashMap<String, IOntology> _ontologies = 
//			new HashMap<String, IOntology>();
//
//	public static IKnowledgeManager get() {
//		if (_this == null) {
//			_this = new KnowledgeManager();
//		}
//		return _this;
//	}
//	
//	@Override
//	public IProperty getProperty(String prop) {
//		return OWL.get().getProperty(prop);
//	}
//
//	public IOntology requireOntology(String s) {
//		IOntology ret = _ontologies.get(s);
//		if (ret == null) {
//			ret = new Ontology(s);
//			_ontologies.put(s, ret);
//		}
//		return ret;
//	}
//	
//	@Override
//	public IConcept getConcept(String prop) {
//		SemanticType st = new SemanticType(prop);
//		IOntology ont = requireOntology(st.getConceptSpace());
//		return ont.getConcept(st.getLocalName());
//	}
//
//	@Override
//	public IConcept getLeastGeneralCommonConcept(IConcept... cc) {
//		return OWL.get().getLeastGeneralCommonConcept(Arrays.asList(cc));
//	}
//	
//	@Override
//	public void dropKbox(String uri) throws ThinklabException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public ISemanticObject<?> parse(String literal, IConcept c)
//			throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public ISemanticObject<?> annotate(Object object) throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public IKbox createKbox(String uri) throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public IKbox requireKbox(String uri) throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Object instantiate(IList a) throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void registerAnnotatedClass(Class<?> cls, IConcept concept) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public ISemanticObject<?> entify(IList semantics) throws ThinklabException {
//		return new SemanticObject(semantics);
//	}
//
//	@Override
//	public IConcept getXSDMapping(String string) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
