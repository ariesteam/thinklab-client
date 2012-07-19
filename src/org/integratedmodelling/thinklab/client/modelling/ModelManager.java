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
import java.util.UUID;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabInternalErrorException;
import org.integratedmodelling.exceptions.ThinklabValidationException;
import org.integratedmodelling.lang.SemanticType;
import org.integratedmodelling.thinklab.api.factories.IModelManager;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IModelParser;
import org.integratedmodelling.thinklab.api.lang.IResolver;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.modelling.ICategorizingObserver;
import org.integratedmodelling.thinklab.api.modelling.IClassifyingObserver;
import org.integratedmodelling.thinklab.api.modelling.IContext;
import org.integratedmodelling.thinklab.api.modelling.IMeasuringObserver;
import org.integratedmodelling.thinklab.api.modelling.IModel;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.IObservation;
import org.integratedmodelling.thinklab.api.modelling.IRankingObserver;
import org.integratedmodelling.thinklab.api.modelling.IScenario;
import org.integratedmodelling.thinklab.api.modelling.IStoryline;
import org.integratedmodelling.thinklab.api.modelling.IUnit;
import org.integratedmodelling.thinklab.api.modelling.IValuingObserver;
import org.integratedmodelling.thinklab.api.modelling.parsing.IClassificationDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IConceptDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IFunctionDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.ILanguageDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IModelObjectDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.INamespaceDefinition;
import org.integratedmodelling.thinklab.api.modelling.parsing.IPropertyDefinition;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.IServer;
import org.integratedmodelling.thinklab.client.utils.MiscUtilities;

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
	HashMap<String, INamespace> namespacesById = new HashMap<String, INamespace>();
	
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

		private INamespace _currentNs;
		private HashMap<String, IModelObjectDefinition> symbolTable =
				new HashMap<String, IModelObjectDefinition>();

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

			if (_currentNs != null) {
				((Namespace)_currentNs).addError(e.getMessage(), lineNumber);
			}
			return true;
		}

		@Override
		public boolean onWarning(String warning, int lineNumber) {
			if (_currentNs != null) {
				((Namespace)_currentNs).addWarning(warning, lineNumber);
			}
			return true;
		}

		@Override
		public boolean onInfo(String info, int lineNumber) {
			// TODO Auto-generated method stub
			return true;
		}

