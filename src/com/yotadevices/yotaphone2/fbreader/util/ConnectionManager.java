package com.yotadevices.yotaphone2.fbreader.util;


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.util.Log;

public class ConnectionManager {
	public interface ConnectionListener {
		void onNetworkUp(int type);
		void onNetworkDown(int type);
	}

	private static volatile ConnectionManager self;
	private HttpClient httpClient;
	private static final String TAG = "ConnectionManager";

	private static int CELL_MODE = 0;
	private static int WIFI_MODE = 1;

	private int mode = CELL_MODE;
	private ConnectivityManager connectivity;

	//Collections.newSetFromMap only from android api 9.0 :(
	final WeakHashMap<ConnectionListener, Boolean> listeners = new WeakHashMap<ConnectionListener, Boolean>();
	final WeakHashMap<Context, Boolean> monitors = new WeakHashMap<Context, Boolean>();

	public HttpClient getHttpClient() {
		return httpClient;
	}

	public boolean connected() {
		return connected(false);
	}

	public boolean connected(boolean onlyWiFi) {
		boolean connected = false;
		mode = CELL_MODE;
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (NetworkInfo netinfo : info) {
					if (netinfo.getState() == NetworkInfo.State.CONNECTED) {
						connected = true;
						if (netinfo.getType() == WIFI_MODE) {
							mode = WIFI_MODE;
						}
						break;
					}
				}
			}
		}
		return onlyWiFi ? connected && mode == WIFI_MODE : connected;
	}

	public boolean isWiFi() {
		return mode == WIFI_MODE;
	}

	public void startNetworkMonitoring(Context context) {
		connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		context.registerReceiver(broadcastReceiver, intentFilter);
		monitors.put(context, Boolean.TRUE);
	}

	public void stopNetworkMonitoring(Context context) {
		if (monitors.containsKey(context)) {
			context.unregisterReceiver(broadcastReceiver);
			monitors.remove(context);
			connectivity = null;
		}
	}

	public void notifyMeOnConnectionChanges(ConnectionListener listener) {
		synchronized (listeners) {
			listeners.put(listener, Boolean.TRUE);
		}
	}

	public static ConnectionManager getInstance() {
		if (self == null) {
			synchronized (ConnectionManager.class) {
				if (self == null) {
					try {
						self = new ConnectionManager();
					}
					catch(Exception e) {
						Log.w(TAG, "Error creating connection manager "+e.toString());
					}
				}
			}
		}
		return self;
	}

	public void destroy() {
		synchronized (ConnectionManager.class) {
			synchronized (listeners) {
				listeners.clear();
			}
			self = null;
		}
	}

	private ConnectionManager() {
		httpClient = createDefaultHttpClient();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			Bundle extras = intent.getExtras();
			NetworkInfo info = (NetworkInfo) extras
					.getParcelable("networkInfo");

			State state = info.getState();
			Log.d("NetworkStateBroadcastReceiver", info.toString() + " "
					+ state.toString() + "Mode: " + info.getTypeName());

			if (state == State.CONNECTED) {
				ConnectionManager.this.onNetworkUp(info.getType());
			} else {
				ConnectionManager.this.onNetworkDown(info.getType());
			}
		}
	};

	private void onNetworkUp(int type) {
		synchronized (listeners) {
			Set<ConnectionListener> set = listeners.keySet();
			for (ConnectionListener listener : set) {
				listener.onNetworkUp(type);
			}
		}
	}

	private void onNetworkDown(int type) {
		synchronized (listeners) {
			Set<ConnectionListener> set = listeners.keySet();
			for(ConnectionListener listener : set) {
				listener.onNetworkDown(type);
			}
		}
	}

	public DefaultHttpClient createDefaultHttpClient() {
		return createDefaultHttpClient(true);
	}

	public DefaultHttpClient createDefaultHttpClient(boolean verifySSL) {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		params.setBooleanParameter("http.protocol.handle-redirects", true);

		HttpConnectionParams.setConnectionTimeout(params, 10000);
		HttpConnectionParams.setSoTimeout(params, 15000);

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http",
				PlainSocketFactory.getSocketFactory(), 80));
		if (verifySSL) {
			schReg.register(new Scheme("https",
					SSLSocketFactory.getSocketFactory(), 443));
		}
		else {
			try {
				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				trustStore.load(null, null);

				SSLSocketFactory sf = new UnsecuredSSLSocketFactory(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				schReg.register(new Scheme("https", sf, 443));
			}
			catch(Exception e) {
				e.printStackTrace(); // can not create https scheme, try default
				schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			}
		}
		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params,schReg);

		return new DefaultHttpClient(conMgr, params);
	}

	private class UnsecuredSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public UnsecuredSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
