package org.geometerplus.fbreader.network.authentication.fbreaderorg;


import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;

import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.NetworkException;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.opds.OPDSNetworkLink;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.zlibrary.core.money.Money;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

public class FBReaderOrgAuthenticationManager extends NetworkAuthenticationManager {
	
    public static final String API_URL = "https://data.fbreader.org/sync/auth/sync_api_interface.php";
	
	private ZLStringOption mySidOption;
	private ZLStringOption myUserIdOption;
	private ZLStringOption myFBIdOption;
	private boolean myFullyInitialized = false;
	
	public FBReaderOrgAuthenticationManager(OPDSNetworkLink link) {
		super(link);
		mySidOption = new ZLStringOption(link.getSiteName(), "sid", "");
		myUserIdOption = new ZLStringOption(link.getSiteName(), "userId", "");
		myFBIdOption = new ZLStringOption(link.getSiteName(), "fb_id", "");
		myUserIdOption.getValue();
		mySidOption.getValue();
		myFBIdOption.getValue();
		UserNameOption.getValue();
	}
	
	private synchronized void logOut(boolean full) {
		if (full) {
			initUser(UserNameOption.getValue(), "", "");
		} else {
			myFullyInitialized = false;
		}
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
		
		HashMap<String, String> result = null;
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
			String success = result.get(ServerInterface.SUCCESS_KEY);
			if ("true".equals(success)) {
				initUser(
						UserNameOption.getValue(),
						result.get(ServerInterface.SIG_KEY), 
						result.get(ServerInterface.USER_ID_KEY)
						);
			} else {
				logOut(false);
				throw new ZLNetworkException(ZLNetworkException.ERROR_AUTHENTICATION_FAILED);
			}
		}
	}
	
	@Override
	public boolean isAuthorised(boolean useNetwork) throws ZLNetworkException {
		String sid = null;
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
			sid = mySidOption.getValue();
		}
		myFullyInitialized = checkSidValidity(sid);
		return myFullyInitialized;
	}

	@Override
	public BookUrlInfo downloadReference(NetworkBookItem book) {
		return null;
	}

	@Override
	public void logOut() {
		logOut(true);
	}

	@Override
	public void refreshAccountInformation() throws ZLNetworkException {
		//
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
				byte[] b = new byte[8];
				inputStream.read(b, 0, 8);
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
	
	private boolean checkSidValidity(String sid) {
		String url = "https://data.fbreader.org/sync/test_catalog/api.php?method=check_sid";
		ZLNetworkUtil.appendParameter(url, "account", UserNameOption.getValue());
		ZLNetworkUtil.appendParameter(url, "sid", sid);
		
		SidValidityRequest request = new SidValidityRequest(url);
		try {
			ZLNetworkManager.Instance().perform(request);
			return request.result;
		}
		catch (ZLNetworkException e) {
			// do nothing
		}
		return false;
	}

	private class SidValidityRequest extends ZLNetworkRequest {
		
		public boolean result = false;
		
		public SidValidityRequest(String url) {
			super(url);
		}
		
		public void handleStream(InputStream inputStream, int length)
			throws IOException, ZLNetworkException {
			byte[] b = new byte[8];
			inputStream.read(b, 0, 8);
			String reply = (new String(b)).trim();
			if ("true".equals(reply)){
				result = true;
			}
		}
	}
}
