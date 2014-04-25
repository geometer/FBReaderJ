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

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;

import org.geometerplus.android.util.FileChooserUtil;
import org.geometerplus.zlibrary.core.options.ZLStringListOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

class FileChooserStringListPreference extends FileChooserPreference {
	private final ZLStringListOption myOption;

	FileChooserStringListPreference(Context context, ZLResource rootResource, String resourceKey, ZLStringListOption option, int regCode, Runnable onValueSetAction) {
		super(context, rootResource, resourceKey, false, regCode, onValueSetAction);

		myOption = option;

		setSummary(getStringValue());
	}

	@Override
	protected void onClick() {
		FileChooserUtil.runDirectoriesManager(
            (Activity)getContext(),
            myRegCode,
			getStringListValue()
		);
	}
	
	private ArrayList<String> getStringListValue(){
		final List<String> values = myOption.getValue();
		ArrayList<String> resList = new ArrayList<String>();
		resList.addAll(values);
		return resList;
	}
	
	@Override
	protected String getStringValue() {
		final List<String> values = myOption.getValue();
		return values.isEmpty() ? "" : values.get(0);
	}

	@Override
	protected void setValueInternal(String value) {
		//System.out.println("FileChooserStringListPreference::setValueInternal() "+value);
		List<String> currentValues = myOption.getValue();
		//System.out.println("1 FileChooserStringListPreference::setValueInternal() currentValue "+currentValues);
		if (currentValues.size() != 1 || !currentValues.get(0).equals(value)) {
			myOption.setValue(Collections.singletonList(value));
			setSummary(value);
		}
		/*boolean found = false;
		for(int i = 0; i < currentValues.size();i++){
			if(currentValues.get(i).equals(value)){
				currentValues.remove(i);
				currentValues.add(i, value);
				found = true;
			}
		}
		if(!found){
			currentValues.add(value);
		}
		myOption.setValue(currentValues);
		setSummary(value);
		System.out.println("2 FileChooserStringListPreference::setValueInternal() currentValue "+currentValues);*/
	}
	
	@Override
	protected void setValueInternal(ArrayList<String> value) {
		System.out.println("FileChooserStringListPreference::setValueInternal() "+value);
		if(value.size() > 0){
			myOption.setValue(value);
			setSummary(value.get(0));
		}
	}
}
