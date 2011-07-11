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

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;
import org.geometerplus.zlibrary.core.network.ZLNetworkManager;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.opds.OPDSNetworkLink;
import org.geometerplus.fbreader.network.authentication.*;
import org.geometerplus.fbreader.network.urlInfo.*;

public class LitResAuthenticationManager extends NetworkAuthenticationManager {
	private volatile boolean myFullyInitialized;

	private final ZLStringOption mySidOption;
	private final ZLStringOption myUserIdOption;
	private final ZLBooleanOption myCanRebillOption;

	private volatile String myInitializedDataSid;
	private volatile String myAccount;
	private final Map<String,NetworkBookItem> myPurchasedBookMap =
		new HashMap<String,NetworkBookItem>();
	private final List<NetworkBookItem> myPurchasedBookList =
		new LinkedList<NetworkBookItem>();

	public LitResAuthenticationManager(OPDSNetworkLink link) {
		super(link, null);

		mySidOption = new ZLStringOption(link.getSiteName(), "sid", "");
		myUserIdOption = new ZLStringOption(link.getSiteName(), "userId", "");
		myCanRebillOption = new ZLBooleanOption(link.getSiteName(), "canRebill", false);
	}

	public synchronized void initUser(String userName, String sid, String userId, boolean canRebill) {
		UserNameOption.setValue(userName);
		mySidOption.setValue(sid);
		myUserIdOption.setValue(userId);
		myCanRebillOption.setValue(canRebill);
		myFullyInitialized = !"".equals(userName) && !"".equals(sid) && !"".equals(userId);
	}

	@Override
	public synchronized void logOut() {
		initUser("", "", "", false);
		myInitializedDataSid = null;
		myPurchasedBookMap.clear();
		myPurchasedBookList.clear();
	}

	@Override
	public boolean isAuthorised(boolean useNetwork) throws ZLNetworkException {
		final String sid;
		synchronized (this) {
			boolean authState =
				UserNameOption.getValue().length() != 0 &&
				mySidOption.getValue().length() != 0;

			if (myFullyInitialized || !useNetwork) {
				return authState;
			}

			if (!authState) {
				logOut();
				return false;
			}
			sid = mySidOption.getValue();
		}

		String url = Link.getUrl(UrlInfo.Type.SignIn);
		if (url == null) {
			throw new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION);
		}

		final LitResLoginXMLReader xmlReader = new LitResLoginXMLReader(Link.getSiteName());

