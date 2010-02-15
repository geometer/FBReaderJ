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
import org.geometerplus.zlibrary.core.optionEntries.ZLSimpleSpinOptionEntry;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextAlignmentOptionEntry;
import org.geometerplus.zlibrary.text.view.ZLTextLineSpaceOptionEntry;
import org.geometerplus.zlibrary.text.view.style.ZLTextBaseStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleDecoration;
import org.geometerplus.zlibrary.text.view.style.ZLTextFullStyleDecoration;

import static org.geometerplus.fbreader.bookmodel.FBTextKind.*;

class FormatOptionsPage extends OptionsPage {
	private static final String KEY_STYLE = "style";
	private static final String KEY_BASE = "Base";

	private static final String KEY_DUMMY = "";
	private static final String KEY_LINESPACING = "lineSpacing";
	private static final String KEY_FIRSTLINEINDENT = "firstLineIndent";
	private static final String KEY_ALIGNMENT = "alignment";
	private static final String KEY_SPACEBEFORE = "spaceBefore";
	private static final String KEY_SPACEAFTER = "spaceAfter";
	private static final String KEY_LEFTINDENT = "leftIndent";
	private static final String KEY_RIGHTINDENT = "rightIndent";
	
	public FormatOptionsPage(ZLDialogContent dialogTab) {
		final ZLResource styleResource = ZLResource.resource(KEY_STYLE);

		myComboEntry = new ComboOptionEntry(this, styleResource.getResource(KEY_BASE).getValue());
		myComboEntry.addValue(myComboEntry.initialValue());

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

		{
			final String name = myComboEntry.initialValue();
			ZLTextBaseStyle baseStyle = collection.getBaseStyle();

			registerEntries(dialogTab,
				KEY_LINESPACING, new ZLTextLineSpaceOptionEntry(baseStyle.LineSpacePercentOption, dialogTab.getResource(KEY_LINESPACING), false),
				KEY_DUMMY, null,//new ZLSimpleSpinOptionEntry("First Line Indent", baseStyle.firstLineIndentDeltaOption(), -300, 300, 1),
				name
			);

			registerEntries(dialogTab,
				KEY_ALIGNMENT, new ZLTextAlignmentOptionEntry(baseStyle.AlignmentOption, dialogTab.getResource(KEY_ALIGNMENT), false),
				KEY_DUMMY, null,
				name
			);
		}

		for (int i = 0; i < stylesNumber; ++i) {
			ZLTextStyleDecoration d = collection.getDecoration(styles[i]);
			if ((d != null) && (d.isFullDecoration())) {
				ZLTextFullStyleDecoration decoration = (ZLTextFullStyleDecoration) d;
				final String name = styleResource.getResource(decoration.getName()).getValue();
				
				registerEntries(dialogTab,
					KEY_SPACEBEFORE, new ZLSimpleSpinOptionEntry(decoration.SpaceBeforeOption, 1),
					KEY_LEFTINDENT, new ZLSimpleSpinOptionEntry(decoration.LeftIndentOption, 1),
					name
				);
				
				registerEntries(dialogTab,
					KEY_SPACEAFTER, new ZLSimpleSpinOptionEntry(decoration.SpaceAfterOption, 1),
					KEY_RIGHTINDENT, new ZLSimpleSpinOptionEntry(decoration.RightIndentOption, 1),
					name
				);
				
				registerEntries(dialogTab,
					KEY_LINESPACING, new ZLTextLineSpaceOptionEntry(decoration.LineSpacePercentOption, dialogTab.getResource(KEY_LINESPACING), true),
					KEY_FIRSTLINEINDENT, new ZLSimpleSpinOptionEntry(decoration.FirstLineIndentDeltaOption, 1),
					name
				);

				registerEntries(dialogTab,
					KEY_ALIGNMENT, new ZLTextAlignmentOptionEntry(decoration.AlignmentOption, dialogTab.getResource(KEY_ALIGNMENT), true),
					KEY_DUMMY, null,
					name
				);
			}
		}

		myComboEntry.onValueSelected(0);
	}
}
