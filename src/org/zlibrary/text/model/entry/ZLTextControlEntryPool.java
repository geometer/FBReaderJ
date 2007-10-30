package org.zlibrary.text.model.entry;


public interface ZLTextControlEntryPool {
    ZLTextParagraphEntry getControlEntry(byte kind, boolean isStart);
}
