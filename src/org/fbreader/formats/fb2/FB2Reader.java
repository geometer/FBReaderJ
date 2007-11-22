package org.fbreader.formats.fb2;

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
	
	private FB2Tag getTag(String s) {
		if (s.contains("-")) {
			s = s.replace('-', '_');
		}
		return FB2Tag.valueOf(s.toUpperCase());
	}
	
	private byte getKind(String s) {
		return (byte) FBTextKind.valueOf(s.toUpperCase()).Index;
	}
	
	private String reference(String[] attributes) {
		int length = attributes.length-1;
		for (int i = 0; i < length; i+=2) {
			if (attributes[i].endsWith(":href")) {
				return attributes[i+1];
			}
		}
		return "";
	}
	
//	private BookModel myBookModel = new BookModel();
	
	@Override
	public void characterDataHandler(char[] ch, int start, int length) {
		myModelReader.addData(String.valueOf(ch, start, length));
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
				
			default:
				break;
		}		
	}

	@Override
	public void startElementHandler(String tagName, String[] attributes) {
		String id = ZLXMLReader.attributeValue(attributes, "id");
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
					myModelReader.pushKind((byte) tag.ordinal());
				} else {
					myModelReader.pushKind((byte) FBTextKind.SECTION_TITLE.Index);
					myInsideTitle = true;
					myModelReader.enterTitle();
				}
				break;
				
			case BODY:
				++myBodyCounter;
				if ((myBodyCounter == 1) || (ZLXMLReader.attributeValue(attributes, "name") == null)) {
					myModelReader.setMainTextModel();
					myReadMainText = true;
				}
				myModelReader.pushKind((byte) FBTextKind.REGULAR.Index);
				break;
			
			case A:
				String ref = reference(attributes);
				if (ref != "") {
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
				
			default:
				break;
		}
	}
	
	public BookModel readBook(String fileName) {
		return read(fileName) ? myModelReader.getModel() : null;
	}
}
