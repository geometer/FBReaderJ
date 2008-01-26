package org.fbreader.formats.fb2;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.bookmodel.BookReader;
import org.fbreader.bookmodel.FBTextKind;
import org.zlibrary.core.xml.*;
import org.zlibrary.text.model.ZLTextParagraph;

public class FB2Reader extends BookReader implements ZLXMLReader {
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

	public FB2Reader(BookModel model) {
 		super(model);
	}

	public boolean read() {
		final ZLXMLProcessor processor = ZLXMLProcessorFactory.getInstance().createXMLProcessor();
		return processor.read(this, getModel().getFileName());
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
		final Base64EncodedImage image = myCurrentImage;
		if (image != null) {
			image.addData(ch, start, length);
		} else {
			addData(ch, start, length);
		}		
	}

	public void characterDataHandlerFinal(char[] ch, int start, int length) {
		if (length == 0) {
			return;
		}
		final Base64EncodedImage image = myCurrentImage;
		if (image != null) {
			image.addData(ch, start, length);
		} else {
			addDataFinal(ch, start, length);
		}		
	}

	public void endElementHandler(String tagName) {
		switch (FB2Tag.getTagByName(tagName)) {
			case FB2Tag.P:
				endParagraph();		
				break;
			case FB2Tag.SUB:
				addControl(FBTextKind.SUB, false);
				break;
			case FB2Tag.SUP:
				addControl(FBTextKind.SUP, false);
				break;
			case FB2Tag.CODE:
				addControl(FBTextKind.CODE, false);
				break;
			case FB2Tag.EMPHASIS:
				addControl(FBTextKind.EMPHASIS, false);
				break;
			case FB2Tag.STRONG:
				addControl(FBTextKind.STRONG, false);
				break;
			case FB2Tag.STRIKETHROUGH:
				addControl(FBTextKind.STRIKETHROUGH, false);
				break;
			
			case FB2Tag.V:
			case FB2Tag.SUBTITLE:
			case FB2Tag.TEXT_AUTHOR:
			case FB2Tag.DATE:
				popKind();
				endParagraph();
				break;	
			
			case FB2Tag.CITE:
			case FB2Tag.EPIGRAPH:
				popKind();
				break;	
			
			case FB2Tag.POEM:
				myInsidePoem = false;
				break;
			
			case FB2Tag.STANZA:
				beginParagraph(ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
				endParagraph();
				popKind();
				break;
				
			case FB2Tag.SECTION:
				if (myReadMainText) {
					endContentsParagraph();
					--mySectionDepth;
					mySectionStarted = false;
				} else {
					unsetCurrentTextModel();
				}
				break;
			
			case FB2Tag.ANNOTATION:
				popKind();
				if (myBodyCounter == 0) {
					insertEndOfSectionParagraph();
					unsetCurrentTextModel();
				}
				break;
			
			case FB2Tag.TITLE:
				popKind();
				exitTitle();
				myInsideTitle = false;
				break;
				
			case FB2Tag.BODY:
				popKind();
				myReadMainText = false;
				unsetCurrentTextModel();
				break;
			
			case FB2Tag.A:
				addControl(myHyperlinkType, false);
				break;
			
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = false;
					insertEndOfSectionParagraph();
					unsetCurrentTextModel();
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

	public void startElementHandler(String tagName, ZLStringMap attributes) {
		String id = attributes.getValue("id");
		if (id != null) {
			if (!myReadMainText) {
				setFootnoteTextModel(id);
			}
			addHyperlinkLabel(id);
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
					addContentsData(SPACE);
				}
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case FB2Tag.SUB:
				addControl(FBTextKind.SUB, true);
				break;
			case FB2Tag.SUP:
				addControl(FBTextKind.SUP, true);
				break;
			case FB2Tag.CODE:
				addControl(FBTextKind.CODE, true);
				break;
			case FB2Tag.EMPHASIS:
				addControl(FBTextKind.EMPHASIS, true);
				break;
			case FB2Tag.STRONG:
				addControl(FBTextKind.STRONG, true);
				break;
			case FB2Tag.STRIKETHROUGH:
				addControl(FBTextKind.STRIKETHROUGH, true);
				break;
			
			case FB2Tag.V:
				pushKind(FBTextKind.VERSE);
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case FB2Tag.TEXT_AUTHOR:
				pushKind(FBTextKind.AUTHOR);
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case FB2Tag.SUBTITLE:
				pushKind(FBTextKind.SUBTITLE);
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			case FB2Tag.DATE:
				pushKind(FBTextKind.DATE);
				beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case FB2Tag.EMPTY_LINE:
				beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				endParagraph();
				break;
			
			case FB2Tag.CITE:
				pushKind(FBTextKind.CITE);
				break;
			case FB2Tag.EPIGRAPH:
				pushKind(FBTextKind.EPIGRAPH);
				break;
			
			case FB2Tag.POEM:
				myInsidePoem = true;
				break;	
			
			case FB2Tag.STANZA:
				pushKind(FBTextKind.STANZA);
				beginParagraph(ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
				endParagraph();
				break;
				
			case FB2Tag.SECTION:
				if (myReadMainText) {
					insertEndOfSectionParagraph();
					++mySectionDepth;
					beginContentsParagraph();
					mySectionStarted  = true;
				}
				break;
			
			case FB2Tag.ANNOTATION:
				if (myBodyCounter == 0) {
					setMainTextModel();
				}
				pushKind(FBTextKind.ANNOTATION);
				break;
			
			case FB2Tag.TITLE:
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
				
			case FB2Tag.BODY:
				++myBodyCounter;
				myParagraphsBeforeBodyNumber = getModel().getBookTextModel().getParagraphsNumber();
				if ((myBodyCounter == 1) || (attributes.getValue("name") == null)) {
					setMainTextModel();
					myReadMainText = true;
				}
				pushKind(FBTextKind.REGULAR);
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
					addHyperlinkControl(myHyperlinkType, ref);
				} else {
					myHyperlinkType = FBTextKind.FOOTNOTE;
					addControl(myHyperlinkType, true);
				}
				break;
			
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = true;
					setMainTextModel();
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
							myParagraphsBeforeBodyNumber != getModel().getBookTextModel().getParagraphsNumber()) {
						addImageReference(imgRef, offset);
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
					addImage(imgId, myCurrentImage);
				}
				break;	
				
			default:
				break;
		}
	}
}
