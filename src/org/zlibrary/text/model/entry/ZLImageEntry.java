package org.zlibrary.text.model.entry;

import org.zlibrary.core.image.ZLImage;

public interface ZLImageEntry extends ZLTextParagraphEntry {
	String getId();
	short getVOffset();
	ZLImage image();
}
