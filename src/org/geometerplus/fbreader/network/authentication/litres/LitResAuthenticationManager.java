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

import org.geometerplus.fbreader.network.*;
import org.geometerplus.fbreader.network.authentication.*;


public class LitResAuthenticationManager extends NetworkAuthenticationManager {

	private boolean mySidChecked;

	private ZLStringOption mySidUserNameOption;
	private ZLStringOption mySidOption;

	private String myInitializedDataSid;
	private Set<String> myPurchasedBooksIds;
	private LinkedList<NetworkLibraryItem> myPurchasedBooksList;
	private String myAccount;


	public LitResAuthenticationManager(NetworkLink link) {
		super(link);
		mySidUserNameOption = new ZLStringOption(link.SiteName, "sidUserName", "");
		mySidOption = new ZLStringOption(link.SiteName, "sid", "");
	}

	public AuthenticationStatus isAuthorised(boolean useNetwork /* = true */) {
		boolean authState = mySidUserNameOption.getValue().length() != 0 && mySidOption.getValue().length() != 0;
		if (mySidChecked || !useNetwork) {
			return new AuthenticationStatus(authState);
		}

		if (!authState) {
			mySidChecked = true;
			mySidUserNameOption.setValue("");
			mySidOption.setValue("");
			return new AuthenticationStatus(false);
		}

		/*String firstName, lastName, newSid;
		shared_ptr<ZLXMLReader> xmlReader = new LitResLoginDataParser(firstName, lastName, newSid);

		std::string url = Link.url(NetworkLink::URL_SIGN_IN);
		ZLNetworkUtil::appendParameter(url, "sid", mySidOption.getValue());

		shared_ptr<ZLExecutionData> networkData =
			ZLNetworkManager::Instance().createXMLParserRequest(
				url, certificate(), xmlReader
			);
		std::string error = ZLNetworkManager::Instance().perform(networkData);

		if (!error.empty()) {
			if (error != NetworkErrors.errorMessage(NetworkErrors.ERROR_AUTHENTICATION_FAILED)) {
				return AuthenticationStatus(error);
			}
			mySidChecked = true;
			mySidUserNameOption.setValue("");
			mySidOption.setValue("");
			return AuthenticationStatus(false);
		}
		mySidChecked = true;
		mySidOption.setValue(newSid);
		return AuthenticationStatus(true);*/
		return new AuthenticationStatus(false); // temp
	}

	public String authorise(String password) {
		/*String firstName, lastName, newSid;
		shared_ptr<ZLXMLReader> xmlReader = new LitResLoginDataParser(firstName, lastName, newSid);

		std::string url = Link.url(NetworkLink::URL_SIGN_IN);
		ZLNetworkUtil::appendParameter(url, "login", UserNameOption.value());
		ZLNetworkUtil::appendParameter(url, "pwd", pwd);
		if (SkipIPOption.value()) {
			ZLNetworkUtil::appendParameter(url, "skip_ip", "1");
		}

		shared_ptr<ZLExecutionData> networkData =
			ZLNetworkManager::Instance().createXMLParserRequest(
				url,
				certificate(),
				xmlReader
			);
		std::string error = ZLNetworkManager::Instance().perform(networkData);

		mySidChecked = true;
		if (!error.empty()) {
			mySidUserNameOption.setValue("");
			mySidOption.setValue("");
			return error;
		}
		mySidOption.setValue(newSid);
		mySidUserNameOption.setValue(UserNameOption.value());
		return "";*/
		return null; // tmp
	}

	public void logOut() {
		mySidChecked = true;
		mySidUserNameOption.setValue("");
		mySidOption.setValue("");
	}

	public BookReference downloadReference(NetworkBookItem book) {
		final String sid = mySidOption.getValue();
		if (sid.length() == 0) {
			return null;
		}
		/*shared_ptr<BookReference> reference =
			book.reference(BookReference::DOWNLOAD_FULL_CONDITIONAL);
		if (reference.isNull()) {
			return 0;
		}
		std::string url = reference->URL;
		ZLNetworkUtil::appendParameter(url, "sid", sid);
		return new DecoratedBookReference(*reference, url);*/
		return null; // tmp
	}

	public boolean skipIPSupported() {
		return true;
	}

	public String currentUserName() {
		String value = mySidUserNameOption.getValue();
		if (value.length() == 0) {
			return null;
		}
		return value;
	}


