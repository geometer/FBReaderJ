package org.fbreader.formats.fb2;

import java.util.Map;
import java.util.Set;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.bookmodel.BookReader;
import org.fbreader.bookmodel.FBTextKind;
import org.zlibrary.core.xml.ZLXMLReader;
import org.zlibrary.text.model.ZLTextParagraph;

public class FB2Reader extends ZLXMLReader {
	private BookReader myModelReader = new BookReader(new BookModel());
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
	private boolean myProcessingImage = false;
	private final StringBuffer myImageBuffer = new StringBuffer();
	private String myCoverImageReference;
	private int myParagraphsBeforeBodyNumber = Integer.MAX_VALUE;
	
	private FB2Tag getTag(String s) {
		if (s.contains("-")) {
			s = s.replace('-', '_');
		}
		return FB2Tag.valueOf(s.toUpperCase());
	}
	
	private String reference(Map<String, String> attributes) {
		Set<String> keys = attributes.keySet();
		for (String s : keys) {
			if (s.endsWith(":href")) {
				return attributes.get(s);
			}
		}
		return "";
	}
	
//	private BookModel myBookModel = new BookModel();
	
	@Override
	public void characterDataHandler(char[] ch, int start, int length) {
		if (length > 0 && myProcessingImage) {
			myImageBuffer.append(String.valueOf(ch, start, length));
		} else {
			myModelReader.addData(String.valueOf(ch, start, length));
		}		
	}
		
	@Override
	public void endElementHandler(String tagName) {
		FB2Tag tag;
		try {
			tag = getTag(tagName);
		} catch (IllegalArgumentException e) {
			return;
		}		
		switch (tag) {
			case P:
				myModelReader.endParagraph();		
				break;
				
			case SUB:
				myModelReader.addControl(FBTextKind.SUB, false);
				break;
			case SUP:
				myModelReader.addControl(FBTextKind.SUP, false);
				break;
			case CODE:
				myModelReader.addControl(FBTextKind.CODE, false);
				break;
			case EMPHASIS:
				myModelReader.addControl(FBTextKind.EMPHASIS, false);
				break;
			case STRONG:
				myModelReader.addControl(FBTextKind.STRONG, false);
				break;
			case STRIKETHROUGH:
				myModelReader.addControl(FBTextKind.STRIKETHROUGH, false);
				break;
			
			case V:
			case SUBTITLE:
			case TEXT_AUTHOR:
			case DATE:
				myModelReader.popKind();
				myModelReader.endParagraph();
				break;	
			
			case CITE:
			case EPIGRAPH:
				myModelReader.popKind();
				break;	
			
			case POEM:
				myInsidePoem = false;
				break;
			
			case STANZA:
				myModelReader.beginParagraph(ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
				myModelReader.endParagraph();
				myModelReader.popKind();
				break;
				
			case SECTION:
				if (myReadMainText) {
					myModelReader.endContentsParagraph();
					--mySectionDepth;
					mySectionStarted = false;
				} else {
					myModelReader.unsetCurrentTextModel();
				}
				break;
			
			case ANNOTATION:
				myModelReader.popKind();
				if (myBodyCounter == 0) {
					myModelReader.insertEndOfSectionParagraph();
					myModelReader.unsetCurrentTextModel();
				}
				break;
			
			case TITLE:
				myModelReader.popKind();
				myModelReader.exitTitle();
				myInsideTitle = false;
				break;
				
			case BODY:
				myModelReader.popKind();
				myReadMainText = false;
				myModelReader.unsetCurrentTextModel();
				break;
			
			case A:
				myModelReader.addControl(myHyperlinkType, false);
				break;
			
			case COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = false;
					myModelReader.insertEndOfSectionParagraph();
					myModelReader.unsetCurrentTextModel();
				}
				break;	
			
			case BINARY:
				if ((myImageBuffer.length() != 0) && (myCurrentImage != null)) {
					myCurrentImage.addData(myImageBuffer);
					myImageBuffer.delete(0, myImageBuffer.length());
					myCurrentImage = null;
				}
				myProcessingImage = false;
				break;	
				
			default:
				break;
		}		
	}

