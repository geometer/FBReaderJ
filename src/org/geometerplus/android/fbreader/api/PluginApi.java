/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

import java.util.*;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.*;

public abstract class PluginApi {
	public static final String ACTION_REGISTER = "android.fbreader.action.plugin.REGISTER";
	public static final String ACTION_RUN = "android.fbreader.action.plugin.RUN";

	public static abstract class TestActivity extends Activity {
		abstract protected List<ActionInfo> implementedActions();

		private void updateIntent(Intent intent) {
			final List<ActionInfo> newActions = implementedActions();
			if (newActions != null) {
				ArrayList<ActionInfo> actions = intent.getParcelableArrayListExtra(KEY);
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

	public static class ActionInfo implements Parcelable {
		private final String myId;
		public final String MenuItemName;

		public ActionInfo(Uri id, String menuItemName) {
			myId = id.toString();
			MenuItemName = menuItemName;
		}

		public Uri getId() {
			return Uri.parse(myId);
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel parcel, int flags) {
			parcel.writeString(myId);
			parcel.writeString(MenuItemName);
		}

		public static final Creator<ActionInfo> CREATOR = new Creator<ActionInfo>() {
			public ActionInfo createFromParcel(Parcel parcel) {
				return new ActionInfo(
					Uri.parse(parcel.readString()),
					parcel.readString()
				);
			}

			public ActionInfo[] newArray(int size) {
				return new ActionInfo[size];
			}
		};
	}

	private static final String KEY = "actions";

	public static List<ActionInfo> getActions(Intent intent) {
		final List<ActionInfo> actions = intent != null
			? intent.<ActionInfo>getParcelableArrayListExtra(KEY) : null;
		return actions != null ? actions : Collections.<ActionInfo>emptyList();
	}
}
