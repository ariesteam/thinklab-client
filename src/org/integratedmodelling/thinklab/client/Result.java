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
	public Object _result = null;
	
	protected JSONObject json() {
		if (js == null)
			js = new JSONObject();
		return js;
	}
	
	public Result(JSONObject js) throws ThinklabClientException {
	
		this.js = js;
		
		try {

			_status = js.getInt("status");
			_result = js.get("result");
		
		} catch (JSONException e) {
			throw new ThinklabClientException(e);
		}
	}
	
	public Result() {
		_status = OK;
	}
	
	public static Result fail() {
		Result ret = new Result();
		ret._status = FAIL;
		return ret;
	}

	public String print() {

		String ret = "";
		try {
			if (_status == OK) {
				if (_result != null) {

				} else if (js != null && js.has("warning")) {
					ret = js.getString("warning");
				}else if (js != null && js.has("info")) {
					ret = js.getString("info");
				}
			} else if (_status == FAIL) {
				ret = "ERROR";
				if (js != null && js.has("error")) {
					ret += ": " + js.getString("error");
				}
			}
		} catch (JSONException e) {
		}
		return ret;
	}

	public static Result ok() {
		return new Result();
	}

	public static Result info(String message) {
		Result ret = new Result();
		try {
			ret.json().put("info", message);
		} catch (JSONException e) {
			// come on
		}
		return ret;
	}
	
}
