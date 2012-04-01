/*
 * Copyright (C) 2009-2012 Geometer Plus <contact@geometerplus.com>
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

import android.content.*;
import android.app.Activity;
import android.preference.Preference;

import org.geometerplus.zlibrary.core.options.ZLStringListOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

class ZLActivityPreference extends Preference {

	private final ZLStringListOption myOption;
	private final int myRequestCode;

	ZLActivityPreference(Context context, ZLStringListOption option, int requestCode, ZLResource rootResource, String resourceKey) {
		super(context);
		myOption = option;
		myRequestCode = requestCode;

		ZLResource resource = rootResource.getResource(resourceKey);
		setTitle(resource.getValue());
		setSummary(ZLMiscUtil.listToString(myOption.getValue(), ": "));
	}

	@Override
	protected void onClick() {
		final Intent intent = new Intent();
		intent.setClass(getContext(), EditableStringListActivity.class);
		intent.putExtra(EditableStringListActivity.LIST, ZLMiscUtil.listToString(myOption.getValue(), "\n"));
		intent.putExtra(EditableStringListActivity.TITLE, getTitle());

		((Activity)getContext()).startActivityForResult(intent, myRequestCode);
	}

	public void setValue(Intent data) {
		String value = data.getStringExtra(EditableStringListActivity.LIST);
		myOption.setValue(ZLMiscUtil.stringToList(value, "\n"));
		setSummary(ZLMiscUtil.listToString(myOption.getValue(), ": "));
	}

}
