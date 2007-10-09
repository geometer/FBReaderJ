package org.zlibrary.model;

import java.util.List;

import org.zlibrary.model.entry.ZLTextParagraphEntry;

public interface ZLTextParagraph {
    enum Kind {
        TEXT_PARAGRAPH,
    };

    Kind getKind();
    int getEntryNumber();
    int getTextLength();
    List<ZLTextParagraphEntry> getEntries();
    void addEntry(ZLTextParagraphEntry entry);
}