/*
 * Copyright (C) 2007-2011 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.formats.fb2;

import java.util.*;

import org.geometerplus.zlibrary.core.constants.XMLNamespaces;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.core.xml.*;

import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

import org.geometerplus.fbreader.bookmodel.*;

public final class FB2Reader extends ZLXMLReaderAdapter {
	private final BookReader myBookReader;

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

	private byte[] myTagStack = new byte[10];
	private int myTagStackSize = 0;

	public FB2Reader(BookModel model) {
 		myBookReader = new BookReader(model);
	}

	boolean readBook() {
		Base64EncodedImage.resetCounter();
		return ZLXMLProcessor.read(this, myBookReader.Model.Book.File);
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
			myBookReader.addData(ch, start, length, false);
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
			myBookReader.addData(ch, start, length, true);
		}		
	}

	public boolean endElementHandler(String tagName) {
		final byte tag = myTagStack[--myTagStackSize];
		switch (tag) {
			case FB2Tag.P:
				myBookReader.endParagraph();		
				break;
			case FB2Tag.SUB:
				myBookReader.addControl(FBTextKind.SUB, false);
				break;
			case FB2Tag.SUP:
				myBookReader.addControl(FBTextKind.SUP, false);
				break;
			case FB2Tag.CODE:
				myBookReader.addControl(FBTextKind.CODE, false);
				break;
			case FB2Tag.EMPHASIS:
				myBookReader.addControl(FBTextKind.EMPHASIS, false);
				break;
			case FB2Tag.STRONG:
				myBookReader.addControl(FBTextKind.STRONG, false);
				break;
			case FB2Tag.STRIKETHROUGH:
				myBookReader.addControl(FBTextKind.STRIKETHROUGH, false);
				break;
			
			case FB2Tag.V:
			case FB2Tag.SUBTITLE:
			case FB2Tag.TEXT_AUTHOR:
			case FB2Tag.DATE:
				myBookReader.popKind();
				myBookReader.endParagraph();
				break;	
			
			case FB2Tag.CITE:
			case FB2Tag.EPIGRAPH:
				myBookReader.popKind();
				break;	
			
			case FB2Tag.POEM:
				myInsidePoem = false;
				break;
			
			case FB2Tag.STANZA:
				myBookReader.beginParagraph(ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
				myBookReader.endParagraph();
				myBookReader.beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				myBookReader.endParagraph();
				myBookReader.popKind();
				break;
				
			case FB2Tag.SECTION:
				if (myReadMainText) {
					myBookReader.endContentsParagraph();
					--mySectionDepth;
					mySectionStarted = false;
				} else {
					myBookReader.unsetCurrentTextModel();
				}
				break;
			
			case FB2Tag.ANNOTATION:
				myBookReader.popKind();
				if (myBodyCounter == 0) {
					myBookReader.insertEndOfSectionParagraph();
					myBookReader.unsetCurrentTextModel();
				}
				break;
			
			case FB2Tag.TITLE:
				myBookReader.popKind();
				myBookReader.exitTitle();
				myInsideTitle = false;
				break;
				
			case FB2Tag.BODY:
				myBookReader.popKind();
				myReadMainText = false;
				if (myReadMainText) {
					myBookReader.insertEndOfSectionParagraph();
				}
				if (mySectionDepth > 0) {
					myBookReader.endContentsParagraph();
					mySectionDepth = 0;
				}
				myBookReader.unsetCurrentTextModel();
				break;
			
			case FB2Tag.A:
				myBookReader.addControl(myHyperlinkType, false);
				break;
			
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = false;
					myBookReader.insertEndOfSectionParagraph();
					myBookReader.unsetCurrentTextModel();
				}
				break;	
			
			case FB2Tag.BINARY:
				if (myCurrentImage != null) {
					myCurrentImage.close();
					myCurrentImage = null;
				}
				break;	
				
			default:
				break;
		}		
		return false;
	}

	public boolean startElementHandler(String tagName, ZLStringMap attributes) {
		String id = attributes.getValue("id");
		if (id != null) {
			if (!myReadMainText) {
				myBookReader.setFootnoteTextModel(id);
			}
			myBookReader.addHyperlinkLabel(id);
		}
		final byte tag = FB2Tag.getTagByName(tagName);
		byte[] tagStack = myTagStack;
		if (tagStack.length == myTagStackSize) {
			tagStack = ZLArrayUtils.createCopy(tagStack, myTagStackSize, myTagStackSize * 2);
			myTagStack = tagStack;
		}
		tagStack[myTagStackSize++] = tag;
		switch (tag) {
			case FB2Tag.P:
				if (mySectionStarted) {
					mySectionStarted = false;
				} else if (myInsideTitle) {
					myBookReader.addContentsData(SPACE);
				}
				myBookReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case FB2Tag.SUB:
				myBookReader.addControl(FBTextKind.SUB, true);
				break;
			case FB2Tag.SUP:
				myBookReader.addControl(FBTextKind.SUP, true);
				break;
			case FB2Tag.CODE:
				myBookReader.addControl(FBTextKind.CODE, true);
				break;
			case FB2Tag.EMPHASIS:
				myBookReader.addControl(FBTextKind.EMPHASIS, true);
				break;
			case FB2Tag.STRONG:
				myBookReader.addControl(FBTextKind.STRONG, true);
				break;
			case FB2Tag.STRIKETHROUGH:
				myBookReader.addControl(FBTextKind.STRIKETHROUGH, true);
				break;
			
			case FB2Tag.V:
				myBookReader.pushKind(FBTextKind.VERSE);
				myBookReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case FB2Tag.TEXT_AUTHOR:
				myBookReader.pushKind(FBTextKind.AUTHOR);
				myBookReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
				
			case FB2Tag.SUBTITLE:
				myBookReader.pushKind(FBTextKind.SUBTITLE);
				myBookReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			case FB2Tag.DATE:
				myBookReader.pushKind(FBTextKind.DATE);
				myBookReader.beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
				break;
			
			case FB2Tag.EMPTY_LINE:
				myBookReader.beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
				myBookReader.endParagraph();
				break;
			
			case FB2Tag.CITE:
				myBookReader.pushKind(FBTextKind.CITE);
				break;
			case FB2Tag.EPIGRAPH:
				myBookReader.pushKind(FBTextKind.EPIGRAPH);
				break;
			
			case FB2Tag.POEM:
				myInsidePoem = true;
				break;	
			
			case FB2Tag.STANZA:
				myBookReader.pushKind(FBTextKind.STANZA);
				myBookReader.beginParagraph(ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
				myBookReader.endParagraph();
				break;
				
			case FB2Tag.SECTION:
				if (myReadMainText) {
					myBookReader.insertEndOfSectionParagraph();
					++mySectionDepth;
					myBookReader.beginContentsParagraph();
					mySectionStarted = true;
				}
				break;
			
			case FB2Tag.ANNOTATION:
				if (myBodyCounter == 0) {
					myBookReader.setMainTextModel();
				}
				myBookReader.pushKind(FBTextKind.ANNOTATION);
				break;
			
			case FB2Tag.TITLE:
				if (myInsidePoem) {
					myBookReader.pushKind(FBTextKind.POEM_TITLE);
				} else if (mySectionDepth == 0) {
					myBookReader.insertEndOfSectionParagraph();
					myBookReader.pushKind(FBTextKind.TITLE);
				} else {
					myBookReader.pushKind(FBTextKind.SECTION_TITLE);
					if (!myBookReader.hasContentsData()) {
						myInsideTitle = true;
						myBookReader.enterTitle();
					}
				}
				break;
				
			case FB2Tag.BODY:
				++myBodyCounter;
				myParagraphsBeforeBodyNumber = myBookReader.Model.BookTextModel.getParagraphsNumber();
				final String name = attributes.getValue("name");
				if (myBodyCounter == 1 || !"notes".equals(name)) {
					myBookReader.setMainTextModel();
					if (name != null) {
						myBookReader.beginContentsParagraph();
						myBookReader.addContentsData(name.toCharArray());
						++mySectionDepth;
					}
					myReadMainText = true;
				}
				myBookReader.pushKind(FBTextKind.REGULAR);
				break;
			
			case FB2Tag.A:
			{
				String ref = getAttributeValue(attributes, XMLNamespaces.XLink, "href");
				if ((ref != null) && (ref.length() != 0)) {
					final String type = attributes.getValue("type");
					if (ref.charAt(0) == '#') {
						myHyperlinkType = "note".equals(type) ? FBTextKind.FOOTNOTE : FBTextKind.INTERNAL_HYPERLINK;
						ref = ref.substring(1);
					} else {
						myHyperlinkType = FBTextKind.EXTERNAL_HYPERLINK;
					}
					myBookReader.addHyperlinkControl(myHyperlinkType, ref);
				} else {
					myHyperlinkType = FBTextKind.FOOTNOTE;
					myBookReader.addControl(myHyperlinkType, true);
				}
				break;
			}
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = true;
					myBookReader.setMainTextModel();
				}
				break;	

			case FB2Tag.IMAGE:
			{
				String imgRef = getAttributeValue(attributes, XMLNamespaces.XLink, "href");
				if ((imgRef != null) && (imgRef.length() != 0) && (imgRef.charAt(0) == '#')) {
					String vOffset = attributes.getValue("voffset");
					short offset = 0;
					try {
						offset = Short.parseShort(vOffset);
					} catch (NumberFormatException e) {
					}
					imgRef = imgRef.substring(1);
					if (!imgRef.equals(myCoverImageReference) ||
							myParagraphsBeforeBodyNumber != myBookReader.Model.BookTextModel.getParagraphsNumber()) {
						myBookReader.addImageReference(imgRef, offset);
					}
					if (myInsideCoverpage) {
						myCoverImageReference = imgRef;
					}
				}
				break;
			}
			case FB2Tag.BINARY:			
				final String contentType = attributes.getValue("content-type");
				final String imgId = attributes.getValue("id");
				if (contentType != null && id != null) {
					myCurrentImage = new Base64EncodedImage(MimeType.get(contentType));
					myBookReader.addImage(imgId, myCurrentImage);
				}
				break;	

			default:
				break;
		}
		return false;
	}

	public boolean processNamespaces() {
		return true;
	}

	public void addExternalEntities(HashMap<String,char[]> entityMap) {
		entityMap.put("FBReaderVersion", ZLibrary.Instance().getVersionName().toCharArray());
	}

	public List<String> externalDTDs() {
		return Collections.emptyList();
	}
}
