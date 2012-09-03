package org.integratedmodelling.thinklab.client.modelling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabInternalErrorException;
import org.integratedmodelling.exceptions.ThinklabValidationException;
import org.integratedmodelling.lang.SemanticType;
import org.integratedmodelling.thinklab.api.factories.IModelManager;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IModelParser;
import org.integratedmodelling.thinklab.api.lang.IResolver;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.modelling.ICategorizingObserver;
import org.integratedmodelling.thinklab.api.modelling.IClassifyingObserver;
import org.integratedmodelling.thinklab.api.modelling.IMeasuringObserver;
import org.integratedmodelling.thinklab.api.modelling.IModel;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.IRankingObserver;
import org.integratedmodelling.thinklab.api.modelling.IScale;
import org.integratedmodelling.thinklab.api.modelling.IScenario;
import org.integratedmodelling.thinklab.api.modelling.IStoryline;
import org.integratedmodelling.thinklab.api.modelling.ISubject;
import org.integratedmodelling.thinklab.api.modelling.IUnit;
import org.integratedmodelling.thinklab.api.modelling.IValuingObserver;
import org.integratedmodelling.thinklab.api.modelling.parsing.IClassificationDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IConceptDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionCall;
import org.integratedmodelling.thinklab.api.modelling.parsing.ILanguageDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.INamespaceDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IPropertyDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.ISubjectGenerator;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.IServer;
import org.integratedmodelling.thinklab.client.project.Project;
import org.integratedmodelling.thinklab.client.project.ProjectManager;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;
import org.integratedmodelling.thinklab.common.owl.KnowledgeManager;

/**
 * A model manager that can parse the Thinklab language and build a model map, without actually being 
 * capable of running the model objects.
 * 
 * @author Ferd
 *
 */
public class ModelManager implements IModelManager {

	private static ModelManager _this = null;
	
	HashMap<String, IModelParser> interpreters = new HashMap<String, IModelParser>();
	Map<String, INamespace> namespacesById = 
			Collections.synchronizedMap(new HashMap<String, INamespace>());
	
	public static ModelManager get() {
		if (_this == null) {
			_this = new ModelManager();
		}
		return _this;
	}
	
	public class Resolver implements IResolver {
		
		IProject project;
		Namespace namespace;
		String resourceId = "";
		long _timestamp = new Date().getTime();
		URL resourceUrl;
		
		/*
		 * TODO load knowledge from server the first time a resolver is created for it.
		 */
		public IServer server;
		
		HashSet<String> _defined = new HashSet<String>();

		private HashMap<String, Object> symbolTable =
				new HashMap<String, Object>();

		public Resolver(IServer server, IProject project) {
			this.server = server;
			this.project = project;
		}
		
		public void initialize(IServer server, IProject project) {
			this.server = server;
			this.project = project;
		}

		@Override
		public boolean onException(Throwable e, int lineNumber) {

			System.out.println("EXCEPTION " + e.getMessage() + " at " + lineNumber);
			
			if (namespace != null) {
				namespace.addError(0, e.getMessage(), lineNumber);
			}
			return true;
		}

		@Override
		public boolean onWarning(String warning, int lineNumber) {
			
//			System.out.println("WARNING " + warning + " at " + lineNumber);
//
			if (namespace != null) {
				namespace.addWarning(warning, lineNumber);
			}
			return true;
		}

		@Override
		public boolean onInfo(String info, int lineNumber) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void onNamespaceDeclared() {
		}

		@Override
		public void onNamespaceDefined() {
		}

		@Override
		public IConceptDefinition resolveExternalConcept(String id, int line) {
			
			if (server != null) {
				IConcept c = server.getKnowledgeManager().getConcept(id);
				if (c == null) {
					onException(
							new ThinklabValidationException("concept " + id + " is not known to the current server"), 
							line);
				} 
			} else {
				onWarning("no server is connected: cannot establish semantics for " + id, line);				
			}
			
			ConceptObject ret = new ConceptObject();
			ret.setId(id);
			
			return ret;
		}
		
		@Override
		public IPropertyDefinition resolveExternalProperty(String id, int line) {
			
			if (server != null) {
				IProperty c = server.getKnowledgeManager().getProperty(id);
				if (c == null) {
					onException(
							new ThinklabValidationException("property " + id + " is not known to the current server"),
							line);
				}
			}  else {
				onWarning("no server is connected: cannot establish semantics for " + id, line);				
			}
			
			/*
			 * TODO - check this. We basically create a new namespace per unseen property, not 
			 * a great strategy really. Only to prevent null pointer exceptions so far.
			 * 
			 * These unresolved namespaces should be accepted in the client but kept 
			 * tidy in the projects as "knowledge dependencies" and checked with each client that is 
			 * connected before accepting the connection.
			 */
			
			PropertyObject ret = new PropertyObject();
			SemanticType st = new SemanticType(id);
			ret.setId(st.getLocalName());
			ret.setNamespace(new Namespace(st.getLocalName()));
			
			/*
			 * TODO discuss import with knowledge manager. Should have been seen before.
			 */
			
			return ret;
		}