		ZLNetworkException exception = null;
		try {
			final LitResNetworkRequest request = new LitResNetworkRequest(url, xmlReader);
			request.addPostParameter("sid", sid);
			ZLNetworkManager.Instance().perform(request);
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
			initUser(UserNameOption.getValue(), xmlReader.Sid, xmlReader.UserId, xmlReader.CanRebill);
			return true;
		}
	}

	@Override
	public void authorise(String password) throws ZLNetworkException {
		String url = Link.getUrl(UrlInfo.Type.SignIn);
		if (url == null) {
			throw new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION);
		}
		final String login;
		synchronized (this) {
			login = UserNameOption.getValue();
		}

		final LitResLoginXMLReader xmlReader = new LitResLoginXMLReader(Link.getSiteName());

		ZLNetworkException exception = null;
		try {
			final LitResNetworkRequest request = new LitResNetworkRequest(url, xmlReader);
			request.addPostParameter("login", login);
			request.addPostParameter("pwd", password);
			ZLNetworkManager.Instance().perform(request);
		} catch (ZLNetworkException e) {
			exception = e;
		}

		synchronized (this) {
			if (exception != null) {
				logOut();
				throw exception;
			}
			initUser(UserNameOption.getValue(), xmlReader.Sid, xmlReader.UserId, xmlReader.CanRebill);
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
		BookUrlInfo reference = book.reference(UrlInfo.Type.BookConditional);
		if (reference == null) {
			return null;
		}
		final String url = ZLNetworkUtil.appendParameter(reference.Url, "sid", sid);
		return new DecoratedBookUrlInfo(reference, url);
	}


	@Override
	public String currentUserName() {
		final String value;
		synchronized (this) {
			value = UserNameOption.getValue();
		}
		if (value.length() == 0) {
			return null;
		}
		return value;
	}


	@Override
	public synchronized boolean needPurchase(NetworkBookItem book) {
		return !myPurchasedBookMap.containsKey(book.Id);
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

		final BookUrlInfo reference = book.reference(UrlInfo.Type.BookBuy);
		if (reference == null) {
			throw new ZLNetworkException(NetworkException.ERROR_BOOK_NOT_PURCHASED); // TODO: more correct error message???
		}

		final LitResPurchaseXMLReader xmlReader = new LitResPurchaseXMLReader(Link.getSiteName());

		ZLNetworkException exception = null;
		try {
			final LitResNetworkRequest request = new LitResNetworkRequest(reference.Url, xmlReader);
			request.addPostParameter("sid", sid);
			ZLNetworkManager.Instance().perform(request);
		} catch (ZLNetworkException e) {
			exception = e;
		}

		synchronized (this) {
			if (xmlReader.Account != null) {
				myAccount = BookBuyUrlInfo.price(xmlReader.Account, "RUB");
			}
			if (exception != null) {
				final String code = exception.getCode();
				if (NetworkException.ERROR_AUTHENTICATION_FAILED.equals(code)) {
					logOut();
				} else if (NetworkException.ERROR_PURCHASE_ALREADY_PURCHASED.equals(code)) {
					myPurchasedBookMap.put(book.Id, book);
					myPurchasedBookList.add(0, book);
				}
				throw exception;
			}
			if (xmlReader.BookId == null || !xmlReader.BookId.equals(book.Id)) {
				throw new ZLNetworkException(NetworkException.ERROR_SOMETHING_WRONG, Link.getSiteName());
			}
			myPurchasedBookMap.put(book.Id, book);
			myPurchasedBookList.add(0, book);
		}
	}

	@Override
	public String topupLink() {
		final String sid;
		synchronized (this) {
			sid = mySidOption.getValue();
		}
		if (sid.length() == 0) {
			return null;
		}
		final String url = Link.getUrl(UrlInfo.Type.TopUp);
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

	@Override
	public synchronized List<NetworkBookItem> purchasedBooks() {
		return Collections.unmodifiableList(myPurchasedBookList);
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
			if (sid.equals(myInitializedDataSid) || !isAuthorised(true)) {
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
		final String query = "pages/catalit_browser/";

		final LitResNetworkRequest request = new LitResNetworkRequest(
			LitResUtil.url(Link, query),
			new LitResXMLReader((OPDSNetworkLink)Link, new LinkedList<NetworkItem>())
		);
		request.addPostParameter("my", "1");
		request.addPostParameter("sid", sid);
		return request;
	}

	private void loadPurchasedBooksOnError() {
		myPurchasedBookMap.clear();
		myPurchasedBookList.clear();
	}

	private void loadPurchasedBooksOnSuccess(LitResNetworkRequest purchasedBooksRequest) {
		LitResXMLReader reader = (LitResXMLReader)purchasedBooksRequest.Reader;
		myPurchasedBookMap.clear();
		myPurchasedBookList.clear();
		for (NetworkItem item : reader.Books) {
			if (item instanceof NetworkBookItem) {
				NetworkBookItem book = (NetworkBookItem)item;
				myPurchasedBookMap.put(book.Id, book);
				myPurchasedBookList.add(book);
			}
		}
	}

	private LitResNetworkRequest loadAccount() {
		final String sid = mySidOption.getValue();
		final String query = "pages/purchase_book/";

		final LitResNetworkRequest request = new LitResNetworkRequest(
			LitResUtil.url(Link, query),
			new LitResPurchaseXMLReader(Link.getSiteName())
		);
		request.addPostParameter("sid", sid);
		request.addPostParameter("art", "0");
		return request;
	}

	private void loadAccountOnError() {
		myAccount = null;
	}

	private void loadAccountOnSuccess(LitResNetworkRequest accountRequest) {
		LitResPurchaseXMLReader reader = (LitResPurchaseXMLReader)accountRequest.Reader;
		myAccount = BookBuyUrlInfo.price(reader.Account, "RUB");
	}

	@Override
	public boolean passwordRecoverySupported() {
		return true;
	}

	@Override
	public void recoverPassword(String email) throws ZLNetworkException {
		final String url = Link.getUrl(UrlInfo.Type.RecoverPassword);
		if (url == null) {
			throw new ZLNetworkException(NetworkException.ERROR_UNSUPPORTED_OPERATION);
		}
		final LitResPasswordRecoveryXMLReader xmlReader =  new LitResPasswordRecoveryXMLReader(Link.getSiteName());
		final LitResNetworkRequest request = new LitResNetworkRequest(url, xmlReader);
		request.addPostParameter("mail", email);
		ZLNetworkManager.Instance().perform(request);
	}

	@Override
	public Map<String,String> getTopupData() {
		final HashMap<String,String> map = new HashMap<String,String>();
		map.put("litres:userId", myUserIdOption.getValue());
		map.put("litres:canRebill", myCanRebillOption.getValue() ? "true" : "false");
		map.put("litres:sid", mySidOption.getValue());
		return map;
	}
}
