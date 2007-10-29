package org.zlibrary.model.entry;


public interface ZLTextControlEntryPool {
    ZLTextParagraphEntry getControlEntry(byte kind, boolean isStart);
}
