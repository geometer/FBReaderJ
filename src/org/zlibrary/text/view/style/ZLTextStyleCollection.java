package org.zlibrary.text.view.style;

import java.util.HashMap;
import java.util.Map;

import org.zlibrary.core.options.util.ZLBoolean3;
import org.zlibrary.core.xml.ZLXMLReader;
import org.zlibrary.text.model.ZLTextAlignmentType;
import org.zlibrary.text.view.ZLTextStyle;

public class ZLTextStyleCollection {
	private static ZLTextStyleCollection ourInstance = null;
	
	private ZLTextStyle myBaseStyle;
	private ZLTextPositionIndicatorStyle myIndicatorStyle;
	private final Map<Byte, ZLTextStyleDecoration> myDecorationMap = new HashMap<Byte,ZLTextStyleDecoration>();
	
	private ZLTextStyleCollection() {
		new TextStyleReader(this).read("data/default/styles.xml");
		if (myBaseStyle == null) {
			myBaseStyle = new ZLTextBaseStyle("", 20);
		}
	}
	
	public static ZLTextStyleCollection instance() {
		if (ourInstance == null) {
			ourInstance = new ZLTextStyleCollection();
		}
		return ourInstance;
	}
	
	public static void deleteInstance() {
		ourInstance = null;
	}
	
	public ZLTextStyle getBaseStyle() {
		return myBaseStyle;
	}
	
	public ZLTextPositionIndicatorStyle getIndicatorStyle() {
		return myIndicatorStyle;
	}
	
	public ZLTextStyleDecoration getDecoration(byte kind) {
		return myDecorationMap.get(kind);
	}
		
//?		ZLTextBaseStyle &baseStyle() const;
		

	private static class TextStyleReader extends ZLXMLReader {
//		static final String TRUE_STRING = "true";
		private ZLTextStyleCollection myCollection;

		private static int intValue(Map<String, String> attributes, String name) {
			int i = 0;
			String value = attributes.get(name);
			if (value != null) {
				try {
					i = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			} 
			return i;
		}

		private static double doubleValue(Map<String, String> attributes, String name) {
			double d = 0;
			String value = attributes.get(name);
			if (value != null) {
				try {
					d = Double.parseDouble(value);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			return d;
		}

		private static boolean booleanValue(Map<String, String> attributes, String name) {
			return "true".equals(attributes.get(name));
		}

		private static ZLBoolean3 b3Value(Map<String, String> attributes, String name) {
			String stringValue = attributes.get(name);
			return (stringValue == null) ? ZLBoolean3.B3_UNDEFINED : 
				((stringValue.equals("true")) ? ZLBoolean3.B3_TRUE : ZLBoolean3.B3_FALSE);
		}
			
		public TextStyleReader(ZLTextStyleCollection collection) {
			myCollection = collection;
		}

		@Override
		public void startElementHandler(String tag, Map<String, String> attributes) {
			final String BASE = "base";
			final String STYLE = "style";

			if (BASE.equals(tag)) {
				myCollection.myBaseStyle = new ZLTextBaseStyle(attributes.get("family"), intValue(attributes,"fontSize"));
			} else if (STYLE.equals(tag)) {
				String idString = attributes.get("id");
				String name = attributes.get("name");
				if ((idString != null) && (name != null)) {
					byte id = Byte.parseByte(idString);
					ZLTextStyleDecoration decoration;

					int fontSizeDelta = intValue(attributes, "fontSizeDelta");
					ZLBoolean3 bold = b3Value(attributes, "bold");
					ZLBoolean3 italic = b3Value(attributes, "italic");
					int verticalShift = intValue(attributes, "vShift");
					ZLBoolean3 allowHyphenations = b3Value(attributes, "allowHyphenations");
					HyperlinkStyle hyperlinkStyle = HyperlinkStyle.NONE;
					String hyperlink = attributes.get("hyperlink");
					if (hyperlink != null) {
						final String INTERNAL_STRING = "internal";
						if (INTERNAL_STRING.equals(hyperlink)) {
							hyperlinkStyle = HyperlinkStyle.INTERNAL;
						}
						final String EXTERNAL_STRING = "external";
						if (EXTERNAL_STRING.equals(hyperlink)) {
							hyperlinkStyle = HyperlinkStyle.EXTERNAL;
						}
					}

					if (booleanValue(attributes, "partial")) {
						decoration = new ZLTextStyleDecoration(name, fontSizeDelta, bold, italic, verticalShift, allowHyphenations);
					} else {
						int spaceBefore = intValue(attributes, "spaceBefore");
						int spaceAfter = intValue(attributes, "spaceAfter");
						int leftIndent = intValue(attributes, "leftIndent");
						int rightIndent = intValue(attributes, "rightIndent");
						int firstLineIndentDelta = intValue(attributes, "firstLineIndentDelta");

						int alignment = ZLTextAlignmentType.ALIGN_UNDEFINED;
						String alignmentString = attributes.get("alignment");
						if (alignmentString != null) {
							if (alignmentString.equals("left")) {
								alignment = ZLTextAlignmentType.ALIGN_LEFT;
							} else if (alignmentString.equals("right")) {
								alignment = ZLTextAlignmentType.ALIGN_RIGHT;
							} else if (alignmentString.equals("center")) {
								alignment = ZLTextAlignmentType.ALIGN_CENTER;
							} else if (alignmentString.equals("justify")) {
								alignment = ZLTextAlignmentType.ALIGN_JUSTIFY;
							}
						}
						double lineSpace = doubleValue(attributes, "lineSpace");

						decoration = new ZLTextFullStyleDecoration(name, fontSizeDelta, bold, italic, spaceBefore, spaceAfter, leftIndent, rightIndent, firstLineIndentDelta, verticalShift, alignment, lineSpace, allowHyphenations);
					}
					decoration.setHyperlinkStyle(hyperlinkStyle);

					String fontFamily = attributes.get("family");
					if (fontFamily != null) {
						decoration.FontFamilyOption.setValue(fontFamily);
					}

					myCollection.myDecorationMap.put(id, decoration);
				}
			}
		}
		
		
	}	
}
