package org.integratedmodelling.thinklab.client;

import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Wrapper for the JSON return value of a thinklab REST request.
 * 
 * @author ferdinando.villa
 *
 */
public class Result extends JSONObject {

	// TODO harmonize with client 
	static public final int OK = 0, FAIL = 1, WAIT = 2; 
	
	long   _status = 0;	
	String _taskID = null;
	public JSONObject js = null;
	public Object _result;
	
	public Result(JSONObject js) throws ThinklabClientException {
	
		this.js = js;
		
		try {

			_status = js.getInt("status");
			_result = js.get("result");
		
		} catch (JSONException e) {
			throw new ThinklabClientException(e);
		}
	}
}
