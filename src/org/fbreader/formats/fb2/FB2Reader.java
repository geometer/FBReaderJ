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
	private StringBuffer myImageBuffer = new StringBuffer();
	private String myCoverImageReference;
	private int myParagraphsBeforeBodyNumber = Integer.MAX_VALUE;
	
	private FB2Tag getTag(String s) {
		if (s.contains("-")) {
			s = s.replace('-', '_');
		}
		return FB2Tag.valueOf(s.toUpperCase());
	}
	
	private byte getKind(String s) {
		return (byte) FBTextKind.valueOf(s.toUpperCase()).Index;
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
			case SUP:
			case CODE:
			case EMPHASIS:
			case STRONG:
			case STRIKETHROUGH:
				myModelReader.addControl(getKind(tagName), false);
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
			case SUP:
			case CODE:
			case EMPHASIS:
			case STRONG:
			case STRIKETHROUGH:
				myModelReader.addControl(getKind(tagName), true);		
				break;
			
			case V:
				myModelReader.pushKind((byte) FBTextKind.VERSE.Index);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case TEXT_AUTHOR:
				myModelReader.pushKind((byte) FBTextKind.AUTHOR.Index);
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case SUBTITLE:
			case DATE:
				myModelReader.pushKind(getKind(tagName));
				myModelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case EMPTY_LINE:
				myModelReader.beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				myModelReader.endParagraph();
				break;
			
			case CITE:
			case EPIGRAPH:
				myModelReader.pushKind(getKind(tagName));
				break;
			
			case POEM:
				myInsidePoem = true;
				break;	
			
			case STANZA:
				myModelReader.pushKind((byte) FBTextKind.STANZA.Index);
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
				myModelReader.pushKind((byte) FBTextKind.ANNOTATION.Index);
				break;
			
			case TITLE:
				if (myInsidePoem) {
					myModelReader.pushKind((byte) FBTextKind.POEM_TITLE.Index);
				} else if (mySectionDepth == 0) {
					myModelReader.insertEndOfSectionParagraph();
					myModelReader.pushKind((byte) FBTextKind.TITLE.Index);
				} else {
					myModelReader.pushKind((byte) FBTextKind.SECTION_TITLE.Index);
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
				myModelReader.pushKind((byte) FBTextKind.REGULAR.Index);
				break;
			
			case A:
				String ref = reference(attributes);
				if (!ref.equals("")) {
					if (ref.charAt(0) == '#') {
						myHyperlinkType = (byte) FBTextKind.FOOTNOTE.Index;
						ref = ref.substring(1);
					} else {
						myHyperlinkType = (byte) FBTextKind.EXTERNAL_HYPERLINK.Index;
					}
					myModelReader.addHyperlinkControl(myHyperlinkType, ref);
				} else {
					myHyperlinkType = (byte) FBTextKind.FOOTNOTE.Index;
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
				int offset = 0;
				try {
					offset = Integer.valueOf(vOffset);
				} catch (NumberFormatException e) {
				}
				if ((imgRef != null) && (imgRef.charAt(0) == '#')) {
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
		return read(fileName) ? myModelReader.getModel() : null;
	}
}
