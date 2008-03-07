package org.fbreader.formats.html;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.bookmodel.BookReader;
import org.fbreader.bookmodel.FBTextKind;
import org.fbreader.formats.html.HtmlTag;
import org.zlibrary.core.xml.ZLStringMap;
import org.zlibrary.core.xml.ZLXMLProcessor;
import org.zlibrary.core.xml.ZLXMLProcessorFactory;
import org.zlibrary.core.html.ZLHtmlProcessor;
import org.zlibrary.core.html.ZLHtmlProcessorFactory;
import org.zlibrary.core.html.ZLHtmlReader;
import org.zlibrary.text.model.ZLTextParagraph;

public class HtmlReader extends BookReader implements ZLHtmlReader {

	private boolean myInsideTitle = false;
	private int myBodyCounter = 0;
	private boolean myReadMainText = false;
	private boolean mySectionStarted = false;
	private byte myHyperlinkType;
	//private int myParagraphsBeforeBodyNumber = Integer.MAX_VALUE;
	private boolean myUnnaturalTextBlockExists = false;
	private final char[] SPACE = { ' ' };
	private String myHrefAttribute = "href";
	private boolean myAdditionalParagraphExists = false;

	public HtmlReader(BookModel model) {
		super(model);
	}

	public boolean read() {
		final ZLHtmlProcessor processor = ZLHtmlProcessorFactory.getInstance()
				.createHtmlProcessor();
		return processor.read(this, getModel().getFileName());
	}

	boolean readBook(String fileName) {
		final ZLHtmlProcessor processor = ZLHtmlProcessorFactory.getInstance()
				.createHtmlProcessor();
		return processor.read(this, fileName);
		// return readDocument(fileName);
	}

	public void startDocumentHandler() {
	}

	public void endDocumentHandler() {
	}

	public boolean dontCacheAttributeValues() {
		return true;
	}

	public void characterDataHandler(char[] ch, int start, int length) {
		if (length == 0) {
			return;
		}
		// final Base64EncodedImage image = myCurrentImage;
		// if (image != null) {
		// image.addData(ch, start, length);
		// } else {
		addData(ch, start, length);
		// }
	}

	public void characterDataHandlerFinal(char[] ch, int start, int length) {
		if (length == 0) {
			return;
		}
		// final Base64EncodedImage image = myCurrentImage;
		// if (image != null) {
		// image.addData(ch, start, length);
		// } else {
		addDataFinal(ch, start, length);
		// }
	}

	private void endUnnaturalTextBlock() {
		/*if (myUnnaturalTextBlockExists) {
			endParagraph();
			myUnnaturalTextBlockExists = false;
		}*/
		if (myAdditionalParagraphExists) {
			endParagraph();
			beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
			myAdditionalParagraphExists = false; 
		}
	}
	
	public void endElementHandler(String tagName) {
		switch (HtmlTag.getTagByName(tagName)) {
			case HtmlTag.P:
				endParagraph();
				startUnnaturalTextBlock();
				break;

			case HtmlTag.H1:
				endUnnaturalTextBlock();
				addControl(FBTextKind.H1, false);
				break;
				
			case HtmlTag.H2:
				endUnnaturalTextBlock();
				addControl(FBTextKind.H2, false);
				break;
				
			case HtmlTag.H3:
				endUnnaturalTextBlock();
				addControl(FBTextKind.H3, false);
				break;
				
			case HtmlTag.H4:
				endUnnaturalTextBlock();
				addControl(FBTextKind.H4, false);
				break;
				
			case HtmlTag.H5:
				endUnnaturalTextBlock();
				addControl(FBTextKind.H5, false);
				break;
				
			case HtmlTag.H6:
				endUnnaturalTextBlock();
				addControl(FBTextKind.H6, false);
				break;
				
			case HtmlTag.A:
				endUnnaturalTextBlock();
				addControl(myHyperlinkType, false);
				break;

			case HtmlTag.BODY:
				popKind();
				myReadMainText = false;
				unsetCurrentTextModel();
				break;

			case HtmlTag.B:
				addControl(FBTextKind.BOLD, false);
				break;
				
			case HtmlTag.I:
				addControl(FBTextKind.ITALIC, false);
				break;

			default:
				break;
		}
	}

