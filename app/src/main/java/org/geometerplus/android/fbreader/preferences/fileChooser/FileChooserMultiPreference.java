/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.options.ZLStringListOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MiscUtil;

import org.geometerplus.android.util.FileChooserUtil;

class FileChooserMultiPreference extends FileChooserPreference {
	private final ZLStringListOption myOption;

	FileChooserMultiPreference(Context context, ZLResource rootResource, String resourceKey, ZLStringListOption option, int requestCode, Runnable onValueSetAction) {
		super(context, rootResource, resourceKey, false, requestCode, onValueSetAction);

		myOption = option;

		setSummary(getStringValue());
	}

	@Override
	protected void onClick() {
		FileChooserUtil.runFolderListDialog(
			(Activity)getContext(),
			myRequestCode,
			myResource.getValue(),
			myResource.getResource("chooserTitle").getValue(),
			myOption.getValue(),
			myChooseWritableDirectoriesOnly
		);
	}

	@Override
	protected String getStringValue() {
		return MiscUtil.join(myOption.getValue(), ", ");
	}

	@Override
	protected void setValueFromIntent(Intent data) {
		final List<String> value = FileChooserUtil.pathListFromData(data);
		if (value.isEmpty()) {
			return;
		}

		myOption.setValue(value);
		setSummary(getStringValue());

		if (myOnValueSetAction != null) {
			myOnValueSetAction.run();
		}
	}
}
