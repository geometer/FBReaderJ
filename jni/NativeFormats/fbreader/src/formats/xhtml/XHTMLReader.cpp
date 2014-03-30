/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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
#include <ZLInputStream.h>
#include <ZLLogger.h>
#include <FileEncryptionInfo.h>

#include "XHTMLReader.h"
#include "../util/EntityFilesCollector.h"
#include "../util/MiscUtil.h"
#include "../css/StyleSheetParser.h"

#include "../../bookmodel/BookReader.h"
#include "../../bookmodel/BookModel.h"

std::map<std::string,XHTMLTagAction*> XHTMLReader::ourTagActions;
std::map<shared_ptr<XHTMLReader::FullNamePredicate>,XHTMLTagAction*> XHTMLReader::ourNsTagActions;

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

class XHTMLGlobalTagAction : public XHTMLTagAction {

private:
	bool isEnabled(XHTMLReadingState state);
};

class XHTMLTextModeTagAction : public XHTMLTagAction {

private:
	bool isEnabled(XHTMLReadingState state);
};

bool XHTMLGlobalTagAction::isEnabled(XHTMLReadingState state) {
	return true;
}

bool XHTMLTextModeTagAction::isEnabled(XHTMLReadingState state) {
	return state == XHTML_READ_BODY;
}

class XHTMLTagStyleAction : public XHTMLGlobalTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagLinkAction : public XHTMLGlobalTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagParagraphAction : public XHTMLTextModeTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagBodyAction : public XHTMLGlobalTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagRestartParagraphAction : public XHTMLTextModeTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagVideoAction : public XHTMLTagAction {

private:
	bool isEnabled(XHTMLReadingState state);

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagSourceAction : public XHTMLTagAction {

private:
	bool isEnabled(XHTMLReadingState state);

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagImageAction : public XHTMLTextModeTagAction {

public:
	XHTMLTagImageAction(shared_ptr<ZLXMLReader::NamePredicate> predicate);
	XHTMLTagImageAction(const std::string &attributeName);

	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	shared_ptr<ZLXMLReader::NamePredicate> myPredicate;
};

class XHTMLSvgImageNamePredicate : public ZLXMLReader::FullNamePredicate {

public:
	XHTMLSvgImageNamePredicate();
	bool accepts(const ZLXMLReader &reader, const char *name) const;

private:
	bool myIsEnabled;

friend class XHTMLTagSvgAction;
};

class XHTMLTagSvgAction : public XHTMLTextModeTagAction {

public:
	XHTMLTagSvgAction(XHTMLSvgImageNamePredicate &predicate);
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	XHTMLSvgImageNamePredicate &myPredicate;
};

class XHTMLTagItemAction : public XHTMLTextModeTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagHyperlinkAction : public XHTMLTextModeTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	std::stack<FBTextKind> myHyperlinkStack;
};

class XHTMLTagControlAction : public XHTMLTextModeTagAction {

public:
	XHTMLTagControlAction(FBTextKind control);

	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	FBTextKind myControl;
};

class XHTMLTagParagraphWithControlAction : public XHTMLTextModeTagAction {

public:
	XHTMLTagParagraphWithControlAction(FBTextKind control);

	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);

private:
	FBTextKind myControl;
};

class XHTMLTagPreAction : public XHTMLTextModeTagAction {

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

	if (reader.myReadState == XHTML_READ_NOTHING) {
		reader.myReadState = XHTML_READ_STYLE;
		reader.myTableParser = new StyleSheetTableParser(reader.myPathPrefix, reader.myStyleSheetTable, *reader.myFontMap);
		ZLLogger::Instance().println("CSS", "parsing style tag content");
	}
}

void XHTMLTagStyleAction::doAtEnd(XHTMLReader &reader) {
	if (reader.myReadState == XHTML_READ_STYLE) {
		reader.myReadState = XHTML_READ_NOTHING;
		reader.myTableParser.reset();
	}
}

void XHTMLTagLinkAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	static const std::string REL = "stylesheet";
	const char *rel = reader.attributeValue(xmlattributes, "rel");
	if (rel == 0 || REL != rel) {
		return;
	}
	static const std::string TYPE = "text/css";

	const char *type = reader.attributeValue(xmlattributes, "type");
	if (type == 0 || TYPE != type) {
		return;
	}

	const char *href = reader.attributeValue(xmlattributes, "href");
	if (href == 0) {
		return;
	}

	std::string cssFilePath = reader.myPathPrefix + MiscUtil::decodeHtmlURL(href);
	//ZLLogger::Instance().registerClass("CSS");
	ZLLogger::Instance().println("CSS", "style file: " + cssFilePath);
	const ZLFile cssFile(cssFilePath);
	cssFilePath = cssFile.path();
	shared_ptr<StyleSheetParserWithCache> parser = reader.myFileParsers[cssFilePath];
	if (parser.isNull()) {
		parser = new StyleSheetParserWithCache(
			cssFile,
			MiscUtil::htmlDirectoryPrefix(cssFilePath),
			*reader.myFontMap,
			reader.myEncryptionMap
		);
		reader.myFileParsers[cssFilePath] = parser;
		ZLLogger::Instance().println("CSS", "creating stream");
		shared_ptr<ZLInputStream> cssStream = cssFile.inputStream(reader.myEncryptionMap);
		if (!cssStream.isNull()) {
			ZLLogger::Instance().println("CSS", "parsing file");
			parser->parseStream(cssStream);
		}
	}
	parser->applyToTable(reader.myStyleSheetTable);
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
	++reader.myBodyCounter;
	if (reader.myBodyCounter > 0) {
		reader.myReadState = XHTML_READ_BODY;
	}
}

void XHTMLTagBodyAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
	--reader.myBodyCounter;
	if (reader.myBodyCounter <= 0) {
		reader.myReadState = XHTML_READ_NOTHING;
	}
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

bool XHTMLTagVideoAction::isEnabled(XHTMLReadingState state) {
	return state == XHTML_READ_BODY || state == XHTML_READ_VIDEO;
}

void XHTMLTagVideoAction::doAtStart(XHTMLReader &reader, const char**) {
	if (reader.myReadState == XHTML_READ_BODY) {
		reader.myReadState = XHTML_READ_VIDEO;
		reader.myVideoEntry = new ZLVideoEntry();
	}
}

void XHTMLTagVideoAction::doAtEnd(XHTMLReader &reader) {
	if (reader.myReadState == XHTML_READ_VIDEO) {
		bookReader(reader).addVideoEntry(*reader.myVideoEntry);
		reader.myVideoEntry.reset();
		reader.myReadState = XHTML_READ_BODY;
	}
}

bool XHTMLTagSourceAction::isEnabled(XHTMLReadingState state) {
	return state == XHTML_READ_VIDEO;
}

void XHTMLTagSourceAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	const char *mime = reader.attributeValue(xmlattributes, "type");
	const char *href = reader.attributeValue(xmlattributes, "src");
	if (mime != 0 && href != 0) {
		reader.myVideoEntry->addSource(
			mime,
			ZLFile(pathPrefix(reader) + MiscUtil::decodeHtmlURL(href)).path()
		);
	}
}

void XHTMLTagSourceAction::doAtEnd(XHTMLReader &reader) {
}

XHTMLTagImageAction::XHTMLTagImageAction(shared_ptr<ZLXMLReader::NamePredicate> predicate) {
	myPredicate = predicate;
}

XHTMLTagImageAction::XHTMLTagImageAction(const std::string &attributeName) {
	myPredicate = new ZLXMLReader::SimpleNamePredicate(attributeName);
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

	const bool flagParagraphIsOpen = bookReader(reader).paragraphIsOpen();
	if (flagParagraphIsOpen) {
		if (reader.myCurrentParagraphIsEmpty) {
			bookReader(reader).addControl(IMAGE, true);
		} else {
			endParagraph(reader);
		}
	}
	const std::string imageName = imageFile.name(false);
	bookReader(reader).addImageReference(imageName, 0, reader.myMarkNextImageAsCover);
	bookReader(reader).addImage(imageName, new ZLFileImage(imageFile, "", 0, 0, reader.myEncryptionMap->info(imageFile.path())));
	reader.myMarkNextImageAsCover = false;
	if (flagParagraphIsOpen && reader.myCurrentParagraphIsEmpty) {
		bookReader(reader).addControl(IMAGE, false);
		endParagraph(reader);
	}
}

