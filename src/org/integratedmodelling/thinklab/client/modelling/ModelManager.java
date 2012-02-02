package org.integratedmodelling.thinklab.client.modelling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.integratedmodelling.exceptions.ThinklabException;
import org.integratedmodelling.exceptions.ThinklabIOException;
import org.integratedmodelling.exceptions.ThinklabResourceNotFoundException;
import org.integratedmodelling.exceptions.ThinklabValidationException;
import org.integratedmodelling.lang.model.Namespace;
import org.integratedmodelling.thinklab.api.knowledge.storage.IKBox;
import org.integratedmodelling.thinklab.api.lang.IModelParser;
import org.integratedmodelling.thinklab.api.lang.IResolver;
import org.integratedmodelling.thinklab.api.modelling.IAgentModel;
import org.integratedmodelling.thinklab.api.modelling.IModel;
import org.integratedmodelling.thinklab.api.modelling.IModelObject;
import org.integratedmodelling.thinklab.api.modelling.INamespace;
import org.integratedmodelling.thinklab.api.modelling.IScenario;
import org.integratedmodelling.thinklab.api.modelling.factories.IModelManager;
import org.integratedmodelling.thinklab.api.modelling.observation.IContext;
import org.integratedmodelling.thinklab.api.project.IProject;
import org.integratedmodelling.thinklab.api.runtime.ISession;
import org.integratedmodelling.thinklab.client.lang.ClientNamespace;
import org.integratedmodelling.thinklab.client.project.ThinklabProject;
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
	HashMap<String, INamespace> namespaces = new HashMap<String, INamespace>();
	
	class Resolver implements IResolver {
		
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
					for (IProject pr : project.getPrerequisiteProjects()) {
						
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
					for (IProject pr : project.getPrerequisiteProjects()) {
						
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
		public void onNamespaceDeclared(String namespaceId, String resourceId,
				Namespace namespace) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onNamespaceDefined(Namespace namespace) {
		}

		@Override
		public void validateNamespaceForResource(String resource,
				String namespace) throws ThinklabException {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static ModelManager get() {
		if (_this == null) {
			_this = new ModelManager();
		}
		return _this;
	}
	
	public void addInterpreter(String extension, IModelParser modelParser) {
		interpreters.put(extension, modelParser);
	}
	
	@Override
	public IAgentModel getAgentModel(String arg0) {
		// TODO Auto-generated method stub
		return null;
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
	public IContext getCoverage(IModel arg0, IKBox arg1, ISession arg2) {
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

	@Override
	public INamespace getNamespace(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<INamespace> getNamespaces() {

		ArrayList<INamespace> ret = new ArrayList<INamespace>();
		for (INamespace n : namespaces.values()) {
			ret.add(n);
		}
		
		/*
		 * sort namespace list alphabetically
		 */
		Collections.sort(ret, new Comparator<INamespace>() {
			@Override
			public int compare(INamespace arg0, INamespace arg1) {
				return arg0.getNamespace().compareTo(arg1.getNamespace());
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

	@Override
	public INamespace loadFile(String file, IProject project) throws ThinklabException {
		
		String extension = MiscUtilities.getFileExtension(file);
		IModelParser parser = interpreters.get(extension);
		INamespace ret = null;
		
		if (parser == null) {
			throw new ThinklabValidationException("don't know how to parse a " + extension + " model file");
		}
		
		Namespace ns = parser.parse(file, new Resolver(project));
		ns.setProject(project);
		ns.setSourceFile(new File(file));
		ns.synchronizeKnowledge();
		
		ret = new ClientNamespace(ns);
		
		namespaces.put(ret.getNamespace(), ret);
		
		return ret;
	}

	@Override
	public void releaseNamespace(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public IContext run(IModel arg0, IKBox arg1, ISession arg2, IContext arg3)
			throws ThinklabException {
		// TODO Auto-generated method stub
		return null;
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

}
