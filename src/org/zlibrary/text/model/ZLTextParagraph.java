package org.zlibrary.text.model;

import java.util.List;

import org.zlibrary.text.model.entry.ZLTextParagraphEntry;

public interface ZLTextParagraph {
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
    List<ZLTextParagraphEntry> getEntries();
    void addEntry(ZLTextParagraphEntry entry);
}