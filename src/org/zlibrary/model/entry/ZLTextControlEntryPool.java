package org.zlibrary.model.entry;

import org.zlibrary.model.ZLTextParagraphEntry;

public interface ZLTextControlEntryPool {
    ZLTextParagraphEntry getControlEntry(byte kind, boolean isStart);
}
