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

import java.util.*;

import android.app.Activity;
import android.content.Context;

import org.geometerplus.zlibrary.core.options.ZLStringListOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.android.util.FileChooserUtil;

class FileChooserStringListPreference extends FileChooserPreference {
	private final ZLStringListOption myOption;
	private final ZLResource myResource;

	FileChooserStringListPreference(Context context, ZLResource rootResource, String resourceKey, ZLStringListOption option, int regCode, Runnable onValueSetAction) {
		super(context, rootResource, resourceKey, false, regCode, onValueSetAction);

		myOption = option;
		
		myResource = rootResource.getResource(resourceKey);

		setSummary(getStringValue());
	}

	@Override
	protected void onClick() {
		FileChooserUtil.runDirectoryManager(
            (Activity)getContext(),
            myRegCode,
			myResource.getValue(),
			myResource.getResource("chooserTitle").getValue(),
			getStringListValue(),
			myChooseWritableDirectoriesOnly
		);
	}
	
	private ArrayList<String> getStringListValue(){
		return new ArrayList<String>(myOption.getValue());
	}
	
	@Override
	protected String getStringValue() {
		return MiscUtil.join(myOption.getValue(), ", ");
	}

	@Override
	protected void setValueInternal(String value) {
		List<String> currentValues = myOption.getValue();
		if (currentValues.size() != 1 || !currentValues.get(0).equals(value)) {
			myOption.setValue(Collections.singletonList(value));
			setSummary(getStringValue());
		}
	}

	protected final void setValue(ArrayList<String> value) {
		if (value.isEmpty()){
			return;
		}

		myOption.setValue(value);
		setSummary(getStringValue());

		if (myOnValueSetAction != null) {
			myOnValueSetAction.run();
		}
	}
}
