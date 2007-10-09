package org.zlibrary.model.impl;

import org.zlibrary.model.ZLTextModel;
import org.zlibrary.model.ZLTextParagraph;
import org.zlibrary.model.ZLModelFactory;

public class ZLModelFactoryImpl extends ZLModelFactory {
    public ZLTextModel createModel() {
        return new ZLTextModelImpl();
    }

    public ZLTextParagraph createParagraph() {
        return new ZLTextParagraphImpl();
    }
}
