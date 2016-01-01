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

package org.geometerplus.android.fbreader.sync;

import android.app.Service;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.fbreader.util.ComparisonUtil;

import org.geometerplus.zlibrary.core.network.ZLNetworkException;
import org.geometerplus.zlibrary.core.network.ZLNetworkRequest;
import org.geometerplus.zlibrary.core.options.ZLEnumOption;

import org.geometerplus.fbreader.fbreader.options.SyncOptions;
import org.geometerplus.fbreader.network.sync.SyncUtil;

import org.geometerplus.android.fbreader.network.auth.ServiceNetworkContext;

class SyncNetworkContext extends ServiceNetworkContext {
	private final SyncOptions mySyncOptions;
	private final ZLEnumOption<SyncOptions.Condition> myFeatureOption;

	private volatile String myAccountName;

	SyncNetworkContext(Service service, SyncOptions syncOptions, ZLEnumOption<SyncOptions.Condition> featureOption) {
		super(service);
		mySyncOptions = syncOptions;
		myFeatureOption = featureOption;
	}

	@Override
	protected void perform(ZLNetworkRequest request, int socketTimeout, int connectionTimeout) throws ZLNetworkException {
		if (!canPerformRequest()) {
			throw new SynchronizationDisabledException();
		}
		final String accountName = SyncUtil.getAccountName(this);
		if (!ComparisonUtil.equal(myAccountName, accountName)) {
			reloadCookie();
			myAccountName = accountName;
		}
		super.perform(request, socketTimeout, connectionTimeout);
	}

	private boolean canPerformRequest() {
		if (!mySyncOptions.Enabled.getValue()) {
			return false;
		}

		switch (myFeatureOption.getValue()) {
			default:
			case never:
				return false;
			case always:
			{
				final NetworkInfo info = getActiveNetworkInfo();
				return info != null && info.isConnected();
			}
			case viaWifi:
			{
				final NetworkInfo info = getActiveNetworkInfo();
				return
					info != null &&
					info.isConnected() &&
					info.getType() == ConnectivityManager.TYPE_WIFI;
			}
		}
	}
}
