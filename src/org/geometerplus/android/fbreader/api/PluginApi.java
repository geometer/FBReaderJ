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

	public static abstract class ActionInfo implements Parcelable {
		protected static final int TYPE_MENU = 1;

		private final String myId;

		protected ActionInfo(Uri id) {
			myId = id.toString();
		}

		protected abstract int getType();

		public Uri getId() {
			return Uri.parse(myId);
		}

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel parcel, int flags) {
			parcel.writeInt(getType());
			parcel.writeString(myId);
		}

		public static final Creator<ActionInfo> CREATOR = new Creator<ActionInfo>() {
			public ActionInfo createFromParcel(Parcel parcel) {
				switch (parcel.readInt()) {
					case TYPE_MENU:
						return new MenuActionInfo(
							Uri.parse(parcel.readString()),
							parcel.readString()
						);
					default:
						return null;
				}
			}

			public ActionInfo[] newArray(int size) {
				return new ActionInfo[size];
			}
		};
	}

	public static class MenuActionInfo extends ActionInfo {
		public final String MenuItemName;

		public MenuActionInfo(Uri id, String menuItemName) {
			super(id);
			MenuItemName = menuItemName;
		}

		@Override
		protected int getType() {
			return TYPE_MENU;
		}

		@Override
		public void writeToParcel(Parcel parcel, int flags) {
			super.writeToParcel(parcel, flags);
			parcel.writeString(MenuItemName);
		}
	}
}
