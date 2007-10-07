package org.zlibrary.model;

public interface ZLTextParagraph {
    enum Kind {
        TEXT_PARAGRAPH,
    };

    Kind getKind();
    int getEntryNumber();
    int getTextLength();
}
