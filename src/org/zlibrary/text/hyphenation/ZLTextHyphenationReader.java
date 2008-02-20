package org.zlibrary.text.hyphenation;

import org.zlibrary.core.xml.ZLStringMap;
import org.zlibrary.core.xml.ZLXMLReaderAdapter;

/*package*/ class ZLTextHyphenationReader extends ZLXMLReaderAdapter {
	public static final String PATTERN = "pattern";
	public static final String LINE_BREAKING_ALGORITHM = "lineBreakingAlgorithm";


	private ZLTextTeXHyphenator myHyphenator;
	private boolean myReadPattern = false;
	private StringBuffer myBuffer = new StringBuffer();

	/*package*/ ZLTextHyphenationReader(ZLTextTeXHyphenator hyphenator) {
		myHyphenator = hyphenator;
	}

	public void startElementHandler(String tag, ZLStringMap attributes) {
		if (PATTERN.equals(tag)) {
			myReadPattern = true;
		} else if (LINE_BREAKING_ALGORITHM.equals(tag)) {
			final String algorithm = attributes.getValue("name");
			if (algorithm != null) {
				myHyphenator.setBreakingAlgorithm(algorithm);
			}
		}
	}

	public void endElementHandler(String tag) {
		if (PATTERN.equals(tag)) {
			myReadPattern = false;
			if (!(myBuffer.length() == 0)) {
				myHyphenator.addPattern(new ZLTextTeXHyphenationPattern(myBuffer.toString().toCharArray(), 0, myBuffer.length()));
			}
			myBuffer = new StringBuffer();
		}
	}

	public void characterDataHandler(char[] ch, int start, int length) {
		if (myReadPattern) {
			myBuffer.append(ch, start, length);
		}
	}
}
