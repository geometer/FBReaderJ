/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
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

static const std::string ANY = "*";
static const std::string EMPTY = "";
static const XHTMLTagInfoList EMPTY_INFO_LIST;

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

private:
	const FBTextKind myTextKind;

public:
	XHTMLTagParagraphAction(FBTextKind textKind = (FBTextKind)-1);
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagBodyAction : public XHTMLGlobalTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagSectionAction : public XHTMLGlobalTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

class XHTMLTagPseudoSectionAction : public XHTMLGlobalTagAction {

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

class XHTMLTagListAction : public XHTMLTextModeTagAction {

private:
	const int myStartIndex;

public:
	XHTMLTagListAction(int startIndex = -1);
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
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

class XHTMLTagOpdsAction : public XHTMLTextModeTagAction {

public:
	void doAtStart(XHTMLReader &reader, const char **xmlattributes);
	void doAtEnd(XHTMLReader &reader);
};

void XHTMLTagStyleAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	static const std::string TYPE = "text/css";

	const char *type = reader.attributeValue(xmlattributes, "type");
	if (type == 0 || TYPE != type) {
		return;
	}

	if (reader.myReadState == XHTML_READ_NOTHING) {
		reader.myReadState = XHTML_READ_STYLE;
		reader.myTableParser = new StyleSheetTableParser(reader.myPathPrefix, reader.myStyleSheetTable, reader.myFontMap, reader.myEncryptionMap);
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
	if (rel == 0 || REL != ZLUnicodeUtil::toLower(rel)) {
		return;
	}
	static const std::string TYPE = "text/css";

	const char *type = reader.attributeValue(xmlattributes, "type");
	if (type == 0 || TYPE != ZLUnicodeUtil::toLower(type)) {
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
			0,
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
	parser->applyToTables(reader.myStyleSheetTable, *reader.myFontMap);
}

void XHTMLTagLinkAction::doAtEnd(XHTMLReader&) {
}

XHTMLTagParagraphAction::XHTMLTagParagraphAction(FBTextKind textKind) : myTextKind(textKind) {
}

void XHTMLTagParagraphAction::doAtStart(XHTMLReader &reader, const char**) {
	if (!reader.myNewParagraphInProgress) {
		reader.pushTextKind(myTextKind);
		reader.beginParagraph();
		reader.myNewParagraphInProgress = true;
	}
}

void XHTMLTagParagraphAction::doAtEnd(XHTMLReader &reader) {
	reader.endParagraph();
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

void XHTMLTagSectionAction::doAtStart(XHTMLReader &reader, const char**) {
}

void XHTMLTagSectionAction::doAtEnd(XHTMLReader &reader) {
	bookReader(reader).insertEndOfSectionParagraph();
}

void XHTMLTagPseudoSectionAction::doAtStart(XHTMLReader &reader, const char**) {
}

void XHTMLTagPseudoSectionAction::doAtEnd(XHTMLReader &reader) {
	bookReader(reader).insertPseudoEndOfSectionParagraph();
}

XHTMLTagListAction::XHTMLTagListAction(int startIndex) : myStartIndex(startIndex) {
}

void XHTMLTagListAction::doAtStart(XHTMLReader &reader, const char**) {
	reader.myListNumStack.push(myStartIndex);
	beginParagraph(reader);
}

void XHTMLTagListAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
	if (!reader.myListNumStack.empty()) {
		reader.myListNumStack.pop();
	}
}

void XHTMLTagItemAction::doAtStart(XHTMLReader &reader, const char**) {
	bool restart = true;
	if (reader.myTagDataStack.size() >= 2) {
		restart = reader.myTagDataStack[reader.myTagDataStack.size() - 2]->Children.size() > 1;
	}
	if (restart) {
		endParagraph(reader);
		beginParagraph(reader);
	}
	if (!reader.myListNumStack.empty()) {
		bookReader(reader).addFixedHSpace(3 * reader.myListNumStack.size());
		int &index = reader.myListNumStack.top();
		if (index == 0) {
			static const std::string bullet = "\xE2\x80\xA2\xC0\xA0";
			bookReader(reader).addData(bullet);
		} else {
			bookReader(reader).addData(ZLStringUtil::numberToString(index++) + ".");
		}
		bookReader(reader).addFixedHSpace(1);
	}
	reader.myNewParagraphInProgress = true;
}

void XHTMLTagItemAction::doAtEnd(XHTMLReader &reader) {
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
	bookReader(reader).addImage(imageName, new ZLFileImage(imageFile, EMPTY, 0, 0, reader.myEncryptionMap->info(imageFile.path())));
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
	reader.pushTextKind(myControl);
	bookReader(reader).addControl(myControl, true);
}

void XHTMLTagControlAction::doAtEnd(XHTMLReader &reader) {
	bookReader(reader).addControl(myControl, false);
}

void XHTMLTagHyperlinkAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	const char *href = reader.attributeValue(xmlattributes, "href");
	if (href != 0 && href[0] != '\0') {
		FBTextKind hyperlinkType = MiscUtil::referenceType(href);
		std::string link = MiscUtil::decodeHtmlURL(href);
		if (hyperlinkType == INTERNAL_HYPERLINK) {
			static const std::string NOTEREF = "noteref";
			const char *epubType = reader.attributeValue(xmlattributes, "epub:type");
			if (epubType == 0) {
				// popular ePub mistake: ':' in attribute name coverted to ascii code
				static const ZLXMLReader::IgnoreCaseNamePredicate epubTypePredicate("epubu0003atype");
				epubType = reader.attributeValue(xmlattributes, epubTypePredicate);
			}
			if (epubType != 0 && NOTEREF == epubType) {
				hyperlinkType = FOOTNOTE;
			}

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
	reader.pushTextKind(myControl);
	beginParagraph(reader);
}

void XHTMLTagParagraphWithControlAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
}

void XHTMLTagPreAction::doAtStart(XHTMLReader &reader, const char**) {
	reader.myPreformatted = true;
	reader.pushTextKind(PREFORMATTED);
	beginParagraph(reader);
}

void XHTMLTagPreAction::doAtEnd(XHTMLReader &reader) {
	endParagraph(reader);
	reader.myPreformatted = false;
}

void XHTMLTagOpdsAction::doAtStart(XHTMLReader &reader, const char **xmlattributes) {
	bookReader(reader).addExtensionEntry("opds", reader.attributeMap(xmlattributes));
}

void XHTMLTagOpdsAction::doAtEnd(XHTMLReader &reader) {
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

		addAction("aside", new XHTMLTagPseudoSectionAction());

		//addAction("font", new XHTMLTagAction());
		addAction("style", new XHTMLTagStyleAction());

		addAction("p", new XHTMLTagParagraphAction(XHTML_TAG_P));
		addAction("h1", new XHTMLTagParagraphWithControlAction(H1));
		addAction("h2", new XHTMLTagParagraphWithControlAction(H2));
		addAction("h3", new XHTMLTagParagraphWithControlAction(H3));
		addAction("h4", new XHTMLTagParagraphWithControlAction(H4));
		addAction("h5", new XHTMLTagParagraphWithControlAction(H5));
		addAction("h6", new XHTMLTagParagraphWithControlAction(H6));

		addAction("ol", new XHTMLTagListAction(1));
		addAction("ul", new XHTMLTagListAction(0));
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

		addAction(ZLXMLNamespace::FBReaderXhtml, "opds", new XHTMLTagOpdsAction());

		//addAction("area", new XHTMLTagAction());
		//addAction("map", new XHTMLTagAction());

		//addAction("base", new XHTMLTagAction());
		//addAction("blockquote", new XHTMLTagAction());
		//addAction("br", new XHTMLTagRestartParagraphAction());
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
	//ZLLogger::Instance().registerClass("XHTML");
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
	myTagDataStack.clear();

	myStyleParser = new StyleSheetSingleStyleParser(myPathPrefix);
	myTableParser.reset();

	return readDocument(file.inputStream(myEncryptionMap));
}

const XHTMLTagInfoList &XHTMLReader::tagInfos(size_t depth) const {
	if (myTagDataStack.size() < depth + 2) {
		return EMPTY_INFO_LIST;
	}
	return myTagDataStack[myTagDataStack.size() - depth - 2]->Children;
}

bool XHTMLReader::matches(const shared_ptr<CSSSelector::Component> next, int depth, int pos) const {
	if (next.isNull()) {
		return true;
	}

	// TODO: check next->Selector.Next
	const CSSSelector &selector = *(next->Selector);
	switch (next->Delimiter) {
		default:
			return false;
		case CSSSelector::Parent:
			return tagInfos(depth + 1).matches(selector, -1) && matches(selector.Next, depth + 1);
		case CSSSelector::Ancestor:
			if (selector.Next.isNull() || selector.Next->Delimiter == CSSSelector::Ancestor) {
				for (size_t i = 1; i < myTagDataStack.size() - depth - 1; ++i) {
					if (tagInfos(depth + i).matches(selector, -1)) {
						return matches(selector.Next, i);
					}
				}
				return false;
			} else {
				for (size_t i = 1; i < myTagDataStack.size() - depth - 1; ++i) {
					if (tagInfos(depth + i).matches(selector, -1) && matches(selector.Next, i)) {
						return true;
					}
				}
				return false;
			}
		case CSSSelector::Predecessor:
			if (!selector.Next.isNull() && selector.Next->Delimiter == CSSSelector::Previous) {
				while (true) {
					// it is guaranteed that pos will be decreased on each step
					pos = tagInfos(depth).find(selector, 1, pos);
					if (pos == -1) {
						return false;
					} else if (matches(selector.Next, depth, pos)) {
						return true;
					}
				}
			} else {
				const int index = tagInfos(depth).find(selector, 0, pos);
				return index != -1 && matches(selector.Next, depth, index);
			}
		case CSSSelector::Previous:
			return tagInfos(depth).matches(selector, pos - 1) && matches(selector.Next, depth, pos - 1);
	}
}

void XHTMLReader::applySingleEntry(shared_ptr<ZLTextStyleEntry> entry) {
	if (entry.isNull()) {
		return;
	}
	addTextStyleEntry(*(entry->start()), myTagDataStack.size());
	shared_ptr<TagData> data = myTagDataStack.back();
	data->StyleEntries.push_back(entry);
	const ZLTextStyleEntry::DisplayCode dc = entry->displayCode();
	if (dc != ZLTextStyleEntry::DC_NOT_DEFINED) {
		data->DisplayCode = dc;
	}
}

void XHTMLReader::applyTagStyles(const std::string &tag, const std::string &aClass) {
	std::vector<std::pair<CSSSelector,shared_ptr<ZLTextStyleEntry> > > controls =
		myStyleSheetTable.allControls(tag, aClass);
	for (std::vector<std::pair<CSSSelector,shared_ptr<ZLTextStyleEntry> > >::const_iterator it = controls.begin(); it != controls.end(); ++it) {
		if (matches(it->first.Next)) {
			applySingleEntry(it->second);
		}
	}
}

void XHTMLReader::addTextStyleEntry(const ZLTextStyleEntry &entry, unsigned char depth) {
	if (!entry.isFeatureSupported(ZLTextStyleEntry::FONT_FAMILY)) {
		myModelReader.addStyleEntry(entry, depth);
		return;
	}

	bool doFixFamiliesList = false;

	const std::vector<std::string> &families = entry.fontFamilies();
	for (std::vector<std::string>::const_iterator it = families.begin(); it != families.end(); ++it) {
		ZLLogger::Instance().println("FONT", "Requested font family: " + *it);
		shared_ptr<FontEntry> fontEntry = myFontMap->get(*it);
		if (!fontEntry.isNull()) {
			const std::string realFamily = myModelReader.putFontEntry(*it, fontEntry);
			if (realFamily != *it) {
				ZLLogger::Instance().println("FONT", "Entry for " + *it + " stored as " + realFamily);
				doFixFamiliesList = true;
				break;
			}
		}
	}

	if (!doFixFamiliesList) {
		myModelReader.addStyleEntry(entry, depth);
	} else {
		std::vector<std::string> realFamilies;
		for (std::vector<std::string>::const_iterator it = families.begin(); it != families.end(); ++it) {
			shared_ptr<FontEntry> fontEntry = myFontMap->get(*it);
			if (!fontEntry.isNull()) {
				realFamilies.push_back(myModelReader.putFontEntry(*it, fontEntry));
			} else {
				realFamilies.push_back(*it);
			}
		}
		myModelReader.addStyleEntry(entry, realFamilies, depth);
	}
}

void XHTMLReader::startElementHandler(const char *tag, const char **attributes) {
	const std::string sTag = ZLUnicodeUtil::toLower(tag);
	if (sTag == "br") {
		restartParagraph(true);
		return;
	}

	std::vector<std::string> classesList;
	const char *aClasses = attributeValue(attributes, "class");
	if (aClasses != 0) {
		const std::vector<std::string> split = ZLStringUtil::split(aClasses, " ", true);
		for (std::vector<std::string>::const_iterator it = split.begin(); it != split.end(); ++it) {
			classesList.push_back(*it);
		}
	}

	if (!myTagDataStack.empty()) {
		myTagDataStack.back()->Children.push_back(XHTMLTagInfo(sTag, classesList));
	}
	myTagDataStack.push_back(new TagData());
	TagData &tagData = *myTagDataStack.back();

	static const std::string HASH = "#";
	const char *id = attributeValue(attributes, "id");
	if (id != 0) {
		myModelReader.addHyperlinkLabel(myReferenceAlias + HASH + id);
	}

	ZLBoolean3 breakBefore = myStyleSheetTable.doBreakBefore(sTag, EMPTY);
	tagData.PageBreakAfter = myStyleSheetTable.doBreakAfter(sTag, EMPTY);
	for (std::vector<std::string>::const_iterator it = classesList.begin(); it != classesList.end(); ++it) {
		const ZLBoolean3 bb = myStyleSheetTable.doBreakBefore(sTag, *it);
		if (bb != B3_UNDEFINED) {
			breakBefore = bb;
		}
		const ZLBoolean3 ba = myStyleSheetTable.doBreakAfter(sTag, *it);
		if (ba != B3_UNDEFINED) {
			tagData.PageBreakAfter = ba;
		}
	}
	if (breakBefore == B3_TRUE) {
		myModelReader.insertEndOfSectionParagraph();
	}

	XHTMLTagAction *action = getAction(sTag);
	if (action != 0 && action->isEnabled(myReadState)) {
		action->doAtStart(*this, attributes);
	}

	applyTagStyles(ANY, EMPTY);
	applyTagStyles(sTag, EMPTY);
	for (std::vector<std::string>::const_iterator it = classesList.begin(); it != classesList.end(); ++it) {
		applyTagStyles(EMPTY, *it);
		applyTagStyles(sTag, *it);
	}
	const char *style = attributeValue(attributes, "style");
	if (style != 0) {
		//ZLLogger::Instance().println("CSS", std::string("parsing style attribute: ") + style);
		applySingleEntry(myStyleParser->parseSingleEntry(style));
	}
	if (tagData.DisplayCode == ZLTextStyleEntry::DC_BLOCK) {
		restartParagraph(false);
	}
}

void XHTMLReader::endElementHandler(const char *tag) {
	const std::string sTag = ZLUnicodeUtil::toLower(tag);
	if (sTag == "br") {
		return;
	}

	const TagData &tagData = *myTagDataStack.back();
	const std::vector<shared_ptr<ZLTextStyleEntry> > &entries = tagData.StyleEntries;
	size_t entryCount = entries.size();
	const unsigned char depth = myTagDataStack.size();
	for (std::vector<shared_ptr<ZLTextStyleEntry> >::const_iterator jt = entries.begin(); jt != entries.end(); ++jt) {
		shared_ptr<ZLTextStyleEntry> entry = *jt;
		shared_ptr<ZLTextStyleEntry> endEntry = entry->end();
		if (!endEntry.isNull()) {
			addTextStyleEntry(*endEntry, depth);
			++entryCount;
		}
	}

	XHTMLTagAction *action = getAction(sTag);
	if (action != 0 && action->isEnabled(myReadState)) {
		action->doAtEnd(*this);
		myNewParagraphInProgress = false;
	}

	for (; entryCount > 0; --entryCount) {
		myModelReader.addStyleCloseEntry();
	}

	if (tagData.PageBreakAfter == B3_TRUE) {
		myModelReader.insertEndOfSectionParagraph();
	} else if (tagData.DisplayCode == ZLTextStyleEntry::DC_BLOCK) {
		restartParagraph(false);
	}

	myTagDataStack.pop_back();
}

void XHTMLReader::beginParagraph(bool restarted) {
	myCurrentParagraphIsEmpty = true;
	myModelReader.beginParagraph();
	for (std::vector<shared_ptr<TagData> >::const_iterator it = myTagDataStack.begin(); it != myTagDataStack.end(); ++it) {
		const std::vector<FBTextKind> &kinds = (*it)->TextKinds;
		for (std::vector<FBTextKind>::const_iterator jt = kinds.begin(); jt != kinds.end(); ++jt) {
			myModelReader.addControl(*jt, true);
		}
		const std::vector<shared_ptr<ZLTextStyleEntry> > &entries = (*it)->StyleEntries;
		bool inheritedOnly = !restarted || it + 1 != myTagDataStack.end();
		const unsigned char depth = it - myTagDataStack.begin() + 1;
		for (std::vector<shared_ptr<ZLTextStyleEntry> >::const_iterator jt = entries.begin(); jt != entries.end(); ++jt) {
			shared_ptr<ZLTextStyleEntry> entry = inheritedOnly ? (*jt)->inherited() : (*jt)->start();
			addTextStyleEntry(*entry, depth);
		}
	}
}

void XHTMLReader::endParagraph() {
	myModelReader.endParagraph();
}

void XHTMLReader::restartParagraph(bool addEmptyLine) {
	if (addEmptyLine && myCurrentParagraphIsEmpty) {
		myModelReader.addFixedHSpace(1);
	}
	const unsigned char depth = myTagDataStack.size();
	ZLTextStyleEntry spaceAfterBlocker(ZLTextStyleEntry::STYLE_OTHER_ENTRY);
	spaceAfterBlocker.setLength(
		ZLTextStyleEntry::LENGTH_SPACE_AFTER,
		0,
		ZLTextStyleEntry::SIZE_UNIT_PIXEL
	);
	addTextStyleEntry(spaceAfterBlocker, depth);
	endParagraph();
	beginParagraph(true);
	ZLTextStyleEntry spaceBeforeBlocker(ZLTextStyleEntry::STYLE_OTHER_ENTRY);
	spaceBeforeBlocker.setLength(
		ZLTextStyleEntry::LENGTH_SPACE_BEFORE,
		0,
		ZLTextStyleEntry::SIZE_UNIT_PIXEL
	);
	addTextStyleEntry(spaceBeforeBlocker, depth);
}

void XHTMLReader::pushTextKind(FBTextKind kind) {
	if (kind != -1) {
		myTagDataStack.back()->TextKinds.push_back(kind);
	}
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
					restartParagraph(true);
					text += 1;
					len -= 1;
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
	return EntityFilesCollector::xhtmlDTDs();
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

XHTMLReader::TagData::TagData() : PageBreakAfter(B3_UNDEFINED), DisplayCode(ZLTextStyleEntry::DC_INLINE) {
}
