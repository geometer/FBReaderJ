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

package org.geometerplus.fbreader.format.xhtml;

import java.util.ArrayList;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.xml.ZLXMLReaderAdapter;

import org.geometerplus.fbreader.bookmodel.*;

public class XHTMLReader extends ZLXMLReaderAdapter {
	private final BookReader myModelReader;
	private String myPathPrefix;

	public XHTMLReader(BookReader modelReader) {
		myModelReader = modelReader;
	}

	BookReader getModelReader() {
		return myModelReader;
	}

	String getPathPrefix() {
		return myPathPrefix;
	}

/*
public:
	static XHTMLTagAction *addAction(const std::string &tag, XHTMLTagAction *action);
	static void fillTagTable();

private:
	static std::map<std::string,XHTMLTagAction*> ourTagActions;

public:
	bool readFile(const std::string &pathPrefix, const std::string &fileName, const std::string &referenceName);
	bool readFile(const std::string &pathPrefix, shared_ptr<ZLInputStream> stream, const std::string &referenceName);

	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);
	void characterDataHandler(const char *text, int len);

private:
	std::string myReferenceName;
	bool myPreformatted;
};

std::map<std::string,XHTMLTagAction*> XHTMLReader::ourTagActions;

XHTMLTagAction::~XHTMLTagAction() {
}

BookReader &XHTMLTagAction::bookReader(XHTMLReader &reader) {
	return reader.myModelReader;
}

const std::string &XHTMLTagAction::pathPrefix(XHTMLReader &reader) {
	return reader.myPathPrefix;
}

class XHTMLTagItemAction : public XHTMLTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagHyperlinkAction : public XHTMLTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	std::stack<FBTextKind> myHyperlinkStack;
};

class XHTMLTagControlAction : public XHTMLTagAction {

public:
	XHTMLTagControlAction(FBTextKind control);

	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	FBTextKind myControl;
};

class XHTMLTagParagraphWithControlAction : public XHTMLTagAction {

public:
	XHTMLTagParagraphWithControlAction(FBTextKind control);

	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	FBTextKind myControl;
};

class XHTMLTagPreAction : public XHTMLTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

void XHTMLTagItemAction::doAtStart(XHTMLReader &reader, const char**) {
	bookReader(reader).endParagraph();
	// TODO: increase left indent
	bookReader(reader).beginParagraph();
	// TODO: replace bullet sign by number inside OL tag
	const std::string bullet = "\xE2\x80\xA2\xC0\xA0";
	bookReader(reader).addData(bullet);
}

void XHTMLTagItemAction::doAtEnd(XHTMLReader &reader) {
	bookReader(reader).endParagraph();
}

XHTMLTagControlAction::XHTMLTagControlAction(FBTextKind control) : myControl(control) {
}

void XHTMLTagControlAction::doAtStart(XHTMLReader &reader, const char**) {
	bookReader(reader).pushKind(myControl);
	bookReader(reader).addControl(myControl, true);
}

void XHTMLTagControlAction::doAtEnd(XHTMLReader &reader) {
	bookReader(reader).addControl(myControl, false);
	bookReader(reader).popKind();
}

void XHTMLTagHyperlinkAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	const char *href = reader.attributeValue(xmlattributes, "href");
	if (href != 0) {
		const std::string link = (*href == '#') ? (reader.myReferenceName + href) : href;
		FBTextKind hyperlinkType = MiscUtil::isReference(link) ? EXTERNAL_HYPERLINK : INTERNAL_HYPERLINK;
		myHyperlinkStack.push(hyperlinkType);
		bookReader(reader).addHyperlinkControl(hyperlinkType, link);
	} else {
		myHyperlinkStack.push(REGULAR);
	}
	const char *name = reader.attributeValue(xmlattributes, "name");
	if (name != 0) {
		bookReader(reader).addHyperlinkLabel(reader.myReferenceName + "#" + name);
	}
}

void XHTMLTagHyperlinkAction::doAtEnd(XHTMLReader &reader) {
	FBTextKind kind = myHyperlinkStack.top();
	if (kind != REGULAR) {
		bookReader(reader).addControl(kind, false);
	}
	myHyperlinkStack.pop();
}

XHTMLTagParagraphWithControlAction::XHTMLTagParagraphWithControlAction(FBTextKind control) : myControl(control) {
}

void XHTMLTagParagraphWithControlAction::doAtStart(XHTMLReader &reader, const char**) {
	if ((myControl == TITLE) && (bookReader(reader).model().bookTextModel()->paragraphsNumber() > 1)) {
		bookReader(reader).insertEndOfSectionParagraph();
	}
	bookReader(reader).pushKind(myControl);
	bookReader(reader).beginParagraph();
}

void XHTMLTagParagraphWithControlAction::doAtEnd(XHTMLReader &reader) {
	bookReader(reader).endParagraph();
	bookReader(reader).popKind();
}

void XHTMLTagPreAction::doAtStart(XHTMLReader &reader, const char**) {
	reader.myPreformatted = true;
	bookReader(reader).beginParagraph();
	bookReader(reader).addControl(CODE, true);
}

void XHTMLTagPreAction::doAtEnd(XHTMLReader &reader) {
	bookReader(reader).addControl(CODE, false);
	bookReader(reader).endParagraph();
	reader.myPreformatted = false;
}

XHTMLTagAction *XHTMLReader::addAction(const std::string &tag, XHTMLTagAction *action) {
	XHTMLTagAction *old = ourTagActions[tag];
	ourTagActions[tag] = action;
	return old;
}

void XHTMLReader::fillTagTable() {
	if (ourTagActions.empty()) {
		//addAction("html",	new XHTMLTagAction());
		addAction("body",	new XHTMLTagParagraphAction());
		//addAction("title",	new XHTMLTagAction());
		//addAction("meta",	new XHTMLTagAction());
		//addAction("script",	new XHTMLTagAction());

		//addAction("font",	new XHTMLTagAction());
		//addAction("style",	new XHTMLTagAction());

		addAction("p",	new XHTMLTagParagraphAction());
		addAction("h1",	new XHTMLTagParagraphWithControlAction(H1));
		addAction("h2",	new XHTMLTagParagraphWithControlAction(H2));
		addAction("h3",	new XHTMLTagParagraphWithControlAction(H3));
		addAction("h4",	new XHTMLTagParagraphWithControlAction(H4));
		addAction("h5",	new XHTMLTagParagraphWithControlAction(H5));
		addAction("h6",	new XHTMLTagParagraphWithControlAction(H6));

		//addAction("ol",	new XHTMLTagAction());
		//addAction("ul",	new XHTMLTagAction());
		//addAction("dl",	new XHTMLTagAction());
		addAction("li",	new XHTMLTagItemAction());

		addAction("strong",	new XHTMLTagControlAction(STRONG));
		addAction("b",	new XHTMLTagControlAction(BOLD));
		addAction("em",	new XHTMLTagControlAction(EMPHASIS));
		addAction("i",	new XHTMLTagControlAction(ITALIC));
		addAction("code",	new XHTMLTagControlAction(CODE));
		addAction("tt",	new XHTMLTagControlAction(CODE));
		addAction("kbd",	new XHTMLTagControlAction(CODE));
		addAction("var",	new XHTMLTagControlAction(CODE));
		addAction("samp",	new XHTMLTagControlAction(CODE));
		addAction("cite",	new XHTMLTagControlAction(CITE));
		addAction("sub",	new XHTMLTagControlAction(SUB));
		addAction("sup",	new XHTMLTagControlAction(SUP));
		addAction("dd",	new XHTMLTagControlAction(DEFINITION_DESCRIPTION));
		addAction("dfn",	new XHTMLTagControlAction(DEFINITION));
		addAction("strike",	new XHTMLTagControlAction(STRIKETHROUGH));

		addAction("a",	new XHTMLTagHyperlinkAction());

		addAction("img",	new XHTMLTagImageAction("src"));
		addAction("object",	new XHTMLTagImageAction("data"));

		//addAction("area",	new XHTMLTagAction());
		//addAction("map",	new XHTMLTagAction());

		//addAction("base",	new XHTMLTagAction());
		//addAction("blockquote",	new XHTMLTagAction());
		addAction("br",	new XHTMLTagRestartParagraphAction());
		//addAction("center",	new XHTMLTagAction());
		addAction("div",	new XHTMLTagParagraphAction());
		//addAction("dt",	new XHTMLTagAction());
		//addAction("head",	new XHTMLTagAction());
		//addAction("hr",	new XHTMLTagAction());
		//addAction("link",	new XHTMLTagAction());
		//addAction("param",	new XHTMLTagAction());
		//addAction("q",	new XHTMLTagAction());
		//addAction("s",	new XHTMLTagAction());

		addAction("pre",	new XHTMLTagPreAction());
		//addAction("big",	new XHTMLTagAction());
		//addAction("small",	new XHTMLTagAction());
		//addAction("u",	new XHTMLTagAction());

		//addAction("table",	new XHTMLTagAction());
		addAction("td",	new XHTMLTagParagraphAction());
		addAction("th",	new XHTMLTagParagraphAction());
		//addAction("tr",	new XHTMLTagAction());
		//addAction("caption",	new XHTMLTagAction());
		//addAction("span",	new XHTMLTagAction());
	}
}

bool XHTMLReader::readFile(const std::string &pathPrefix, const std::string &fileName, const std::string &referenceName) {
	myModelReader.addHyperlinkLabel(referenceName);

	fillTagTable();

	myPathPrefix = pathPrefix;
	myReferenceName = referenceName;

	myPreformatted = false;

	return readDocument(pathPrefix + fileName);
}

bool XHTMLReader::readFile(const std::string &pathPrefix, shared_ptr<ZLInputStream> stream, const std::string &referenceName) {
	myModelReader.addHyperlinkLabel(referenceName);

	fillTagTable();

	myPathPrefix = pathPrefix;
	myReferenceName = referenceName;

	myPreformatted = false;

	return readDocument(stream);
}

void XHTMLReader::startElementHandler(const char *tag, const char **attributes) {
	static const std::string HASH = "#";
	const char *id = attributeValue(attributes, "id");
	if (id != 0) {
		myModelReader.addHyperlinkLabel(myReferenceName + HASH + id);
	}

	XHTMLTagAction *action = ourTagActions[ZLUnicodeUtil::toLower(tag)];
	if (action != 0) {
		action->doAtStart(*this, attributes);
	}
}

void XHTMLReader::endElementHandler(const char *tag) {
	XHTMLTagAction *action = ourTagActions[ZLUnicodeUtil::toLower(tag)];
	if (action != 0) {
		action->doAtEnd(*this);
	}
}

void XHTMLReader::characterDataHandler(const char *text, int len) {
	if (myPreformatted) {
		if ((*text == '\r') || (*text == '\n')) {
			myModelReader.addControl(CODE, false);
			myModelReader.endParagraph();
			myModelReader.beginParagraph();
			myModelReader.addControl(CODE, true);
		}
		int spaceCounter = 0;
		while ((spaceCounter < len) && isspace((unsigned char)*text)) {
			++spaceCounter;
		}
		myModelReader.addFixedHSpace(spaceCounter);
		text += spaceCounter;
		len -= spaceCounter;
	}
	if (len > 0) {
		myModelReader.addData(std::string(text, len));
	}
}
*/

	private static ArrayList ourExternalDTDs = new ArrayList();

	public ArrayList externalDTDs() {
		if (ourExternalDTDs.isEmpty()) {
			ourExternalDTDs.add(ZLibrary.JAR_DATA_PREFIX + "data/formats/xhtml/xhtml-lat1.ent");
			ourExternalDTDs.add(ZLibrary.JAR_DATA_PREFIX + "data/formats/xhtml/xhtml-special.ent");
			ourExternalDTDs.add(ZLibrary.JAR_DATA_PREFIX + "data/formats/xhtml/xhtml-symbol.ent");
		}
		return ourExternalDTDs;
	}
}
