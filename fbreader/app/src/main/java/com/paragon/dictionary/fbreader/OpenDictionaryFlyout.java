/*
 * Copyright (C) 2013 Paragon Software Group
 * Author: Andrey Tumanov <Andrey_Tumanov@penreader.com>
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

package com.paragon.dictionary.fbreader;

import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.paragon.open.dictionary.api.*;

import org.geometerplus.android.fbreader.dict.DictionaryUtil;

public class OpenDictionaryFlyout {
	private final Direction myDirection;
	private final String myPackageName;

	public OpenDictionaryFlyout(Dictionary dictionary) {
		myDirection = dictionary.getDirection();
		myPackageName = dictionary.getApplicationPackageName();
	}

	private Dictionary getDictionary(final Context context) {
		if (myPackageName == null) {
			return null;
		}
		final OpenDictionaryAPI api = new OpenDictionaryAPI(context);
		HashSet<Dictionary> dictionaries = api.getDictionaries(myDirection);
		for (Dictionary dictionary : dictionaries) {
			if (myPackageName.equalsIgnoreCase(dictionary.getApplicationPackageName())) {
				return dictionary;
			}
		}
		Log.e("FBReader", "OpenDictionaryFlyout:getDictionary - Dictionary with direction [" +
				myDirection.toString() + "] and package name [" + myPackageName + "] not found");
		return null;
	}

	public void showTranslation(final Activity activity, final String text, DictionaryUtil.PopupFrameMetric frameMetrics) {
		Log.d("FBReader", "OpenDictionaryFlyout:showTranslation");
		final Dictionary dictionary = getDictionary(activity);
		if (dictionary == null) {
			Log.e("FBReader", "OpenDictionaryFlyout:showTranslation - null dictionary received");
			return;
		}

		if (!dictionary.isTranslationAsTextSupported()) {
			dictionary.showTranslation(text);
		} else {
			OpenDictionaryActivity.setDictionary(dictionary);
			Intent intent = new Intent(activity, OpenDictionaryActivity.class);
			intent.putExtra(OpenDictionaryActivity.OPEN_DICTIONARY_QUERY_KEY, text);
			intent.putExtra(OpenDictionaryActivity.OPEN_DICTIONARY_HEIGHT_KEY, frameMetrics.Height);
			intent.putExtra(OpenDictionaryActivity.OPEN_DICTIONARY_GRAVITY_KEY, frameMetrics.Gravity);
			activity.startActivity(intent);
		}
	}
}
