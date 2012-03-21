package org.integratedmodelling.thinklab.client.modelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabIOException;
import org.integratedmodelling.exceptions.ThinklabResourceNotFoundException;
import org.integratedmodelling.thinklab.api.knowledge.IConcept;
import org.integratedmodelling.thinklab.api.knowledge.IExpression;
import org.integratedmodelling.thinklab.api.knowledge.IProperty;
import org.integratedmodelling.thinklab.api.lang.IResolver;
import org.integratedmodelling.thinklab.api.lang.parsing.IConceptDefinition;
import org.integratedmodelling.thinklab.api.lang.parsing.ILanguageDefinition;
import org.integratedmodelling.thinklab.api.lang.parsing.IPropertyDefinition;
import org.integratedmodelling.thinklab.api.modelling.IAgentModel;
import org.integratedmodelling.thinklab.api.modelling.ICategorizingObserver;
import org.integratedmodelling.thinklab.api.modelling.IClassifyingObserver;
import org.integratedmodelling.thinklab.api.modelling.IContext;
import org.integratedmodelling.thinklab.api.modelling.IDataSource;
import org.integratedmodelling.thinklab.api.modelling.IMeasuringObserver;
import org.integratedmodelling.thinklab.api.modelling.IModel;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.IObservation;
import org.integratedmodelling.thinklab.api.modelling.IRankingObserver;
import org.integratedmodelling.thinklab.api.modelling.IScenario;
import org.integratedmodelling.thinklab.api.modelling.IStoryline;
import org.integratedmodelling.thinklab.api.modelling.IValuingObserver;
import org.integratedmodelling.thinklab.api.plugin.IThinklabPlugin;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.client.project.ThinklabProject;

public class Resolver implements IResolver {
	
	ThinklabProject project;

	public Resolver(IProject project) {
		this.project = (ThinklabProject)project;
	}
	
	@Override
	public boolean onException(Throwable e, int lineNumber)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onWarning(String warning, int lineNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onInfo(String info, int lineNumber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InputStream resolveNamespace(String namespace, String reference)
			throws ThinklabException {
		
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
				
				/*
				 * plugin resource has precedence even over local file with same path
				 */
				if (project != null) {

					/*
					 * find file in source folder, if found return open filestream
					 */
					File f = project.findResource(reference);
					if (f != null) {
						return new FileInputStream(f);
					}
				}

				File f = new File(reference);
				
				if (f.exists() && f.isFile() && f.canRead()) {
					return new FileInputStream(f);
				} else if (reference.contains("://")) {
					URL url = new URL(reference);						
					return url.openStream();
				}
			} catch (Exception e) {
				throw new ThinklabIOException(e);
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
					File f = prj.findResource(reference);
					if (f != null) {
						try {
							return new FileInputStream(f);
						} catch (FileNotFoundException e) {
							throw new ThinklabIOException(e);
						}
					}
				}
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
						return new FileInputStream(f);
					} catch (FileNotFoundException e) {
						throw new ThinklabIOException(e);
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
					return new FileInputStream(f);
				} catch (FileNotFoundException e) {
					throw new ThinklabIOException(e);
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
							return new FileInputStream(f);
						} catch (FileNotFoundException e) {
							throw new ThinklabIOException(e);
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

		throw new ThinklabResourceNotFoundException(message);
		
	}

	@Override
	public void onNamespaceDeclared(String namespaceId, String resourceId, INamespace namespace) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNamespaceDefined(INamespace namespace) {

	
	}

	@Override
	public void validateNamespaceForResource(String resource,
			String namespace) throws ThinklabException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IConceptDefinition resolveExternalConcept(String id, INamespace namespace, int line)
			throws ThinklabException {
		
		ConceptObject ret = new ConceptObject();
//		ret.setId(id);
		
		/*
		 * TODO discuss import with knowledge manager. Should have been seen before.
		 */
		
		return ret;
	}
	
	@Override
	public IPropertyDefinition resolveExternalProperty(String id, INamespace namespace, int line)
			throws ThinklabException {
		
		PropertyObject ret = new PropertyObject();
		ret.setId(id);
		
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
		} else if (cls.equals(IDataSource.class)) {
			return new DataSourceDefinition();
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
		} 
		
		return null;
	}
}