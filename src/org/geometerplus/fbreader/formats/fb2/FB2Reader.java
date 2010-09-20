/*
 * Copyright (C) 2007-2010 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.fbreader.bookmodel.*;
import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.core.util.*;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

public final class FB2Reader extends BookReader implements ZLXMLReader {
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
	private String myHrefAttribute;

	private byte[] myTagStack = new byte[10];
	private int myTagStackSize = 0;

	public FB2Reader(BookModel model) {
 		super(model);
	}

	boolean readBook() {
		Base64EncodedImage.resetCounter();
		return ZLXMLProcessor.read(this, Model.Book.File);
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
			addData(ch, start, length, false);
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
			addData(ch, start, length, true);
		}		
	}

	public boolean endElementHandler(String tagName) {
		final byte tag = myTagStack[--myTagStackSize];
		switch (tag) {
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
				beginParagraph(ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
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
				setFootnoteTextModel(id);
			}
			addHyperlinkLabel(id);
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
					mySectionStarted = true;
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
				myParagraphsBeforeBodyNumber = Model.BookTextModel.getParagraphsNumber();
				if ((myBodyCounter == 1) || (attributes.getValue("name") == null)) {
					setMainTextModel();
					myReadMainText = true;
				}
				pushKind(FBTextKind.REGULAR);
				break;
			
			case FB2Tag.A:
				if (myHrefAttribute != null) {
					String ref = attributes.getValue(myHrefAttribute);
					String type = attributes.getValue("type");
					if ((ref != null) && (ref.length() != 0)) {
						if (ref.charAt(0) == '#') {
							myHyperlinkType = "note".equals(type) ? FBTextKind.FOOTNOTE : FBTextKind.INTERNAL_HYPERLINK;
							ref = ref.substring(1);
						} else {
							myHyperlinkType = FBTextKind.EXTERNAL_HYPERLINK;
						}
						addHyperlinkControl(myHyperlinkType, ref);
					} else {
						myHyperlinkType = FBTextKind.FOOTNOTE;
						addControl(myHyperlinkType, true);
					}
				}
				break;
			
			case FB2Tag.COVERPAGE:
				if (myBodyCounter == 0) {
					myInsideCoverpage = true;
					setMainTextModel();
				}
				break;	
			
			case FB2Tag.IMAGE:
				if (myHrefAttribute != null) {
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
								myParagraphsBeforeBodyNumber != Model.BookTextModel.getParagraphsNumber()) {
							addImageReference(imgRef, offset);
						}
						if (myInsideCoverpage) {
							myCoverImageReference = imgRef;
						}
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
		return false;
	}

	public boolean processNamespaces() {
		return true;
	}

	public void namespaceMapChangedHandler(HashMap<String,String> namespaceMap) {
		myHrefAttribute = null;
		for (Map.Entry<String,String> entry : namespaceMap.entrySet()) {
			if ("http://www.w3.org/1999/xlink".equals(entry.getValue())) {
				myHrefAttribute = (entry.getKey() + ":href").intern();
				break;
			}
		}
	}

	public void addExternalEntities(HashMap<String,char[]> entityMap) {
		entityMap.put("FBReaderVersion", ZLibrary.Instance().getVersionName().toCharArray());
	}

	public List<String> externalDTDs() {
		return Collections.emptyList();
	}
}
