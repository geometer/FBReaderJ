/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.optionsDialog;

import org.geometerplus.zlibrary.core.dialogs.ZLDialogContent;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextLineSpaceOptionEntry;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleDecoration;
import org.geometerplus.zlibrary.text.view.style.ZLTextFullStyleDecoration;

import static org.geometerplus.fbreader.bookmodel.FBTextKind.*;

class FormatOptionsPage extends OptionsPage {
	private static final String KEY_STYLE = "style";

	private static final String KEY_LINESPACING = "lineSpacing";
	
	public FormatOptionsPage(ZLDialogContent dialogTab) {
		final ZLResource styleResource = ZLResource.resource(KEY_STYLE);

		myComboEntry = new ComboOptionEntry(this);

		ZLTextStyleCollection collection = ZLTextStyleCollection.Instance();
		byte styles[] = { REGULAR, TITLE, SECTION_TITLE, SUBTITLE, H1, H2, H3, H4, H5, H6, ANNOTATION, EPIGRAPH, PREFORMATTED, AUTHOR,/* DATEKIND,*/ POEM_TITLE, STANZA, VERSE };
		final int stylesNumber = styles.length;
		for (int i = 0; i < stylesNumber; ++i) {
			final ZLTextStyleDecoration decoration = collection.getDecoration(styles[i]);
			if (decoration != null) {
				myComboEntry.addValue(styleResource.getResource(decoration.getName()).getValue());
			}
		}
		dialogTab.addOption("optionsFor", myComboEntry);

		for (int i = 0; i < stylesNumber; ++i) {
			ZLTextStyleDecoration d = collection.getDecoration(styles[i]);
			if (d instanceof ZLTextFullStyleDecoration) {
				ZLTextFullStyleDecoration decoration = (ZLTextFullStyleDecoration)d;
				final String name = styleResource.getResource(decoration.getName()).getValue();
				
				registerEntry(dialogTab,
					KEY_LINESPACING, new ZLTextLineSpaceOptionEntry(decoration.LineSpacePercentOption, dialogTab.getResource(KEY_LINESPACING)),
					name
				);
			}
		}

		myComboEntry.onValueSelected(0);
	}
}
