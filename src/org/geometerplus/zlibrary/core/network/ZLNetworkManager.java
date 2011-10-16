/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.network;

import java.util.*;
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.net.*;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

public class ZLNetworkManager {
	private static ZLNetworkManager ourManager;

	public static ZLNetworkManager Instance() {
		if (ourManager == null) {
			ourManager = new ZLNetworkManager();
		}
		return ourManager;
	}

	public static interface CredentialsCreator {
		Credentials createCredentials(String scheme, AuthScope scope);
	}

	private CredentialsCreator myCredentialsCreator;

	private class MyCredentialsProvider extends BasicCredentialsProvider {
		private final HttpUriRequest myRequest;

		MyCredentialsProvider(HttpUriRequest request) {
			myRequest = request;
		}

		@Override
		public Credentials getCredentials(AuthScope authscope) {
			final Credentials c = super.getCredentials(authscope);
			if (c != null) {
				return c;
			}
			if (myCredentialsCreator != null) {
				return myCredentialsCreator.createCredentials(myRequest.getURI().getScheme(), authscope);
			}
			return null;
		}
	};

	private final HttpContext myHttpContext = new BasicHttpContext();
	private final CookieStore myCookieStore = new CookieStore() {
		public synchronized void addCookie(Cookie cookie) {
			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.saveCookies(Collections.singletonList(cookie));
			}
		}

		public synchronized void addCookies(Cookie[] cookies) {
			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.saveCookies(Arrays.asList(cookies));
			}
		}

		public synchronized void clear() {
			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.removeAll();
			}
		}

		public synchronized boolean clearExpired(Date date) {
			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.removeObsolete(date);
				// TODO: detect if any Cookie has been removed
				return true;
			}
			return false;
		}

		public synchronized List<Cookie> getCookies() {
			final CookieDatabase db = CookieDatabase.getInstance();
			return db != null ? db.loadCookies() : Collections.<Cookie>emptyList();
		}
	};

	{
		myHttpContext.setAttribute(ClientContext.COOKIE_STORE, myCookieStore);
	}

	/*private void setCommonHTTPOptions(HttpMessage request) throws ZLNetworkException {
		httpConnection.setInstanceFollowRedirects(true);
		httpConnection.setAllowUserInteraction(true);
	}*/

	public void setCredentialsCreator(CredentialsCreator creator) {
		myCredentialsCreator = creator;
	}

	public void perform(ZLNetworkRequest request) throws ZLNetworkException {
		boolean success = false;
		DefaultHttpClient httpClient = null;
		HttpEntity entity = null;
		try {
			request.doBefore();
			final HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(params, 30000);
			HttpConnectionParams.setConnectionTimeout(params, 15000);
			httpClient = new DefaultHttpClient(params);
			final HttpRequestBase httpRequest;
			if (request.PostData !=  null) {
				httpRequest = new HttpPost(request.URL);
				((HttpPost)httpRequest).setEntity(new StringEntity(request.PostData, "utf-8"));
				/*
					httpConnection.setRequestProperty(
						"Content-Length",
						Integer.toString(request.PostData.getBytes().length)
					);
					httpConnection.setRequestProperty(
						"Content-Type", 
						"application/x-www-form-urlencoded"
					);
				*/
			} else if (!request.PostParameters.isEmpty()) {
				httpRequest = new HttpPost(request.URL);
				final List<BasicNameValuePair> list =
					new ArrayList<BasicNameValuePair>(request.PostParameters.size());
				for (Map.Entry<String,String> entry : request.PostParameters.entrySet()) {
					list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				((HttpPost)httpRequest).setEntity(new UrlEncodedFormEntity(list, "utf-8"));
			} else {
				httpRequest = new HttpGet(request.URL);
			}
			httpRequest.setHeader("User-Agent", ZLNetworkUtil.getUserAgent());
			httpRequest.setHeader("Accept-Encoding", "gzip");
			httpRequest.setHeader("Accept-Language", Locale.getDefault().getLanguage());
			httpClient.setCredentialsProvider(new MyCredentialsProvider(httpRequest));
			HttpResponse response = null;
			IOException lastException = null;
			for (int retryCounter = 0; retryCounter < 3 && entity == null; ++retryCounter) {
				try {
					response = httpClient.execute(httpRequest, myHttpContext);
					entity = response.getEntity();
					lastException = null;
				} catch (IOException e) {
					lastException = e;
				}
			}
			if (lastException != null) {
				throw lastException;
			}
			final int responseCode = response.getStatusLine().getStatusCode();

			InputStream stream = null;
			if (entity != null && responseCode == HttpURLConnection.HTTP_OK) {
				stream = entity.getContent();
			}

			if (stream != null) {
				try {
					final Header encoding = entity.getContentEncoding();
					if (encoding != null && "gzip".equalsIgnoreCase(encoding.getValue())) {
						stream = new GZIPInputStream(stream);
					}
					request.handleStream(stream, (int)entity.getContentLength());
				} finally {
					stream.close();
				}
				success = true;
			} else {
				if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
					throw new ZLNetworkException(ZLNetworkException.ERROR_AUTHENTICATION_FAILED);
				} else {
					throw new ZLNetworkException(true, response.getStatusLine().toString());
				}
			}
		} catch (ZLNetworkException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			final String code;
			if (e instanceof UnknownHostException) {
				code = ZLNetworkException.ERROR_RESOLVE_HOST;
			} else {
				code = ZLNetworkException.ERROR_CONNECT_TO_HOST;
			}
			throw new ZLNetworkException(code, ZLNetworkUtil.hostFromUrl(request.URL), e);
		} finally {
			request.doAfter(success);
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException e) {
				}
			}
		}
	}

	public void perform(List<ZLNetworkRequest> requests) throws ZLNetworkException {
		if (requests.size() == 0) {
			return;
		}
		if (requests.size() == 1) {
			perform(requests.get(0));
			return;
		}
		HashSet<String> errors = new HashSet<String>();
		// TODO: implement concurrent execution !!!
		for (ZLNetworkRequest r: requests) {
			try {
				perform(r);
			} catch (ZLNetworkException e) {
				e.printStackTrace();
				errors.add(e.getMessage());
			}
		}
		if (errors.size() > 0) {
			StringBuilder message = new StringBuilder();
			for (String e : errors) {
				if (message.length() != 0) {
					message.append(", ");
				}
				message.append(e);
			}
			throw new ZLNetworkException(true, message.toString());
		}
	}

	public final void downloadToFile(String url, final File outFile) throws ZLNetworkException {
		downloadToFile(url, null, outFile, 8192);
	}

	public final void downloadToFile(String url, String sslCertificate, final File outFile) throws ZLNetworkException {
		downloadToFile(url, sslCertificate, outFile, 8192);
	}

	public final void downloadToFile(String url, String sslCertificate, final File outFile, final int bufferSize) throws ZLNetworkException {
		perform(new ZLNetworkRequest(url, sslCertificate, null) {
			public void handleStream(InputStream inputStream, int length) throws IOException, ZLNetworkException {
				OutputStream outStream = new FileOutputStream(outFile);
				try {
					final byte[] buffer = new byte[bufferSize];
					while (true) {
						final int size = inputStream.read(buffer);
						if (size <= 0) {
							break;
						}
						outStream.write(buffer, 0, size);
					}
				} finally {
					outStream.close();
				}
			}
		});
	}
}
