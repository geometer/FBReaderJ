/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.GZIPInputStream;

import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;

import org.fbreader.util.ComparisonUtil;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

public class ZLNetworkManager {
	public static interface CookieStore extends org.apache.http.client.CookieStore {
		void clearDomain(String domain);
		void reset();
	}

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
				ComparisonUtil.equal(myScope.getHost(), scope.getHost()) &&
				ComparisonUtil.equal(myScope.getScheme(), scope.getScheme()) &&
				ComparisonUtil.equal(myScope.getRealm(), scope.getRealm());
		}

		public int hashCode() {
			if (myScope == null) {
				return 0;
			}
			return
				myScope.getPort() +
				ComparisonUtil.hashCode(myScope.getHost()) +
				ComparisonUtil.hashCode(myScope.getScheme()) +
				ComparisonUtil.hashCode(myScope.getRealm());
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

	static interface BearerAuthenticator {
		Map<String,String> authenticate(URI uri, String realm, Map<String,String> params);

		String getAccountName(String host, String realm);
		void setAccountName(String host, String realm, String accountName);
	}

	volatile CredentialsCreator myCredentialsCreator;

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
				ComparisonUtil.equal(Domain, k.Domain) &&
				ComparisonUtil.equal(Path, k.Path) &&
				ComparisonUtil.equal(Name, k.Name);
		}

		@Override
		public int hashCode() {
			return
				ComparisonUtil.hashCode(Domain) +
				ComparisonUtil.hashCode(Path) +
				ComparisonUtil.hashCode(Name);
		}
	};

	final CookieStore CookieStore = new CookieStore() {
		private volatile Map<Key,Cookie> myCookies;

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

		public synchronized void clearDomain(String domain) {
			myCookies = null;

			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.removeForDomain(domain);
			}
		}

		public synchronized void reset() {
			myCookies = null;
		}

		public synchronized List<Cookie> getCookies() {
			if (myCookies == null) {
				myCookies = Collections.synchronizedMap(new HashMap<Key,Cookie>());
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

	void perform(ZLNetworkRequest request, BearerAuthenticator authenticator, int socketTimeout, int connectionTimeout) throws ZLNetworkException {
		boolean success = false;
		DefaultHttpClient httpClient = null;
		HttpEntity entity = null;
		try {
			final HttpContext httpContext = new BasicHttpContext();
			httpContext.setAttribute(ClientContext.COOKIE_STORE, CookieStore);

			request.doBefore();
			final HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(params, socketTimeout);
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
			httpClient = new DefaultHttpClient(params) {
				protected AuthenticationHandler createTargetAuthenticationHandler() {
					final AuthenticationHandler base = super.createTargetAuthenticationHandler();
					return new AuthenticationHandler() {
						public Map<String,Header> getChallenges(HttpResponse response, HttpContext context) throws MalformedChallengeException {
							return base.getChallenges(response, context);
						}

						public boolean isAuthenticationRequested(HttpResponse response, HttpContext context) {
							return base.isAuthenticationRequested(response, context);
						}

						public AuthScheme selectScheme(Map<String,Header> challenges, HttpResponse response, HttpContext context) throws AuthenticationException {
							try {
								return base.selectScheme(challenges, response, context);
							} catch (AuthenticationException e) {
								final Header bearerHeader = challenges.get("bearer");
								if (bearerHeader != null) {
									String realm = null;
									for (HeaderElement elt : bearerHeader.getElements()) {
										final String name = elt.getName();
										if (name == null) {
											continue;
										}
										if ("realm".equals(name) || name.endsWith(" realm")) {
											realm = elt.getValue();
											break;
										}
									}
									throw new BearerAuthenticationException(realm, response.getEntity());
								}
								throw e;
							}
						}
					};
				}
			};
			final HttpRequestBase httpRequest;
			if (request instanceof ZLNetworkRequest.Get) {
				httpRequest = new HttpGet(request.URL);
			} else if (request instanceof ZLNetworkRequest.PostWithBody) {
				httpRequest = new HttpPost(request.URL);
				((HttpPost)httpRequest).setEntity(new StringEntity(((ZLNetworkRequest.PostWithBody)request).Body, "utf-8"));
				/*
					httpConnection.setRequestProperty(
						"Content-Length",
						Integer.toString(request.Body.getBytes().length)
					);
				*/
			} else if (request instanceof ZLNetworkRequest.PostWithMap) {
				final Map<String,String> parameters =
					((ZLNetworkRequest.PostWithMap)request).PostParameters;
				httpRequest = new HttpPost(request.URL);
				final List<BasicNameValuePair> list =
					new ArrayList<BasicNameValuePair>(parameters.size());
				for (Map.Entry<String,String> entry : parameters.entrySet()) {
					list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				((HttpPost)httpRequest).setEntity(new UrlEncodedFormEntity(list, "utf-8"));
			} else if (request instanceof ZLNetworkRequest.FileUpload) {
				final ZLNetworkRequest.FileUpload uploadRequest = (ZLNetworkRequest.FileUpload)request;
				final File file = ((ZLNetworkRequest.FileUpload)request).File;
				httpRequest = new HttpPost(request.URL);
				final MultipartEntity data = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE,
					null,
					Charset.forName("utf-8")
				);
				data.addPart("file", new FileBody(uploadRequest.File));
				((HttpPost)httpRequest).setEntity(data);
			} else {
				throw new ZLNetworkException("Unknown request type");
			}
			httpRequest.setHeader("User-Agent", ZLNetworkUtil.getUserAgent());
			if (!request.isQuiet()) {
				httpRequest.setHeader("X-Accept-Auto-Login", "True");
			}
			httpRequest.setHeader("Accept-Encoding", "gzip");
			httpRequest.setHeader("Accept-Language", ZLResource.getLanguage());
			for (Map.Entry<String,String> header : request.Headers.entrySet()) {
				httpRequest.setHeader(header.getKey(), header.getValue());
			}
			httpClient.setCredentialsProvider(new MyCredentialsProvider(httpRequest, request.isQuiet()));
			final HttpResponse response = execute(httpClient, httpRequest, httpContext, authenticator);
			entity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				final AuthState state = (AuthState)httpContext.getAttribute(ClientContext.TARGET_AUTH_STATE);
				if (state != null) {
					final AuthScopeKey key = new AuthScopeKey(state.getAuthScope());
					if (myCredentialsCreator.removeCredentials(key)) {
						entity = null;
					}
				}
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
					throw new ZLNetworkAuthenticationException();
				} else {
					throw new ZLNetworkException(response.getStatusLine().toString());
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
			throw ZLNetworkException.forCode(code, ZLNetworkUtil.hostFromUrl(request.URL), e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZLNetworkException(e.getMessage(), e);
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

	private HttpResponse execute(DefaultHttpClient client, HttpRequestBase request, HttpContext context, BearerAuthenticator authenticator) throws IOException, ZLNetworkException {
		try {
			return client.execute(request, context);
		} catch (BearerAuthenticationException e) {
			final Map<String,String> response =
				authenticator.authenticate(request.getURI(), e.Realm, e.Params);
			final String error = response.get("error");
			if (error != null) {
				throw new ZLNetworkAuthenticationException(error, e);
			}
			authenticator.setAccountName(request.getURI().getHost(), e.Realm, response.get("user"));
			return client.execute(request, context);
		}
	}
}
