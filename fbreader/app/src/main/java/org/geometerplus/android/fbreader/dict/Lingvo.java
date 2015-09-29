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

import android.content.Intent;

import com.abbyy.mobile.lingvo.api.MinicardContract;

import org.geometerplus.zlibrary.core.language.Language;

import org.geometerplus.android.fbreader.FBReaderMainActivity;

final class Lingvo extends DictionaryUtil.PackageInfo {
	Lingvo(String id, String title) {
		super(id, title, true);
	}

	@Override
	void open(String text, Runnable outliner, FBReaderMainActivity fbreader, DictionaryUtil.PopupFrameMetric frameMetrics) {
		final Intent intent = getActionIntent(text);
		intent.putExtra(MinicardContract.EXTRA_GRAVITY, frameMetrics.Gravity);
		intent.putExtra(MinicardContract.EXTRA_HEIGHT, frameMetrics.Height);
		intent.putExtra(MinicardContract.EXTRA_FORCE_LEMMATIZATION, true);
		intent.putExtra(MinicardContract.EXTRA_TRANSLATE_VARIANTS, true);
		intent.putExtra(MinicardContract.EXTRA_LIGHT_THEME, true);
		final String targetLanguage = DictionaryUtil.TargetLanguageOption.getValue();
		if (!Language.ANY_CODE.equals(targetLanguage)) {
			intent.putExtra(MinicardContract.EXTRA_LANGUAGE_TO, targetLanguage);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		InternalUtil.startDictionaryActivity(fbreader, intent, this);
	}
}
