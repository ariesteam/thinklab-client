package org.integratedmodelling.thinklab.client;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class Test {

	public static void main(String[] args) throws ResourceException, IOException {
		
		ClientResource cr = new ClientResource("http://restlet-example-serialization.appspot.com/contacts/123");
		Representation rep = cr.get(MediaType.APPLICATION_JSON);
		rep.write(System.out);

	}
	
}
