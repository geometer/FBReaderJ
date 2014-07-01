/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.preferences;

import android.content.Context;
import android.content.pm.*;
import android.content.Intent;
import android.preference.*;

import android.net.Uri;

import java.util.*;

import org.geometerplus.zlibrary.core.filetypes.FileType;
import org.geometerplus.zlibrary.core.filetypes.FileTypeCollection;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.fbreader.formats.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MimeType;
import org.geometerplus.android.fbreader.preferences.ZLPreferenceActivity.Screen;

class FormatPreference extends ListPreference {
	private final ZLStringOption myOption;
	private final HashSet<String> myPaths = new HashSet<String>();
	private final Screen myScreen;
	private final String myFormat;
	private final boolean myIsJava;
	private final boolean myIsNative;
	private final boolean myIsPredefined;
	private final boolean myIsPlugin;
	private final ZLResource myResource;

	FormatPreference(Context context, String formatName, Screen scr, ZLResource resource, String resourceKey) {
		super(context);

		myOption = Formats.filetypeOption(formatName);
		myFormat = formatName;
		FileType ft = FileTypeCollection.Instance.typeById(myFormat);
		setTitle(ft.Id);
		myScreen = scr;
		myIsJava = PluginCollection.Instance().getPlugin(ft, FormatPlugin.Type.JAVA) != null;
		myIsNative = PluginCollection.Instance().getPlugin(ft, FormatPlugin.Type.NATIVE) != null;
		myIsPlugin = PluginCollection.Instance().getPlugin(ft, FormatPlugin.Type.EXTERNAL) != null;
		myIsPredefined = Formats.getPredefinedFormats().contains(formatName);
		myResource = resource.getResource(resourceKey);
		final String emptySummary = myResource.getResource("appNotSet").getValue();

		if (!myOption.getValue().equals("") && (!myOption.getValue().equals(Formats.JAVA_OPTION)) && (!myOption.getValue().equals(Formats.PLUGIN_OPTION)) && (!myOption.getValue().equals(Formats.NATIVE_OPTION))) {
			final PackageManager pm = getContext().getPackageManager();
			try {
				ApplicationInfo info = pm.getApplicationInfo(myOption.getValue(), 0);
				setSummary(info.loadLabel(pm).toString());
			} catch (PackageManager.NameNotFoundException e) {
				if (myIsJava) {
					myOption.setValue(Formats.JAVA_OPTION);
					setSummary(myResource.getResource("java").getValue());
				} else if (myIsNative) {
					myOption.setValue(Formats.NATIVE_OPTION);
					setSummary(myResource.getResource("native").getValue());
				} else if (myIsPlugin) {
					myOption.setValue(Formats.PLUGIN_OPTION);
					setSummary(myResource.getResource("plugin").getValue().replace("%s", myFormat));
				} else {
					myOption.setValue("");
					setSummary(emptySummary);
				}
			}
		} else if (myOption.getValue().equals(Formats.JAVA_OPTION)) {
			setSummary(myResource.getResource("java").getValue());
		} else if (myOption.getValue().equals(Formats.NATIVE_OPTION)) {
			setSummary(myResource.getResource("native").getValue());
		} else if (myOption.getValue().equals(Formats.PLUGIN_OPTION)) {
			setSummary(myResource.getResource("plugin").getValue().replace("%s", myFormat));
		} else {
			setSummary(emptySummary);
		}
	}

	protected void onClick() {
		fillList();
		super.onClick();
	}

	public boolean runIfNotEmpty() {
		if (fillList()) {
			super.onClick();
			return true;
		} else {
			return false;
		}
	}

	protected boolean fillList() {
		final PackageManager pm = getContext().getPackageManager();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		//FIXME: is it right way to obtain extension?
		String extension = FileTypeCollection.Instance.typeById(myFormat).defaultExtension(FileTypeCollection.Instance.typeById(myFormat).mimeTypes().get(0));
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("file:///sdcard/fgsfds." + extension));
		myPaths.add("org.geometerplus.zlibrary.ui.android");
		if (myIsJava) {
			values.add(Formats.JAVA_OPTION);
			names.add(myResource.getResource("java").getValue());
		}
		if (myIsNative) {
			values.add(Formats.NATIVE_OPTION);
			names.add(myResource.getResource("native").getValue());
		}
		if (myIsPlugin) {
			values.add(Formats.PLUGIN_OPTION);
			names.add(myResource.getResource("plugin").getValue().replace("%s", myFormat));
		}
		final FileType ft = FileTypeCollection.Instance.typeById(myFormat);
		for (MimeType type : ft.mimeTypes()) {
			intent.setDataAndType(Uri.parse("file:///sdcard/fgsfds." + extension), type.Name);
			for (ResolveInfo packageInfo : pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
				if (!myPaths.contains(packageInfo.activityInfo.applicationInfo.packageName)) {
					values.add(packageInfo.activityInfo.applicationInfo.packageName);
					names.add(packageInfo.activityInfo.applicationInfo.loadLabel(pm).toString());
					myPaths.add(packageInfo.activityInfo.applicationInfo.packageName);
				}
			}
		}
		boolean foundSomething = (values.size() > 0);
		myPaths.clear();
		if (!myIsPredefined) {
			final String deleteItem = myResource.getResource("delete").getValue();
			values.add("DELETE");
			names.add(deleteItem);
		}
		if (myIsPredefined && !myIsJava && !myIsNative && !myIsPlugin) {
			values.add("");
			names.add(myResource.getResource("appNotSet").getValue());
		}
		setEntries(names.toArray(new String[names.size()]));
		setEntryValues(values.toArray(new String[values.size()]));
		if (!myOption.getValue().equals("") || myIsPredefined) {
			setValue(myOption.getValue());
		}
		return foundSomething;

	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			if (getValue().equals("DELETE")) {
				myScreen.removePreference(this);
				Formats.removeFormat(getTitle().toString());
				myOption.setValue("");
			} else {
				setSummary(getEntry());
				myOption.setValue(getValue());
			}
		}
	}
}
