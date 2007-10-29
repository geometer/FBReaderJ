package org.zlibrary.model.entry;

public interface ZLTextControlEntry extends ZLTextParagraphEntry {
    byte getKind();
    boolean isStart();
    boolean isHyperlink();
}
