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

package org.geometerplus.fbreader.formats.html;

import java.util.HashMap;
import java.io.*;
import java.nio.charset.*;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReader;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.zlibrary.core.html.*;
import org.geometerplus.zlibrary.core.util.ZLArrayUtils;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;

import org.geometerplus.zlibrary.core.xml.ZLXMLProcessor;
import org.geometerplus.fbreader.formats.xhtml.XHTMLReader;

public class HtmlReader extends BookReader implements ZLHtmlReader {
	private final byte[] myStyleTable = new byte[HtmlTag.TAG_NUMBER];
	{
		myStyleTable[HtmlTag.H1] = FBTextKind.H1;
		myStyleTable[HtmlTag.H2] = FBTextKind.H2;
		myStyleTable[HtmlTag.H3] = FBTextKind.H3;
		myStyleTable[HtmlTag.H4] = FBTextKind.H4;
		myStyleTable[HtmlTag.H5] = FBTextKind.H5;
		myStyleTable[HtmlTag.H6] = FBTextKind.H6;
		myStyleTable[HtmlTag.B] = FBTextKind.BOLD;
		myStyleTable[HtmlTag.SUB] = FBTextKind.SUB;
		myStyleTable[HtmlTag.SUP] = FBTextKind.SUP;
		myStyleTable[HtmlTag.S] = FBTextKind.STRIKETHROUGH;
		myStyleTable[HtmlTag.PRE] = FBTextKind.PREFORMATTED;
		myStyleTable[HtmlTag.EM] = FBTextKind.EMPHASIS;
		myStyleTable[HtmlTag.DFN] = FBTextKind.DEFINITION;
		myStyleTable[HtmlTag.CITE] = FBTextKind.CITE;
		myStyleTable[HtmlTag.CODE] = FBTextKind.CODE;
		myStyleTable[HtmlTag.STRONG] = FBTextKind.STRONG;
		myStyleTable[HtmlTag.I] = FBTextKind.ITALIC;
	}

	protected final CharsetDecoder myAttributeDecoder;

	private boolean myInsideTitle = false;
	private boolean mySectionStarted = false;
	private byte myHyperlinkType;
	private final char[] SPACE = { ' ' };
	private String myHrefAttribute = "href";
	private boolean myOrderedListIsStarted = false;
	//private boolean myUnorderedListIsStarted = false;
	private int myOLCounter = 0;
	private byte[] myControls = new byte[10];
	private byte myControlsNumber = 0;
	
	public HtmlReader(BookModel model) throws UnsupportedEncodingException {
		super(model);
		try {	
			//String encoding = model.Book.getEncoding();
			myAttributeDecoder = createDecoder();
			setByteDecoder(createDecoder());
		} catch (UnsupportedCharsetException e) {
			throw new UnsupportedEncodingException(e.getMessage());
		}
	}