//		@Override
//		public InputStream resolveNamespace(String namespace, String reference) {
//			
//			/*
//			 * TODO
//			 * if we have both namespace and reference, push a non-void resolver context so that next import can use
//			 * the same location in a relative ref; pop the resolving context after the namespace has been read.
//			 * Otherwise, push a void resolver context
//			 */
//
//			/*
//			 * reference trumps namespace; if both are specified, the name check is done later in validateNamespace
//			 */
//			if (reference != null) {
//
//				try {
//					
//					File f = new File(reference);
//
//					if (f.exists() && f.isFile() && f.canRead()) {
//						if (resourceId == null) {
//							resourceId = f.toString();
//						}
//						_timestamp = f.lastModified();
//						return new FileInputStream(f);
//					} else if (reference.contains(":/")) {
//						URL url = new URL(reference);
//						if (url.toString().startsWith("file:")) {
//							f = new File(url.getFile());
//							if (resourceId == null) {
//								resourceId = f.toString();
//							}
//							_timestamp = f.lastModified();
//						}
//						return url.openStream();
//					}
//
//					/*
//					 * plugin resource has precedence even over local file with same path
//					 */
//					if (project != null) {
//
//						/*
//						 * find file in source folder, if found return open filestream
//						 */
//						f = project.findResource(reference);
//						if (f != null) {
//							if (resourceId == null) {
//								resourceId = f.toString();
//							}
//							return new FileInputStream(f);
//						}
//					}
//
//					f = new File(reference);
//					
//					if (f.exists() && f.isFile() && f.canRead()) {
//						if (resourceId == null) {
//							resourceId = f.toString();
//						}
//						return new FileInputStream(f);
//					} else if (reference.contains("://")) {
//						URL url = new URL(reference);						
//						if (resourceId == null) {
//							resourceId = url.toString();
//						}
//						return url.openStream();
//					}
//					
//					/*
//					 * if we get here we haven't found it, look it up in all DIRECTLY imported projects (non-recursively)
//					 */
//					if (project != null) {
//						for (IThinklabPlugin pr : project.getPrerequisites()) {
//							
//							Project prj = (Project)pr;
//							
//							/*
//							 * lookup file here, if found return open filestream
//							 */
//							f = prj.findResourceForNamespace(namespace);
//							if (f != null) {
//								try {
//									if (resourceId == null) {
//										resourceId = f.toString();
//									}
//									return new FileInputStream(f);
//								} catch (FileNotFoundException e) {
//									throw new ThinklabIOException(e);
//								}
//							}
//						}
//					}
//					
//
//				} catch (Exception e) {
//					onException( new ThinklabIOException(e), 0);
//				}
//				
//			} else if (namespace != null) {
//
//				/*
//				 * find resource using path corresponding to namespace, either in plugin classpath or
//				 * relative filesystem.
//				 */
//
//				if (project != null) {
//					/*
//					 * find file in source folder, if found return open filestream
//					 * TODO must lookup any supported language
//					 */
//					File f = project.findResourceForNamespace(namespace);
//					if (f != null) {
//						try {
//							if (resourceId == null) {
//								resourceId = f.toString();
//							}
//							return new FileInputStream(f);
//						} catch (FileNotFoundException e) {
//							onException( new ThinklabIOException(e), 0);
//						}
//					}
//				}
//				
//				String fres = namespace.replace('.', '/');	
//
//				/*
//				 * TODO try with the (non-existent yet) pushed resolver context first
//				 */
//
//				/*
//				 * dumb (i.e., null resolver context)
//				 */
//				File f = new File(fres);
//				if (f.exists() && f.isFile() && f.canRead()) {
//					try {
//						if (resourceId == null) {
//							resourceId = f.toString();
//						}
//						return new FileInputStream(f);
//					} catch (FileNotFoundException e) {
//						onException( new ThinklabIOException(e), 0);
//					}
//				}
//				
//				/*
//				 * if we get here we haven't found it, look it up in all DIRECTLY imported projects (non-recursively)
//				 */
//				if (project != null) {
//					try {
//					for (IThinklabPlugin pr : project.getPrerequisites()) {
//						
//						IProject prj = (IProject)pr;
//						
//						/*
//						 * lookup file here, if found return open filestream
//						 */
//						f = prj.findResourceForNamespace(namespace);
//						if (f != null) {
//								if (resourceId == null) {
//									resourceId = f.toString();
//								}
//								return new FileInputStream(f);
//						}
//					}
//					} catch (Exception e) {
//						onException(new ThinklabException(e), 0);
//					}
//				}
//			}
//
//			/*
//			 * throw exception here - CHECK We don't get here if it was found, but I'm unsure if this should be
//			 * handled in the caller instead.
//			 */
//			String message = "";
//			if (namespace == null)
//				message = "cannot read model resource from " + reference;
//			else if (reference == null) 
//				message = "cannot find source for namespace " + namespace;
//			else 
//				message = "cannot read namespace " + namespace + " from resource " + reference;
//
//			onException( new ThinklabResourceNotFoundException(message), 0);
//			
//			return null;
//
//			
//		}

		@Override
		public void onNamespaceDeclared() {
		}

		@Override
		public void onNamespaceDefined() {
			if (this.namespace.hasErrors()) {
				System.out.println("coccodio");
			}
		}

