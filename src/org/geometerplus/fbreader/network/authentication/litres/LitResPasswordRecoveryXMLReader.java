/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

public class LitResPasswordRecoveryXMLReader extends LitResAuthenticationXMLReader {
	private static final String TAG_PASSWORD_RECOVERY_OK = "catalit-pass-recover-ok";
	private static final String TAG_PASSWORD_RECOVERY_FAILED = "catalit-pass-recover-failed";

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		if (TAG_PASSWORD_RECOVERY_FAILED == tag) {
			final String error = attributes.getValue("error");
			if ("1".equals(error)) {
				setException(ZLNetworkException.forCode(NetworkException.ERROR_NO_USER_FOR_EMAIL));
			} else if ("2".equals(error)) {
				setException(ZLNetworkException.forCode(NetworkException.ERROR_EMAIL_NOT_SPECIFIED));
			} else {
				final String comment = attributes.getValue("coment");
				if (comment != null) {
					setException(new ZLNetworkException(comment));
				} else {
					setException(ZLNetworkException.forCode(NetworkException.ERROR_INTERNAL, error));
				}
			}
		} else if (TAG_PASSWORD_RECOVERY_OK == tag) {
			// NOP
		} else {
			setException(ZLNetworkException.forCode(ZLNetworkException.ERROR_SOMETHING_WRONG, LitResUtil.HOST_NAME));
		}
		return true;
	}
}
