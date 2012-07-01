package org.integratedmodelling.thinklab.client.resource;

/**
 * A ISource wraps a service, URL or file that can be turned into a datasource
 * definition for a Thinklab language. Used for now only in the GUI.
 * 
 * Annotate with the Source annotation to bind it to a particular file type
 * or service URL.
 * 
 * @author Ferd
 *
 */
public interface ISource {
	
	public String getDefinitionPrototype(String language);

}