		@Override
		public void onModelObjectDefined(IModelObject ret) {
			if (!isGeneratedId(ret.getId()))
				namespace.getSymbolTable().put(ret.getId(), ret);
		}

		@Override
		public ILanguageDefinition newLanguageObject(Class<?> cls) {
			
			if (cls.equals(INamespace.class)) {
				return new Namespace();
			} else if (cls.equals(ICategorizingObserver.class)) {
				return new Categorization();
			} else if (cls.equals(IClassifyingObserver.class)) {
				return new Classification();
			} else if (cls.equals(IMeasuringObserver.class)) {
				return new Measurement();
			} else if (cls.equals(IRankingObserver.class)) {
				return new Ranking();
			} else if (cls.equals(IValuingObserver.class)) {
				return new Value();
			} else if (cls.equals(IModel.class)) {
				return new Model();
			} else if (cls.equals(IStoryline.class)) {
				return new Storyline();
			} else if (cls.equals(IScenario.class)) {
				return new Scenario();
			} else if (cls.equals(IConcept.class)) {
				return new ConceptObject();
			} else if (cls.equals(IProperty.class)) {
				return new PropertyObject();
			} else if (cls.equals(IUnit.class)) {
				return new UnitDefinition();
			} else if (cls.equals(IMetadata.class)) {
				return new Metadata();
			} else if (cls.equals(IFunctionCall.class)) {
				return new FunctionCall();
			}  else if (cls.equals(IClassificationDefinition.class)) {
				return new ClassificationDefinition();
			} else if (cls.equals(ISubjectGenerator.class)) {
				return new SubjectGenerator();
			}
			
			return null;
		}
		

		@Override
		public boolean isGeneratedId(String id) {
			return id == null || id.endsWith("___");
		}

		@Override
		public String generateId(IModelObject o) {
			return UUID.randomUUID().toString() + "___";
		}

		@Override
		public IModelObject getLastProcessedObject() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isInteractive() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void handleObserveStatement(Object observable, ISubjectGenerator ctx,  boolean distribute, IPropertyDefinition property, int lineNumber)  {
			// TODO Auto-generated method stub
			
		}

		@Override
		public IResolver getImportResolver(IProject project) {
			Resolver ret = new Resolver(server, project);
			ret._defined = _defined;
			return ret;
		}

		@Override
		public synchronized INamespace getNamespace(String id, int lineNumber) {

			INamespace ns = namespacesById.get(id);
			
			if (ns == null && this.project != null && this.project.providesNamespace(id)) {
				
				/*
				 * preload it
				 */
				try {
					ns = loadFile(this.project.findResourceForNamespace(id).toString(), id, this.project, this);
				} catch (ThinklabException e) {
					onException(e, lineNumber);
				}
			}
			
			return ns;
		}

		@Override
		public IResolver getNamespaceResolver(String namespace, String resource) {
			
			if (namespacesById.get(namespace) != null) {
				System.out.println("warning: namespace " + namespace + " is being redefined");
				releaseNamespace(namespace);
			}

			/*
			 * create namespace
			 */
			Namespace ns = new Namespace();
			ns.setId(namespace);
			ns.setResourceUrl(resource);
			ns.setProject(project);
			synchronized(namespacesById) {
				namespacesById.put(namespace, ns);
			}
			
			/*
			 * report it to the project - it may need this if this is an import.
			 */
			((Project)project).notifyNamespace(ns);

			long timestamp = new Date().getTime();
			URL url = null;
			
			/*
			 * resolve the resource ID to an openable URL
			 */
			if (resource != null) {
				
				File f = new File(resource);

				try {
					if (f.exists() && f.isFile() && f.canRead()) {
						timestamp = f.lastModified();
						url = f.toURI().toURL();					
					} else if (resource.contains(":/")) {
						url = new URL(resource);
						if (url.toString().startsWith("file:")) {
							f = new File(url.getFile());
							timestamp = f.lastModified();
						}
					}
				} catch (Exception e) {
					onException(e, -1);
				}
			}
			
			ns.setTimeStamp(timestamp);
			
			/*
			 * create new resolver with same project and new namespace
			 */
			Resolver ret = new Resolver(server, project);
			ret.namespace = ns;
			ret.resourceId = resource;
			ret.resourceUrl = url;
						
			return ret;

		}

		@Override
		public INamespaceDefinition getNamespace() {
			return namespace;
		}

		@Override
		public InputStream openStream() {
			
			if (resourceUrl == null)
				onException(
						new ThinklabInternalErrorException(
							"internal error: namespace " + namespace.getId() + " has no associated resource"), -1);

			try {
				return resourceUrl.openStream();
			} catch (IOException e) {
				onException(e, -1);
			}
			
			return null;
		}

		@Override
		public Map<String, Object> getSymbolTable() {
			return this.namespace.getSymbolTable() ;
		}

		@Override
		public IProject getProject() {
			return project;
		}

