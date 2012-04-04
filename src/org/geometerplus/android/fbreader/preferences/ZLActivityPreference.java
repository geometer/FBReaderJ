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

import java.util.*;

import android.content.*;
import android.app.Activity;
import android.preference.Preference;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLMiscUtil;

class ZLActivityPreference extends Preference {

	public static interface ListHolder {

		public List<String> getValue();
		public void setValue(List<String> l);
	}

	private final ListHolder myOption;
	private final int myRequestCode;
	private final List<String> mySuggestions;
	private final String myType;

	ZLActivityPreference(Context context, ListHolder option, Map<Integer,ZLActivityPreference> map, List<String> suggestions, String type, ZLResource rootResource, String resourceKey) {
		super(context);
		myOption = option;
		myRequestCode = map.size();
		map.put(myRequestCode, this);
		mySuggestions = (suggestions != null) ? suggestions : new ArrayList<String>();
		myType = type;

		ZLResource resource = rootResource.getResource(resourceKey);
		setTitle(resource.getValue());
		updateSummary();
	}

	@Override
	protected void onClick() {
		final Intent intent = new Intent();
		intent.setClass(getContext(), EditableStringListActivity.class);
		intent.putStringArrayListExtra(
			EditableStringListActivity.LIST,
			new ArrayList<String>(myOption.getValue())
		);
		intent.putStringArrayListExtra(
			EditableStringListActivity.SUGGESTIONS,
			new ArrayList<String>(mySuggestions)
		);
		intent.putExtra(EditableStringListActivity.TITLE, getTitle());
		intent.putExtra(EditableStringListActivity.TYPE, myType);

		((Activity)getContext()).startActivityForResult(intent, myRequestCode);
	}

	public void setValue(Intent data) {
		final List<String> value = data.getStringArrayListExtra(EditableStringListActivity.LIST);
		myOption.setValue(value);
		updateSummary();
	}

	private void updateSummary() {
		setSummary(ZLMiscUtil.listToString(myOption.getValue(), ":"));
	}
}
