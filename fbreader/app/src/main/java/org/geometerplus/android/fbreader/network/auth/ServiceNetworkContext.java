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

package org.geometerplus.android.fbreader.network.auth;

import java.net.URI;
import java.util.Map;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

public class ServiceNetworkContext extends AndroidNetworkContext {
	private final Service myService;

	public ServiceNetworkContext(Service service) {
		myService = service;
	}

	public Context getContext() {
		return myService;
	}

	@Override
	protected Map<String,String> authenticateWeb(URI uri, String realm, String authUrl, String completeUrl, String verificationUrl) {
		final NotificationManager notificationManager =
			(NotificationManager)myService.getSystemService(Context.NOTIFICATION_SERVICE);
		final Intent intent = new Intent(myService, WebAuthorisationScreen.class);
		intent.setData(Uri.parse(authUrl));
		intent.putExtra(WebAuthorisationScreen.COMPLETE_URL_KEY, completeUrl);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
		final PendingIntent pendingIntent = PendingIntent.getActivity(myService, 0, intent, 0);
		final String text =
			ZLResource.resource("dialog")
				.getResource("backgroundAuthentication")
				.getResource("message")
				.getValue();
		final Notification notification = new NotificationCompat.Builder(myService)
			.setSmallIcon(R.drawable.fbreader)
			.setTicker(text)
			.setContentTitle(realm)
			.setContentText(text)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)
			.build();
		notificationManager.notify(0, notification);
		return errorMap("Notification sent");
	}
}
