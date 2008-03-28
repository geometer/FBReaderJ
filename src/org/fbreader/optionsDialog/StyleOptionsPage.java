package org.fbreader.optionsDialog;

import org.zlibrary.core.dialogs.ZLDialogContent;
import org.zlibrary.core.optionEntries.ZLFontFamilyOptionEntry;
import org.zlibrary.core.optionEntries.ZLSimpleBoolean3OptionEntry;
import org.zlibrary.core.optionEntries.ZLSimpleBooleanOptionEntry;
import org.zlibrary.core.optionEntries.ZLSimpleSpinOptionEntry;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.view.ZLTextFontFamilyWithBaseOptionEntry;
import org.zlibrary.text.view.style.ZLTextBaseStyle;
import org.zlibrary.text.view.style.ZLTextStyleCollection;
import org.zlibrary.text.view.style.ZLTextStyleDecoration;

import static org.fbreader.bookmodel.FBTextKind.*;

public class StyleOptionsPage extends OptionsPage {
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

		ZLTextStyleCollection collection = ZLTextStyleCollection.getInstance();
		byte styles[] = { REGULAR, TITLE, SECTION_TITLE, SUBTITLE, H1, H2, H3, H4, H5, H6, CONTENTS_TABLE_ENTRY, RECENT_BOOK_LIST, LIBRARY_AUTHOR_ENTRY, LIBRARY_BOOK_ENTRY, ANNOTATION, EPIGRAPH, AUTHOR,/* DATEKIND, */POEM_TITLE, STANZA, VERSE, CITE, INTERNAL_HYPERLINK, EXTERNAL_HYPERLINK, FOOTNOTE, ITALIC, EMPHASIS, BOLD, STRONG, DEFINITION, DEFINITION_DESCRIPTION, PREFORMATTED, CODE };
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
			ZLTextBaseStyle baseStyle = collection.baseStyle();

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
