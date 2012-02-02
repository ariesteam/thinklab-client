package org.integratedmodelling.thinklab.client;

import java.util.ArrayList;
import java.util.Collection;

import org.integratedmodelling.collections.Pair;
import org.integratedmodelling.list.PolyList;
import org.integratedmodelling.thinklab.client.exceptions.ThinklabClientException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Wrapper for the JSON return value of a thinklab REST request.
 * 
 * @author ferdinando.villa
 *
 */
public class Result {

	static public final int OK = 0, FAIL = 1, WAIT = 2; 
	
	int    _status = 0;	
	String _taskID = null;
	public JSONObject js = null;
	public Object _result = null;
	
	private Session _session;

	private int _type;
	
	protected JSONObject json() {
		if (js == null)
			js = new JSONObject();
		return js;
	}
	
	public Session getSession() {
		return _session;
	}
	
	public Result setSession(Session session) {
		_session = session;
		return this;
	}
	
	public Result(JSONObject js) throws ThinklabClientException {
	
		this.js = js;
		
		try {

			_status = js.getInt("status");
			_type   = 0;
			if (js.has("type")) {
				_type = js.getInt("type");
			}
			if (js.has("result")) {
				if (_type == 10) {
					_result = PolyList.parse(js.getString("result"));
				} else {
					_result = js.get("result");
				}
			}
		} catch (Exception e) {
			throw new ThinklabClientException(e);
		}
	}
	
	public Result() {
		_status = OK;
	}
	
	public static Result fail(Session session) {
		Result ret = new Result().setSession(session);
		ret._status = FAIL;
		return ret;
	}

	public String print() {

		String ret = "";
		try {
			if (_status == OK) {
				if (_result != null) {
					if (js != null && js.has("info"))
						ret = js.getString("info") + "\n";
					ret += "[" + _result.toString() + "]";
				} else if (js != null && js.has("warning")) {
					ret = js.getString("warning");
				}else if (js != null && js.has("info")) {
					ret = js.getString("info");
				}
			} else if (_status == FAIL) {
				ret = "** error **";
				if (js != null && js.has("error")) {
					ret += ": " + js.getString("error");
				}
			} else if (_status == WAIT) {
				ret = "[" + js.getString("taskid") + "]";
			}
		} catch (JSONException e) {
		}
		return ret;
	}

	public static Result ok(Session session) {
		return new Result().setSession(session);
	}

	public Result info(String message) {

		try {
			json().put("info", message);
		} catch (JSONException e) {
			// come on
		}
		return this;
	}
	
	public Result error(String message) {

		try {
			json().put("error", message);
		} catch (JSONException e) {
			// come on
		}
		return this;
	}

	public Result put(String var, Object val) {

		try {
			json().put(var, val);
		} catch (JSONException e) {
			// come on
		}
		return this;
	}
	
	public Object getResult() {
		return (_result == null || JSONObject.NULL.equals(_result)) ? null : _result;
	}

	public int size() throws ThinklabClientException {
		
		return (_result == null || JSONObject.NULL.equals(_result)) ? 
				0 : 
				(_result instanceof JSONArray ? ((JSONArray)_result).length() : 1);
	}
	
	public Object getResult(int i) throws ThinklabClientException {

		try {
			JSONArray js = asArray();
			return js == null? null : asArray().get(i);
		} catch (Exception e) {
			throw new ThinklabClientException(e);		
		}
	}
	
	public Object getResult(int i, int j) throws ThinklabClientException {

		Object ret = null;
		try {
			JSONArray jj = asArray();
			if (jj != null) {
				Object js = asArray().get(i);
				if (js instanceof JSONArray)
					ret = ((JSONArray)js).get(j);
			}
		} catch (Exception e) {
			throw new ThinklabClientException(e);		
		}
		return (ret != null && !JSONObject.NULL.equals(ret)) ? ret : null;
	}

	public JSONArray asArray() throws ThinklabClientException {

		if (!(_result instanceof JSONArray))
			throw new ThinklabClientException("result is not an array");
		if (JSONObject.NULL.equals(_result))
			return null;
		return (JSONArray)_result;
	}
	
	public Object get(String field) throws ThinklabClientException {
		
		try {
			return
				js == null ?
					null :
					(js.has(field) ? js.get(field) : null);
		} catch (JSONException e) {
			throw new ThinklabClientException(e);
		}
	}

	public int getStatus() {
		return _status;
	}

	public Result setResult(String ss) {
		_result = ss;
		return this;
	}

	/**
	 * Return all the files to download inserted in the result by the remote command.
	 * 
	 * @return
	 * @throws ThinklabClientException
	 */
	public Collection<Pair<String, String>> getDownloads()
			throws ThinklabClientException {

		ArrayList<Pair<String, String>> ret = new ArrayList<Pair<String, String>>();
		try {
			if (js != null && js.has("downloads")) {
				JSONArray dl = js.getJSONArray("downloads");
				for (int i = 0; i < dl.length(); i++) {
					String fn = dl.getJSONArray(i).getString(0);
					String fh = dl.getJSONArray(i).getString(1);
					ret.add(new Pair<String, String>(fn, fh));
				}
			}
		} catch (JSONException e) {
			throw new ThinklabClientException(e);
		}
		return ret;
	}
	
}
