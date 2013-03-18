/*
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.network.action;

import java.util.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;

public class LanguageFilterAction extends RootAction {
	public LanguageFilterAction(Activity activity) {
		super(activity, ActionCode.LANGUAGE_FILTER, "languages", R.drawable.ic_menu_languages);
	}

	@Override
	public void run(NetworkTree tree) {
		final NetworkLibrary library = NetworkLibrary.Instance();

		final List<Language> allLanguages = new ArrayList<Language>();
		for (String code : library.languageCodes()) {
			allLanguages.add(new Language(code));
		}
		Collections.sort(allLanguages);
		final Collection<String> activeLanguageCodes = library.activeLanguageCodes();
		final CharSequence[] languageNames = new CharSequence[allLanguages.size()];
		final boolean[] checked = new boolean[allLanguages.size()];

		int index = 0;
		for (Language language : allLanguages) {
			languageNames[index] = language.Name;
			checked[index] = activeLanguageCodes.contains(language.Code);
			++index;
		}

		final DialogInterface.OnMultiChoiceClickListener listener =
			new DialogInterface.OnMultiChoiceClickListener() {
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					checked[which] = isChecked;
				}
			};
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final AlertDialog dialog = new AlertDialog.Builder(myActivity)
			.setMultiChoiceItems(languageNames, checked, listener)
			.setTitle(dialogResource.getResource("languageFilterDialog").getResource("title").getValue())
			.setPositiveButton(dialogResource.getResource("button").getResource("ok").getValue(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					final TreeSet<Language> newActiveLanguages = new TreeSet<Language>();
					for (int i = 0; i < checked.length; ++i) {
						if (checked[i]) {
							newActiveLanguages.add(allLanguages.get(i));
						}
					}
					final List<String> codes = new ArrayList<String>(newActiveLanguages.size());
					for (Language language : newActiveLanguages) {
						codes.add(language.Code);
					}
					library.setActiveLanguageCodes(codes);
					library.synchronize();
				}
			})
			.create();
		dialog.show();
	}
}
