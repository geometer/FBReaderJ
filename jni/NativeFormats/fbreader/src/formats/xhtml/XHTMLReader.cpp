/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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

#include <cstring>
#include <cctype>

#include <ZLFile.h>
#include <ZLFileUtil.h>
#include <ZLFileImage.h>
#include <ZLUnicodeUtil.h>
#include <ZLStringUtil.h>
#include <ZLXMLNamespace.h>
#include <ZLLogger.h>

#include "XHTMLReader.h"
#include "../util/EntityFilesCollector.h"
#include "../util/MiscUtil.h"
#include "../css/StyleSheetParser.h"

#include "../../bookmodel/BookReader.h"
#include "../../bookmodel/BookModel.h"

std::map<std::string,XHTMLTagAction*> XHTMLReader::ourTagActions;

XHTMLTagAction::~XHTMLTagAction() {
}

BookReader &XHTMLTagAction::bookReader(XHTMLReader &reader) {
	return reader.myModelReader;
}

const std::string &XHTMLTagAction::pathPrefix(XHTMLReader &reader) {
	return reader.myPathPrefix;
}

void XHTMLTagAction::beginParagraph(XHTMLReader &reader) {
	reader.beginParagraph();
}

void XHTMLTagAction::endParagraph(XHTMLReader &reader) {
	reader.endParagraph();
}

class XHTMLTagStyleAction : public XHTMLTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagLinkAction : public XHTMLTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagParagraphAction : public XHTMLTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagBodyAction : public XHTMLTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagRestartParagraphAction : public XHTMLTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagImageAction : public XHTMLTagAction {

public:
	XHTMLTagImageAction(shared_ptr<ZLXMLReader::AttributeNamePredicate> predicate);
	XHTMLTagImageAction(const std::string &attributeName);

	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	shared_ptr<ZLXMLReader::AttributeNamePredicate> myPredicate;
};

class XHTMLSvgImageAttributeNamePredicate : public ZLXMLReader::NamespaceAttributeNamePredicate {

public:
	XHTMLSvgImageAttributeNamePredicate();
	bool accepts(const ZLXMLReader &reader, const char *name) const;

private:
	bool myIsEnabled;

friend class XHTMLTagSvgAction;
};

class XHTMLTagSvgAction : public XHTMLTagAction {

public:
	XHTMLTagSvgAction(XHTMLSvgImageAttributeNamePredicate &predicate);
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	XHTMLSvgImageAttributeNamePredicate &myPredicate;
};

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

void XHTMLTagStyleAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	static const std::string TYPE = "text/css";

	const char *type = reader.attributeValue(xmlattributes, "type");
	if ((type == 0) || (TYPE != type)) {
		return;
	}

	if (reader.myReadState == XHTMLReader::READ_NOTHING) {
		reader.myReadState = XHTMLReader::READ_STYLE;
		reader.myTableParser = new StyleSheetTableParser(reader.myStyleSheetTable);
		ZLLogger::Instance().println("CSS", "parsing style tag content");
	}
}

void XHTMLTagStyleAction::doAtEnd(XHTMLReader &reader) {
	if (reader.myReadState == XHTMLReader::READ_STYLE) {
		reader.myReadState = XHTMLReader::READ_NOTHING;
		reader.myTableParser.reset();
	}
}

void XHTMLTagLinkAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	static const std::string REL = "stylesheet";
	const char *rel = reader.attributeValue(xmlattributes, "rel");
	if ((rel == 0) || (REL != rel)) {
		return;
	}
	static const std::string TYPE = "text/css";

	const char *type = reader.attributeValue(xmlattributes, "type");
	if ((type == 0) || (TYPE != type)) {
		return;
	}

	const char *href = reader.attributeValue(xmlattributes, "href");
	if (href == 0) {
		return;
	}

	ZLLogger::Instance().println("CSS", "style file: " + reader.myPathPrefix + MiscUtil::decodeHtmlURL(href));
	shared_ptr<ZLInputStream> cssStream = ZLFile(reader.myPathPrefix + MiscUtil::decodeHtmlURL(href)).inputStream();
	if (cssStream.isNull()) {
		return;
	}
	ZLLogger::Instance().println("CSS", "parsing file");
	StyleSheetTableParser parser(reader.myStyleSheetTable);
	parser.parse(*cssStream);
	//reader.myStyleSheetTable.dump();
}

void XHTMLTagLinkAction::doAtEnd(XHTMLReader&) {
}

void XHTMLTagParagraphAction::doAtStart(XHTMLReader &reader, const char**) {
	if (!reader.myNewParagraphInProgress) {
		beginParagraph(reader);
		reader.myNewParagraphInProgress = true;
	}
}

void XHTMLTagParagraphAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
}

void XHTMLTagBodyAction::doAtStart(XHTMLReader &reader, const char**) {
	reader.myReadState = XHTMLReader::READ_BODY;
}

void XHTMLTagBodyAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
	reader.myReadState = XHTMLReader::READ_NOTHING;
}

void XHTMLTagRestartParagraphAction::doAtStart(XHTMLReader &reader, const char**) {
	if (reader.myCurrentParagraphIsEmpty) {
		bookReader(reader).addData(" ");
	}
	endParagraph(reader);
	beginParagraph(reader);
}

void XHTMLTagRestartParagraphAction::doAtEnd(XHTMLReader&) {
}

void XHTMLTagItemAction::doAtStart(XHTMLReader &reader, const char**) {
	endParagraph(reader);
	// TODO: increase left indent
	beginParagraph(reader);
	// TODO: replace bullet sign by number inside OL tag
	const std::string bullet = "\xE2\x80\xA2\xC0\xA0";
	bookReader(reader).addData(bullet);
}

void XHTMLTagItemAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
}

XHTMLTagImageAction::XHTMLTagImageAction(shared_ptr<ZLXMLReader::AttributeNamePredicate> predicate) {
	myPredicate = predicate;
}

XHTMLTagImageAction::XHTMLTagImageAction(const std::string &attributeName) {
	myPredicate = new ZLXMLReader::FixedAttributeNamePredicate(attributeName);
}

void XHTMLTagImageAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	const char *fileName = reader.attributeValue(xmlattributes, *myPredicate);
	if (fileName == 0) {
		return;
	}

	const std::string fullfileName = pathPrefix(reader) + MiscUtil::decodeHtmlURL(fileName);
	ZLFile imageFile(fullfileName);
	if (!imageFile.exists()) {
		return;
	}

	bool flag = bookReader(reader).paragraphIsOpen();
	if (flag) {
		endParagraph(reader);
	}
	const std::string imageName = imageFile.name(false);
	bookReader(reader).addImageReference(imageName, 0, false);
	bookReader(reader).addImage(imageName, new ZLFileImage(imageFile, "", 0));
	if (flag) {
		beginParagraph(reader);
	}
}

XHTMLTagSvgAction::XHTMLTagSvgAction(XHTMLSvgImageAttributeNamePredicate &predicate) : myPredicate(predicate) {
}

void XHTMLTagSvgAction::doAtStart(XHTMLReader&, const char**) {
	myPredicate.myIsEnabled = true;
}

void XHTMLTagSvgAction::doAtEnd(XHTMLReader&) {
	myPredicate.myIsEnabled = false;
}

XHTMLSvgImageAttributeNamePredicate::XHTMLSvgImageAttributeNamePredicate() : ZLXMLReader::NamespaceAttributeNamePredicate(ZLXMLNamespace::XLink, "href"), myIsEnabled(false) {
}

bool XHTMLSvgImageAttributeNamePredicate::accepts(const ZLXMLReader &reader, const char *name) const {
	return myIsEnabled && NamespaceAttributeNamePredicate::accepts(reader, name);
}

void XHTMLTagImageAction::doAtEnd(XHTMLReader&) {
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
	if (href != 0 && href[0] != '\0') {
		const FBTextKind hyperlinkType = MiscUtil::referenceType(href);
		std::string link = MiscUtil::decodeHtmlURL(href);
		if (hyperlinkType == INTERNAL_HYPERLINK) {
			if (link[0] == '#') {
				link = reader.myReferenceAlias + link;
			} else {
				link = reader.normalizedReference(reader.myReferenceDirName + link);
			}
		}
		myHyperlinkStack.push(hyperlinkType);
		bookReader(reader).addHyperlinkControl(hyperlinkType, link);
	} else {
		myHyperlinkStack.push(REGULAR);
	}
	const char *name = reader.attributeValue(xmlattributes, "name");
	if (name != 0) {
		bookReader(reader).addHyperlinkLabel(
			reader.myReferenceAlias + "#" + MiscUtil::decodeHtmlURL(name)
		);
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
	beginParagraph(reader);
}

void XHTMLTagParagraphWithControlAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
	bookReader(reader).popKind();
}

void XHTMLTagPreAction::doAtStart(XHTMLReader &reader, const char**) {
	reader.myPreformatted = true;
	beginParagraph(reader);
	bookReader(reader).addControl(CODE, true);
}

