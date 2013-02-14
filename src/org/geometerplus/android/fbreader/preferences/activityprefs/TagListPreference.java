/*
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.preferences.activityprefs;

import java.util.*;

import android.content.Context;
import android.content.Intent;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.fbreader.book.Tag;

public class TagListPreference extends ZLActivityPreference {
	private final TreeMap<String,Tag> myTags = new TreeMap<String,Tag>();

	public TagListPreference(Context context, ListHolder holder, Map<Integer,ZLActivityPreference> map, ZLResource rootResource, String resourceKey) {
		super(context, holder, map, rootResource, resourceKey);
	}

	public void setTags(Collection<Tag> tags) {
		myTags.clear();
		for (Tag t : tags) {
			myTags.put(t.toString("/"), t);
		} 
	}

	@Override
	protected ArrayList<String> suggestions() {
		return new ArrayList<String>(myTags.keySet());
	}

	@Override
	protected Intent prepareIntent(Intent intent) {
		intent.setClass(getContext(), EditableSpinnerActivity.class);
		return intent;
	}
}
