package org.geometerplus.fbreader.network.authentication.fbreaderorg;


import java.io.IOException;
import java.io.InputStream;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.json.JSONException;
import org.json.JSONObject;


public class Request extends ZLNetworkRequest {
		
	public Request(String url) {
		super(url);
	}

	
	public Request(String url, String sslCertificate, String postData) {
		super(url, sslCertificate, postData);
	}
	
	
	private JSONObject myResponse;
	
	public JSONObject getResponse()
	{
		return myResponse;
	}
	

	@Override
	public void handleStream(InputStream inputStream, int length)
			throws IOException, ZLNetworkException {
		byte[] buf = new byte[(length != -1) ? length : 1024];
		String jsonRespondString = null;
		if (length == -1)
		{
			StringBuilder sb = new StringBuilder();
			int symbolsReaded;
			while ((symbolsReaded = inputStream.read(buf, 0, 1024)) != -1)
			{
				sb.append(new String(buf, 0, symbolsReaded));
			}
			jsonRespondString = sb.toString();		
		} else 
		{
			inputStream.read(buf, 0, length);
			jsonRespondString = new String(buf);
		}
		try {
			myResponse = new JSONObject(jsonRespondString);
		} catch (JSONException e) {
			myResponse = null;
		}
	}
}
