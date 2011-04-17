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
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;

import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLResourceFile;

public class ZLNetworkManager {
	private static ZLNetworkManager ourManager;

	public static ZLNetworkManager Instance() {
		if (ourManager == null) {
			ourManager = new ZLNetworkManager();
		}
		return ourManager;
	}

	private static void collectStandardTrustManagers(List<TrustManager> collection) {
		try {
			final TrustManagerFactory factory = TrustManagerFactory.getInstance("X509");
			factory.init((KeyStore)null);
			final TrustManager[] managers = factory.getTrustManagers();
			if (managers != null) {
				for (TrustManager tm: managers) {
					collection.add(tm);
				}
			}
		} catch (NoSuchAlgorithmException e) {
		} catch (KeyStoreException e) {
		}
	}

	private static TrustManager createZLTrustManager(String certificate) throws ZLNetworkException {
		final InputStream stream;
		try {
			final ZLResourceFile file = ZLResourceFile.createResourceFile(certificate);
			stream = file.getInputStream();
		} catch (IOException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_BAD_FILE, certificate, ex);
		}
		try {
			return new ZLX509TrustManager(stream);
		} catch (CertificateExpiredException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_EXPIRED, certificate, ex);
		} catch (CertificateNotYetValidException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_NOT_YET_VALID, certificate, ex);
		} catch (CertificateException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_BAD_FILE, certificate, ex);
		} finally {
			try {
				stream.close();
			} catch (IOException ex) {
			}
		}
	}

	private void setCommonHTTPOptions(ZLNetworkRequest request, HttpURLConnection httpConnection) throws ZLNetworkException {
		httpConnection.setInstanceFollowRedirects(true);
		httpConnection.setConnectTimeout(15000); // FIXME: hardcoded timeout value!!!
		httpConnection.setReadTimeout(30000); // FIXME: hardcoded timeout value!!!
		//httpConnection.setRequestProperty("Connection", "Close");
		httpConnection.setRequestProperty("User-Agent", ZLNetworkUtil.getUserAgent());
		httpConnection.setRequestProperty("Accept-Language", Locale.getDefault().getLanguage());
		httpConnection.setAllowUserInteraction(true);
		if (httpConnection instanceof HttpsURLConnection) {
			HttpsURLConnection httpsConnection = (HttpsURLConnection)httpConnection;

			final ArrayList<TrustManager> managers = new ArrayList<TrustManager>();
			if (request.SSLCertificate != null) {
				managers.add(createZLTrustManager(request.SSLCertificate));
			}
			collectStandardTrustManagers(managers);

			try {
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null, managers.toArray(new TrustManager[]{}), null);
				httpsConnection.setSSLSocketFactory(context.getSocketFactory());
			} catch (GeneralSecurityException ex) {
				throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_SUBSYSTEM, ex);
			}
		}
	}

	public void perform(ZLNetworkRequest request) throws ZLNetworkException {
		boolean success = false;
		try {
			request.doBefore();
			HttpURLConnection httpConnection = null;
			int response = -1;
			final int retryLimit = 3;
			for (int retryCounter = 0; retryCounter < retryLimit && (response == -1 || response == 302); ++retryCounter) {
				final URLConnection connection = new URL(request.URL).openConnection();
				if (!(connection instanceof HttpURLConnection)) {
					throw new ZLNetworkException(ZLNetworkException.ERROR_UNSUPPORTED_PROTOCOL);
				}
				httpConnection = (HttpURLConnection)connection;
				setCommonHTTPOptions(request, httpConnection);
				if (request.PostData != null) {
					httpConnection.setRequestMethod("POST");
					httpConnection.setRequestProperty(
						"Content-Length",
						Integer.toString(request.PostData.getBytes().length)
					);
					httpConnection.setRequestProperty(
						"Content-Type", 
						"application/x-www-form-urlencoded"
					);
					httpConnection.setUseCaches (false);
					httpConnection.setDoInput(true);
					httpConnection.setDoOutput(true);
					final OutputStreamWriter writer =
						new OutputStreamWriter(httpConnection.getOutputStream());
					try {
						writer.write(request.PostData);
						writer.flush();
					} finally {
						writer.close();
					}
				} else {
					httpConnection.connect();
				}
				response = httpConnection.getResponseCode();
				if (response == 302) {
					request.URL = httpConnection.getHeaderField("Location");
				}
			}

			InputStream stream = null;
			if (response == HttpURLConnection.HTTP_OK) {
				stream = httpConnection.getInputStream();
			} else if (500 <= response && response < 600) {
				stream = httpConnection.getErrorStream();
			}

			if (stream != null) {
				try {
					if ("gzip".equalsIgnoreCase(httpConnection.getContentEncoding())) {
						stream = new GZIPInputStream(stream);
					}
					request.handleStream(httpConnection, stream);
				} finally {
					stream.close();
				}
				success = true;
			} else {
				if (response == HttpURLConnection.HTTP_UNAUTHORIZED) {
					throw new ZLNetworkException(ZLNetworkException.ERROR_AUTHENTICATION_FAILED);
				} else if (response >= 400) {
					throw new ZLNetworkException(true, httpConnection.getResponseMessage());
				} else {
					throw new ZLNetworkException(ZLNetworkException.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(request.URL));
				}
			}
		} catch (SSLHandshakeException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_CONNECT, ZLNetworkUtil.hostFromUrl(request.URL), ex);
		} catch (SSLKeyException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_BAD_KEY, ZLNetworkUtil.hostFromUrl(request.URL), ex);
		} catch (SSLPeerUnverifiedException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_PEER_UNVERIFIED, ZLNetworkUtil.hostFromUrl(request.URL), ex);
		} catch (SSLProtocolException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_PROTOCOL_ERROR, ex);
		} catch (SSLException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_SSL_SUBSYSTEM, ex);
		} catch (ConnectException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_CONNECTION_REFUSED, ZLNetworkUtil.hostFromUrl(request.URL), ex);
		} catch (NoRouteToHostException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_HOST_CANNOT_BE_REACHED, ZLNetworkUtil.hostFromUrl(request.URL), ex);
		} catch (UnknownHostException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_RESOLVE_HOST, ZLNetworkUtil.hostFromUrl(request.URL), ex);
		} catch (MalformedURLException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_INVALID_URL, ex);
		} catch (SocketTimeoutException ex) {
			throw new ZLNetworkException(ZLNetworkException.ERROR_TIMEOUT, ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new ZLNetworkException(ZLNetworkException.ERROR_SOMETHING_WRONG, ZLNetworkUtil.hostFromUrl(request.URL), ex);
		} finally {
			request.doAfter(success);
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
			public void handleStream(URLConnection connection, InputStream inputStream) throws IOException, ZLNetworkException {
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
