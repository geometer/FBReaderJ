/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.formats.xhtml;

import java.util.*;
import java.io.IOException;

import org.geometerplus.zlibrary.core.xml.*;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLArchiveEntryFile;
import org.geometerplus.zlibrary.core.constants.XMLNamespaces;

import org.geometerplus.fbreader.bookmodel.*;
import org.geometerplus.fbreader.formats.util.MiscUtil;

public class XHTMLReader extends ZLXMLReaderAdapter {
	private static final HashMap<String,XHTMLTagAction> ourTagActions = new HashMap<String,XHTMLTagAction>();
	private static XHTMLTagAction ourNullAction = new XHTMLTagAction() {
		protected void doAtStart(XHTMLReader reader, ZLStringMap xmlattributes) {}
		protected void doAtEnd(XHTMLReader reader) {}
	};

	public static XHTMLTagAction addAction(String tag, XHTMLTagAction action) {
		XHTMLTagAction old = ourTagActions.get(tag);
		ourTagActions.put(tag, action);
		return old;
	}

	public static void fillTagTable() {
		if (!ourTagActions.isEmpty()) {
			return;
		}

		//addAction("html", new XHTMLTagAction());
		addAction("body", new XHTMLTagBodyAction());
		//addAction("title", new XHTMLTagAction());
		//addAction("meta", new XHTMLTagAction());
		//addAction("script", new XHTMLTagAction());

		//addAction("font", new XHTMLTagAction());
		//addAction("style", new XHTMLTagAction());

		addAction("p", new XHTMLTagParagraphAction());
		addAction("h1", new XHTMLTagParagraphWithControlAction(FBTextKind.H1));
		addAction("h2", new XHTMLTagParagraphWithControlAction(FBTextKind.H2));
		addAction("h3", new XHTMLTagParagraphWithControlAction(FBTextKind.H3));
		addAction("h4", new XHTMLTagParagraphWithControlAction(FBTextKind.H4));
		addAction("h5", new XHTMLTagParagraphWithControlAction(FBTextKind.H5));
		addAction("h6", new XHTMLTagParagraphWithControlAction(FBTextKind.H6));

		//addAction("ol", new XHTMLTagAction());
		//addAction("ul", new XHTMLTagAction());
		//addAction("dl", new XHTMLTagAction());
		addAction("li", new XHTMLTagItemAction());

		addAction("strong", new XHTMLTagControlAction(FBTextKind.STRONG));
		addAction("b", new XHTMLTagControlAction(FBTextKind.BOLD));
		addAction("em", new XHTMLTagControlAction(FBTextKind.EMPHASIS));
		addAction("i", new XHTMLTagControlAction(FBTextKind.ITALIC));
		final XHTMLTagAction codeControlAction = new XHTMLTagControlAction(FBTextKind.CODE);
		addAction("code", codeControlAction);
		addAction("tt", codeControlAction);
		addAction("kbd", codeControlAction);
		addAction("var", codeControlAction);
		addAction("samp", codeControlAction);
		addAction("cite", new XHTMLTagControlAction(FBTextKind.CITE));
		addAction("sub", new XHTMLTagControlAction(FBTextKind.SUB));
		addAction("sup", new XHTMLTagControlAction(FBTextKind.SUP));
		addAction("dd", new XHTMLTagControlAction(FBTextKind.DEFINITION_DESCRIPTION));
		addAction("dfn", new XHTMLTagControlAction(FBTextKind.DEFINITION));
		addAction("strike", new XHTMLTagControlAction(FBTextKind.STRIKETHROUGH));

		addAction("a", new XHTMLTagHyperlinkAction());

		addAction("img", new XHTMLTagImageAction(null, "src"));
		addAction("image", new XHTMLTagImageAction(XMLNamespaces.XLink, "href"));
		addAction("object", new XHTMLTagImageAction(null, "data"));

		//addAction("area", new XHTMLTagAction());
		//addAction("map", new XHTMLTagAction());

		//addAction("base", new XHTMLTagAction());
		//addAction("blockquote", new XHTMLTagAction());
		addAction("br", new XHTMLTagRestartParagraphAction());
		//addAction("center", new XHTMLTagAction());
		addAction("div", new XHTMLTagParagraphAction());
		addAction("dt", new XHTMLTagParagraphAction());
		//addAction("head", new XHTMLTagAction());
		//addAction("hr", new XHTMLTagAction());
		//addAction("link", new XHTMLTagAction());
		//addAction("param", new XHTMLTagAction());
		//addAction("q", new XHTMLTagAction());
		//addAction("s", new XHTMLTagAction());

		addAction("pre", new XHTMLTagPreAction());
		//addAction("big", new XHTMLTagAction());
		//addAction("small", new XHTMLTagAction());
		//addAction("u", new XHTMLTagAction());

		//addAction("table", new XHTMLTagAction());
		addAction("td", new XHTMLTagParagraphAction());
		addAction("th", new XHTMLTagParagraphAction());
		//addAction("tr", new XHTMLTagAction());
		//addAction("caption", new XHTMLTagAction());
		//addAction("span", new XHTMLTagAction());
	}

