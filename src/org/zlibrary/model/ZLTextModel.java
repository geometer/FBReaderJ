package org.zlibrary.model;

import java.util.List;

public interface ZLTextModel {
    public enum Kind {
        PLAIN_TEXT_MODEL,
    };
    
    Kind getKind();
    int getParagraphsNumber();
    ZLTextParagraph getParagraph(int index);
    void selectParagraph(int index);

    void addControl(byte textKind, boolean isStart);
    void addText(String text);
    void addText(List<String> text);

    String dump();    
}
