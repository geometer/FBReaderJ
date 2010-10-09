/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

	private ZLStringOption mySidUserNameOption;
	private ZLStringOption mySidOption;

	private String myInitializedDataSid;
	private String myAccount;
	private final HashMap<String, NetworkLibraryItem> myPurchasedBooks = new HashMap<String, NetworkLibraryItem>();


	public LitResAuthenticationManager(INetworkLink link, String sslCertificate) {
		super(link, sslCertificate);
		mySidUserNameOption = new ZLStringOption(link.getSiteName(), "sidUserName", "");
		mySidOption = new ZLStringOption(link.getSiteName(), "sid", "");
	}

	@Override
	public AuthenticationStatus isAuthorised(boolean useNetwork /* = true */) {
		final String sid;
		synchronized (this) {
			boolean authState =
				mySidUserNameOption.getValue().length() != 0 &&
				mySidOption.getValue().length() != 0;

			if (mySidChecked || !useNetwork) {
				return new AuthenticationStatus(authState);
			}

			if (!authState) {
				mySidChecked = true;
				mySidUserNameOption.setValue("");
				mySidOption.setValue("");
				return new AuthenticationStatus(false);
			}
			sid = mySidOption.getValue();
		}

		String url = Link.getLink(INetworkLink.URL_SIGN_IN);
		if (url == null) {
			return new AuthenticationStatus(new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION));
		}
		url = ZLNetworkUtil.appendParameter(url, "sid", sid);

		final LitResLoginXMLReader xmlReader = new LitResLoginXMLReader(Link.getSiteName());

		synchronized (this) {
			try {
				ZLNetworkManager.Instance().perform(new LitResNetworkRequest(url, SSLCertificate, xmlReader));
			} catch (ZLNetworkException e) {
				if (NetworkException.ERROR_AUTHENTICATION_FAILED.equals(e.getCode())) {
					return new AuthenticationStatus(e);
				}
				mySidChecked = true;
				mySidUserNameOption.setValue("");
				mySidOption.setValue("");
				return new AuthenticationStatus(false);
			}
			mySidChecked = true;
			mySidOption.setValue(xmlReader.Sid);
			return new AuthenticationStatus(true);
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

		synchronized (this) {
			try {
				ZLNetworkManager.Instance().perform(new LitResNetworkRequest(url, SSLCertificate, xmlReader));
			} catch (ZLNetworkException e) {
				mySidUserNameOption.setValue("");
				mySidOption.setValue("");
				throw e;
			} finally {
				mySidChecked = true;
			}
			mySidOption.setValue(xmlReader.Sid);
			mySidUserNameOption.setValue(UserNameOption.getValue());
		}
	}

	@Override
	public synchronized void logOut() {
		mySidChecked = true;
		mySidUserNameOption.setValue("");
		mySidOption.setValue("");
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

		synchronized (this) {
			try {
				ZLNetworkManager.Instance().perform(new LitResNetworkRequest(query, SSLCertificate, xmlReader));
			} catch (ZLNetworkException e) {
				if (NetworkException.ERROR_AUTHENTICATION_FAILED.equals(e.getCode())) {
					mySidChecked = true;
					mySidUserNameOption.setValue("");
					mySidOption.setValue("");
				} else if (NetworkException.ERROR_PURCHASE_ALREADY_PURCHASED.equals(e.getCode())) {
					myPurchasedBooks.put(book.Id, book);
				}
				throw e;
			} finally {
				if (xmlReader.Account != null) {
					myAccount = BuyBookReference.price(xmlReader.Account, "RUB");
				}
			}
			if (xmlReader.BookId == null || !xmlReader.BookId.equals(book.Id)) {
				throw new ZLNetworkException(NetworkException.ERROR_SOMETHING_WRONG, Link.getSiteName());
			}
			myPurchasedBooks.put(book.Id, book);
		}
	}

	@Override
	public boolean refillAccountSupported() {
		return true;
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
				mySidChecked = true;
				mySidUserNameOption.setValue("");
				mySidOption.setValue("");		
				throw new ZLNetworkException(NetworkException.ERROR_AUTHENTICATION_FAILED);
			}
			networkRequest = loadPurchasedBooks();
		}

		synchronized (this) {
			try {
				ZLNetworkManager.Instance().perform(networkRequest);
			} catch (ZLNetworkException e) {
				//loadPurchasedBooksOnError();
				if (NetworkException.ERROR_AUTHENTICATION_FAILED.equals(e.getCode())) {
					mySidChecked = true;
					mySidUserNameOption.setValue("");
					mySidOption.setValue("");
				}
				throw e;
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

		synchronized (this) {
			try {
				ZLNetworkManager.Instance().perform(requests);
			} catch (ZLNetworkException e) {
				myInitializedDataSid = null;
				loadPurchasedBooksOnError();
				loadAccountOnError();
				throw e;
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
	public boolean registrationSupported() {
		return true;
	}

	@Override
	public void registerUser(String login, String password, String email) throws ZLNetworkException {
		String url = Link.getLink(INetworkLink.URL_SIGN_UP);
		if (url == null) {
			throw new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION);
		}
		url = ZLNetworkUtil.appendParameter(url, "new_login", login);
		url = ZLNetworkUtil.appendParameter(url, "new_pwd1", password);
		url = ZLNetworkUtil.appendParameter(url, "mail", email);

		final LitResRegisterUserXMLReader xmlReader = new LitResRegisterUserXMLReader(Link.getSiteName());

		synchronized (this) {
			try {
				ZLNetworkManager.Instance().perform(new LitResNetworkRequest(url, SSLCertificate, xmlReader));
			} catch (ZLNetworkException e) {
				mySidUserNameOption.setValue("");
				mySidOption.setValue("");
				throw e;
			} finally {
				mySidChecked = true;
			}
			mySidOption.setValue(xmlReader.Sid);
			mySidUserNameOption.setValue(login);
		}
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
}
