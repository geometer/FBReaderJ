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

package org.geometerplus.zlibrary.core.network;

import java.io.*;
import javax.net.ssl.*;
import java.security.GeneralSecurityException;
import java.security.cert.*;


class ZLX509TrustManager implements X509TrustManager {

	private final X509Certificate myCertificate;

	public ZLX509TrustManager(InputStream stream) throws CertificateException {
		final CertificateFactory factory = CertificateFactory.getInstance("X509");
		Certificate cert = factory.generateCertificate(stream);
		if (!(cert instanceof X509Certificate)) {
			throw new CertificateException("That's impossible!!! Certificate with invalid type has been returned by X.509 certificate factory.");
		}
		myCertificate = (X509Certificate) cert;
		myCertificate.checkValidity();
	}

	public X509Certificate[] getAcceptedIssuers() {
	    return null;
	}

	public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
	}

	public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
		for (X509Certificate certificate : certs) {
			certificate.checkValidity();
			try {
				certificate.verify(myCertificate.getPublicKey());
			} catch (GeneralSecurityException e) {
				throw new CertificateException(e);
			}
		}
	}
}
