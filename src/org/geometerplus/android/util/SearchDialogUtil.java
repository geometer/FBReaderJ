/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.util;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import org.geometerplus.zlibrary.core.resources.ZLResource;

public abstract class SearchDialogUtil {
	public static AlertDialog.Builder createDialog(Activity activity, Class<? extends Activity> clazz, String initialPattern) {
		return createDialog(activity, clazz, initialPattern, null);
	}

	public static AlertDialog.Builder createDialog(final Activity activity, final Class<? extends Activity> clazz, final String initialPattern, final Bundle bundle) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		builder.setTitle(ZLResource.resource("menu").getResource("search").getValue());

		final EditText input = new EditText(activity);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		input.setText(initialPattern);
		builder.setView(input);

		final ZLResource dialogResource = ZLResource.resource("dialog").getResource("button");
		builder.setPositiveButton(dialogResource.getResource("ok").getValue(), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				activity.startActivity(
					new Intent(Intent.ACTION_SEARCH)
						.setClass(activity, clazz)
						.putExtra(SearchManager.QUERY, input.getText().toString())
						.putExtra(SearchManager.APP_DATA, bundle)
				);
			}
		});
		builder.setNegativeButton(dialogResource.getResource("cancel").getValue(), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		return builder;
	}
}
