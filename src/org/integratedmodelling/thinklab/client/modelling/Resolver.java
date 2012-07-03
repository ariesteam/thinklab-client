package org.integratedmodelling.thinklab.client.modelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabIOException;
import org.integratedmodelling.exceptions.ThinklabResourceNotFoundException;
import org.integratedmodelling.lang.SemanticType;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IResolver;
import org.integratedmodelling.thinklab.api.metadata.IMetadata;
import org.integratedmodelling.thinklab.api.modelling.IAgentModel;
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
import org.integratedmodelling.thinklab.api.modelling.parsing.IPropertyDefinition;
import org.integratedmodelling.thinklab.api.plugin.IThinklabPlugin;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.IServer;
import org.integratedmodelling.thinklab.client.project.ThinklabProject;

public class Resolver implements IResolver {
	
	ThinklabProject project;
	String resourceId = "(not set)";
	long _timestamp = new Date().getTime();

	/*
	 * TODO load knowledge from server the first time a resolver is created for it.
	 */
	IServer server;
	
	HashSet<String> _defined = new HashSet<String>();

	private INamespace _currentNs;

	public Resolver(IServer server, IProject project) {
		this.project = (ThinklabProject)project;
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

	@Override
	public InputStream resolveNamespace(String namespace, String reference) {
		
		/*
		 * TODO
		 * if we have both namespace and reference, push a non-void resolver context so that next import can use
		 * the same location in a relative ref; pop the resolving context after the namespace has been read.
		 * Otherwise, push a void resolver context
		 */

		/*
		 * reference trumps namespace; if both are specified, the name check is done later in validateNamespace
		 */
		if (reference != null) {

			try {
				
				File f = new File(reference);

				if (f.exists() && f.isFile() && f.canRead()) {
					if (resourceId == null) {
						resourceId = f.toString();
					}
					_timestamp = f.lastModified();
					return new FileInputStream(f);
				} else if (reference.contains(":/")) {
					URL url = new URL(reference);
					if (url.toString().startsWith("file:")) {
						f = new File(url.getFile());
						if (resourceId == null) {
							resourceId = f.toString();
						}
						_timestamp = f.lastModified();
					}
					return url.openStream();
				}

				/*
				 * plugin resource has precedence even over local file with same path
				 */
				if (project != null) {

					/*
					 * find file in source folder, if found return open filestream
					 */
					f = project.findResource(reference);
					if (f != null) {
						if (resourceId == null) {
							resourceId = f.toString();
						}
						return new FileInputStream(f);
					}
				}

				f = new File(reference);
				
				if (f.exists() && f.isFile() && f.canRead()) {
					if (resourceId == null) {
						resourceId = f.toString();
					}
					return new FileInputStream(f);
				} else if (reference.contains("://")) {
					URL url = new URL(reference);						
					if (resourceId == null) {
						resourceId = url.toString();
					}
					return url.openStream();
				}
				
				/*
				 * if we get here we haven't found it, look it up in all DIRECTLY imported projects (non-recursively)
				 */
				if (project != null) {
					for (IThinklabPlugin pr : project.getPrerequisites()) {
						
						ThinklabProject prj = (ThinklabProject)pr;
						
						/*
						 * lookup file here, if found return open filestream
						 */
						f = prj.findResourceForNamespace(namespace, "tql");
						if (f != null) {
							try {
								if (resourceId == null) {
									resourceId = f.toString();
								}
								return new FileInputStream(f);
							} catch (FileNotFoundException e) {
								throw new ThinklabIOException(e);
							}
						}
					}
				}
				

			} catch (Exception e) {
				onException( new ThinklabIOException(e), 0);
			}
			
		} else if (namespace != null) {

			/*
			 * find resource using path corresponding to namespace, either in plugin classpath or
			 * relative filesystem.
			 */

			if (project != null) {
				/*
				 * find file in source folder, if found return open filestream
				 * TODO must lookup any supported language
				 */
				File f = project.findResourceForNamespace(namespace, "tql");
				if (f != null) {
					try {
						if (resourceId == null) {
							resourceId = f.toString();
						}
						return new FileInputStream(f);
					} catch (FileNotFoundException e) {
						onException( new ThinklabIOException(e), 0);
					}
				}
			}
			
			String fres = namespace.replace('.', '/');	

			/*
			 * TODO try with the (non-existent yet) pushed resolver context first
			 */

			/*
			 * dumb (i.e., null resolver context)
			 */
			File f = new File(fres);
			if (f.exists() && f.isFile() && f.canRead()) {
				try {
					if (resourceId == null) {
						resourceId = f.toString();
					}
					return new FileInputStream(f);
				} catch (FileNotFoundException e) {
					onException( new ThinklabIOException(e), 0);
				}
			}
			
			/*
			 * if we get here we haven't found it, look it up in all DIRECTLY imported projects (non-recursively)
			 */
			if (project != null) {
				for (IThinklabPlugin pr : project.getPrerequisites()) {
					
					ThinklabProject prj = (ThinklabProject)pr;
					
					/*
					 * lookup file here, if found return open filestream
					 */
					f = prj.findResourceForNamespace(namespace, "tql");
					if (f != null) {
						try {
							if (resourceId == null) {
								resourceId = f.toString();
							}
							return new FileInputStream(f);
						} catch (FileNotFoundException e) {
							onException( new ThinklabIOException(e), 0);
						}
					}
				}
			}
		}

		/*
		 * throw exception here - CHECK We don't get here if it was found, but I'm unsure if this should be
		 * handled in the caller instead.
		 */
		String message = "";
		if (namespace == null)
			message = "cannot read model resource from " + reference;
		else if (reference == null) 
			message = "cannot find source for namespace " + namespace;
		else 
			message = "cannot read namespace " + namespace + " from resource " + reference;

		onException( new ThinklabResourceNotFoundException(message), 0);
		
		return null;

		
	}

	@Override
	public void onNamespaceDeclared(String namespaceId, INamespace namespace) {
		_defined.add(namespaceId);
		_currentNs = namespace;
		((Namespace)namespace).setTimeStamp(_timestamp);
	}

	@Override
	public void onNamespaceDefined(INamespace namespace) {
		if (((Namespace)namespace).hasErrors()) {
			System.out.println("coccodio");
		}
	}

	@Override
	public void validateNamespaceForResource(String resource,
			String namespace)  {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IConceptDefinition resolveExternalConcept(String id, INamespace namespace, int line)
			 {
		
		ConceptObject ret = new ConceptObject();
		ret.setId(id);
		
		/*
		 * TODO discuss import with knowledge manager. Should have been seen before.
		 */
		
		return ret;
	}
	
	@Override
	public IPropertyDefinition resolveExternalProperty(String id, INamespace namespace, int line)
			 {
		
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
	public void onModelObjectDefined(INamespace namespace, IModelObject ret) {
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
//		} else if (cls.equals(IDataSource.class)) {
//			return new DataSourceDefinition();
		} else if (cls.equals(IStoryline.class)) {
			return new Storyline();
		} else if (cls.equals(IScenario.class)) {
			return new Scenario();
		} else if (cls.equals(IAgentModel.class)) {
			return new AgentModel();
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
	public void handleObserveStatement(Object observable, INamespace ns,  IContext ctx, boolean resetContext) 
			 {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IResolver getImportResolver() {
		Resolver ret = new Resolver(server, project);
		ret._defined = _defined;
		return ret;
	}

	@Override
	public boolean isNamespaceDefined(String id) {
		return _defined.contains(id);
	}

	@Override
	public IModelObjectDefinition resolveModelObject(String ns, String object) {
		// TODO Auto-generated method stub
		return null;
	}
}