//		@Override
//		public void validateNamespaceForResource(String resource, String namespace)  {
//			// TODO Auto-generated method stub
//			
//		}

		@Override
		public IConceptDefinition resolveExternalConcept(String id, int line)
				 {
			
			ConceptObject ret = new ConceptObject();
			ret.setId(id);
			
			/*
			 * TODO discuss import with knowledge manager and server if any. Should have been seen before.
			 */
			
			return ret;
		}
		
		@Override
		public IPropertyDefinition resolveExternalProperty(String id, int line) {
			
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public IExpression resolveFunction(String functionId,
				Collection<String> parameterNames) {
			// TODO use current server; supply defaults for core library
			return null;
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
			} else if (cls.equals(IContext.class)) {
				return new Context();
			} else if (cls.equals(IStoryline.class)) {
				return new Storyline();
			} else if (cls.equals(IScenario.class)) {
				return new Scenario();
			} else if (cls.equals(IConcept.class)) {
				return new ConceptObject();
			} else if (cls.equals(IProperty.class)) {
				return new PropertyObject();
			} else if (cls.equals(IObservation.class)) {
				return new Observation();
			} else if (cls.equals(IUnit.class)) {
				return new UnitDefinition();
			} else if (cls.equals(IMetadata.class)) {
				return new Metadata();
			} else if (cls.equals(IFunctionDefinition.class)) {
				return new FunctionDefinition();
			}  else if (cls.equals(IClassificationDefinition.class)) {
				return new ClassificationDefinition();
			}
			
			return null;
		}
		

		@Override
		public boolean isGeneratedId(String id) {
			return id.endsWith("___");
		}

		@Override
		public String generateId(IModelObject o) {
			return UUID.randomUUID().toString() + "___";
		}

		@Override
		public Object runFunction(IFunctionDefinition function) {
			
			IExpression f = resolveFunction(function.getId(), function.getParameters().keySet());
			if (f != null) {
				try {
					return f.eval(function.getParameters());
				} catch (ThinklabException e) {
				}
			}
			return null;
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
		public void handleObserveStatement(Object observable,  IContext ctx, boolean resetContext, int lineNumber)  {
			// TODO Auto-generated method stub
			
		}

		@Override
		public IResolver getImportResolver(IProject project) {
			Resolver ret = new Resolver(server, project);
			ret._defined = _defined;
			return ret;
		}

		@Override
		public INamespace getNamespace(String id, int lineNumber) {
			
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

//		@Override
//		public IModelObjectDefinition resolveModelObject(String ns, String object) {
//			// TODO Auto-generated method stub
//			return null;
//		}

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
			
			namespacesById.put(namespace, ns);
			
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
		public HashMap<String, IModelObjectDefinition> getSymbolTable() {
			return this.symbolTable ;
		}
	}
	
	public void addInterpreter(String extension, IModelParser modelParser) {
		interpreters.put(extension, modelParser);
	}
	
	@Override
	public Collection<IScenario> getApplicableScenarios(IModel arg0,
			IContext arg1, boolean arg2) throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IModelObject> getDependencies(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModel getModel(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IModelObject getModelObject(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
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
	public INamespace getNamespace(String arg0) {
		return namespacesById.get(arg0);
	}

	@Override
	public Collection<INamespace> getNamespaces() {

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

	@Override
	public IScenario getScenario(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSource(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public INamespace loadFile(String file, String namespaceId, IProject project, IResolver resolver) throws ThinklabException {
		
		String extension = MiscUtilities.getFileExtension(file);
		IModelParser parser = interpreters.get(extension);
		Namespace ret = null;
		
		if (parser == null) {
			throw new ThinklabValidationException("don't know how to parse a " + extension + " model file");
		}
		
		ret = (Namespace) parser.parse(namespaceId, file, resolver);
		
		if (ret != null && !((Namespace)(ret)).hasErrors()) {
		
			ret.setProject(project);
			ret.setResourceUrl(file);
			ret.synchronizeKnowledge();
			namespacesById.put(ret.getId(), ret);
		}
		
		return ret;
	}

	@Override
	public INamespace loadFile(String file, String namespaceId, IProject project) throws ThinklabException {
		return loadFile(file, namespaceId, project, createResolver(project));
	}
	
	@Override
	public void releaseNamespace(String arg0) {
		namespacesById.remove(arg0);
	}

	@Override
	public Collection<INamespace> load(IProject project)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean canParseExtension(String fileExtension) {
		return interpreters.containsKey(fileExtension);
	}

	
	
	public INamespace loadNamespace(String namespaceId, String resource, String resourceType)
			throws ThinklabException {
		
		String extension = resourceType;
		if (extension == null)
			extension = MiscUtilities.getFileExtension(resource);
		
		IModelParser parser = interpreters.get(extension);
		Namespace ret = null;
		
		if (parser == null) {
			throw new ThinklabValidationException("don't know how to parse a " + extension + " resource");
		}
		
		ret = (Namespace) parser.parse(namespaceId, resource, createResolver(null));

		if (ret != null) {
			ret.setId(namespaceId);
			ret.synchronizeKnowledge();
			namespacesById.put(ret.getId(), ret);
		}
		
		return ret;
	}

	@Override
	public IContext getCoverage(IModel model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IExpression resolveFunction(String functionId,
			Collection<String> parameterNames) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<INamespace> loadSourceDirectory(File sourcedir, IProject project)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IObservation observe(Object object, IContext context)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
	}

	public IResolver getResolver(IServer server, IProject project) {
		return new Resolver(server, project);
	}

}
