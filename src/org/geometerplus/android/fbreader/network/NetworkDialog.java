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

package org.geometerplus.android.fbreader.network;

import java.util.TreeMap;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.network.INetworkLink;


abstract class NetworkDialog {

	// dialog identifiers
	public static final int DIALOG_AUTHENTICATION = 0;
	public static final int DIALOG_REGISTER_USER = 1;
	public static final int DIALOG_CUSTOM_CATALOG = 2;

	private static final TreeMap<Integer, NetworkDialog> ourInstances = new TreeMap<Integer, NetworkDialog>();

	public static NetworkDialog getDialog(int id) {
		NetworkDialog dlg = ourInstances.get(Integer.valueOf(id));
		if (dlg == null) {
			switch (id) {
			case DIALOG_AUTHENTICATION:
				dlg = new AuthenticationDialog();
				break;
			case DIALOG_REGISTER_USER:
				dlg = new RegisterUserDialog();
				break;
			case DIALOG_CUSTOM_CATALOG:
				dlg = new CustomCatalogDialog();
				break;
			}
			if (dlg != null) {
				ourInstances.put(Integer.valueOf(id), dlg);
			}
		}
		return dlg;
	}

	protected final ZLResource myResource;

	protected INetworkLink myLink;
	protected String myErrorMessage;
	protected Runnable myOnSuccessRunnable;

	public NetworkDialog(String key) {
		myResource = ZLResource.resource("dialog").getResource(key);
	}

	public static void show(Activity activity, int id, INetworkLink link, Runnable onSuccessRunnable) {
		getDialog(id).showInternal(activity, id, link, onSuccessRunnable);
	}

	private void showInternal(Activity activity, int id, INetworkLink link, Runnable onSuccessRunnable) {
		myLink = link;
		myErrorMessage = null;
		myOnSuccessRunnable = onSuccessRunnable;
		activity.showDialog(id);
	}

	public abstract Dialog createDialog(final Activity activity);
	public abstract void prepareDialog(Dialog dialog);

	protected void setupLabel(View layout, int labelId, String key, int labelForId) {
		final View viewFor = layout.findViewById(labelForId);
		viewFor.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		final TextView label = (TextView) layout.findViewById(labelId);
		label.setText(myResource.getResource(key).getValue());
		label.getLayoutParams().height = viewFor.getMeasuredHeight();
	}
}