	@Override
	public void startElementHandler(String tagName, Map<String, String> attributes) {
		String id = attributes.get("id");
		if (id != null) {
			if (!myReadMainText) {
				myModelReader.setFootnoteTextModel(id);
			}
			myModelReader.addHyperlinkLabel(id);
		}
		FB2Tag tag;
		try {
			tag = getTag(tagName);
		} catch (IllegalArgumentException e) {
			return;
		}
		switch (tag) {
			case P:
				if (mySectionStarted) {
					mySectionStarted = false;
				} else if (myInsideTitle) {
					myModelReader.addContentsData(" ");
				}
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case SUB:
				myModelReader.addControl(FBTextKind.SUB, true);
				break;
			case SUP:
				myModelReader.addControl(FBTextKind.SUP, true);
				break;
			case CODE:
				myModelReader.addControl(FBTextKind.CODE, true);
				break;
			case EMPHASIS:
				myModelReader.addControl(FBTextKind.EMPHASIS, true);
				break;
			case STRONG:
				myModelReader.addControl(FBTextKind.STRONG, true);
				break;
			case STRIKETHROUGH:
				myModelReader.addControl(FBTextKind.STRIKETHROUGH, true);
				break;
			
			case V:
				myModelReader.pushKind(FBTextKind.VERSE);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case TEXT_AUTHOR:
				myModelReader.pushKind(FBTextKind.AUTHOR);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case SUBTITLE:
				myModelReader.pushKind(FBTextKind.SUBTITLE);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			case DATE:
				myModelReader.pushKind(FBTextKind.DATE);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case EMPTY_LINE:
				myModelReader.beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				myModelReader.endParagraph();
				break;
			
			case CITE:
				myModelReader.pushKind(FBTextKind.CITE);
				break;
			case EPIGRAPH:
				myModelReader.pushKind(FBTextKind.EPIGRAPH);
				break;
			
			case POEM:
				myInsidePoem = true;
				break;	
			
			case STANZA:
				myModelReader.pushKind(FBTextKind.STANZA);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
				myModelReader.endParagraph();
				break;
				
			case SECTION:
				if (myReadMainText) {
					myModelReader.insertEndOfSectionParagraph();
					++mySectionDepth;
					myModelReader.beginContentsParagraph();
					mySectionStarted  = true;
				}
				break;
			
			case ANNOTATION:
				if (myBodyCounter == 0) {
					myModelReader.setMainTextModel();
				}
				myModelReader.pushKind(FBTextKind.ANNOTATION);
				break;
			
			case TITLE:
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
				
			case BODY:
				++myBodyCounter;
				myParagraphsBeforeBodyNumber = myModelReader.getModel().getBookModel().getParagraphsNumber();
				if ((myBodyCounter == 1) || (attributes.get("name") == null)) {
					myModelReader.setMainTextModel();
					myReadMainText = true;
				}
				myModelReader.pushKind(FBTextKind.REGULAR);
				break;
			
			case A:
				String ref = reference(attributes);
				if ((ref != null) && !ref.isEmpty()) {
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
			
			case COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = true;
					myModelReader.setMainTextModel();
				}
				break;	
			
			case IMAGE:
				String imgRef = reference(attributes);
				String vOffset = attributes.get("voffset");
				short offset = 0;
				try {
					offset = Short.valueOf(vOffset);
				} catch (NumberFormatException e) {
				}
				if ((imgRef != null) && !imgRef.isEmpty() && (imgRef.charAt(0) == '#')) {
					imgRef = imgRef.substring(1);
					if (!imgRef.equals(myCoverImageReference) ||
							myParagraphsBeforeBodyNumber != myModelReader.getModel().getBookModel().getParagraphsNumber()) {
						myModelReader.addImageReference(imgRef, offset);
					}
					if (myInsideCoverpage) {
						myCoverImageReference = imgRef;
					}
				}
				break;
			
			case BINARY:			
				String contentType = attributes.get("content-type");
				String imgId = attributes.get("id");
				if ((contentType != null) && (id != null)) {
					myCurrentImage = new Base64EncodedImage(contentType);
					myModelReader.addImage(imgId, myCurrentImage);
					myProcessingImage = true;
				}
				break;	
				
			default:
				break;
		}
	}
	
	public BookModel readBook(String fileName) {
		long start = System.currentTimeMillis();
		boolean success = read(fileName);
		System.err.println("loading book time = " + (System.currentTimeMillis() - start));
		return success ? myModelReader.getModel() : null;
	}
}
