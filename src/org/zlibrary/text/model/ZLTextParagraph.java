package org.zlibrary.text.model;

import java.util.List;

public interface ZLTextParagraph extends Iterable<ZLTextParagraph.Entry> {
	interface Entry {
	}

	enum Kind {
		TEXT_PARAGRAPH,
		TREE_PARAGRAPH,
		EMPTY_LINE_PARAGRAPH,
		BEFORE_SKIP_PARAGRAPH,
		AFTER_SKIP_PARAGRAPH,
		END_OF_SECTION_PARAGRAPH,
		END_OF_TEXT_PARAGRAPH,
	};

	Kind getKind();
	int getEntryNumber();
	int getTextLength();
}
