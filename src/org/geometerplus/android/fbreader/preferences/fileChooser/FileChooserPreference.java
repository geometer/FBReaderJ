/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.preferences.fileChooser;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.preference.Preference;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;

import org.geometerplus.zlibrary.core.resources.ZLResource;

abstract class FileChooserPreference extends Preference {
	private final int myRegCode;
	private final ZLResource myResource;
	private final IFileProvider.FilterMode myFilterMode;

	FileChooserPreference(Context context, ZLResource rootResource, String resourceKey, IFileProvider.FilterMode filterMode, int regCode) {
		super(context);

		myRegCode = regCode;
		myFilterMode = filterMode;
		myResource = rootResource.getResource(resourceKey);
		setTitle(myResource.getValue());
	}

	@Override
	protected void onClick() {
		final HashMap<String,String> textResources = new HashMap<String,String>();
		textResources.put("title", myResource.getResource("chooserTitle").getValue());
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		textResources.put("ok", buttonResource.getResource("ok").getValue());
		textResources.put("cancel", buttonResource.getResource("cancel").getValue());
		final ZLResource resource = dialogResource.getResource("fileChooser");
		textResources.put("root", resource.getResource("root").getValue());
		textResources.put("newFolder", resource.getResource("newFolder").getValue());
		textResources.put("folderNameHint", resource.getResource("folderNameHint").getValue());
		final ZLResource menuResource = resource.getResource("menu");
		textResources.put("menuOrigin", menuResource.getResource("origin").getValue());
		textResources.put("menuReload", menuResource.getResource("reload").getValue());
		final ZLResource sortResource = resource.getResource("sortBy");
		textResources.put("sortBy", sortResource.getValue());
		textResources.put("sortByName", sortResource.getResource("name").getValue());
		textResources.put("sortBySize", sortResource.getResource("size").getValue());
		textResources.put("sortByDate", sortResource.getResource("date").getValue());
		textResources.put("permissionDenied", resource.getResource("permissionDenied").getValue());
		
		final Intent intent = new Intent(getContext(), FileChooserActivity.class);
		intent.putExtra(FileChooserActivity._TextResources, textResources);
		intent.putExtra(FileChooserActivity._Rootpath, (Parcelable)new LocalFile(getStringValue()));
		intent.putExtra(FileChooserActivity._ActionBar, true);
		intent.putExtra(FileChooserActivity._SaveLastLocation, false);
		intent.putExtra(FileChooserActivity._DisplayHiddenFiles, true);
		intent.putExtra(FileChooserActivity._FilterMode, myFilterMode);
		//intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.AnyDirectories);
		//intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
		//intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.FilesOnly);
		//intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.FilesAndDirectories);
		((Activity)getContext()).startActivityForResult(intent, myRegCode);
	}

	protected abstract String getStringValue();
	protected abstract void setValue(String value);
}
