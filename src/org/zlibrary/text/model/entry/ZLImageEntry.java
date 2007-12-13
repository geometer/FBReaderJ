package org.zlibrary.text.model.entry;

import org.zlibrary.core.image.ZLImage;
import org.zlibrary.text.model.ZLTextParagraph;

public interface ZLImageEntry extends ZLTextParagraph.Entry {
	String getId();
	short getVOffset();
	ZLImage getImage();
}