XHTMLTagSvgAction::XHTMLTagSvgAction(XHTMLSvgImageNamePredicate &predicate) : myPredicate(predicate) {
}

void XHTMLTagSvgAction::doAtStart(XHTMLReader&, const char**) {
	myPredicate.myIsEnabled = true;
}

void XHTMLTagSvgAction::doAtEnd(XHTMLReader&) {
	myPredicate.myIsEnabled = false;
}

XHTMLSvgImageNamePredicate::XHTMLSvgImageNamePredicate() : ZLXMLReader::FullNamePredicate(ZLXMLNamespace::XLink, "href"), myIsEnabled(false) {
}

bool XHTMLSvgImageNamePredicate::accepts(const ZLXMLReader &reader, const char *name) const {
	return myIsEnabled && FullNamePredicate::accepts(reader, name);
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
	if (myControl == TITLE && bookReader(reader).model().bookTextModel()->paragraphsNumber() > 1) {
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
	bookReader(reader).addControl(PREFORMATTED, true);
}

void XHTMLTagPreAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
	reader.myPreformatted = false;
}

XHTMLTagAction *XHTMLReader::addAction(const std::string &tag, XHTMLTagAction *action) {
	XHTMLTagAction *old = ourTagActions[tag];
	ourTagActions[tag] = action;
	return old;
}

XHTMLTagAction *XHTMLReader::addAction(const std::string &ns, const std::string &name, XHTMLTagAction *action) {
	shared_ptr<FullNamePredicate> predicate = new FullNamePredicate(ns, name);
	XHTMLTagAction *old = ourNsTagActions[predicate];
	ourNsTagActions[predicate] = action;
	return old;
}

XHTMLTagAction *XHTMLReader::getAction(const std::string &tag) {
	const std::string lTag = ZLUnicodeUtil::toLower(tag);
	XHTMLTagAction *action = ourTagActions[lTag];
	if (action != 0) {
		return action;
	}
	for (std::map<shared_ptr<FullNamePredicate>,XHTMLTagAction*>::const_iterator it = ourNsTagActions.begin(); it != ourNsTagActions.end(); ++it) {
		if (it->first->accepts(*this, lTag)) {
			return it->second;
		}
	}
	return 0;
}

void XHTMLReader::fillTagTable() {
	if (ourTagActions.empty()) {
		//addAction("html", new XHTMLTagAction());
		addAction("body", new XHTMLTagBodyAction());
		//addAction("title", new XHTMLTagAction());
		//addAction("meta", new XHTMLTagAction());
		//addAction("script", new XHTMLTagAction());

		//addAction("font", new XHTMLTagAction());
		addAction("style", new XHTMLTagStyleAction());

		addAction("p", new XHTMLTagParagraphAction());
		addAction("h1", new XHTMLTagParagraphWithControlAction(H1));
		addAction("h2", new XHTMLTagParagraphWithControlAction(H2));
		addAction("h3", new XHTMLTagParagraphWithControlAction(H3));
		addAction("h4", new XHTMLTagParagraphWithControlAction(H4));
		addAction("h5", new XHTMLTagParagraphWithControlAction(H5));
		addAction("h6", new XHTMLTagParagraphWithControlAction(H6));

		//addAction("ol", new XHTMLTagAction());
		//addAction("ul", new XHTMLTagAction());
		//addAction("dl", new XHTMLTagAction());
		addAction("li", new XHTMLTagItemAction());

		addAction("strong", new XHTMLTagControlAction(STRONG));
		addAction("b", new XHTMLTagControlAction(BOLD));
		addAction("em", new XHTMLTagControlAction(EMPHASIS));
		addAction("i", new XHTMLTagControlAction(ITALIC));
		addAction("code", new XHTMLTagControlAction(CODE));
		addAction("tt", new XHTMLTagControlAction(CODE));
		addAction("kbd", new XHTMLTagControlAction(CODE));
		addAction("var", new XHTMLTagControlAction(CODE));
		addAction("samp", new XHTMLTagControlAction(CODE));
		addAction("cite", new XHTMLTagControlAction(CITE));
		addAction("sub", new XHTMLTagControlAction(SUB));
		addAction("sup", new XHTMLTagControlAction(SUP));
		addAction("dd", new XHTMLTagControlAction(DEFINITION_DESCRIPTION));
		addAction("dfn", new XHTMLTagControlAction(DEFINITION));
		addAction("strike", new XHTMLTagControlAction(STRIKETHROUGH));

		addAction("a", new XHTMLTagHyperlinkAction());

		addAction("img", new XHTMLTagImageAction("src"));
		addAction("object", new XHTMLTagImageAction("data"));
		XHTMLSvgImageNamePredicate *predicate = new XHTMLSvgImageNamePredicate();
		addAction("svg", new XHTMLTagSvgAction(*predicate));
		addAction("image", new XHTMLTagImageAction(predicate));
		addAction(ZLXMLNamespace::Svg, "svg", new XHTMLTagSvgAction(*predicate));
		addAction(ZLXMLNamespace::Svg, "image", new XHTMLTagImageAction(predicate));

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
		addAction("link", new XHTMLTagLinkAction());
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

		addAction("video", new XHTMLTagVideoAction());
		addAction("source", new XHTMLTagSourceAction());
	}
}

