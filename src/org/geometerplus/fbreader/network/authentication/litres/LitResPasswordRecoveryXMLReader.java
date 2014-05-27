/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

import org.geometerplus.fbreader.network.NetworkException;

class LitResPasswordRecoveryXMLReader extends LitResAuthenticationXMLReader {
	private static final String TAG_PASSWORD_RECOVERY_OK = "catalit-pass-recover-ok";
	private static final String TAG_PASSWORD_RECOVERY_FAILED = "catalit-pass-recover-failed";

	public LitResPasswordRecoveryXMLReader(String hostName) {
		super(hostName);
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		if (TAG_PASSWORD_RECOVERY_FAILED == tag) {
			final String error = attributes.getValue("error");
			if ("1".equals(error)) {
				setException(new ZLNetworkException(NetworkException.ERROR_NO_USER_EMAIL));
			} else if ("2".equals(error)) {
				setException(new ZLNetworkException(NetworkException.ERROR_EMAIL_WAS_NOT_SPECIFIED));
			} else {
				setException(new ZLNetworkException(NetworkException.ERROR_INTERNAL));
			}
		} else if (TAG_PASSWORD_RECOVERY_OK == tag) {
			// NOP
		} else {
			setException(new ZLNetworkException(NetworkException.ERROR_SOMETHING_WRONG, HostName));
		}
		return true;
	}
}