	public boolean needPurchase(NetworkBookItem book) {
		if (myPurchasedBooksIds == null) {
			return true;
		}
		return !myPurchasedBooksIds.contains(book.Id);
	}

	public String purchaseBook(NetworkBookItem book) {
		final String sid = mySidOption.getValue();
		if (sid.length() == 0) {
			return NetworkErrors.errorMessage(NetworkErrors.ERROR_AUTHENTICATION_FAILED);
		}

		/*shared_ptr<BookReference> reference = book.reference(BookReference::BUY);
		if (reference.isNull()) {
			// TODO: add correct error message
			return "Oh, that's impossible";
		}
		std::string query = reference->URL;
		ZLNetworkUtil::appendParameter(query, "sid", sid);

		std::string account, bookId;
		shared_ptr<ZLXMLReader> xmlReader = new LitResPurchaseDataParser(account, bookId);

		shared_ptr<ZLExecutionData> networkData = ZLNetworkManager::Instance().createXMLParserRequest(
			query, certificate(), xmlReader
		);
		std::string error = ZLNetworkManager::Instance().perform(networkData);

		if (!account.empty()) {
			myAccount = BuyBookReference::price(account, "RUB");
		}
		if (error == NetworkErrors.errorMessage(NetworkErrors.ERROR_AUTHENTICATION_FAILED)) {
			mySidChecked = true;
			mySidUserNameOption.setValue("");
			mySidOption.setValue("");
		}
		const std::string alreadyPurchasedError = NetworkErrors.errorMessage(NetworkErrors.ERROR_PURCHASE_ALREADY_PURCHASED);
		if (error != alreadyPurchasedError) {
			if (!error.empty()) {
				return error;
			}
			if (bookId != book.Id) {
				return NetworkErrors.errorMessage(NetworkErrors.ERROR_SOMETHING_WRONG, Link.SiteName);
			}
		}
		myPurchasedBooksIds.insert(book.Id);
		myPurchasedBooksList.push_back(new NetworkBookItem(book, 0));
		return error;*/
		return null; // tmp
	}


	public String refillAccountLink() {
		final String sid = mySidOption.getValue();
		if (sid.length() == 0) {
			return null;
		}
		final String url = Link.Links.get(NetworkLink.URL_REFILL_ACCOUNT);
		if (url == null) {
			return null;
		}
		return ZLNetworkUtil.appendParameter(url, "sid", sid);
	}

	public String currentAccount() {
		return myAccount;
	}

	public String reloadPurchasedBooks() {
		final String sid = mySidOption.getValue();
		if (sid.length() == 0) {
			return NetworkErrors.errorMessage(NetworkErrors.ERROR_AUTHENTICATION_FAILED);
		}
		if (!sid.equals(myInitializedDataSid)) {
			mySidChecked = true;
			mySidUserNameOption.setValue("");
			mySidOption.setValue("");		
			return NetworkErrors.errorMessage(NetworkErrors.ERROR_AUTHENTICATION_FAILED);
		}

		/*std::set<std::string> purchasedBooksIds;
		NetworkItem::List purchasedBooksList;

		shared_ptr<ZLExecutionData> networkData = loadPurchasedBooks(purchasedBooksIds, purchasedBooksList);

		std::string error = ZLNetworkManager::Instance().perform(networkData);
		if (!error.empty()) {
			//loadPurchasedBooksOnError(purchasedBooksIds, purchasedBooksList);
			if (error == NetworkErrors.errorMessage(NetworkErrors.ERROR_AUTHENTICATION_FAILED)) {
				mySidChecked = true;
				mySidUserNameOption.setValue("");
				mySidOption.setValue("");
			}
			return error;
		}
		loadPurchasedBooksOnSuccess(purchasedBooksIds, purchasedBooksList);
		myPurchasedBooksIds = purchasedBooksIds;
		myPurchasedBooksList = purchasedBooksList;
		return "";*/
		return null; // tmp
	}

	public void collectPurchasedBooks(List<NetworkLibraryItem> list) {
		if (myPurchasedBooksList != null) {
			list.addAll(myPurchasedBooksList);
		}
	}


	public boolean needsInitialization() {
		final String sid = mySidOption.getValue();
		if (sid.length() == 0) {
			return false;
		}
		return !sid.equals(myInitializedDataSid);
	}

