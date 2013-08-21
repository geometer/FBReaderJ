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

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.paragon.open.dictionary.api.*;
import org.geometerplus.android.fbreader.DictionaryUtil;


public class OpenDictionaryFlyout {
    private final Dictionary myDictionary;

    public OpenDictionaryFlyout(Dictionary dictionary) {
        myDictionary = dictionary;
    }

    public void showTranslation(final Activity activity, final String text, DictionaryUtil.PopupFrameMetric frameMetrics) {
        Log.d("FBReader", "OpenDictionaryFlyout:showTranslation");
        if (!myDictionary.isTranslationAsTextSupported())
            myDictionary.showTranslation(text);
        else
        {
            OpenDictionaryActivity.setDictionary(myDictionary);
            Intent intent = new Intent(activity, OpenDictionaryActivity.class);
            intent.putExtra(OpenDictionaryActivity.OPEN_DICTIONARY_QUERY_KEY, text);
            intent.putExtra(OpenDictionaryActivity.OPEN_DICTIONARY_HEIGHT_KEY, frameMetrics.height);
            intent.putExtra(OpenDictionaryActivity.OPEN_DICTIONARY_GRAVITY_KEY, frameMetrics.gravity);
            activity.startActivity(intent);
        }
    }
}