/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

import java.util.*;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.*;

public abstract class PluginApi {
	public static final String ACTION_REGISTER = "android.fbreader.action.plugin.REGISTER";
	public static final String ACTION_RUN = "android.fbreader.action.plugin.RUN";

	public static abstract class PluginInfo extends BroadcastReceiver {
		public static final String KEY = "actions";

		public void onReceive(Context context, Intent intent) {
			final List<ActionInfo> newActions = implementedActions(context);
			if (newActions != null) {
				final Bundle bundle = getResultExtras(true);
				ArrayList<ActionInfo> actions = bundle.<ActionInfo>getParcelableArrayList(KEY);
				if (actions == null) {
					actions = new ArrayList<ActionInfo>();
				}
				actions.addAll(newActions);
				bundle.putParcelableArrayList(KEY, actions);
			}
		}

		protected abstract List<ActionInfo> implementedActions(Context context);
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
}
