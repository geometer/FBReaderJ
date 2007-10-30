package org.zlibrary.text.model.entry;

public interface ZLTextControlEntry extends ZLTextParagraphEntry {
    byte getKind();
    boolean isStart();
    boolean isHyperlink();
}
