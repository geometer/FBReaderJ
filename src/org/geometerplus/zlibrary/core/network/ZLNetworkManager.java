/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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
import org.apache.http.auth.*;
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

import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

public class ZLNetworkManager {
	private static ZLNetworkManager ourManager;

	public static ZLNetworkManager Instance() {
		if (ourManager == null) {
			ourManager = new ZLNetworkManager();
		}
		return ourManager;
	}

	private static class AuthScopeKey {
		private final AuthScope myScope;

		public AuthScopeKey(AuthScope scope) {
			myScope = scope;
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof AuthScopeKey)) {
				return false;
			}

			final AuthScope scope = ((AuthScopeKey)obj).myScope;
			if (myScope == null) {
				return scope == null;
			}
			if (scope == null) {
				return false;
			}
			return
				myScope.getPort() == scope.getPort() &&
				MiscUtil.equals(myScope.getHost(), scope.getHost()) &&
				MiscUtil.equals(myScope.getScheme(), scope.getScheme()) &&
				MiscUtil.equals(myScope.getRealm(), scope.getRealm());
		}

		public int hashCode() {
			if (myScope == null) {
				return 0;
			}
			return
				myScope.getPort() +
				MiscUtil.hashCode(myScope.getHost()) +
				MiscUtil.hashCode(myScope.getScheme()) +
				MiscUtil.hashCode(myScope.getRealm());
		}
	}

	public static abstract class CredentialsCreator {
		final private HashMap<AuthScopeKey,Credentials> myCredentialsMap =
			new HashMap<AuthScopeKey,Credentials>();

		private volatile String myUsername;
		private volatile String myPassword;

		synchronized public void setCredentials(String username, String password) {
			myUsername = username;
			myPassword = password;
			release();
		}

		synchronized public void release() {
			notifyAll();
		}

		public Credentials createCredentials(String scheme, AuthScope scope, boolean quietly) {
			final String authScheme = scope.getScheme();
			if (!"basic".equalsIgnoreCase(authScheme) &&
				!"digest".equalsIgnoreCase(authScheme)) {
				return null;
			}

			final AuthScopeKey key = new AuthScopeKey(scope);
			Credentials creds = myCredentialsMap.get(key);
			if (creds != null || quietly) {
				return creds;
			}

			final String host = scope.getHost();
			final String area = scope.getRealm();
			final ZLStringOption usernameOption =
				new ZLStringOption("username", host + ":" + area, "");
			if (!quietly) {
				startAuthenticationDialog(host, area, scheme, usernameOption.getValue());
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}

			if (myUsername != null && myPassword != null) {
				usernameOption.setValue(myUsername);
				creds = new UsernamePasswordCredentials(myUsername, myPassword);
				myCredentialsMap.put(key, creds);
			}
			myUsername = null;
			myPassword = null;
			return creds;
		}

		public boolean removeCredentials(AuthScopeKey key) {
			return myCredentialsMap.remove(key) != null;
		}

		abstract protected void startAuthenticationDialog(String host, String area, String scheme, String username);
	}

	private volatile CredentialsCreator myCredentialsCreator;

	private class MyCredentialsProvider extends BasicCredentialsProvider {
		private final HttpUriRequest myRequest;
		private final boolean myQuietly;

		MyCredentialsProvider(HttpUriRequest request, boolean quietly) {
			myRequest = request;
			myQuietly = quietly;
		}

		@Override
		public Credentials getCredentials(AuthScope authscope) {
			final Credentials c = super.getCredentials(authscope);
			if (c != null) {
				return c;
			}
			if (myCredentialsCreator != null) {
				return myCredentialsCreator.createCredentials(myRequest.getURI().getScheme(), authscope, myQuietly);
			}
			return null;
		}
	};

	private static class Key {
		final String Domain;
		final String Path;
		final String Name;

		Key(Cookie c) {
			Domain = c.getDomain();
			Path = c.getPath();
			Name = c.getName();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Key)) {
				return false;
			}
			final Key k = (Key)o;
			return
				MiscUtil.equals(Domain, k.Domain) &&
				MiscUtil.equals(Path, k.Path) &&
				MiscUtil.equals(Name, k.Name);
		}

		@Override
		public int hashCode() {
			return
				MiscUtil.hashCode(Domain) +
				MiscUtil.hashCode(Path) +
				MiscUtil.hashCode(Name);
		}
	};

	private final CookieStore myCookieStore = new CookieStore() {
		private HashMap<Key,Cookie> myCookies;

		public synchronized void addCookie(Cookie cookie) {
			if (myCookies == null) {
				getCookies();
			}
			myCookies.put(new Key(cookie), cookie);
			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.saveCookies(Collections.singletonList(cookie));
			}
		}

		public synchronized void clear() {
			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.removeAll();
			}
			if (myCookies != null) {
				myCookies.clear();
			}
		}

		public synchronized boolean clearExpired(Date date) {
			myCookies = null;

			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.removeObsolete(date);
				// TODO: detect if any Cookie has been removed
				return true;
			}
			return false;
		}

		public synchronized List<Cookie> getCookies() {
			if (myCookies == null) {
				myCookies = new HashMap<Key,Cookie>();
				final CookieDatabase db = CookieDatabase.getInstance();
				if (db != null) {
					for (Cookie c : db.loadCookies()) {
						myCookies.put(new Key(c), c);
					}
				}
			}
			return new ArrayList<Cookie>(myCookies.values());
		}
	};

	/*private void setCommonHTTPOptions(HttpMessage request) throws ZLNetworkException {
		httpConnection.setInstanceFollowRedirects(true);
		httpConnection.setAllowUserInteraction(true);
	}*/

	public void setCredentialsCreator(CredentialsCreator creator) {
		myCredentialsCreator = creator;
	}

	public CredentialsCreator getCredentialsCreator() {
		return myCredentialsCreator;
	}

	public void perform(ZLNetworkRequest request) throws ZLNetworkException {
		perform(request, 30000, 15000);
	}

	private void perform(ZLNetworkRequest request, int socketTimeout, int connectionTimeout) throws ZLNetworkException {
		boolean success = false;
		DefaultHttpClient httpClient = null;
		HttpEntity entity = null;
		try {
			final HttpContext httpContext = new BasicHttpContext();
			httpContext.setAttribute(ClientContext.COOKIE_STORE, myCookieStore);

			request.doBefore();
			final HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(params, socketTimeout);
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
			httpClient = new DefaultHttpClient(params);
			final HttpRequestBase httpRequest;
			if (request.PostData != null) {
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
			for (Map.Entry<String,String> header : request.Headers.entrySet()) {
				httpRequest.setHeader(header.getKey(), header.getValue());
			}	
			httpClient.setCredentialsProvider(new MyCredentialsProvider(httpRequest, request.isQuiet()));
			HttpResponse response = null;
			IOException lastException = null;
			for (int retryCounter = 0; retryCounter < 3 && entity == null; ++retryCounter) {
				try {
					response = httpClient.execute(httpRequest, httpContext);
					entity = response.getEntity();
					lastException = null;
					if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
						final AuthState state = (AuthState)httpContext.getAttribute(ClientContext.TARGET_AUTH_STATE);
						if (state != null) {
							final AuthScopeKey key = new AuthScopeKey(state.getAuthScope());
							if (myCredentialsCreator.removeCredentials(key)) {
								entity = null;
							}
						}
					}
				} catch (IOException e) {
					lastException = e;
				}
			}
			if (lastException != null) {
				throw lastException;
			}
			final int responseCode = response.getStatusLine().getStatusCode();

			InputStream stream = null;
			if (entity != null &&
				(responseCode == HttpURLConnection.HTTP_OK ||
				 responseCode == HttpURLConnection.HTTP_PARTIAL)) {
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
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZLNetworkException(true, e.getMessage(), e);
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

	public void perform(List<? extends ZLNetworkRequest> requests) throws ZLNetworkException {
		if (requests.size() == 0) {
			return;
		}
		if (requests.size() == 1) {
			perform(requests.get(0));
			return;
		}
		HashSet<String> errors = new HashSet<String>();
		// TODO: implement concurrent execution !!!
		for (ZLNetworkRequest r : requests) {
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
		downloadToFile(url, outFile, 8192);
	}

	public final void downloadToFile(String url, final File outFile, final int bufferSize) throws ZLNetworkException {
		perform(new ZLNetworkRequest(url) {
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
		}, 0, 0);
	}
}