	protected final CharsetDecoder createDecoder() throws UnsupportedEncodingException {
		return Charset.forName(Model.Book.getEncoding()).newDecoder()
			.onMalformedInput(CodingErrorAction.REPLACE)
			.onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	public boolean readBook() throws IOException {
		return ZLHtmlProcessor.read(this, getInputStream());
	}

	public InputStream getInputStream() throws IOException {
		return Model.Book.File.getInputStream();
	}

	public void startDocumentHandler() {
	}

	public void endDocumentHandler() {
		unsetCurrentTextModel();
	}

	public void byteDataHandler(byte[] data, int start, int length) {
		addByteData(data, start, length);
	}

	private HashMap<String,char[]> myEntityMap;
	public void entityDataHandler(String entity) {
		if (myEntityMap == null) {
			myEntityMap = new HashMap<String,char[]>(ZLXMLProcessor.getEntityMap(XHTMLReader.xhtmlDTDs()));
		}
		char[] data = myEntityMap.get(entity);
		if (data == null) {
			if ((entity.length() > 0) && (entity.charAt(0) == '#')) {
				try {
					int number;
					if (entity.charAt(1) == 'x') {
						number = Integer.parseInt(entity.substring(2), 16);
					} else {
						number = Integer.parseInt(entity.substring(1));
					}
					data = new char[] { (char)number };
				} catch (NumberFormatException e) {
				}
			}
			if (data == null) {
				data = new char[0];
			}
			myEntityMap.put(entity, data);
		}
		addData(data);
	}

	private void openControl(byte control) {
		addControl(control, true);
		if (myControlsNumber == myControls.length) {
			myControls = ZLArrayUtils.createCopy(myControls, myControlsNumber, 2 * myControlsNumber);
		}
		myControls[myControlsNumber++] = control;
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
	
	public final void endElementHandler(String tagName) {
		endElementHandler(HtmlTag.getTagByName(tagName));
	}

	public void endElementHandler(byte tag) {
		switch (tag) {
			case HtmlTag.SCRIPT:
			case HtmlTag.SELECT:
			case HtmlTag.STYLE:
			case HtmlTag.P:
				startNewParagraph();
				break;

			case HtmlTag.H1:
			case HtmlTag.H2:
			case HtmlTag.H3:
			case HtmlTag.H4:
			case HtmlTag.H5:
			case HtmlTag.H6:
			case HtmlTag.PRE:
				closeControl(myStyleTable[tag]);
				startNewParagraph();
				break;

			case HtmlTag.A:
				closeControl(myHyperlinkType);
				break;

			case HtmlTag.BODY:
				break;

			case HtmlTag.HTML:
				//unsetCurrentTextModel();
				break;
				
			case HtmlTag.B:
			case HtmlTag.S:
			case HtmlTag.SUB:
			case HtmlTag.SUP:
			case HtmlTag.EM:
			case HtmlTag.DFN:
			case HtmlTag.CITE:
			case HtmlTag.CODE:
			case HtmlTag.STRONG:
			case HtmlTag.I:
				closeControl(myStyleTable[tag]);
				break;

			case HtmlTag.OL:
				myOrderedListIsStarted = false;
				myOLCounter = 0;
				break;
				
			case HtmlTag.UL:
				//myUnorderedListIsStarted = false;
				break;
				
			default:
				break;
		}
	}

	public final void startElementHandler(String tagName, int offset, ZLHtmlAttributeMap attributes) {
		startElementHandler(HtmlTag.getTagByName(tagName), offset, attributes);
	}

	public void startElementHandler(byte tag, int offset, ZLHtmlAttributeMap attributes) {
		switch (tag) {
			case HtmlTag.HTML:
				break;

			case HtmlTag.BODY:
				setMainTextModel();
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
				String ref = attributes.getStringValue(myHrefAttribute, myAttributeDecoder);
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
				}
				break;
			}
			
			case HtmlTag.IMG: {
				/*
				String ref = attributes.getStringValue(mySrcAttribute, myAttributeDecoder);
				if ((ref != null) && (ref.length() != 0)) {
					addImageReference(ref, (short)0);
					String filePath = ref;
					if (!":\\".equals(ref.substring(1, 3))) {
						filePath = Model.Book.File.getPath();
						filePath = filePath.substring(0, filePath.lastIndexOf('\\') + 1) + ref;
					}
					addImage(ref, new ZLFileImage("image/auto", ZLFile.createFileByPath(filePath)));
				}
				*/
				break;
			}
			
			case HtmlTag.B:
			case HtmlTag.S:
			case HtmlTag.SUB:
			case HtmlTag.SUP:
			case HtmlTag.PRE:
			case HtmlTag.STRONG:
			case HtmlTag.CODE:
			case HtmlTag.EM:
			case HtmlTag.CITE:
			case HtmlTag.DFN:
			case HtmlTag.I:
				openControl(myStyleTable[tag]);
				break;
				
			case HtmlTag.H1:
			case HtmlTag.H2:
			case HtmlTag.H3:
			case HtmlTag.H4:
			case HtmlTag.H5:
			case HtmlTag.H6:
				startNewParagraph();
				openControl(myStyleTable[tag]);
				break;
				
			case HtmlTag.OL:
				myOrderedListIsStarted = true;
				break;
				
			case HtmlTag.UL:
				//myUnorderedListIsStarted = true;
				break;
				
			case HtmlTag.LI:
				startNewParagraph();
				if (myOrderedListIsStarted) {
					char[] number = (new Integer(++myOLCounter)).toString().toCharArray();
					addData(number);
					addData(new char[] {'.', ' '});
				} else {
					addData(new char[] {'*', ' '});
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