		@Override
		public boolean validateFunctionCall(IFunctionCall ret) {
			// TODO USE SERVER PROTOTYPES
			return true;
		}

		@Override
		public void defineSymbol(String id, Object value, int lineNumber) {
			/*
			 * TODO may want to save line number to provide warnings or error messages if
			 * things get overridden.
			 */
			this.namespace.getSymbolTable().put(id, value);
		}
	}
	
	public void addInterpreter(String extension, IModelParser modelParser) {
		interpreters.put(extension, modelParser);
	}
	
//	@Override
//	public Collection<IScenario> getApplicableScenarios(IModel arg0,
//			IContext arg1, boolean arg2) throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public IContext getContext(String arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<IModelObject> getDependencies(String arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public IModel getModel(String arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public IModelObject getModelObject(String arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
	/**
	 * Override this to return a new subclass of Resolver to intercept
	 * errors and warnings.
	 * 
	 * @param project
	 * @return
	 */
	public Resolver createResolver(IProject project) {
		return new Resolver(null, project);
	}

	@Override
	public synchronized INamespace getNamespace(String arg0) {
		return namespacesById.get(arg0);
	}

	@Override
	public synchronized Collection<INamespace> getNamespaces() {

		ArrayList<INamespace> ret = new ArrayList<INamespace>();
		for (INamespace n : namespacesById.values()) {
			ret.add(n);
		}
		
		/*
		 * sort namespace list alphabetically
		 */
		Collections.sort(ret, new Comparator<INamespace>() {
			@Override
			public int compare(INamespace arg0, INamespace arg1) {
				return arg0.getId().compareTo(arg1.getId());
			}
		});
		
		return ret;
	}
//
//	@Override
//	public IScenario getScenario(String arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getSource(String arg0) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public synchronized INamespace loadFile(String file, String namespaceId, IProject project, IResolver resolver) throws ThinklabException {
		
		String extension = MiscUtilities.getFileExtension(file);
		IModelParser parser = interpreters.get(extension);
		
		Namespace ret = (Namespace) namespacesById.get(namespaceId);
		if (ret != null)
			return ret;
		
		if (parser == null) {
			throw new ThinklabValidationException("don't know how to parse a " + extension + " model file");
		}
		
		IResolver res = resolver.getNamespaceResolver(namespaceId, file);
		
		ret = (Namespace) parser.parse(namespaceId, file, res);
		
		if (ret != null && !((Namespace)(ret)).hasErrors()) {
			ret.setProject(project);
			ret.setResourceUrl(file);
			ret.synchronizeKnowledge();
		}
		
		return ret;
	}

	@Override
	public INamespace loadFile(String file, String namespaceId, IProject project) throws ThinklabException {
		return loadFile(file, namespaceId, project, createResolver(project));
	}
	
	@Override
	public void releaseNamespace(String arg0) {
		synchronized(namespacesById) {
			Namespace ns = (Namespace) namespacesById.get(arg0);
			if (ns != null) {
				if (ProjectManager.get().getCurrentServer() != null && ns.ontology != null) {
					KnowledgeManager km = 
							(KnowledgeManager) ProjectManager.get().getCurrentServer().getKnowledgeManager();
					km.releaseOntology(ns.ontology);
				}
				namespacesById.remove(arg0);
			}
		}
	}

//	@Override
//	public Collection<INamespace> load(IProject project)
//			throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public boolean canParseExtension(String fileExtension) {
		return interpreters.containsKey(fileExtension);
	}

	
	
	public synchronized INamespace loadNamespace(String namespaceId, String resource, String resourceType)
			throws ThinklabException {
		
		Namespace ret = (Namespace) namespacesById.get(namespaceId);
		if (ret != null)
			return ret;
		
		String extension = resourceType;
		if (extension == null)
			extension = MiscUtilities.getFileExtension(resource);
		
		IModelParser parser = interpreters.get(extension);
		if (parser == null) {
			throw new ThinklabValidationException("don't know how to parse a " + extension + " resource");
		}
		
		ret = (Namespace) parser.parse(namespaceId, resource, createResolver(null));

		if (ret != null) {
			ret.setId(namespaceId);
			ret.synchronizeKnowledge();
		}
		
		return ret;
	}

	@Override
	public IScale getCoverage(IModel model) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public Collection<INamespace> loadSourceDirectory(File sourcedir, IProject project)
//			throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public IObservation observe(Object object, IContext context)
//			throws ThinklabException {
//		// TODO Auto-generated method stub
//		return null;
//	}

	public IResolver getResolver(IServer server, IProject project) {
		return new Resolver(server, project);
	}

	public void notifyNamespace(INamespace ns) {
		synchronized(namespacesById) {
				namespacesById.put(ns.getId(), ns);
		}
	}

	@Override
	public ISubject observe(Object observable) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISubject observe(Object observable, ISubject context,
			IProperty property, boolean distribute) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IScenario> getScenarios(IModel model, ISubject context)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

}
