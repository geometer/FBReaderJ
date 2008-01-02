package org.fbreader.formats.fb2;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.bookmodel.BookReader;
import org.fbreader.bookmodel.FBTextKind;
import org.zlibrary.core.xml.ZLXMLReader;
import org.zlibrary.text.model.ZLTextParagraph;

public class FB2Reader extends ZLXMLReader {
	private BookReader myModelReader;
	
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
	private String myHrefAttribute = ":href";
	
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
		final BookReader modelReader = myModelReader;
		switch (FB2Tag.getTagByName(tagName)) {
			case FB2Tag.P:
				modelReader.endParagraph();		
				break;
			case FB2Tag.SUB:
				modelReader.addControl(FBTextKind.SUB, false);
				break;
			case FB2Tag.SUP:
				modelReader.addControl(FBTextKind.SUP, false);
				break;
			case FB2Tag.CODE:
				modelReader.addControl(FBTextKind.CODE, false);
				break;
			case FB2Tag.EMPHASIS:
				modelReader.addControl(FBTextKind.EMPHASIS, false);
				break;
			case FB2Tag.STRONG:
				modelReader.addControl(FBTextKind.STRONG, false);
				break;
			case FB2Tag.STRIKETHROUGH:
				modelReader.addControl(FBTextKind.STRIKETHROUGH, false);
				break;
			
			case FB2Tag.V:
			case FB2Tag.SUBTITLE:
			case FB2Tag.TEXT_AUTHOR:
			case FB2Tag.DATE:
				modelReader.popKind();
				modelReader.endParagraph();
				break;	
			
			case FB2Tag.CITE:
			case FB2Tag.EPIGRAPH:
				modelReader.popKind();
				break;	
			
			case FB2Tag.POEM:
				myInsidePoem = false;
				break;
			
			case FB2Tag.STANZA:
				modelReader.beginParagraph(ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
				modelReader.endParagraph();
				modelReader.popKind();
				break;
				
			case FB2Tag.SECTION:
				if (myReadMainText) {
					modelReader.endContentsParagraph();
					--mySectionDepth;
					mySectionStarted = false;
				} else {
					modelReader.unsetCurrentTextModel();
				}
				break;
			
			case FB2Tag.ANNOTATION:
				modelReader.popKind();
				if (myBodyCounter == 0) {
					modelReader.insertEndOfSectionParagraph();
					modelReader.unsetCurrentTextModel();
				}
				break;
			
			case FB2Tag.TITLE:
				modelReader.popKind();
				modelReader.exitTitle();
				myInsideTitle = false;
				break;
				
			case FB2Tag.BODY:
				modelReader.popKind();
				myReadMainText = false;
				modelReader.unsetCurrentTextModel();
				break;
			
			case FB2Tag.A:
				modelReader.addControl(myHyperlinkType, false);
				break;
			
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = false;
					modelReader.insertEndOfSectionParagraph();
					modelReader.unsetCurrentTextModel();
				}
				break;	
			
			case FB2Tag.BINARY:
				if (myCurrentImage != null) {
					myCurrentImage.trimToSize();
					myCurrentImage = null;
				}
				break;	
				
			default:
				break;
		}		
	}

	public void startElementHandler(String tagName, StringMap attributes) {
		final BookReader modelReader = myModelReader;
		String id = attributes.getValue("id");
		if (id != null) {
			if (!myReadMainText) {
				modelReader.setFootnoteTextModel(id);
			}
			modelReader.addHyperlinkLabel(id);
		}
		switch (FB2Tag.getTagByName(tagName)) {
			case FB2Tag.FICTIONBOOK:
			{
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
				}
				break;
			}
			case FB2Tag.P:
				if (mySectionStarted) {
					mySectionStarted = false;
				} else if (myInsideTitle) {
					modelReader.addContentsData(SPACE);
				}
				modelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case FB2Tag.SUB:
				modelReader.addControl(FBTextKind.SUB, true);
				break;
			case FB2Tag.SUP:
				modelReader.addControl(FBTextKind.SUP, true);
				break;
			case FB2Tag.CODE:
				modelReader.addControl(FBTextKind.CODE, true);
				break;
			case FB2Tag.EMPHASIS:
				modelReader.addControl(FBTextKind.EMPHASIS, true);
				break;
			case FB2Tag.STRONG:
				modelReader.addControl(FBTextKind.STRONG, true);
				break;
			case FB2Tag.STRIKETHROUGH:
				modelReader.addControl(FBTextKind.STRIKETHROUGH, true);
				break;
			
			case FB2Tag.V:
				modelReader.pushKind(FBTextKind.VERSE);
				modelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case FB2Tag.TEXT_AUTHOR:
				modelReader.pushKind(FBTextKind.AUTHOR);
				modelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case FB2Tag.SUBTITLE:
				modelReader.pushKind(FBTextKind.SUBTITLE);
				modelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			case FB2Tag.DATE:
				modelReader.pushKind(FBTextKind.DATE);
				modelReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case FB2Tag.EMPTY_LINE:
				modelReader.beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				modelReader.endParagraph();
				break;
			
			case FB2Tag.CITE:
				modelReader.pushKind(FBTextKind.CITE);
				break;
			case FB2Tag.EPIGRAPH:
				modelReader.pushKind(FBTextKind.EPIGRAPH);
				break;
			
			case FB2Tag.POEM:
				myInsidePoem = true;
				break;	
			
			case FB2Tag.STANZA:
				modelReader.pushKind(FBTextKind.STANZA);
				modelReader.beginParagraph(ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
				modelReader.endParagraph();
				break;
				
			case FB2Tag.SECTION:
				if (myReadMainText) {
					modelReader.insertEndOfSectionParagraph();
					++mySectionDepth;
					modelReader.beginContentsParagraph();
					mySectionStarted  = true;
				}
				break;
			
			case FB2Tag.ANNOTATION:
				if (myBodyCounter == 0) {
					modelReader.setMainTextModel();
				}
				modelReader.pushKind(FBTextKind.ANNOTATION);
				break;
			
			case FB2Tag.TITLE:
				if (myInsidePoem) {
					modelReader.pushKind(FBTextKind.POEM_TITLE);
				} else if (mySectionDepth == 0) {
					modelReader.insertEndOfSectionParagraph();
					modelReader.pushKind(FBTextKind.TITLE);
				} else {
					modelReader.pushKind(FBTextKind.SECTION_TITLE);
					myInsideTitle = true;
					modelReader.enterTitle();
				}
				break;
				
			case FB2Tag.BODY:
				++myBodyCounter;
				myParagraphsBeforeBodyNumber = modelReader.getModel().getBookTextModel().getParagraphsNumber();
				if ((myBodyCounter == 1) || (attributes.getValue("name") == null)) {
					modelReader.setMainTextModel();
					myReadMainText = true;
				}
				modelReader.pushKind(FBTextKind.REGULAR);
				break;
			
			case FB2Tag.A:
				String ref = attributes.getValue(myHrefAttribute);
				if ((ref != null) && (ref.length() != 0)) {
					if (ref.charAt(0) == '#') {
						myHyperlinkType = FBTextKind.FOOTNOTE;
						ref = ref.substring(1);
					} else {
						myHyperlinkType = FBTextKind.EXTERNAL_HYPERLINK;
					}
					modelReader.addHyperlinkControl(myHyperlinkType, ref);
				} else {
					myHyperlinkType = FBTextKind.FOOTNOTE;
					modelReader.addControl(myHyperlinkType, true);
				}
				break;
			
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = true;
					modelReader.setMainTextModel();
				}
				break;	
			
			case FB2Tag.IMAGE:
				String imgRef = attributes.getValue(myHrefAttribute);
				if ((imgRef != null) && (imgRef.length() != 0) && (imgRef.charAt(0) == '#')) {
					String vOffset = attributes.getValue("voffset");
					short offset = 0;
					try {
						offset = Short.parseShort(vOffset);
					} catch (NumberFormatException e) {
					}
					imgRef = imgRef.substring(1);
					if (!imgRef.equals(myCoverImageReference) ||
							myParagraphsBeforeBodyNumber != modelReader.getModel().getBookTextModel().getParagraphsNumber()) {
						modelReader.addImageReference(imgRef, offset);
					}
					if (myInsideCoverpage) {
						myCoverImageReference = imgRef;
					}
				}
				break;
			
			case FB2Tag.BINARY:			
				String contentType = attributes.getValue("content-type");
				String imgId = attributes.getValue("id");
				if ((contentType != null) && (id != null)) {
					myCurrentImage = new Base64EncodedImage(contentType);
					modelReader.addImage(imgId, myCurrentImage);
				}
				break;	
				
			default:
				break;
		}
	}

	public boolean readBook(BookModel model) {
 		myModelReader = new BookReader(model);
		return read(model.getFileName());
	}
}
