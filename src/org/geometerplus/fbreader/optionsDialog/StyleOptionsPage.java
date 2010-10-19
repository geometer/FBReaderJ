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
import org.geometerplus.zlibrary.core.optionEntries.ZLFontFamilyOptionEntry;
import org.geometerplus.zlibrary.core.optionEntries.ZLSimpleBoolean3OptionEntry;
import org.geometerplus.zlibrary.core.optionEntries.ZLSimpleBooleanOptionEntry;
import org.geometerplus.zlibrary.core.optionEntries.ZLSimpleSpinOptionEntry;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.view.ZLTextFontFamilyWithBaseOptionEntry;
import org.geometerplus.zlibrary.text.view.style.ZLTextBaseStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleDecoration;

import org.geometerplus.fbreader.bookmodel.FBTextKind;

public class StyleOptionsPage extends OptionsPage implements FBTextKind {
	private final static String KEY_STYLE = "style";
	private final static String KEY_BASE = "Base";

	private final static String KEY_BOLD = "bold";
	private final static String KEY_ITALIC = "italic";
	private final static String KEY_FONTFAMILY = "fontFamily";
	private final static String KEY_FONTSIZE = "fontSize";
	private final static String KEY_FONTSIZEDIFFERENCE = "fontSizeDifference";
	private final static String KEY_ALLOWHYPHENATIONS = "allowHyphenations";
	private final static String KEY_AUTOHYPHENATIONS = "autoHyphenations";
	
	public StyleOptionsPage(ZLDialogContent dialogTab, ZLPaintContext context) {
		final ZLResource styleResource = ZLResource.resource(KEY_STYLE);

		myComboEntry = new ComboOptionEntry(this, styleResource.getResource(KEY_BASE).getValue());
		myComboEntry.addValue(myComboEntry.initialValue());

		ZLTextStyleCollection collection = ZLTextStyleCollection.Instance();
		byte styles[] = { REGULAR, TITLE, SECTION_TITLE, SUBTITLE, H1, H2, H3, H4, H5, H6, ANNOTATION, EPIGRAPH, AUTHOR, POEM_TITLE, STANZA, VERSE, CITE, INTERNAL_HYPERLINK, EXTERNAL_HYPERLINK, FOOTNOTE, ITALIC, EMPHASIS, BOLD, STRONG, DEFINITION, DEFINITION_DESCRIPTION, PREFORMATTED, CODE };
		final int STYLES_NUMBER = styles.length;
		for (int i = 0; i < STYLES_NUMBER; ++i) {
			final ZLTextStyleDecoration decoration = collection.getDecoration(styles[i]);
			if (decoration != null) {
				myComboEntry.addValue(styleResource.getResource(decoration.getName()).getValue());
			}
		}
		dialogTab.addOption("optionsFor", myComboEntry);

		{
			final String name = myComboEntry.initialValue();
			ZLTextBaseStyle baseStyle = collection.getBaseStyle();

			registerEntry(dialogTab,
				KEY_FONTFAMILY, new ZLFontFamilyOptionEntry(baseStyle.FontFamilyOption, context),
				name
			);

			registerEntry(dialogTab,
				KEY_FONTSIZE, new ZLSimpleSpinOptionEntry(baseStyle.FontSizeOption, 2),
				name
			);

			registerEntry(dialogTab,
				KEY_BOLD, new ZLSimpleBooleanOptionEntry(baseStyle.BoldOption),
				name
			);

			registerEntry(dialogTab,
				KEY_ITALIC, new ZLSimpleBooleanOptionEntry(baseStyle.ItalicOption),
				name
			);

			registerEntry(dialogTab,
				KEY_AUTOHYPHENATIONS, new ZLSimpleBooleanOptionEntry(baseStyle.AutoHyphenationOption),
				name
			);
		}

		for (int i = 0; i < STYLES_NUMBER; ++i) {
			ZLTextStyleDecoration decoration = collection.getDecoration(styles[i]);
			if (decoration != null) {
				final String name = styleResource.getResource(decoration.getName()).getValue();

				registerEntry(dialogTab,
					KEY_FONTFAMILY, new ZLTextFontFamilyWithBaseOptionEntry(decoration.FontFamilyOption, context,  dialogTab.getResource(KEY_FONTFAMILY)),
					name
				);

				registerEntry(dialogTab,
					KEY_FONTSIZEDIFFERENCE, new ZLSimpleSpinOptionEntry(decoration.FontSizeDeltaOption, 2),
					name
				);

				registerEntry(dialogTab,
					KEY_BOLD, new ZLSimpleBoolean3OptionEntry(decoration.BoldOption),
					name
				);

				registerEntry(dialogTab,
					KEY_ITALIC, new ZLSimpleBoolean3OptionEntry(decoration.ItalicOption),
					name
				);

				registerEntry(dialogTab,
					KEY_ALLOWHYPHENATIONS, new ZLSimpleBoolean3OptionEntry(decoration.AllowHyphenationsOption),
					name
				);
			}
		}

		myComboEntry.onValueSelected(0);
	}
}
