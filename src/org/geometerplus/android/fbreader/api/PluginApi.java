/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.api;

import java.io.Serializable;
import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public abstract class PluginApi {
	public static final String ACTION_REGISTER = "android.fbreader.action.plugin.REGISTER";
	public static final String ACTION_RUN = "android.fbreader.action.plugin.RUN";

	public static abstract class TestActivity extends Activity {
		abstract protected List<ActionInfo> implementedActions();

		private void updateIntent(Intent intent) {
			final List<ActionInfo> newActions = implementedActions();
			if (newActions != null) {
				ArrayList<ActionInfo> actions =
					(ArrayList<ActionInfo>)intent.getSerializableExtra(KEY);
				if (actions == null) {
					actions = new ArrayList<ActionInfo>();
				}
				actions.addAll(newActions);
				intent.putExtra(KEY, actions);
			}

			startNextMatchingActivity(intent);
			setResult(1, intent);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			updateIntent(getIntent());
			finish();
		}

		@Override
		protected void onNewIntent(Intent intent) {
			super.onNewIntent(intent);
			updateIntent(intent);
		}
	}

	public static class ActionInfo implements Serializable {
		private final String myId;
		public final String MenuItemName;

		public ActionInfo(Uri id, String menuItemName) {
			myId = id.toString();
			MenuItemName = menuItemName;
		}

		public Uri getId() {
			return Uri.parse(myId);
		}
	}

	private static final String KEY = "actions";

	public static List<ActionInfo> getActions(Intent intent) {
		final List<ActionInfo> actions = intent != null
			? (ArrayList<ActionInfo>)intent.getSerializableExtra(KEY) : null;
		return actions != null ? actions : Collections.<ActionInfo>emptyList();
	}
}
