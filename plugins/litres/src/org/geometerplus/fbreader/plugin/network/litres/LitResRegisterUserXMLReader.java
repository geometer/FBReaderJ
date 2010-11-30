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

package org.geometerplus.fbreader.plugin.network.litres;

import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public class LitResRegisterUserXMLReader extends LitResAuthenticationXMLReader {
	private static final String TAG_AUTHORIZATION_OK = "catalit-authorization-ok";
	private static final String TAG_REGISTRATION_FAILED = "catalit-registration-failed";

	private final ZLResource myResource;

	public String Sid;

	public LitResRegisterUserXMLReader(String hostName, ZLResource resource) {
		super(hostName);
 		myResource = resource;
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		tag = tag.toLowerCase().intern();
		if (TAG_REGISTRATION_FAILED == tag) {
			final String error = attributes.getValue("error");
			if ("1".equals(error)) {
				setErrorMessage(myResource.getResource("usernameAlreadyTaken").getValue());
			} else if ("2".equals(error)) {
				setErrorMessage(myResource.getResource("usernameNotSpecified").getValue());
			} else if ("3".equals(error)) {
				setErrorMessage(myResource.getResource("passwordNotSpecified").getValue());
			} else if ("4".equals(error)) {
				setErrorMessage(myResource.getResource("invalidEMail").getValue());
			} else if ("5".equals(error)) {
				setErrorMessage(myResource.getResource("tooManyRegistrations").getValue());
			} else if ("6".equals(error)) {
				System.err.println("emailAlreadyTaken");
				System.err.println(myResource.getResource("emailAlreadyTaken"));
				System.err.println(myResource.getResource("emailAlreadyTaken").getValue());
				setErrorMessage(myResource.getResource("emailAlreadyTaken").getValue());
			} else {
				final String comment = attributes.getValue("coment");
				if (comment != null) {
					setErrorMessage(comment);
				} else {
					setErrorMessage(myResource.getResource("errorDuringRegistration").getValue().replace("%s", error));
				}
			}
		} else if (TAG_AUTHORIZATION_OK == tag) {
			Sid = attributes.getValue("sid");
		} else {
			setException(new ZLNetworkException("somethingWrongMessage", HostName));
		}
		return true;
	}
}