	private final BookReader myModelReader;
	String myPathPrefix;
	private String myLocalPathPrefix;
	String myReferencePrefix;
	boolean myPreformatted;
	boolean myInsideBody;
	private final Map<String,String> myFileNumbers;
	private final Map<String,String> myLocalFileNumbers = new HashMap<String,String>();

	public XHTMLReader(BookReader modelReader, Map<String,String> fileNumbers) {
		myModelReader = modelReader;
		myFileNumbers = fileNumbers;
	}

	final BookReader getModelReader() {
		return myModelReader;
	}

	final String getLocalFileAlias(String fileName) {
		String alias = myLocalFileNumbers.get(fileName);
		if (alias == null) {
			alias = getFileAlias(myLocalPathPrefix + fileName);
			myLocalFileNumbers.put(fileName, alias);
		}
		return alias;
	}

	public final String getFileAlias(String fileName) {
		String num = myFileNumbers.get(fileName);
		if (num == null) {
			fileName = MiscUtil.decodeHtmlReference(fileName);
			fileName = ZLArchiveEntryFile.normalizeEntryName(fileName);
			num = myFileNumbers.get(fileName);
		}
		if (num == null) {
			num = String.valueOf(myFileNumbers.size());
			myFileNumbers.put(fileName, num);
		}
		return num;
	}

	public void readFile(ZLFile file, String referencePrefix) throws IOException {
		fillTagTable();

		myReferencePrefix = referencePrefix;

		myPathPrefix = MiscUtil.htmlDirectoryPrefix(file);
		final String localPrefix = MiscUtil.archiveEntryName(myPathPrefix);
		if (!localPrefix.equals(myLocalPathPrefix)) {
			myLocalPathPrefix = localPrefix;
			myLocalFileNumbers.clear();
		}

		myPreformatted = false;
		myInsideBody = false;

		read(file);
	}

	private final HashMap<String,XHTMLTagAction> myActions = new HashMap<String,XHTMLTagAction>();
	private XHTMLTagAction getTagAction(String tag) {
		XHTMLTagAction action = myActions.get(tag);
		if (action == null) {
			action = ourTagActions.get(tag.toLowerCase());
			if (action == null) {
				action = ourNullAction;
			}
			myActions.put(tag, action);
		}
		return action == ourNullAction ? null : action;
	}

	@Override
	public boolean startElementHandler(String tag, ZLStringMap attributes) {
		String id = attributes.getValue("id");
		if (id != null) {
			myModelReader.addHyperlinkLabel(myReferencePrefix + id);
		}

		final XHTMLTagAction action = getTagAction(tag);
		if (action != null) {
			action.doAtStart(this, attributes);
		}
		return false;
	}

	@Override
	public boolean endElementHandler(String tag) {
		final XHTMLTagAction action = getTagAction(tag);
		if (action != null) {
			action.doAtEnd(this);
		}
		return false;
	}

	@Override
	public void characterDataHandler(char[] data, int start, int len) {
		if (myPreformatted) {
			final char first = data[start];
			if ((first == '\r') || (first == '\n')) {
				myModelReader.addControl(FBTextKind.CODE, false);
				myModelReader.endParagraph();
				myModelReader.beginParagraph();
				myModelReader.addControl(FBTextKind.CODE, true);
			}
			int spaceCounter = 0;
cycle:
			while (spaceCounter < len) {
				switch (data[start + spaceCounter]) {
					case 0x08:
					case 0x09:
					case 0x0A:
					case 0x0B:
					case 0x0C:
					case 0x0D:
					case ' ':
						break;
					default:
						break cycle;
				}
				++spaceCounter;
			}
			myModelReader.addFixedHSpace((short)spaceCounter);
			start += spaceCounter;
			len -= spaceCounter;
		}
		if (len > 0) {
			if (myInsideBody && !myModelReader.paragraphIsOpen()) {
				myModelReader.beginParagraph();
			}
			myModelReader.addData(data, start, len, false);
		}
	}

	private static ArrayList<String> ourExternalDTDs = new ArrayList<String>();

	public static List<String> xhtmlDTDs() {
		if (ourExternalDTDs.isEmpty()) {
			ourExternalDTDs.add("formats/xhtml/xhtml-lat1.ent");
			ourExternalDTDs.add("formats/xhtml/xhtml-special.ent");
			ourExternalDTDs.add("formats/xhtml/xhtml-symbol.ent");
		}
		return ourExternalDTDs;
	}

	@Override
	public List<String> externalDTDs() {
		return xhtmlDTDs();
	}

	@Override
	public boolean dontCacheAttributeValues() {
		return true;
	}

	@Override
	public boolean processNamespaces() {
		return true;
	}
}
