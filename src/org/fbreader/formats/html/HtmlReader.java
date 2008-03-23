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
	private final char[] SPACE = { ' ' };
	private String myHrefAttribute = "href";
	private boolean myAdditionalParagraphExists = false;
	private boolean myOrderedListIsStarted = false;
	private boolean myUnorderedListIsStarted = false;
	private int myOLCounter = 0;
	
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

	private void startNewParagraph() {
		endParagraph();
		beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	}
	
	public void endElementHandler(String tagName) {
		switch (HtmlTag.getTagByName(tagName)) {
			
			case HtmlTag.SCRIPT:
			case HtmlTag.SELECT:
				startNewParagraph();
				break;
				
			case HtmlTag.P:
				startNewParagraph();
				break;

			case HtmlTag.H1:
				addControl(FBTextKind.H1, false);
				startNewParagraph();
				break;
				
			case HtmlTag.H2:
				addControl(FBTextKind.H2, false);
				startNewParagraph();
				break;
				
			case HtmlTag.H3:
				addControl(FBTextKind.H3, false);
				startNewParagraph();
				break;
				
			case HtmlTag.H4:
				addControl(FBTextKind.H4, false);
				startNewParagraph();
				break;
				
			case HtmlTag.H5:
				addControl(FBTextKind.H5, false);
				startNewParagraph();
				break;
				
			case HtmlTag.H6:
				addControl(FBTextKind.H6, false);
				startNewParagraph();
				break;
				
			case HtmlTag.A:
				addControl(myHyperlinkType, false);
				break;

			case HtmlTag.BODY:
				myReadMainText = false;
				break;

			case HtmlTag.HTML:
				unsetCurrentTextModel();
				break;
				
			case HtmlTag.B:
				addControl(FBTextKind.BOLD, false);
				break;
				
			case HtmlTag.STRONG:
				addControl(FBTextKind.STRONG, false);
				break;
				
			case HtmlTag.I:
				addControl(FBTextKind.ITALIC, false);
				break;

			case HtmlTag.OL:
				myOrderedListIsStarted = false;
				myOLCounter = 0;
				break;
				
			case HtmlTag.UL:
				myUnorderedListIsStarted = false;
				break;
				
			default:
				break;
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
					//System.out.println(key);
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
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
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

			case HtmlTag.B:
				addControl(FBTextKind.BOLD, true);
				break;
				
			case HtmlTag.STRONG:
				addControl(FBTextKind.STRONG, true);
				break;
				
			case HtmlTag.I:
				addControl(FBTextKind.ITALIC, true);
				break;
				
			case HtmlTag.H1:
				startNewParagraph();
				addControl(FBTextKind.H1, true);
				break;
				
			case HtmlTag.H2:
				startNewParagraph();
				addControl(FBTextKind.H2, true);
				break;
				
			case HtmlTag.H3:
				startNewParagraph();
				addControl(FBTextKind.H3, true);
				break;
				
			case HtmlTag.H4:
				startNewParagraph();
				addControl(FBTextKind.H4, true);
				break;
				
			case HtmlTag.H5:
				startNewParagraph();
				addControl(FBTextKind.H5, true);
				break;
				
			case HtmlTag.H6:
				startNewParagraph();
				addControl(FBTextKind.H6, true);
				break;
				
			case HtmlTag.OL:
				myOrderedListIsStarted = true;
				break;
				
			case HtmlTag.UL:
				myUnorderedListIsStarted = true;
				break;
				
			case HtmlTag.LI:
				startNewParagraph();
				if (myOrderedListIsStarted) {
					char[] number = (new Integer(++myOLCounter)).toString().toCharArray();
					addDataFinal(number, 0, number.length);
					addDataFinal(new char[] {'.', ' '}, 0, 2);
				} else {
					addDataFinal(new char[] {'*', ' '}, 0, 2);
				}
				break;
				
			case HtmlTag.SCRIPT:
			case HtmlTag.SELECT:
			case HtmlTag.STYLE:
				endParagraph();
				break;
				
			case HtmlTag.TR: 
			case HtmlTag.BR:
				startNewParagraph();
				break;
			default:
				break;
		}
	}
}