	/**
	 * если тэг типа bold встретился вне параграфа, то нужно начать новый,
	 * а если внутри, то нужно продолжить параграф 
	 */ 
	private void startUnnaturalTextBlock() {
		if (!isTextParagraphExists()) {
			myUnnaturalTextBlockExists = true;
			beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
		} else {
			endParagraph();
			myAdditionalParagraphExists = true;
			beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
		}
	}
	
	public void startElementHandler(String tagName, ZLStringMap attributes) {
		String id = attributes.getValue("id");
		if (id != null) {
			if (!myReadMainText) {
				setFootnoteTextModel(id);
			}
			addHyperlinkLabel(id);
		}
		switch (HtmlTag.getTagByName(tagName)) {
			case HtmlTag.HTML:
				final int attibutesNumber = attributes.getSize();
				for (int i = 0; i < attibutesNumber; ++i) {
					final String key = attributes.getKey(i);
					if (key.startsWith("xmlns:")) {
						final String value = attributes.getValue(key);
						if (value.endsWith("/xlink")) {
							myHrefAttribute = (key.substring(6) + "href")
									.intern();
							break;
						}
					}
					System.out.println(key);
				}
				break;

			case HtmlTag.BODY:
				++myBodyCounter;
				//myParagraphsBeforeBodyNumber = getModel().getBookTextModel()
					//	.getParagraphsNumber();
				if ((myBodyCounter == 1)
						|| (attributes.getValue("name") == null)) {
					setMainTextModel();
					myReadMainText = true;
				}
				pushKind(FBTextKind.REGULAR);
				startUnnaturalTextBlock();
				break;

			case HtmlTag.P:
				if (mySectionStarted) {
					mySectionStarted = false;
				} else if (myInsideTitle) {
					addContentsData(SPACE);
				}
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;

			case HtmlTag.A:
				String ref = attributes.getValue(myHrefAttribute);
				if ((ref != null) && (ref.length() != 0)) {
					if (ref.charAt(0) == '#') {
						myHyperlinkType = FBTextKind.FOOTNOTE;
						ref = ref.substring(1);
					} else {
						myHyperlinkType = FBTextKind.EXTERNAL_HYPERLINK;
					}
					addHyperlinkControl(myHyperlinkType, ref);
				} else {
					myHyperlinkType = FBTextKind.FOOTNOTE;
					addControl(myHyperlinkType, true);
				}
				break;

			case HtmlTag.BR: {
				boolean paragraphExists = isTextParagraphExists();
				if (paragraphExists) {
					endParagraph();
				}
				beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				endParagraph();
				if (paragraphExists) {
					beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				}
				break;
			}
			case HtmlTag.B:
				addControl(FBTextKind.BOLD, true);
				break;
				
			case HtmlTag.I:
				addControl(FBTextKind.ITALIC, true);
				break;
				
			case HtmlTag.H1:
				startUnnaturalTextBlock();
				addControl(FBTextKind.H1, true);
				break;
				
			case HtmlTag.H2:
				startUnnaturalTextBlock();
				addControl(FBTextKind.H2, true);
				break;
				
			case HtmlTag.H3:
				startUnnaturalTextBlock();
				addControl(FBTextKind.H3, true);
				break;
				
			case HtmlTag.H4:
				startUnnaturalTextBlock();
				addControl(FBTextKind.H4, true);
				break;
				
			case HtmlTag.H5:
				startUnnaturalTextBlock();
				addControl(FBTextKind.H5, true);
				break;
				
			case HtmlTag.H6:
				startUnnaturalTextBlock();
				addControl(FBTextKind.H6, true);
				break;
			default:
				break;
		}
	}
}
