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

package org.geometerplus.fbreader.network.authentication.litres;

import java.util.*;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.*;

public class LitResAuthenticationManager extends NetworkAuthenticationManager {
	private boolean mySidChecked;

	private final ZLStringOption mySidUserNameOption;
	private final ZLStringOption mySidOption;
	private final ZLStringOption myUserIdOption;

	private String myInitializedDataSid;
	private String myAccount;
	private final HashMap<String, NetworkLibraryItem> myPurchasedBooks = new HashMap<String, NetworkLibraryItem>();

	public LitResAuthenticationManager(INetworkLink link, String sslCertificate) {
		super(link, sslCertificate);
		mySidUserNameOption = new ZLStringOption(link.getSiteName(), "sidUserName", "");
		mySidOption = new ZLStringOption(link.getSiteName(), "sid", "");
		myUserIdOption = new ZLStringOption(link.getSiteName(), "userId", "");
	}

	@Override
	public synchronized void initUser(String userName, String sid) throws ZLNetworkException {
		mySidChecked = false;
		mySidUserNameOption.setValue(userName);
		mySidOption.setValue(sid);
		if (!isAuthorised(true)) {
			throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
		}
	}

	private synchronized void initUser(String userName, String sid, String userId) {
		mySidChecked = true;
		mySidUserNameOption.setValue(userName);
		mySidOption.setValue(sid);
		myUserIdOption.setValue(userId);
	}

	@Override
	public synchronized void logOut() {
		initUser("", "", "");
	}

	@Override
	public boolean isAuthorised(boolean useNetwork /* = true */) throws ZLNetworkException {
		final String sid;
		synchronized (this) {
			boolean authState =
				mySidUserNameOption.getValue().length() != 0 &&
				mySidOption.getValue().length() != 0;

			if (mySidChecked || !useNetwork) {
				return authState;
			}

			if (!authState) {
				logOut();
				return false;
			}
			sid = mySidOption.getValue();
		}

		String url = Link.getLink(INetworkLink.URL_SIGN_IN);
		if (url == null) {
			throw new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION);
		}
		url = ZLNetworkUtil.appendParameter(url, "sid", sid);

		final LitResLoginXMLReader xmlReader = new LitResLoginXMLReader(Link.getSiteName());

		ZLNetworkException exception = null;
		try {
			ZLNetworkManager.Instance().perform(new LitResNetworkRequest(url, SSLCertificate, xmlReader));
		} catch (ZLNetworkException e) {
			exception = e;
		}

