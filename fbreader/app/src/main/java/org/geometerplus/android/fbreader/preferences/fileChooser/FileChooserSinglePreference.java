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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MiscUtil;

import org.geometerplus.android.util.FileChooserUtil;

class FileChooserSinglePreference extends FileChooserPreference {
	private final ZLStringOption myOption;

	FileChooserSinglePreference(Context context, ZLResource rootResource, String resourceKey, ZLStringOption option, int requestCode, Runnable onValueSetAction) {
		super(context, rootResource, resourceKey, true, requestCode, onValueSetAction);
		myOption = option;

		setSummary(getStringValue());
	}

	@Override
	protected void onClick() {
		FileChooserUtil.runDirectoryChooser(
			(Activity)getContext(),
			myRequestCode,
			myResource.getResource("chooserTitle").getValue(),
			getStringValue(),
			myChooseWritableDirectoriesOnly
		);
	}

	@Override
	protected String getStringValue() {
		return myOption.getValue();
	}

	@Override
	protected void setValueFromIntent(Intent data) {
		final String value = FileChooserUtil.folderPathFromData(data);
		if (MiscUtil.isEmptyString(value)) {
			return;
		}

		final String currentValue = myOption.getValue();
		if (!currentValue.equals(value)) {
			myOption.setValue(value);
			setSummary(value);
		}

		if (myOnValueSetAction != null) {
			myOnValueSetAction.run();
		}
	}
}
