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

package org.geometerplus.android.fbreader.network;

import android.content.Intent;
import android.os.Bundle;

import org.geometerplus.fbreader.network.*;

public class NetworkTopLevelActivity extends NetworkBaseActivity {
	private volatile Intent myIntent;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		myIntent = getIntent();

		if (!NetworkView.Instance().isInitialized()) {
			if (NetworkInitializer.Instance == null) {
				new NetworkInitializer(this);
				NetworkInitializer.Instance.start();
			} else {
				NetworkInitializer.Instance.setActivity(this);
			}
		} else {
			onModelChanged();
			if (myIntent != null) {
				processIntent(myIntent);
				myIntent = null;
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		processIntent(intent);
	}

	void processSavedIntent() {
		if (myIntent != null) {
			processIntent(myIntent);
			myIntent = null;
		}
	}

	private void processIntent(Intent intent) {
		if (AddCustomCatalogActivity.ADD_CATALOG.equals(intent.getAction())) {
			final ICustomNetworkLink link = AddCustomCatalogActivity.getLinkFromIntent(intent);
			if (link != null) {
				runOnUiThread(new Runnable() {
					public void run() {
						final NetworkLibrary library = NetworkLibrary.Instance();
						library.addCustomLink(link);
						library.synchronize();
						onModelChanged();
					}
				});
			}
		}
	}

	@Override
	public void onDestroy() {
		if (!NetworkView.Instance().isInitialized() && NetworkInitializer.Instance != null) {
			NetworkInitializer.Instance.setActivity(null);
		}
		super.onDestroy();
	}

	@Override
	public boolean onSearchRequested() {
		if (searchIsInProgress()) {
			return false;
		}
		final NetworkLibrary library = NetworkLibrary.Instance();
		startSearch(library.NetworkSearchPatternOption.getValue(), true, null, false);
		return true;
	}
}
