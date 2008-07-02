/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.html;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.formats.html.HtmlTag;
import org.geometerplus.zlibrary.core.xml.ZLStringMap;
import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.zlibrary.core.xml.ZLXMLProcessorFactory;
import org.geometerplus.zlibrary.core.html.ZLHtmlProcessor;
import org.geometerplus.zlibrary.core.html.ZLHtmlProcessorFactory;
import org.geometerplus.zlibrary.core.html.ZLHtmlReader;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.util.ZLArrayUtils;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

public class HtmlReader extends BookReader implements ZLHtmlReader {

	private boolean myInsideTitle = false;
	private int myBodyCounter = 0;
	private boolean myReadMainText = false;
	private boolean mySectionStarted = false;
	private byte myHyperlinkType;
	//private int myParagraphsBeforeBodyNumber = Integer.MAX_VALUE;
	private final char[] SPACE = { ' ' };
	private String myHrefAttribute = "href";
	private String mySrcAttribute = "src";
	private boolean myAdditionalParagraphExists = false;
	private boolean myOrderedListIsStarted = false;
	private boolean myUnorderedListIsStarted = false;
	private int myOLCounter = 0;
	private byte[] myControls = new byte[10];
	private byte myControlsNumber = 0;
	
	public HtmlReader(BookModel model) {
		super(model);
	}

	public boolean read() {
		final ZLHtmlProcessor processor = ZLHtmlProcessorFactory.getInstance()
				.createHtmlProcessor();
		return processor.read(this, Model.Description.FileName);
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
		unsetCurrentTextModel();
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
		addDataFinal(ch, start, length);
	}

	private void openControl(byte control) {
		addControl(control, true);
		if (++myControlsNumber > myControls.length) {
			byte[] temp = ZLArrayUtils.createCopy(myControls, 0, myControls.length);
			myControls = new byte[2 * myControlsNumber];
			for (int i = 0; i < myControlsNumber - 1; i++) {
				myControls[i] = temp[i];
			}
		}
		myControls[myControlsNumber - 1] = control;
	}
	
	private void closeControl(byte control) {
		for (int i = 0; i < myControlsNumber; i++) {
			addControl(myControls[i], false);
		}
		boolean flag = false;
		int removedControl = myControlsNumber;
		for (int i = 0; i < myControlsNumber; i++) {
			if (!flag && (myControls[i] == control)) {
				flag = true;
				removedControl = i;
				continue;
			}
			addControl(myControls[i], true);
		}
		if (removedControl == myControlsNumber) {
			return;
		}
		--myControlsNumber;
		for (int i = removedControl; i < myControlsNumber; i++) {
			myControls[i] = myControls[i + 1];
		}
	}
	
