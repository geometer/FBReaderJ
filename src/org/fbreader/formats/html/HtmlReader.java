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
	private boolean myInsidePoem = false;
	private boolean myInsideTitle = false;
	private int myBodyCounter = 0;
	private boolean myReadMainText = false;
	private int mySectionDepth = 0;
	private boolean mySectionStarted = false;
	
	private byte myHyperlinkType;
	
	private boolean myInsideCoverpage = false;
	private String myCoverImageReference;
	private int myParagraphsBeforeBodyNumber = Integer.MAX_VALUE;

	private boolean myUnnaturalParagraphExists = false;
	
	private final char[] SPACE = { ' ' }; 
	private String myHrefAttribute = ":href";

	public HtmlReader(BookModel model) {
 		super(model);
	}

	public boolean read() {
		final ZLHtmlProcessor processor = ZLHtmlProcessorFactory.getInstance().createHtmlProcessor();
		return processor.read(this, getModel().getFileName());
	}
	
	boolean readBook(String fileName) {
		final ZLHtmlProcessor processor = ZLHtmlProcessorFactory.getInstance().createHtmlProcessor();
		return processor.read(this, fileName);
		//return readDocument(fileName);
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
		//final Base64EncodedImage image = myCurrentImage;
		//if (image != null) {
		//	image.addData(ch, start, length);
		//} else {
			addData(ch, start, length);
		//}		
	}

	public void characterDataHandlerFinal(char[] ch, int start, int length) {
		if (length == 0) {
			return;
		}
		//final Base64EncodedImage image = myCurrentImage;
		//if (image != null) {
		//	image.addData(ch, start, length);
		//} else {
			addDataFinal(ch, start, length);
		//}		
	}

	public void endElementHandler(String tagName) {
		switch (HtmlTag.getTagByName(tagName)) {
			case HtmlTag.P:
				endParagraph();		
				break;
				
			case HtmlTag.A:
				if (myUnnaturalParagraphExists) {
					endParagraph();
				}
				addControl(myHyperlinkType, false);
				break;
				
			case HtmlTag.TITLE:
				popKind();
				exitTitle();
				myInsideTitle = false;
				break;
				
			case HtmlTag.BODY:
				popKind();
				myReadMainText = false;
				unsetCurrentTextModel();
				break;
			
			case HtmlTag.B:
				if (myUnnaturalParagraphExists) {
					endParagraph();
				}
				addControl(FBTextKind.STRONG, false);
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
							myHrefAttribute = (key.substring(6) + ":href").intern();
							break;
						}
					}
					System.out.println(key);
				}
				break;
				
			case HtmlTag.BODY:
				++myBodyCounter;
				myParagraphsBeforeBodyNumber = getModel().getBookTextModel().getParagraphsNumber();
				if ((myBodyCounter == 1) || (attributes.getValue("name") == null)) {
					setMainTextModel();
					myReadMainText = true;
				}
				pushKind(FBTextKind.REGULAR);
				break;
				
			case HtmlTag.TITLE:
				if (myInsidePoem) {
					pushKind(FBTextKind.POEM_TITLE);
				} else if (mySectionDepth == 0) {
					insertEndOfSectionParagraph();
					pushKind(FBTextKind.TITLE);
				} else {
					pushKind(FBTextKind.SECTION_TITLE);
					myInsideTitle = true;
					enterTitle();
				}
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
				if (!isTextParagraphExists()) {
					myUnnaturalParagraphExists = true;
					beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				}
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
			
			case HtmlTag.BR:
				if (!isTextParagraphExists()) {
					endParagraph();
				}
				beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				endParagraph();
				if (!isTextParagraphExists()) {
					beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				}
				break;
				
			case HtmlTag.B:
				if (!isTextParagraphExists()) {
					myUnnaturalParagraphExists = true;
					beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				}
				addControl(FBTextKind.STRONG, true);
				break;
			case HtmlTag.H1:
				beginParagraph(FBTextKind.H1);
				break;
			case HtmlTag.H2:
				beginParagraph(FBTextKind.H2);
				break;
			case HtmlTag.H3:
				beginParagraph(FBTextKind.H3);
				break;
			case HtmlTag.H4:
				beginParagraph(FBTextKind.H4);
				break;
			case HtmlTag.H5:
				beginParagraph(FBTextKind.H5);
				break;
			case HtmlTag.H6:
				beginParagraph(FBTextKind.H6);
				break;
			default:
				break;
		}
	}
}
