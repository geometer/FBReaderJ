package org.vimgadgets.linebreak;

public final class LineBreak {
	static {
		System.loadLibrary("linebreak");
		init();
	}

	private static native void init();
	private static native void setLineBreaksForCharArray(char[] data, String lang, byte[] breaks);
	private static native void setLineBreaksForString(String data, String lang, byte[] breaks);

	private final String myLanguage;

	public LineBreak(String lang) {
		myLanguage = lang;
	}

	public void setLineBreaks(char[] data, byte[] breaks) {
		setLineBreaksForCharArray(data, myLanguage, breaks);
	}

	public void setLineBreaks(String data, byte[] breaks) {
		setLineBreaksForString(data, myLanguage, breaks);
	}
}