		synchronized (this) {
			if (exception != null) {
				if (NetworkException.ERROR_AUTHENTICATION_FAILED.equals(exception.getCode())) {
					throw exception;
				}
				logOut();
				return false;
			}
			initUser(UserNameOption.getValue(), xmlReader.Sid, xmlReader.UserId);
			return true;
		}
	}

	@Override
	public void authorise(String password) throws ZLNetworkException {
		String url = Link.getLink(INetworkLink.URL_SIGN_IN);
		if (url == null) {
			throw new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION);
		}
		final String login;
		synchronized (this) {
			login = UserNameOption.getValue();
		}
		url = ZLNetworkUtil.appendParameter(url, "login", login);
		url = ZLNetworkUtil.appendParameter(url, "pwd", password);

		final LitResLoginXMLReader xmlReader = new LitResLoginXMLReader(Link.getSiteName());

		ZLNetworkException exception = null;
		try {
			ZLNetworkManager.Instance().perform(new LitResNetworkRequest(url, SSLCertificate, xmlReader));
		} catch (ZLNetworkException e) {
			exception = e;
		}

		synchronized (this) {
			mySidChecked = true;
			if (exception != null) {
				logOut();
				throw exception;
			}
			initUser(UserNameOption.getValue(), xmlReader.Sid, xmlReader.UserId);
		}
	}

	@Override
	public BookReference downloadReference(NetworkBookItem book) {
		final String sid;
		synchronized (this) {
			sid = mySidOption.getValue();
		}
		if (sid.length() == 0) {
			return null;
		}
		BookReference reference = book.reference(BookReference.Type.DOWNLOAD_FULL_CONDITIONAL);
		if (reference == null) {
			return null;
		}
		String url = reference.URL;
		url = ZLNetworkUtil.appendParameter(url, "sid", sid);
		return new DecoratedBookReference(reference, url);
	}


	@Override
	public String currentUserName() {
		final String value;
		synchronized (this) {
			value = mySidUserNameOption.getValue();
		}
		if (value.length() == 0) {
			return null;
		}
		return value;
	}


	@Override
	public synchronized boolean needPurchase(NetworkBookItem book) {
		return !myPurchasedBooks.containsKey(book.Id);
	}

	@Override
	public void purchaseBook(NetworkBookItem book) throws ZLNetworkException {
		final String sid;
		synchronized (this) {
			sid = mySidOption.getValue();
		}
		if (sid.length() == 0) {
			throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
		}

		BookReference reference = book.reference(BookReference.Type.BUY);
		if (reference == null) {
			throw new ZLNetworkException(NetworkException.ERROR_BOOK_NOT_PURCHASED); // TODO: more correct error message???
		}
		String query = reference.URL;
		query = ZLNetworkUtil.appendParameter(query, "sid", sid);

		final LitResPurchaseXMLReader xmlReader = new LitResPurchaseXMLReader(Link.getSiteName());

		ZLNetworkException exception = null;
		try {
			ZLNetworkManager.Instance().perform(new LitResNetworkRequest(query, SSLCertificate, xmlReader));
		} catch (ZLNetworkException e) {
			exception = e;
		}

		synchronized (this) {
			if (xmlReader.Account != null) {
				myAccount = BuyBookReference.price(xmlReader.Account, "RUB");
			}
			if (exception != null) {
				final String code = exception.getCode();
				if (NetworkException.ERROR_AUTHENTICATION_FAILED.equals(code)) {
					logOut();
				} else if (NetworkException.ERROR_PURCHASE_ALREADY_PURCHASED.equals(code)) {
					myPurchasedBooks.put(book.Id, book);
				}
				throw exception;
			}
			if (xmlReader.BookId == null || !xmlReader.BookId.equals(book.Id)) {
				throw new ZLNetworkException(NetworkException.ERROR_SOMETHING_WRONG, Link.getSiteName());
			}
			myPurchasedBooks.put(book.Id, book);
		}
	}

	@Override
	public String refillAccountLink() {
		final String sid;
		synchronized (this) {
			sid = mySidOption.getValue();
		}
		if (sid.length() == 0) {
			return null;
		}
		final String url = Link.getLink(INetworkLink.URL_REFILL_ACCOUNT);
		if (url == null) {
			return null;
		}
		return ZLNetworkUtil.appendParameter(url, "sid", sid);
	}

	@Override
	public synchronized String currentAccount() {
		return myAccount;
	}

	void reloadPurchasedBooks() throws ZLNetworkException {
		final LitResNetworkRequest networkRequest;
		synchronized (this) {
			final String sid = mySidOption.getValue();
			if (sid.length() == 0) {
				throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
			}
			if (!sid.equals(myInitializedDataSid)) {
				logOut();
				throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
			}
			networkRequest = loadPurchasedBooks();
		}

		ZLNetworkException exception = null;
		try {
			ZLNetworkManager.Instance().perform(networkRequest);
		} catch (ZLNetworkException e) {
			exception = e;
		}

		synchronized (this) {
			if (exception != null) {
				//loadPurchasedBooksOnError();
				if (NetworkException.ERROR_AUTHENTICATION_FAILED.equals(exception.getCode())) {
					logOut();
				}
				throw exception;
			}
			loadPurchasedBooksOnSuccess(networkRequest);
		}
	}

	synchronized void collectPurchasedBooks(List<NetworkLibraryItem> list) {
		list.addAll(myPurchasedBooks.values());
	}


	@Override
	public synchronized boolean needsInitialization() {
		final String sid = mySidOption.getValue();
		if (sid.length() == 0) {
			return false;
		}
		return !sid.equals(myInitializedDataSid);
	}

	@Override
	public void initialize() throws ZLNetworkException {
		final String sid;
		final LitResNetworkRequest purchasedBooksRequest;
		final LitResNetworkRequest accountRequest;
		synchronized (this) {
			sid = mySidOption.getValue();
			if (sid.length() == 0) {
				throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
			}
			if (sid.equals(myInitializedDataSid)) {
				return;
			}

			purchasedBooksRequest = loadPurchasedBooks();
			accountRequest = loadAccount();
		}

		final LinkedList<ZLNetworkRequest> requests = new LinkedList<ZLNetworkRequest>();
		requests.add(purchasedBooksRequest);
		requests.add(accountRequest);

		ZLNetworkException exception = null;
		try {
			ZLNetworkManager.Instance().perform(requests);
		} catch (ZLNetworkException e) {
			exception = e;
		}

		synchronized (this) {
			if (exception != null) {
				myInitializedDataSid = null;
				loadPurchasedBooksOnError();
				loadAccountOnError();
				throw exception;
			}
			myInitializedDataSid = sid;
			loadPurchasedBooksOnSuccess(purchasedBooksRequest);
			loadAccountOnSuccess(accountRequest);
		}
	}

	private LitResNetworkRequest loadPurchasedBooks() {
		final String sid = mySidOption.getValue();

		String query = "pages/catalit_browser/";
		query = ZLNetworkUtil.appendParameter(query, "my", "1");
		query = ZLNetworkUtil.appendParameter(query, "sid", sid);

		return new LitResNetworkRequest(
			LitResUtil.url(Link, query),
			SSLCertificate,
			new LitResXMLReader(Link, new LinkedList<NetworkLibraryItem>())
		);
	}

	private void loadPurchasedBooksOnError() {
		myPurchasedBooks.clear();
	}

	private void loadPurchasedBooksOnSuccess(LitResNetworkRequest purchasedBooksRequest) {
		LitResXMLReader reader = (LitResXMLReader) purchasedBooksRequest.Reader;
		myPurchasedBooks.clear();
		for (NetworkLibraryItem item: reader.Books) {
			if (item instanceof NetworkBookItem) {
				NetworkBookItem book = (NetworkBookItem) item;
				myPurchasedBooks.put(book.Id, book);
			}
		}
	}

	private LitResNetworkRequest loadAccount() {
		final String sid = mySidOption.getValue();

		String query = "pages/purchase_book/";
		query = ZLNetworkUtil.appendParameter(query, "sid", sid);
		query = ZLNetworkUtil.appendParameter(query, "art", "0");

		return new LitResNetworkRequest(
			LitResUtil.url(Link, query),
			SSLCertificate,
			new LitResPurchaseXMLReader(Link.getSiteName())
		);
	}

	private void loadAccountOnError() {
		myAccount = null;
	}

	private void loadAccountOnSuccess(LitResNetworkRequest accountRequest) {
		LitResPurchaseXMLReader reader = (LitResPurchaseXMLReader) accountRequest.Reader;
		myAccount = BuyBookReference.price(reader.Account, "RUB");
	}

	@Override
	public boolean passwordRecoverySupported() {
		return true;
	}

	@Override
	public void recoverPassword(String email) throws ZLNetworkException {
		String url = Link.getLink(INetworkLink.URL_RECOVER_PASSWORD);
		if (url == null) {
			throw new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION);
		}
		url = ZLNetworkUtil.appendParameter(url, "mail", email);
		final LitResPasswordRecoveryXMLReader xmlReader =  new LitResPasswordRecoveryXMLReader(Link.getSiteName());
		ZLNetworkManager.Instance().perform(new LitResNetworkRequest(url, SSLCertificate, xmlReader));
	}

	@Override
	public Map<String,String> getSmsRefillingData() {
		final HashMap<String,String> map = new HashMap<String,String>();
		map.put("litres:userId", myUserIdOption.getValue());
		map.put("litres:sid", mySidOption.getValue());
		return map;
	}
}
