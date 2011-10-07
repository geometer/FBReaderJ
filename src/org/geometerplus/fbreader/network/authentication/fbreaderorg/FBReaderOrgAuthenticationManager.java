package org.geometerplus.fbreader.network.authentication.fbreaderorg;


import org.geometerplus.fbreader.network.NetworkBookItem;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.fbreader.network.opds.OPDSNetworkLink;
import org.geometerplus.fbreader.network.urlInfo.BookUrlInfo;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.options.ZLStringOption;

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
		Debug.waitForDebugger();
		mySidOption = new ZLStringOption(link.getSiteName(), "sid", "");
		myUserIdOption = new ZLStringOption(link.getSiteName(), "userId", "");
		myFBIdOption = new ZLStringOption(link.getSiteName(), "fb_id", "");
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
		
		Bundle result = null;
		ZLNetworkException exception = null;
		
		try {
		result = ServerInterface.ourAuthLogin(
				UserNameOption.getValue(), 
				password
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
			}
			else {
				logOut(false);
			}
		}
	}

	@Override
	public BookUrlInfo downloadReference(NetworkBookItem book) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAuthorised(boolean useNetwork) throws ZLNetworkException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logOut() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshAccountInformation() throws ZLNetworkException {
		// TODO Auto-generated method stub
		
	}
	
	

}