XHTMLReader::XHTMLReader(BookReader &modelReader, shared_ptr<EncryptionMap> map) : myModelReader(modelReader), myEncryptionMap(map) {
	myMarkNextImageAsCover = false;
}

void XHTMLReader::setMarkFirstImageAsCover() {
	myMarkNextImageAsCover = true;
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
	myReadState = XHTML_READ_NOTHING;
	myBodyCounter = 0;
	myCurrentParagraphIsEmpty = true;

	myStyleSheetTable.clear();
	myFontMap = new FontMap();
	myCSSStack.clear();
	myStyleEntryStack.clear();
	myStylesToRemove = 0;

	myDoPageBreakAfterStack.clear();
	myStyleParser = new StyleSheetSingleStyleParser(myPathPrefix);
	myTableParser.reset();

	shared_ptr<ZLInputStream> stream = file.inputStream(myEncryptionMap);
	if (!stream.isNull()) {
		return readDocument(file.inputStream(myEncryptionMap));
	} else {
		if (file.exists() && !myEncryptionMap.isNull()) {
			myModelReader.insertEncryptedSectionParagraph();
		}
		return false;
	}
}

bool XHTMLReader::addTextStyleEntry(const std::string tag, const std::string aClass) {
	shared_ptr<ZLTextStyleEntry> entry = myStyleSheetTable.control(tag, aClass);
	if (!entry.isNull()) {
		addTextStyleEntry(*entry);
		myStyleEntryStack.push_back(entry);
		return true;
	}
	return false;
}

void XHTMLReader::addTextStyleEntry(const ZLTextStyleEntry &entry) {
	if (entry.isFeatureSupported(ZLTextStyleEntry::FONT_FAMILY)) {
		ZLLogger::Instance().println("FONT", "Requested font family: " + entry.fontFamily());
	}
	myModelReader.addStyleEntry(entry);
}

