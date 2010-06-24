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

package org.geometerplus.fbreader.network.authentication;

import org.geometerplus.zlibrary.core.options.ZLStringOption;

import org.geometerplus.fbreader.network.*;


public abstract class NetworkAuthenticationManager {

	public final NetworkLink Link;
	public final ZLStringOption UserNameOption;
	public final String SSLCertificate;

	public NetworkAuthenticationManager(NetworkLink link, String sslCertificate) {
		Link = link;
		UserNameOption = new ZLStringOption(link.SiteName, "userName", "");
		SSLCertificate = sslCertificate;
	}

	/*
	 * Common manager methods
	 */
	public abstract AuthenticationStatus isAuthorised(boolean useNetwork /* = true */);
	public abstract String authorise(String password); // returns error message
	public abstract void logOut();
	public abstract BookReference downloadReference(NetworkBookItem book);

	/*
	 * Account specific methods (can be called only if authorised!!!)
	 */
	public abstract String currentUserName();

	public boolean needsInitialization() {
		return false;
	}

	// returns error message
	public String initialize() {
		return NetworkErrors.errorMessage(NetworkErrors.ERROR_UNSUPPORTED_OPERATION);
	}

	// returns true if link must be purchased before downloading
	public boolean needPurchase(NetworkBookItem book) {
		return true;
	}

	// returns error message
	public String purchaseBook(NetworkBookItem book) {
		return NetworkErrors.errorMessage(NetworkErrors.ERROR_UNSUPPORTED_OPERATION);
	}

	public String currentAccount() {
		return null;
	}

	//public abstract ZLNetworkSSLCertificate certificate();

	/*
	 * refill account
	 */

	public boolean refillAccountSupported() {
		return false;
	}

	public String refillAccountLink() {
		return null;
	}

	/*
	 * new User Registration
	 */
	public boolean registrationSupported() {
		return false;
	}

	public String registerUser(String login, String password, String email) {
		return NetworkErrors.errorMessage(NetworkErrors.ERROR_UNSUPPORTED_OPERATION);
	}

	/*
	 * Password Recovery
	 */
	public boolean passwordRecoverySupported() {
		return false;
	}

	public String recoverPassword(String email) {
		return NetworkErrors.errorMessage(NetworkErrors.ERROR_UNSUPPORTED_OPERATION);
	}
}
