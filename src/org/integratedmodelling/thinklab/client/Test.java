package org.integratedmodelling.thinklab.client;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class Test {

	public static void main(String[] args) throws ResourceException, IOException {

		
//		ClientResource cr = new ClientResource("http://restlet-example-serialization.appspot.com/contacts/123");
//		// Get the Contact object
//		ContactResource resource = cr.wrap(ContactResource.class);
//		Contact contact = resource.retrieve();
//
//		if (contact != null) {
//		    System.out.println("firstname: " + contact.getFirstName());
//		    System.out.println(" lastname: " + contact.getLastName());
//		    System.out.println("     nage: " + contact.getAge());
//		}
		
		ClientResource cr = new ClientResource("http://127.0.0.1:8182/rest/");
		Representation rep = cr.get(MediaType.APPLICATION_JSON);
		try {
			JSONObject js = new JSONObject(rep.getText());
			System.out.println(js);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rep.write(System.out);
	}
	
}
