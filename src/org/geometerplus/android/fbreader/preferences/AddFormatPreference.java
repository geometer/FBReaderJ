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

//package org.geometerplus.android.fbreader.preferences;

//import java.util.*;

//import org.geometerplus.android.fbreader.preferences.ZLPreferenceActivity.Screen;
//import org.geometerplus.fbreader.formats.Formats;
//import org.geometerplus.fbreader.filetype.*;
//import org.geometerplus.zlibrary.core.resources.ZLResource;

//import android.content.Context;
//import android.preference.*;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;

//import android.util.Log;

//class AddFormatPreference extends EditTextPreference {
//	private final Screen myScreen;
//	private final ZLResource myResource;

//	AddFormatPreference(Context context, Screen scr, ZLResource resource, String resourceKey) {
//		super(context);

//		myScreen = scr;
//		myResource = resource.getResource(resourceKey);
//		setTitle(myResource.getResource("addNewFormat").getValue());
//		setOrder(100500);
//		getEditText().setSingleLine();
//	}

//	private void showErrorDialog(final String errName) {
//		((Activity)getContext()).runOnUiThread(new Runnable() {
//			public void run() {
//				final String title = errName;
//				new AlertDialog.Builder(((Activity)getContext()))
//					.setTitle(title)
//					.setIcon(0)
//					.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int which) {
//						}
//					})
//					.create().show();
//				}
//		});
//	}

//	@Override
//	protected void onDialogClosed(boolean result) {
//		if (result) {
//			FileType f = Formats.getExistingFileType(getEditText().getText().toString());
//			if (f != null) {
//				showErrorDialog(ZLResource.resource("errorMessage").getResource("formatExists").getValue().replace("%s", f.Id));
//				super.onDialogClosed(result);
//				return;
//			}
//			if (Formats.addFormat(getEditText().getText().toString())) {
//				FormatPreference newPref = (FormatPreference)myScreen.addPreference(new FormatPreference(getContext(), getEditText().getText().toString(), myScreen, myScreen.Resource, "format"));

//				if (!newPref.runIfNotEmpty()) {
//					showErrorDialog(ZLResource.resource("errorMessage").getResource("appNotFound").getValue());

//					myScreen.removePreference(newPref);
//					Formats.removeFormat(getEditText().getText().toString());
//				}
//			} else {
//				showErrorDialog(ZLResource.resource("errorMessage").getResource("invalidFormat").getValue());
//			}
//		}
//		super.onDialogClosed(result);
//	}

//}
