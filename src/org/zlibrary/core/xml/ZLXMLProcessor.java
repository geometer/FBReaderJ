package org.zlibrary.core.xml;

import java.io.InputStream;

public interface ZLXMLProcessor {
	boolean read(ZLXMLReader reader, InputStream stream);
}
