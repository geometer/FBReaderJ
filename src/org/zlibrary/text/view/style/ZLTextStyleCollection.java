package org.zlibrary.text.view.style;

import java.util.Map;

import org.zlibrary.core.util.ZLBoolean3;
import org.zlibrary.core.xml.ZLXMLReader;
import org.zlibrary.text.model.ZLTextAlignmentType;
import org.zlibrary.text.view.ZLTextStyle;

public class ZLTextStyleCollection {
	private static ZLTextStyleCollection ourInstance = null;
	
	private ZLTextBaseStyle myBaseStyle;
	private ZLTextPositionIndicatorStyle myIndicatorStyle;
	private final ZLTextStyleDecoration[] myDecorationMap = new ZLTextStyleDecoration[256];
	
	private ZLTextStyleCollection() {
		new TextStyleReader(this).read("data/default/styles.xml");
		if (myBaseStyle == null) {
			myBaseStyle = new ZLTextBaseStyle("", 20);
		}
	}
	
	public static ZLTextStyleCollection getInstance() {
		if (ourInstance == null) {
			ourInstance = new ZLTextStyleCollection();
		}
		return ourInstance;
	}
	
	public static void deleteInstance() {
		ourInstance = null;
	}
	
	public ZLTextBaseStyle getBaseStyle() {
		return myBaseStyle;
	}
	
	public ZLTextPositionIndicatorStyle getIndicatorStyle() {
		return myIndicatorStyle;
	}
	
	public ZLTextStyleDecoration getDecoration(byte kind) {
		return myDecorationMap[kind & 0xFF];
	}
		
	public ZLTextBaseStyle baseStyle() {
		return myBaseStyle;
	}

	private static class TextStyleReader extends ZLXMLReader {
		private ZLTextStyleCollection myCollection;

		private static int intValue(Map<String,String> attributes, String name) {
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

		private static double doubleValue(Map<String,String> attributes, String name) {
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

		private static boolean booleanValue(Map<String,String> attributes, String name) {
			return "true".equals(attributes.get(name));
		}

		private static int b3Value(Map<String,String> attributes, String name) {
			return ZLBoolean3.getByString(attributes.get(name));
		}
			
		public TextStyleReader(ZLTextStyleCollection collection) {
			myCollection = collection;
		}

		public void startElementHandler(String tag, Map<String,String> attributes) {
			final String BASE = "base";
			final String STYLE = "style";

			if (BASE.equals(tag)) {
				myCollection.myBaseStyle = new ZLTextBaseStyle(attributes.get("family"), intValue(attributes, "fontSize"));
			} else if (STYLE.equals(tag)) {
				String idString = attributes.get("id");
				String name = attributes.get("name");
				if ((idString != null) && (name != null)) {
					byte id = Byte.parseByte(idString);
					ZLTextStyleDecoration decoration;

					int fontSizeDelta = intValue(attributes, "fontSizeDelta");
					int bold = b3Value(attributes, "bold");
					int italic = b3Value(attributes, "italic");
					int verticalShift = intValue(attributes, "vShift");
					int allowHyphenations = b3Value(attributes, "allowHyphenations");
					byte hyperlinkStyle = HyperlinkStyle.NONE;
					String hyperlink = attributes.get("hyperlink");
					if (hyperlink != null) {
						if ("internal".equals(hyperlink)) {
							hyperlinkStyle = HyperlinkStyle.INTERNAL;
						}
						if ("external".equals(hyperlink)) {
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

						byte alignment = ZLTextAlignmentType.ALIGN_UNDEFINED;
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

					myCollection.myDecorationMap[id & 0xFF] = decoration;
				}
			}
		}
	}	
}