	public String initialize() {
		final String sid = mySidOption.getValue();
		if (sid.length() == 0) {
			return NetworkErrors.errorMessage(NetworkErrors.ERROR_AUTHENTICATION_FAILED);
		}
		if (sid.equals(myInitializedDataSid)) {
			return null;
		}

		/*std::string dummy1;

		ZLExecutionData::Vector dataList;
		dataList.push_back(loadPurchasedBooks(myPurchasedBooksIds, myPurchasedBooksList));
		dataList.push_back(loadAccount(dummy1));

		std::string error = ZLNetworkManager::Instance().perform(dataList);
		if (!error.empty()) {
			myInitializedDataSid.clear();
			loadPurchasedBooksOnError(myPurchasedBooksIds, myPurchasedBooksList);
			loadAccountOnError();
			return error;
		}
		myInitializedDataSid = sid;
		loadPurchasedBooksOnSuccess(myPurchasedBooksIds, myPurchasedBooksList);
		loadAccountOnSuccess();
		return "";*/
		return null; // tmp
	}

	/*private ZLExecutionData loadPurchasedBooks(Set<String> purchasedBooksIds, List<NetworkLibraryItem> purchasedBooksList) {
		const std::string &sid = mySidOption.getValue();
		purchasedBooksIds.clear();
		purchasedBooksList.clear();

		std::string query;
		ZLNetworkUtil::appendParameter(query, "my", "1");
		ZLNetworkUtil::appendParameter(query, "sid", sid);

		return ZLNetworkManager::Instance().createXMLParserRequest(
			LitResUtil::url(Link, "pages/catalit_browser/" + query), 
			certificate(),
			new LitResDataParser(Link, purchasedBooksList)
		);
	}

	private void loadPurchasedBooksOnError(Set<String> purchasedBooksIds, List<NetworkLibraryItem> purchasedBooksList) {
		purchasedBooksIds.clear();
		purchasedBooksList.clear();
	}

	private void loadPurchasedBooksOnSuccess(Set<String> purchasedBooksIds, List<NetworkLibraryItem> purchasedBooksList) {
		for (NetworkItem::List::iterator it = purchasedBooksList.begin(); it != purchasedBooksList.end(); ++it) {
			NetworkBookItem &book = (NetworkBookItem&)**it;
			book.Index = 0;
			purchasedBooksIds.insert(book.Id);
		}
	}

	private ZLExecutionData loadAccount(String dummy1) {
		const std::string &sid = mySidOption.getValue();

		myAccount.clear();

		std::string query;
		ZLNetworkUtil::appendParameter(query, "sid", sid);
		ZLNetworkUtil::appendParameter(query, "art", "0");

		return ZLNetworkManager::Instance().createXMLParserRequest(
			LitResUtil::url(Link, "pages/purchase_book/" + query),
			certificate(),
			new LitResPurchaseDataParser(myAccount, dummy1)
		);
	}

	private void loadAccountOnError() {
		myAccount.clear();
	}

	private void loadAccountOnSuccess() {
		myAccount = BuyBookReference::price(myAccount, "RUB");
	}*/


	public boolean registrationSupported() {
		return true;
	}

	public String registerUser(String login, String password, String email) {
		/*String newSid;
		shared_ptr<ZLXMLReader> xmlReader = new LitResRegisterUserDataParser(newSid);

		std::string url = Link.url(NetworkLink::URL_SIGN_UP);
		ZLNetworkUtil::appendParameter(url, "new_login", login);
		ZLNetworkUtil::appendParameter(url, "new_pwd1", password);
		ZLNetworkUtil::appendParameter(url, "mail", email);

		shared_ptr<ZLExecutionData> networkData =
			ZLNetworkManager::Instance().createXMLParserRequest(
				url, certificate(), xmlReader
			);
		std::string error = ZLNetworkManager::Instance().perform(networkData);

		mySidChecked = true;
		if (!error.empty()) {
			mySidUserNameOption.setValue("");
			mySidOption.setValue("");
			return error;
		}
		mySidOption.setValue(newSid);
		mySidUserNameOption.setValue(login);
		return "";*/
		return null; // tmp
	}


	public boolean passwordRecoverySupported() {
		return true;
	}

	public String recoverPassword(String email) {
		/*String url = Link.url(NetworkLink::URL_RECOVER_PASSWORD);
		ZLNetworkUtil::appendParameter(url, "mail", email);

		shared_ptr<ZLExecutionData> networkData =
			ZLNetworkManager::Instance().createXMLParserRequest(
				url, certificate(), new LitResPasswordRecoveryDataParser()
			);
		return ZLNetworkManager::Instance().perform(networkData);*/
		return null; // tmp
	}
}
