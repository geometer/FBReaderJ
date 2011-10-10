package org.geometerplus.fbreader.network.authentication.fbreaderorg;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.NetworkException;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.opds.OPDSNetworkLink;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.DecoratedBookUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.zlibrary.core.money.Money;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

import android.os.Bundle;
import android.os.Debug;

public class FBReaderOrgAuthenticationManager extends NetworkAuthenticationManager {
	
    public static final String API_URL = "https://data.fbreader.org/sync/auth/sync_api_interface.php";
	
	private ZLStringOption mySidOption;
	private ZLStringOption myUserIdOption;
	private ZLStringOption myFBIdOption;
	private boolean myFullyInitialized;
	
	public FBReaderOrgAuthenticationManager(OPDSNetworkLink link) {
		super(link);
		mySidOption = new ZLStringOption(link.getSiteName(), "sid", "");
		myUserIdOption = new ZLStringOption(link.getSiteName(), "userId", "");
		myFBIdOption = new ZLStringOption(link.getSiteName(), "fb_id", "");
		String uid = myUserIdOption.getValue();
		String sid = mySidOption.getValue();
		String fbid = myFBIdOption.getValue();
		String username = UserNameOption.getValue();
		myFullyInitialized = !"".equals(sid) && !"".equals(uid) && !"".equals(fbid) && !"".equals(username);
	}
	
	private synchronized void logOut(boolean full) {
		initUser(UserNameOption.getValue(), "", "");
	}
	
	public synchronized void initUser(String userName, String sid, String userId) {
		UserNameOption.setValue(userName);
		mySidOption.setValue(sid);
		myUserIdOption.setValue(userId);
		myFBIdOption.setValue("11223344");
		myFullyInitialized = !"".equals(userName) && !"".equals(sid) && !"".equals(userId);
	}

	@Override
	public void authorise(String password) throws ZLNetworkException {
		
		Bundle result = null;
		ZLNetworkException exception = null;
		
		try {
		result = ServerInterface.ourAuthLogin(
				UserNameOption.getValue(), 
				password,
				myFBIdOption.getValue()
				);
		} catch (ZLNetworkException e) {
			exception = e;
		}
		
		synchronized (this) {
			if (exception != null) {
				logOut(false);
				throw exception;
			}
			boolean success = result.getBoolean(ServerInterface.SUCCESS_KEY);
			if (success) {
				initUser(
						UserNameOption.getValue(), 
						result.getString(ServerInterface.SIG_KEY), 
						result.getString(ServerInterface.USER_ID_KEY)
						);
				//Link.libraryItem().
			} else {
				logOut(false);
				throw new ZLNetworkException(ZLNetworkException.ERROR_AUTHENTICATION_FAILED);
			}
		}
	}

	@Override
	public BookUrlInfo downloadReference(NetworkBookItem book) {
		final String sid;
		synchronized (this) {
			sid = mySidOption.getValue();
		}
		if (sid.length() == 0) {
			return null;
		}
		BookUrlInfo reference = book.reference(UrlInfo.Type.Book);
		if (reference == null) {
			return null;
		}
		
		BookUrlRequest request = new BookUrlRequest(reference.Url);
		try {
			ZLNetworkManager.Instance().perform(request);
		} catch (ZLNetworkException e) {
			return null;
		}
		return new DecoratedBookUrlInfo(reference, request.result);
	}

	@Override
	public boolean isAuthorised(boolean useNetwork) throws ZLNetworkException {
		synchronized (this) {
			boolean authState =
				UserNameOption.getValue().length() != 0 &&
				mySidOption.getValue().length() != 0;

			if (myFullyInitialized || !useNetwork) {
				return authState;
			}

			if (!authState) {
				logOut(false);
				return false;
			}
		}
		return false;
	}

	@Override
	public void logOut() {
		logOut(true);
	}

	@Override
	public void refreshAccountInformation() throws ZLNetworkException {
		
	}
	
	@Override
	public Money currentAccount() {
		if (myFullyInitialized) {
			return new Money(new BigDecimal(100500), "RUB");
		} else {
			return null;
		}
	}
	
	@Override
	public void purchaseBook(NetworkBookItem book) throws ZLNetworkException {
		String url = book.getAllInfos(UrlInfo.Type.BookBuy).get(0).Url;
		
		ZLNetworkRequest request = new ZLNetworkRequest(url) {
			@Override
			public void handleStream(InputStream inputStream, int length)
					throws IOException, ZLNetworkException {
				byte[] b = new byte[4096];
				inputStream.read(b);
				String result = (new String(b)).trim();
				if (!"ok".equals(result)){
					throw new ZLNetworkException(NetworkException.ERROR_BOOK_NOT_PURCHASED);
				}
			}
		};
		request.addPostParameter("account", UserNameOption.getValue());
		ZLNetworkManager.Instance().perform(request);
		return;
	};
	
	public UrlInfo addAuthParametersToURL(UrlInfo urlInfo){
		String url = urlInfo.Url;
		url = ZLNetworkUtil.appendParameter(url, "account", UserNameOption.getValue());
		url = ZLNetworkUtil.appendParameter(url, "sid", mySidOption.getValue());
		return new UrlInfo(urlInfo.InfoType, url);
	}
	
	class BookUrlRequest extends ZLNetworkRequest {
		
		public String result = null;
		
		public BookUrlRequest(String url) {
			super(url);
		}
		
		@Override
		public void handleStream(InputStream inputStream, int length)
				throws IOException, ZLNetworkException {
			byte[] b = new byte[4096];
			inputStream.read(b);
			String result = (new String(b)).trim();
			if ("ok".equals(result.substring(0,2))){
				result = result.substring(3);
			}
		}
	};
	
}