	private void startNewParagraph() {
		endParagraph();
		beginParagraph(ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	}
	
	public void endElementHandler(String tagName) {
		switch (HtmlTag.getTagByName(tagName)) {
			
			case HtmlTag.SCRIPT:
			case HtmlTag.SELECT:
			case HtmlTag.STYLE:
				startNewParagraph();
				break;
				
			case HtmlTag.P:
				startNewParagraph();
				break;

			case HtmlTag.H1:
				closeControl(FBTextKind.H1);
				startNewParagraph();
				break;
				
			case HtmlTag.H2:
				closeControl(FBTextKind.H2);
				startNewParagraph();
				break;
				
			case HtmlTag.H3:
				closeControl(FBTextKind.H3);
				startNewParagraph();
				break;
				
			case HtmlTag.H4:
				closeControl(FBTextKind.H4);
				startNewParagraph();
				break;
				
			case HtmlTag.H5:
				closeControl(FBTextKind.H5);
				startNewParagraph();
				break;
				
			case HtmlTag.H6:
				closeControl(FBTextKind.H6);
				startNewParagraph();
				break;
				
			case HtmlTag.A:
				closeControl(myHyperlinkType);
				break;

			case HtmlTag.BODY:
				myReadMainText = false;
				break;

			case HtmlTag.HTML:
				//unsetCurrentTextModel();
				break;
				
			case HtmlTag.B:
				closeControl(FBTextKind.BOLD);
				break;
				
			case HtmlTag.S:
				closeControl(FBTextKind.STRIKETHROUGH);
				break;
				
			case HtmlTag.SUB:
				closeControl(FBTextKind.SUB);
				break;
				
			case HtmlTag.SUP:
				closeControl(FBTextKind.SUP);
				break;
				
			case HtmlTag.PRE:
				closeControl(FBTextKind.PREFORMATTED);
				startNewParagraph();
				break;
				
			case HtmlTag.EM:
				closeControl(FBTextKind.EMPHASIS);
				break;
				
			case HtmlTag.DFN:
				closeControl(FBTextKind.DEFINITION);
				break;
				
			case HtmlTag.CITE:
				closeControl(FBTextKind.CITE);
				break;
				
			case HtmlTag.CODE:
				closeControl(FBTextKind.CODE);
				break;
				
			case HtmlTag.STRONG:
				closeControl(FBTextKind.STRONG);
				break;
				
			case HtmlTag.I:
				closeControl(FBTextKind.ITALIC);
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
				//myParagraphsBeforeBodyNumber = Model.BookTextModel
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

			case HtmlTag.A:{
				String ref = attributes.getValue(myHrefAttribute);
				if ((ref != null) && (ref.length() != 0)) {
					if (ref.charAt(0) == '#') {
						myHyperlinkType = FBTextKind.FOOTNOTE;
						ref = ref.substring(1);
					} else {
						myHyperlinkType = FBTextKind.EXTERNAL_HYPERLINK;
					}
					addHyperlinkControl(myHyperlinkType, ref);
					myControls[myControlsNumber] = myHyperlinkType;
					myControlsNumber++;
					//openControl(myHyperlinkType);
					//System.out.println("open 37 - " + myControlsNumber);
				} else {
					//myHyperlinkType = FBTextKind.FOOTNOTE;
					//openControl(myHyperlinkType);
					//addControl(myHyperlinkType, true);
				}
				break;
			}
			
			case HtmlTag.IMG: {
				String ref = attributes.getValue(mySrcAttribute);
				if ((ref != null) && (ref.length() != 0)) {
					addImageReference(ref, (short)0);
					if (":\\".equals(ref.substring(1, 3))) {
						addImage(ref, new ZLFileImage("image/auto", ref));
					} else {
						String fileName = Model.Description.FileName;
						addImage(ref, new ZLFileImage("image/auto", 
								fileName.substring(0, fileName.lastIndexOf('\\') + 1) + ref));
					}
				}
				break;
			}
			
			case HtmlTag.B:
				openControl(FBTextKind.BOLD);
				break;
				
			case HtmlTag.S:
				openControl(FBTextKind.STRIKETHROUGH);
				break;
				
			case HtmlTag.SUB:
				openControl(FBTextKind.SUB);
				break;
				
			case HtmlTag.SUP:
				openControl(FBTextKind.SUP);
				break;
				
			case HtmlTag.PRE:
				openControl(FBTextKind.PREFORMATTED);
				break;
				
			case HtmlTag.STRONG:
				openControl(FBTextKind.STRONG);
				break;
				
			case HtmlTag.CODE:
				openControl(FBTextKind.CODE);
				break;
				
			case HtmlTag.EM:
				openControl(FBTextKind.EMPHASIS);
				break;
				
			case HtmlTag.CITE:
				openControl(FBTextKind.CITE);
				break;
				
			case HtmlTag.DFN:
				openControl(FBTextKind.DEFINITION);
				break;
				
			case HtmlTag.I:
				openControl(FBTextKind.ITALIC);
				break;
				
			case HtmlTag.H1:
				startNewParagraph();
				openControl(FBTextKind.H1);
				break;
				
			case HtmlTag.H2:
				startNewParagraph();
				openControl(FBTextKind.H2);
				break;
				
			case HtmlTag.H3:
				startNewParagraph();
				openControl(FBTextKind.H3);
				break;
				
			case HtmlTag.H4:
				startNewParagraph();
				openControl(FBTextKind.H4);
				break;
				
			case HtmlTag.H5:
				startNewParagraph();
				openControl(FBTextKind.H5);
				break;
				
			case HtmlTag.H6:
				startNewParagraph();
				openControl(FBTextKind.H6);
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
