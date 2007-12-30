package org.fbreader.formats.fb2;

import java.util.Map;
import java.util.HashMap;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.bookmodel.BookReader;
import org.fbreader.bookmodel.FBTextKind;
import org.zlibrary.core.xml.ZLXMLReader;
import org.zlibrary.text.model.ZLTextParagraph;

public class FB2Reader extends ZLXMLReader {
	public static long LoadingTime;

	private BookReader myModelReader;
//	private String myFileName;
	
	private boolean myInsidePoem = false;
	private boolean myInsideTitle = false;
	private int myBodyCounter = 0;
	private boolean myReadMainText = false;
	private int mySectionDepth = 0;
	private boolean mySectionStarted = false;
	
	private byte myHyperlinkType;
	
	private Base64EncodedImage myCurrentImage;
	private boolean myInsideCoverpage = false;
	private String myCoverImageReference;
	private int myParagraphsBeforeBodyNumber = Integer.MAX_VALUE;

	private final char[] SPACE = { ' ' }; 
	
	private static String reference(Map<String, String> attributes) {
		for (String s : attributes.keySet()) {
			if (s.endsWith(":href")) {
				return attributes.get(s);
			}
		}
		return null;
	}
	
//	private BookModel myBookModel = new BookModel();
	
	public void characterDataHandler(char[] ch, int start, int length) {
		if (length == 0) {
			return;
		}
		if (myCurrentImage != null) {
			myCurrentImage.addData(ch, start, length);
		} else {
			myModelReader.addData(ch, start, length);
		}		
	}

	public void characterDataHandlerFinal(char[] ch, int start, int length) {
		if (length == 0) {
			return;
		}
		if (myCurrentImage != null) {
			myCurrentImage.addData(ch, start, length);
		} else {
			myModelReader.addDataFinal(ch, start, length);
		}		
	}

	public void endElementHandler(String tagName) {
		switch (FB2Tag.getTagByName(tagName)) {
			case FB2Tag.P:
				myModelReader.endParagraph();		
				break;
			case FB2Tag.SUB:
				myModelReader.addControl(FBTextKind.SUB, false);
				break;
			case FB2Tag.SUP:
				myModelReader.addControl(FBTextKind.SUP, false);
				break;
			case FB2Tag.CODE:
				myModelReader.addControl(FBTextKind.CODE, false);
				break;
			case FB2Tag.EMPHASIS:
				myModelReader.addControl(FBTextKind.EMPHASIS, false);
				break;
			case FB2Tag.STRONG:
				myModelReader.addControl(FBTextKind.STRONG, false);
				break;
			case FB2Tag.STRIKETHROUGH:
				myModelReader.addControl(FBTextKind.STRIKETHROUGH, false);
				break;
			
			case FB2Tag.V:
			case FB2Tag.SUBTITLE:
			case FB2Tag.TEXT_AUTHOR:
			case FB2Tag.DATE:
				myModelReader.popKind();
				myModelReader.endParagraph();
				break;	
			
			case FB2Tag.CITE:
			case FB2Tag.EPIGRAPH:
				myModelReader.popKind();
				break;	
			
			case FB2Tag.POEM:
				myInsidePoem = false;
				break;
			
			case FB2Tag.STANZA:
				myModelReader.beginParagraph(ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
				myModelReader.endParagraph();
				myModelReader.popKind();
				break;
				
			case FB2Tag.SECTION:
				if (myReadMainText) {
					myModelReader.endContentsParagraph();
					--mySectionDepth;
					mySectionStarted = false;
				} else {
					myModelReader.unsetCurrentTextModel();
				}
				break;
			
			case FB2Tag.ANNOTATION:
				myModelReader.popKind();
				if (myBodyCounter == 0) {
					myModelReader.insertEndOfSectionParagraph();
					myModelReader.unsetCurrentTextModel();
				}
				break;
			
			case FB2Tag.TITLE:
				myModelReader.popKind();
				myModelReader.exitTitle();
				myInsideTitle = false;
				break;
				
			case FB2Tag.BODY:
				myModelReader.popKind();
				myReadMainText = false;
				myModelReader.unsetCurrentTextModel();
				break;
			
			case FB2Tag.A:
				myModelReader.addControl(myHyperlinkType, false);
				break;
			
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = false;
					myModelReader.insertEndOfSectionParagraph();
					myModelReader.unsetCurrentTextModel();
				}
				break;	
			
			case FB2Tag.BINARY:
				if (myCurrentImage != null) {
					myCurrentImage.trimToSize();
				}
				myCurrentImage = null;
				break;	
				
			default:
				break;
		}		
	}

