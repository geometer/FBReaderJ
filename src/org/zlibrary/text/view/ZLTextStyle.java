package org.zlibrary.text.view;

public interface ZLTextStyle {
	public String getFontFamily();
	public int getFontSize();

	public boolean bold();
	public boolean italic();
	public int leftIndent();
	public int rightIndent();
	public int firstLineIndentDelta();
	public int lineSpace();
	public int verticalShift();
	public int spaceBefore();
	public int spaceAfter();
	public boolean isDecorated();
	public byte alignment();
}
