package org.zlibrary.model;

public interface ZLTextParagraphEntry {
    enum Kind {
        TEXT_ENTRY,
        IMAGE_ENTRY,
        CONTROL_ENTRY,
        HYPERLINK_CONTROL_ENTRY,
        FORCED_CONTROL_ENTRY,
        FIXED_HSPACE_ENTRY
    };   

}
