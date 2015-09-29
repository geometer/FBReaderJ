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

import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.OnClickWrapper;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.android.fbreader.FBReaderMainActivity;

final class Dictan extends DictionaryUtil.PackageInfo {
	private static final int MAX_LENGTH_FOR_TOAST = 180;

	Dictan(String id, String title) {
		super(id, title);
	}

	@Override
	void open(String text, Runnable outliner, FBReaderMainActivity fbreader, DictionaryUtil.PopupFrameMetric frameMetrics) {
		final Intent intent = getActionIntent(text);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		intent.putExtra("article.mode", 20);
		intent.putExtra("article.text.size.max", MAX_LENGTH_FOR_TOAST);
		try {
			fbreader.startActivityForResult(intent, FBReaderMainActivity.REQUEST_DICTIONARY);
			fbreader.overridePendingTransition(0, 0);
			if (outliner != null) {
				outliner.run();
			}
		} catch (ActivityNotFoundException e) {
			InternalUtil.installDictionaryIfNotInstalled(fbreader, this);
		}
	}

	void onActivityResult(final FBReaderMainActivity fbreader, int resultCode, final Intent data) {
		if (data == null) {
			fbreader.hideDictionarySelection();
			return;
		}

		final int errorCode = data.getIntExtra("error.code", -1);
		if (resultCode != FBReaderMainActivity.RESULT_OK || errorCode != -1) {
			showError(fbreader, errorCode, data);
			return;
		}

		String text = data.getStringExtra("article.text");
		if (text == null) {
			showError(fbreader, -1, data);
			return;
		}
		// a hack for obsolete (before 5.0 beta) dictan versions
		final int index = text.indexOf("\000");
		if (index >= 0) {
			text = text.substring(0, index);
		}
		final boolean hasExtraData;
		if (text.length() == MAX_LENGTH_FOR_TOAST) {
			text = trimArticle(text);
			hasExtraData = true;
		} else {
			hasExtraData = data.getBooleanExtra("article.resources.contains", false);
		}

		final SuperActivityToast toast;
		if (hasExtraData) {
			toast = new SuperActivityToast(fbreader, SuperToast.Type.BUTTON);
			toast.setButtonIcon(
				android.R.drawable.ic_menu_more,
				ZLResource.resource("toast").getResource("more").getValue()
			);
			toast.setOnClickWrapper(new OnClickWrapper("dict", new SuperToast.OnClickListener() {
				@Override
				public void onClick(View view, Parcelable token) {
					final String word = data.getStringExtra("article.word");
					final Intent intent = getActionIntent(word);
					try {
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						fbreader.startActivity(intent);
						fbreader.overridePendingTransition(0, 0);
					} catch (ActivityNotFoundException e) {
						// ignore
					}
				}
			}));
		} else {
			toast = new SuperActivityToast(fbreader, SuperToast.Type.STANDARD);
		}
		toast.setText(text);
		toast.setDuration(DictionaryUtil.TranslationToastDurationOption.getValue().Value);
		InternalUtil.showToast(toast, fbreader);
	}

	private static String trimArticle(String text) {
		final int len = text.length();
		final int eolIndex = text.lastIndexOf("\n");
		final int spaceIndex = text.lastIndexOf(" ");
		if (spaceIndex < eolIndex || eolIndex >= len * 2 / 3) {
			return text.substring(0, eolIndex);
		} else {
			return text.substring(0, spaceIndex);
		}
	}

	private static void showError(final FBReaderMainActivity fbreader, int code, Intent data) {
		final ZLResource resource = ZLResource.resource("dictanErrors");
		String message;
		switch (code) {
			default:
				message = data.getStringExtra("error.message");
				if (message == null) {
					message = resource.getResource("unknown").getValue();
				}
				break;
			case 100:
			{
				final String word = data.getStringExtra("article.word");
				message = resource.getResource("noArticle").getValue().replaceAll("%s", word);
				break;
			}
			case 130:
				message = resource.getResource("cannotOpenDictionary").getValue();
				break;
			case 131:
				message = resource.getResource("noDictionarySelected").getValue();
				break;
		}

		final SuperActivityToast toast = new SuperActivityToast(fbreader, SuperToast.Type.STANDARD);
		toast.setText("Dictan: " + message);
		toast.setDuration(DictionaryUtil.ErrorToastDurationOption.getValue().Value);
		InternalUtil.showToast(toast, fbreader);
	}
}
