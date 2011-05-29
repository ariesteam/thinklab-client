package org.integratedmodelling.thinklab.client.shell;

import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;

public interface CommandLine {

	/**
	 * Ask a question and return the answer. 
	 * 
	 * @param prompt
	 * @return
	 * @throws ThinklabIOException
	 */
	public abstract String ask(String prompt) throws ThinklabClientException;

	/**
	 * Like ask(prompt), returns default if user presses enter.
	 * 
	 * @param prompt
	 * @param defaultresponse
	 * @return
	 * @throws ThinklabIOException
	 */
	public abstract String ask(String prompt, String defaultresponse)
			throws ThinklabClientException;

	/**
	 * Just print the passed text.
	 * 
	 * @param text
	 */
	public abstract void say(String text);

	/**
	 * Get a response without printing a question.
	 * @return
	 * @throws ThinklabClientException
	 */
	public abstract String ask() throws ThinklabClientException;

}