package org.geometerplus.fbreader.network.authentication.fbreaderorg;


import java.util.Iterator;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;


public class ServerInterface{
	
    public static final String HOST = "https://data.fbreader.org/sync/auth/";
    public static final String API_URL = "https://data.fbreader.org/sync/auth/sync_api_interface.php";

    public static final String ID_KEY = "id";
    public static final String SIG_KEY = "signature";
    public static final String SUCCESS_KEY = "success";
    public static final String USER_ID_KEY = "user_id";
    public static final String ERROR_MESSAGE = "error_message";
    public static final String METHOD_KEY = "method";
    public static final String ARGS_KEY = "arguments";
    public static final String DATA_ARRAY_KEY = "data_array";
    public static final String ERROR_CODE = "error_code";
    
    public static final int DB_ERROR = 1;
    public static final int ALREADY_REGISTERED = 2;
    public static final int NO_ACCOUNT = 3;
    public static final int WRONG_PW = 4;
    public static final int WRONG_DIGEST = 5;
    //	Server php defines:
	//    define("ID_KEY", "id");
	//    define("SIG_KEY", "signature");
	//    define("SUCCESS_KEY", "success");
	//    define("ERROR_MESSAGE", "error_message");
	//    define("METHOD_KEY", "method");
	//    define("ARGS_KEY", "arguments");
//    define("ERROR_CODE", "error_code");
//    define("DB_ERROR", 1);
//    define("ALREADY_REGISTERED", 2);
//    define("NO_ACCOUNT", 3);
//    define("WRONG_PW", 4);
//    define("WRONG_DIGEST", 5);
    
    public static Bundle jsonToBundle(JSONObject json) {
    	Bundle result = new Bundle();
    	Iterator<?> jsonKeys = json.keys();
    	while (jsonKeys.hasNext()) {
    		Object next = jsonKeys.next();
    		if (next instanceof String) {
    			String key = (String)next;
    			if (key.equals(SUCCESS_KEY)) {
        			result.putBoolean(key, json.optBoolean(key));
        			continue;
        		}
    			if (key.equals(ERROR_CODE)) {
    				result.putInt(key, json.optInt(key));
    				continue;
    			}
    			result.putString(key, json.optString(key, ""));
    		}
    	}
    	return result;
    }
    
	
	private static JSONObject callAPI(ApiMethod method, JSONArray args) 
							throws ZLNetworkException {
		ZLNetworkManager networkManager = new ZLNetworkManager();
		JSONObject query = new JSONObject();

		try {
			query.put(METHOD_KEY, method);
			query.put(ARGS_KEY, args);
			Request request = new Request(API_URL, null, query.toString());
			networkManager.perform(request);
			return request.getResponse();
		}
		catch (JSONException e) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SOMETHING_WRONG, e);
		}
	}
	
	
	public static Bundle ourAuthRegister(String account, String password)
					throws ZLNetworkException {
		return ourAuth(account, password, ApiMethod.REGISTER);
	}

	public static Bundle ourAuthLogin(String account, String password) 
					throws ZLNetworkException {
		return ourAuth(account, password, ApiMethod.LOGIN);
	}

	private static Bundle ourAuth(String account, 
								   String password, ApiMethod method) 
									throws ZLNetworkException {
		JSONArray args = new JSONArray();
		args.put(account);
		args.put(Digests.hashSHA256(password));
		return jsonToBundle(callAPI(method, args));
	}
	
	
	public static class ServerInterfaceException extends Exception {
		private static final long serialVersionUID = 2763341583848143956L;
		public ServerInterfaceException(String message) {
			super(message);
		}
		public ServerInterfaceException(String message, Throwable cause) {
			super(message, cause);
		}
		public ServerInterfaceException(Throwable cause) {
			super(cause);
		}
	}
	
	private enum ApiMethod {
        REGISTER("register"),
        LOGIN("login");
        
		private String myValue;
		
		private ApiMethod(String str) {
			myValue = str;
		}
		
		@Override
		public String toString() {
			return myValue;
		}
	}
}
