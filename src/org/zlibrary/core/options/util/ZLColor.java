package org.zlibrary.core.options.util;

/**
 * class Color. Color is presented as the triple of short's (Red, Green, Blue components)
 * Each component should be in the range 0..255
 */
public class ZLColor {
	public final short Red;
	public final short Green;
	public final short Blue;
	
	public ZLColor() {
		Red = 0;
		Green = 0;
		Blue = 0;
	}

	public ZLColor(short r, short g, short b) {
		Red = (short)(r & 0xFF);
		Green = (short)(g & 0xFF);
		Blue = (short)(b & 0xFF);
	}
	
	public ZLColor(int intValue) {
		Red = (short)((intValue >> 16) & 0xFF);
		Green = (short)((intValue >> 8) & 0xFF);
		Blue = (short)(intValue & 0xFF);
	}
	
	public int getIntValue() {
		return (Red << 16) + (Green << 8) + Blue;
	}

	public boolean equals(Object o) {
		if (o == this) { 
			return true;
		}

		if (!(o instanceof ZLColor)) {
			return false;
		}

		ZLColor color = (ZLColor)o;
		return (color.Red == Red) && (color.Green == Green) && (color.Blue == Blue);
	}

	public int hashCode() {
		return getIntValue();
	}
}
