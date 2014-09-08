package com.yotadevices.yotaphone2.fbreader;

import android.net.Uri;
import android.os.AsyncTask;

import com.yotadevices.yotaphone2.fbreader.util.ConnectionManager;
import com.yotadevices.yotaphone2.fbreader.util.StreamCopier;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class OxfordDefinition {
	private final static String REQUEST_URL = "https://www.oxfordlearnersdictionaries.com/api/v1/dictionaries/english/search/first";
	private final static String TEST_API_KEY = "YABUWq45zaAj5GPoDd9hUf7izRw6pGGgMehDsqCyJ6tN4E4Bm0aacJ2dN2yeBWH1";
	public static class Definition {
		public String ArticleURI;
		public String Xml;
	}

	public interface DefinitionResult {
		public enum Error {
			NOTHING_TO_DEFINE,
			INCORRECT_REQUEST,
			NO_CONNECTION
		}
		public void onObtainDefinition(Definition result);
		public void onDefinitionError(Error error);
	}

	public static class AsyncDefinitionLoader extends AsyncTask<String, Integer, Boolean> {
		private final DefinitionResult mListener;
		private DefinitionResult.Error mError;
		private Definition mResult;

		public AsyncDefinitionLoader(DefinitionResult listener) {
			mListener = listener;
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (params.length == 0 || params[0].equals("")) {
				mError = DefinitionResult.Error.NOTHING_TO_DEFINE;
				return Boolean.FALSE;
			}
			DefaultHttpClient client = ConnectionManager.getInstance().createDefaultHttpClient(false); // Oxford API server has untrusted certificate
			client.setRedirectHandler(new DefaultRedirectHandler() {
				@Override
				public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
					boolean isRedirect = super.isRedirectRequested(response, context);
					if (!isRedirect) {
						int responseCode = response.getStatusLine().getStatusCode();
						if (responseCode == 301 || responseCode == 302) {
							return true;
						}
					}
					return isRedirect;
				}
			});

			HttpGet request = new HttpGet(REQUEST_URL + "?q=" +  Uri.encode(params[0]) );
			request.addHeader("accessKey", TEST_API_KEY);
			try {
				HttpResponse response = client.execute(request);
				int status = response.getStatusLine().getStatusCode();
				if (status != HttpStatus.SC_OK) {
					mError = DefinitionResult.Error.INCORRECT_REQUEST;
					return Boolean.FALSE;
				}
				InputStream is = response.getEntity().getContent();
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				StreamCopier.copy(is, os);
				is.close();

				mResult = parseDowloadedData(os.toByteArray());
				os.close();
				return Boolean.TRUE;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mError = DefinitionResult.Error.INCORRECT_REQUEST;
			return Boolean.FALSE;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				mListener.onObtainDefinition(mResult);
			}
			else {
				mListener.onDefinitionError(mError);
			}
			super.onPostExecute(result);
		}

		Definition parseDowloadedData(byte[] bytes) throws UnsupportedEncodingException, JSONException {
			final String data = new String(bytes, "UTF-8");
			JSONTokener json = new JSONTokener(data);
			JSONObject fields = (JSONObject)json.nextValue();
			Definition out = new Definition();
			out.Xml = fields.getString("entryContent");
			out.ArticleURI = fields.getString("entryUrl");

			return out;
		}
	}

	public static void getDefinition(String word, DefinitionResult listener) {
		new AsyncDefinitionLoader(listener).execute(word);
	}
}
