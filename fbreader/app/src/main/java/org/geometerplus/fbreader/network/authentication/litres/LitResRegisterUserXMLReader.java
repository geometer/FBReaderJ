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

public class LitResRegisterUserXMLReader extends LitResAuthenticationXMLReader {
	private static final String TAG_AUTHORIZATION_OK = "catalit-authorization-ok";
	private static final String TAG_REGISTRATION_FAILED = "catalit-registration-failed";

	public String Sid;

	public static final class AlreadyInUseException extends ZLNetworkException {
		public AlreadyInUseException(String code) {
			super(errorMessage(code));
		}
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		if (TAG_REGISTRATION_FAILED == tag) {
			final String error = attributes.getValue("error");
			if ("1".equals(error)) {
				setException(new AlreadyInUseException("usernameAlreadyInUse"));
			} else if ("2".equals(error)) {
				setException(ZLNetworkException.forCode("usernameNotSpecified"));
			} else if ("3".equals(error)) {
				setException(ZLNetworkException.forCode("passwordNotSpecified"));
			} else if ("4".equals(error)) {
				setException(ZLNetworkException.forCode("invalidEMail"));
			} else if ("5".equals(error)) {
				setException(ZLNetworkException.forCode("tooManyRegistrations"));
			} else if ("6".equals(error)) {
				setException(new AlreadyInUseException("emailAlreadyInUse"));
			} else {
				final String comment = attributes.getValue("coment");
				if (comment != null) {
					setException(new ZLNetworkException(comment));
				} else {
					setException(ZLNetworkException.forCode(NetworkException.ERROR_INTERNAL));
				}
			}
		} else if (TAG_AUTHORIZATION_OK == tag) {
			Sid = attributes.getValue("sid");
			if (Sid == null) {
				setException(ZLNetworkException.forCode("somethingWrongMessage", LitResUtil.HOST_NAME));
			}
		} else {
			setException(ZLNetworkException.forCode("somethingWrongMessage", LitResUtil.HOST_NAME));
		}
		return true;
	}
}