void XHTMLReader::startElementHandler(const char *tag, const char **attributes) {
	static const std::string HASH = "#";
	const char *id = attributeValue(attributes, "id");
	if (id != 0) {
		myModelReader.addHyperlinkLabel(myReferenceAlias + HASH + id);
	}

	const std::string sTag = ZLUnicodeUtil::toLower(tag);

	std::vector<std::string> classesList;
	const char *aClasses = attributeValue(attributes, "class");
	if (aClasses != 0) {
		const std::vector<std::string> split = ZLStringUtil::split(aClasses, " ");
		for (std::vector<std::string>::const_iterator it = split.begin(); it != split.end(); ++it) {
			if (!it->empty()) {
				classesList.push_back(*it);
			}
		}
	}
	if (classesList.empty()) {
		classesList.push_back("");
	}

	bool breakBefore = false;
	bool breakAfter = false;
	for (std::vector<std::string>::const_iterator it = classesList.begin(); it != classesList.end(); ++it) {
		// TODO: use 3-value logic (yes, no, inherit)
		if (myStyleSheetTable.doBreakBefore(sTag, *it)) {
			breakBefore = true;
		}
		// TODO: use 3-value logic (yes, no, inherit)
		if (myStyleSheetTable.doBreakAfter(sTag, *it)) {
			breakAfter = true;
		}
	}
	if (breakBefore) {
		myModelReader.insertEndOfSectionParagraph();
	}
	myDoPageBreakAfterStack.push_back(breakAfter);

	XHTMLTagAction *action = getAction(sTag);
	if (action != 0 && action->isEnabled(myReadState)) {
		action->doAtStart(*this, attributes);
	}

	const int sizeBefore = myStyleEntryStack.size();
	addTextStyleEntry(sTag, "");
	for (std::vector<std::string>::const_iterator it = classesList.begin(); it != classesList.end(); ++it) {
		addTextStyleEntry("", *it);
		addTextStyleEntry(sTag, *it);
		const char *style = attributeValue(attributes, "style");
		if (style != 0) {
			//ZLLogger::Instance().println("CSS", std::string("parsing style attribute: ") + style);
			shared_ptr<ZLTextStyleEntry> entry = myStyleParser->parseSingleEntry(style);
			addTextStyleEntry(*entry);
			myStyleEntryStack.push_back(entry);
		}
	}
	myCSSStack.push_back(myStyleEntryStack.size() - sizeBefore);
}

void XHTMLReader::endElementHandler(const char *tag) {
	for (int i = myCSSStack.back(); i > 0; --i) {
		myModelReader.addStyleCloseEntry();
	}
	myStylesToRemove = myCSSStack.back();
	myCSSStack.pop_back();

	XHTMLTagAction *action = getAction(tag);
	if (action != 0 && action->isEnabled(myReadState)) {
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
		addTextStyleEntry(**it);
		doBlockSpaceBefore =
			doBlockSpaceBefore ||
			(*it)->isFeatureSupported(ZLTextStyleEntry::LENGTH_SPACE_BEFORE);
	}

	if (doBlockSpaceBefore) {
		ZLTextStyleEntry blockingEntry(ZLTextStyleEntry::STYLE_OTHER_ENTRY);
		blockingEntry.setLength(
			ZLTextStyleEntry::LENGTH_SPACE_BEFORE,
			0,
			ZLTextStyleEntry::SIZE_UNIT_PIXEL
		);
		addTextStyleEntry(blockingEntry);
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
		ZLTextStyleEntry blockingEntry(ZLTextStyleEntry::STYLE_OTHER_ENTRY);
		blockingEntry.setLength(
			ZLTextStyleEntry::LENGTH_SPACE_AFTER,
			0,
			ZLTextStyleEntry::SIZE_UNIT_PIXEL
		);
		addTextStyleEntry(blockingEntry);
	}
	for (; myStylesToRemove > 0; --myStylesToRemove) {
		addTextStyleEntry(*myStyleEntryStack.back());
		myStyleEntryStack.pop_back();
	}
	myModelReader.endParagraph();
}

void XHTMLReader::characterDataHandler(const char *text, std::size_t len) {
	switch (myReadState) {
		case XHTML_READ_NOTHING:
		case XHTML_READ_VIDEO:
			break;
		case XHTML_READ_STYLE:
			if (!myTableParser.isNull()) {
				myTableParser->parseString(text, len);
			}
			break;
		case XHTML_READ_BODY:
			if (myPreformatted) {
				if (*text == '\r' || *text == '\n') {
					endParagraph();
					text += 1;
					len -= 1;
					beginParagraph();
					myModelReader.addControl(PREFORMATTED, true);
				}
				std::size_t spaceCounter = 0;
				while (spaceCounter < len && std::isspace((unsigned char)*(text + spaceCounter))) {
					++spaceCounter;
				}
				myModelReader.addFixedHSpace(spaceCounter);
				text += spaceCounter;
				len -= spaceCounter;
			} else if (myNewParagraphInProgress || !myModelReader.paragraphIsOpen()) {
				while (std::isspace((unsigned char)*text)) {
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
	const std::size_t index = reference.find('#');
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
