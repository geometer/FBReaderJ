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

package org.geometerplus.android.fbreader.dict;

import java.util.*;

import android.content.Context;

import com.paragon.dictionary.fbreader.OpenDictionaryFlyout;
import com.paragon.open.dictionary.api.Dictionary;
import com.paragon.open.dictionary.api.OpenDictionaryAPI;

import org.geometerplus.android.fbreader.FBReaderMainActivity;

final class OpenDictionary extends DictionaryUtil.PackageInfo {
	static void collect(Context context, Map<DictionaryUtil.PackageInfo,Integer> dictMap) {
		final SortedSet<Dictionary> dictionariesTreeSet =
			new TreeSet<Dictionary>(new Comparator<Dictionary>() {
				@Override
				public int compare(Dictionary lhs, Dictionary rhs) {
					return lhs.toString().compareTo(rhs.toString());
				}
			}
		);
		dictionariesTreeSet.addAll(
			new OpenDictionaryAPI(context).getDictionaries()
		);

		for (Dictionary dict : dictionariesTreeSet) {
			dictMap.put(new OpenDictionary(dict), DictionaryUtil.FLAG_SHOW_AS_DICTIONARY);
		}
	}

	final OpenDictionaryFlyout Flyout;

	OpenDictionary(Dictionary dictionary) {
		super(dictionary.getUID(), dictionary.getName());
		put("package", dictionary.getApplicationPackageName());
		put("class", ".Start");
		Flyout = new OpenDictionaryFlyout(dictionary);
	}

	@Override
	void open(String text, Runnable outliner, FBReaderMainActivity fbreader, DictionaryUtil.PopupFrameMetric frameMetrics) {
		Flyout.showTranslation(fbreader, text, frameMetrics);
	}
}