	public void startElementHandler(String tagName, Map<String, String> attributes) {
		String id = attributes.get("id");
		if (id != null) {
			if (!myReadMainText) {
				myModelReader.setFootnoteTextModel(id);
			}
			myModelReader.addHyperlinkLabel(id);
		}
		switch (FB2Tag.getTagByName(tagName)) {
			case FB2Tag.P:
				if (mySectionStarted) {
					mySectionStarted = false;
				} else if (myInsideTitle) {
					myModelReader.addContentsData(SPACE);
				}
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case FB2Tag.SUB:
				myModelReader.addControl(FBTextKind.SUB, true);
				break;
			case FB2Tag.SUP:
				myModelReader.addControl(FBTextKind.SUP, true);
				break;
			case FB2Tag.CODE:
				myModelReader.addControl(FBTextKind.CODE, true);
				break;
			case FB2Tag.EMPHASIS:
				myModelReader.addControl(FBTextKind.EMPHASIS, true);
				break;
			case FB2Tag.STRONG:
				myModelReader.addControl(FBTextKind.STRONG, true);
				break;
			case FB2Tag.STRIKETHROUGH:
				myModelReader.addControl(FBTextKind.STRIKETHROUGH, true);
				break;
			
			case FB2Tag.V:
				myModelReader.pushKind(FBTextKind.VERSE);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case FB2Tag.TEXT_AUTHOR:
				myModelReader.pushKind(FBTextKind.AUTHOR);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case FB2Tag.SUBTITLE:
				myModelReader.pushKind(FBTextKind.SUBTITLE);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			case FB2Tag.DATE:
				myModelReader.pushKind(FBTextKind.DATE);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case FB2Tag.EMPTY_LINE:
				myModelReader.beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				myModelReader.endParagraph();
				break;
			
			case FB2Tag.CITE:
				myModelReader.pushKind(FBTextKind.CITE);
				break;
			case FB2Tag.EPIGRAPH:
				myModelReader.pushKind(FBTextKind.EPIGRAPH);
				break;
			
			case FB2Tag.POEM:
				myInsidePoem = true;
				break;	
			
			case FB2Tag.STANZA:
				myModelReader.pushKind(FBTextKind.STANZA);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
				myModelReader.endParagraph();
				break;
				
			case FB2Tag.SECTION:
				if (myReadMainText) {
					myModelReader.insertEndOfSectionParagraph();
					++mySectionDepth;
					myModelReader.beginContentsParagraph();
					mySectionStarted  = true;
				}
				break;
			
			case FB2Tag.ANNOTATION:
				if (myBodyCounter == 0) {
					myModelReader.setMainTextModel();
				}
				myModelReader.pushKind(FBTextKind.ANNOTATION);
				break;
			
			case FB2Tag.TITLE:
				if (myInsidePoem) {
					myModelReader.pushKind(FBTextKind.POEM_TITLE);
				} else if (mySectionDepth == 0) {
					myModelReader.insertEndOfSectionParagraph();
					myModelReader.pushKind(FBTextKind.TITLE);
				} else {
					myModelReader.pushKind(FBTextKind.SECTION_TITLE);
					myInsideTitle = true;
					myModelReader.enterTitle();
				}
				break;
				
			case FB2Tag.BODY:
				++myBodyCounter;
				myParagraphsBeforeBodyNumber = myModelReader.getModel().getBookTextModel().getParagraphsNumber();
				if ((myBodyCounter == 1) || (attributes.get("name") == null)) {
					myModelReader.setMainTextModel();
					myReadMainText = true;
				}
				myModelReader.pushKind(FBTextKind.REGULAR);
				break;
			
			case FB2Tag.A:
				String ref = reference(attributes);
				if ((ref != null) && (ref.length() != 0)) {
					if (ref.charAt(0) == '#') {
						myHyperlinkType = FBTextKind.FOOTNOTE;
						ref = ref.substring(1);
					} else {
						myHyperlinkType = FBTextKind.EXTERNAL_HYPERLINK;
					}
					myModelReader.addHyperlinkControl(myHyperlinkType, ref);
				} else {
					myHyperlinkType = FBTextKind.FOOTNOTE;
					myModelReader.addControl(myHyperlinkType, true);
				}
				break;
			
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = true;
					myModelReader.setMainTextModel();
				}
				break;	
			
			case FB2Tag.IMAGE:
				String imgRef = reference(attributes);
				if ((imgRef != null) && (imgRef.length() != 0) && (imgRef.charAt(0) == '#')) {
					String vOffset = attributes.get("voffset");
					short offset = 0;
					try {
						offset = Short.parseShort(vOffset);
					} catch (NumberFormatException e) {
					}
					imgRef = imgRef.substring(1);
					if (!imgRef.equals(myCoverImageReference) ||
							myParagraphsBeforeBodyNumber != myModelReader.getModel().getBookTextModel().getParagraphsNumber()) {
						myModelReader.addImageReference(imgRef, offset);
					}
					if (myInsideCoverpage) {
						myCoverImageReference = imgRef;
					}
				}
				break;
			
			case FB2Tag.BINARY:			
				String contentType = attributes.get("content-type");
				String imgId = attributes.get("id");
				if ((contentType != null) && (id != null)) {
					myCurrentImage = new Base64EncodedImage(contentType);
					myModelReader.addImage(imgId, myCurrentImage);
				}
				break;	
				
			default:
				break;
		}
	}

	public boolean readBook(BookModel model) {
 		myModelReader = new BookReader(model);
		long start = System.currentTimeMillis();
		boolean success = read(model.getFileName());
		LoadingTime = System.currentTimeMillis() - start;
		System.err.println("loading book time = " + LoadingTime);
		return success;
	}
}