void XHTMLTagPreAction::doAtEnd(XHTMLReader &reader) {
	bookReader(reader).addControl(CODE, false);
	endParagraph(reader);
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
		addAction("body",	new XHTMLTagBodyAction());
		//addAction("title",	new XHTMLTagAction());
		//addAction("meta",	new XHTMLTagAction());
		//addAction("script",	new XHTMLTagAction());

		//addAction("font",	new XHTMLTagAction());
		addAction("style",	new XHTMLTagStyleAction());

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
		XHTMLSvgImageAttributeNamePredicate *predicate = new XHTMLSvgImageAttributeNamePredicate();
		addAction("image",	new XHTMLTagImageAction(predicate));
		addAction("svg",	new XHTMLTagSvgAction(*predicate));

		//addAction("area",	new XHTMLTagAction());
		//addAction("map",	new XHTMLTagAction());

		//addAction("base",	new XHTMLTagAction());
		//addAction("blockquote",	new XHTMLTagAction());
		addAction("br",	new XHTMLTagRestartParagraphAction());
		//addAction("center",	new XHTMLTagAction());
		addAction("div", new XHTMLTagParagraphAction());
		addAction("dt", new XHTMLTagParagraphAction());
		//addAction("head",	new XHTMLTagAction());
		//addAction("hr",	new XHTMLTagAction());
		addAction("link",	new XHTMLTagLinkAction());
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

XHTMLReader::XHTMLReader(BookReader &modelReader) : myModelReader(modelReader) {
}

bool XHTMLReader::readFile(const ZLFile &file, const std::string &referenceName) {
	fillTagTable();

	myPathPrefix = MiscUtil::htmlDirectoryPrefix(file.path());
	myReferenceAlias = fileAlias(referenceName);
	myModelReader.addHyperlinkLabel(myReferenceAlias);

	const int index = referenceName.rfind('/', referenceName.length() - 1);
	myReferenceDirName = referenceName.substr(0, index + 1);

	myPreformatted = false;
	myNewParagraphInProgress = false;
	myReadState = READ_NOTHING;
	myCurrentParagraphIsEmpty = true;

	myStyleSheetTable.clear();
	myCSSStack.clear();
	myStyleEntryStack.clear();
	myStylesToRemove = 0;

	myDoPageBreakAfterStack.clear();
	myStyleParser = new StyleSheetSingleStyleParser();
	myTableParser.reset();

	return readDocument(file);
}

bool XHTMLReader::addStyleEntry(const std::string tag, const std::string aClass) {
	shared_ptr<ZLTextStyleEntry> entry = myStyleSheetTable.control(tag, aClass);
	if (!entry.isNull()) {
		myModelReader.addStyleEntry(*entry);
		myStyleEntryStack.push_back(entry);
		return true;
	}
	return false;
}

void XHTMLReader::startElementHandler(const char *tag, const char **attributes) {
	static const std::string HASH = "#";
	const char *id = attributeValue(attributes, "id");
	if (id != 0) {
		myModelReader.addHyperlinkLabel(myReferenceAlias + HASH + id);
	}

	const std::string sTag = ZLUnicodeUtil::toLower(tag);

	const char *aClass = attributeValue(attributes, "class");
	const std::string sClass = (aClass != 0) ? aClass : "";

	if (myStyleSheetTable.doBreakBefore(sTag, sClass)) {
		myModelReader.insertEndOfSectionParagraph();
	}
	myDoPageBreakAfterStack.push_back(myStyleSheetTable.doBreakAfter(sTag, sClass));

	XHTMLTagAction *action = ourTagActions[sTag];
	if (action != 0) {
		action->doAtStart(*this, attributes);
	}

	const int sizeBefore = myStyleEntryStack.size();
	addStyleEntry(sTag, "");
	addStyleEntry("", sClass);
	addStyleEntry(sTag, sClass);
	const char *style = attributeValue(attributes, "style");
	if (style != 0) {
		ZLLogger::Instance().println("CSS", std::string("parsing style attribute: ") + style);
		shared_ptr<ZLTextStyleEntry> entry = myStyleParser->parseString(style);
		myModelReader.addStyleEntry(*entry);
		myStyleEntryStack.push_back(entry);
	} else {
	}
	myCSSStack.push_back(myStyleEntryStack.size() - sizeBefore);
}

void XHTMLReader::endElementHandler(const char *tag) {
	for (int i = myCSSStack.back(); i > 0; --i) {
		myModelReader.addStyleCloseEntry();
	}
	myStylesToRemove = myCSSStack.back();
	myCSSStack.pop_back();

	XHTMLTagAction *action = ourTagActions[ZLUnicodeUtil::toLower(tag)];
	if (action != 0) {
		action->doAtEnd(*this);
		myNewParagraphInProgress = false;
	}

	for (; myStylesToRemove > 0; --myStylesToRemove) {
		myStyleEntryStack.pop_back();
	}

	if (myDoPageBreakAfterStack.back()) {
		myModelReader.insertEndOfSectionParagraph();
	}
	myDoPageBreakAfterStack.pop_back();
}

void XHTMLReader::beginParagraph() {
	myCurrentParagraphIsEmpty = true;
	myModelReader.beginParagraph();
	bool doBlockSpaceBefore = false;
	for (std::vector<shared_ptr<ZLTextStyleEntry> >::const_iterator it = myStyleEntryStack.begin(); it != myStyleEntryStack.end(); ++it) {
		myModelReader.addStyleEntry(**it);
		doBlockSpaceBefore =
			doBlockSpaceBefore ||
			(*it)->isFeatureSupported(ZLTextStyleEntry::LENGTH_SPACE_BEFORE);
	}

	if (doBlockSpaceBefore) {
		ZLTextStyleEntry blockingEntry;
		blockingEntry.setLength(
			ZLTextStyleEntry::LENGTH_SPACE_BEFORE,
			0,
			ZLTextStyleEntry::SIZE_UNIT_PIXEL
		);
		myModelReader.addStyleEntry(blockingEntry);
	}
}

void XHTMLReader::endParagraph() {
	bool doBlockSpaceAfter = false;
	for (std::vector<shared_ptr<ZLTextStyleEntry> >::const_iterator it = myStyleEntryStack.begin(); it != myStyleEntryStack.end() - myStylesToRemove; ++it) {
		doBlockSpaceAfter =
			doBlockSpaceAfter ||
			(*it)->isFeatureSupported(ZLTextStyleEntry::LENGTH_SPACE_AFTER);
	}
	if (doBlockSpaceAfter) {
		ZLTextStyleEntry blockingEntry;
		blockingEntry.setLength(
			ZLTextStyleEntry::LENGTH_SPACE_AFTER,
			0,
			ZLTextStyleEntry::SIZE_UNIT_PIXEL
		);
		myModelReader.addStyleEntry(blockingEntry);
	}
	for (; myStylesToRemove > 0; --myStylesToRemove) {
		myModelReader.addStyleEntry(*myStyleEntryStack.back());
		myStyleEntryStack.pop_back();
	}
	myModelReader.endParagraph();
}

void XHTMLReader::characterDataHandler(const char *text, size_t len) {
	switch (myReadState) {
		case READ_NOTHING:
			break;
		case READ_STYLE:
			if (!myTableParser.isNull()) {
				myTableParser->parse(text, len);
			}
			break;
		case READ_BODY:
			if (myPreformatted) {
				if ((*text == '\r') || (*text == '\n')) {
					myModelReader.addControl(CODE, false);
					endParagraph();
					beginParagraph();
					myModelReader.addControl(CODE, true);
				}
				size_t spaceCounter = 0;
				while ((spaceCounter < len) && isspace((unsigned char)*(text + spaceCounter))) {
					++spaceCounter;
				}
				myModelReader.addFixedHSpace(spaceCounter);
				text += spaceCounter;
				len -= spaceCounter;
			} else if ((myNewParagraphInProgress) || !myModelReader.paragraphIsOpen()) {
				while (isspace((unsigned char)*text)) {
					++text;
					if (--len == 0) {
						break;
					}
				}
			}
			if (len > 0) {
				myCurrentParagraphIsEmpty = false;
				if (!myModelReader.paragraphIsOpen()) {
					myModelReader.beginParagraph();
				}
				myModelReader.addData(std::string(text, len));
				myNewParagraphInProgress = false;
			}
			break;
	}
}

const std::vector<std::string> &XHTMLReader::externalDTDs() const {
	return EntityFilesCollector::Instance().externalDTDs("xhtml");
}

bool XHTMLReader::processNamespaces() const {
	return true;
}

const std::string XHTMLReader::normalizedReference(const std::string &reference) const {
	const size_t index = reference.find('#');
	if (index == std::string::npos) {
		return fileAlias(reference);
	} else {
		return fileAlias(reference.substr(0, index)) + reference.substr(index);
	}
}

const std::string &XHTMLReader::fileAlias(const std::string &fileName) const {
	std::map<std::string,std::string>::const_iterator it = myFileNumbers.find(fileName);
	if (it != myFileNumbers.end()) {
		return it->second;
	}

	const std::string correctedFileName =
		ZLFileUtil::normalizeUnixPath(MiscUtil::decodeHtmlURL(fileName));
	it = myFileNumbers.find(correctedFileName);
	if (it != myFileNumbers.end()) {
		return it->second;
	}

	std::string num;
	ZLStringUtil::appendNumber(num, myFileNumbers.size());
	myFileNumbers.insert(std::make_pair(correctedFileName, num));
	it = myFileNumbers.find(correctedFileName);
	return it->second;
}
