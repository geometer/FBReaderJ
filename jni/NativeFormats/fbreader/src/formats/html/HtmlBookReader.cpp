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
#include <ZLFileImage.h>
#include <ZLStringUtil.h>

#include "HtmlBookReader.h"
#include "HtmlTagActions.h"
#include "../txt/PlainTextFormat.h"
#include "../util/MiscUtil.h"
#include "../../bookmodel/BookModel.h"
#include "../css/StyleSheetParser.h"

HtmlTagAction::HtmlTagAction(HtmlBookReader &reader) : myReader(reader) {
}

HtmlTagAction::~HtmlTagAction() {
}

void HtmlTagAction::reset() {
}

DummyHtmlTagAction::DummyHtmlTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void DummyHtmlTagAction::run(const HtmlReader::HtmlTag&) {
}

HtmlControlTagAction::HtmlControlTagAction(HtmlBookReader &reader, FBTextKind kind) : HtmlTagAction(reader), myKind(kind) {
}

void HtmlControlTagAction::run(const HtmlReader::HtmlTag &tag) {
	std::vector<FBTextKind> &list = myReader.myKindList;
	int index;
	for (index = list.size() - 1; index >= 0; --index) {
		if (list[index] == myKind) {
			break;
		}
	}
	if (tag.Start) {
		if (index == -1) {
			bookReader().pushKind(myKind);
			myReader.myKindList.push_back(myKind);
			bookReader().addControl(myKind, true);
		}
	} else {
		if (index >= 0) {
			for (int i = list.size() - 1; i >= index; --i) {
				bookReader().addControl(list[i], false);
				bookReader().popKind();
			}
			for (unsigned int j = index + 1; j < list.size(); ++j) {
				bookReader().addControl(list[j], true);
				bookReader().pushKind(list[j]);
			}
			list.erase(list.begin() + index);
		}
	}
}

HtmlHeaderTagAction::HtmlHeaderTagAction(HtmlBookReader &reader, FBTextKind kind) : HtmlTagAction(reader), myKind(kind) {
}

void HtmlHeaderTagAction::run(const HtmlReader::HtmlTag &tag) {
	myReader.myIsStarted = false;
	if (tag.Start) {
		if (myReader.myBuildTableOfContent && !myReader.myIgnoreTitles) {
			if (!bookReader().contentsParagraphIsOpen()) {
				bookReader().insertEndOfSectionParagraph();
				bookReader().enterTitle();
				bookReader().beginContentsParagraph();
			}
		}
		bookReader().pushKind(myKind);
	} else {
		bookReader().popKind();
		if (myReader.myBuildTableOfContent && !myReader.myIgnoreTitles) {
			bookReader().endContentsParagraph();
			bookReader().exitTitle();
		}
	}
	bookReader().beginParagraph();
}

HtmlIgnoreTagAction::HtmlIgnoreTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void HtmlIgnoreTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		if (myTagNames.find(tag.Name) == myTagNames.end()) {
			++myReader.myIgnoreDataCounter;
			myTagNames.insert(tag.Name);
		}
	} else {
		if (myTagNames.find(tag.Name) != myTagNames.end()) {
			--myReader.myIgnoreDataCounter;
			myTagNames.erase(tag.Name);
		}
	}
}

HtmlHrefTagAction::HtmlHrefTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void HtmlHrefTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		for (unsigned int i = 0; i < tag.Attributes.size(); ++i) {
			if (tag.Attributes[i].Name == "name") {
				bookReader().addHyperlinkLabel(tag.Attributes[i].Value);
			} else if ((hyperlinkType() == REGULAR) && (tag.Attributes[i].Name == "href")) {
				std::string value = tag.Attributes[i].Value;
				if (!myReader.myFileName.empty() &&
						(value.length() > myReader.myFileName.length()) &&
						(value.substr(0, myReader.myFileName.length()) == myReader.myFileName)) {
					value = value.substr(myReader.myFileName.length());
				}
				if (!value.empty()) {
					if (value[0] == '#') {
						setHyperlinkType(INTERNAL_HYPERLINK);
						bookReader().addHyperlinkControl(INTERNAL_HYPERLINK, value.substr(1));
					} else {
						FBTextKind hyperlinkType = MiscUtil::referenceType(value);
						if (hyperlinkType != INTERNAL_HYPERLINK) {
							setHyperlinkType(hyperlinkType);
							bookReader().addHyperlinkControl(hyperlinkType, value);
						}
					}
				}
			}
		}
	} else if (hyperlinkType() != REGULAR) {
		bookReader().addControl(hyperlinkType(), false);
		setHyperlinkType(REGULAR);
	}
}

void HtmlHrefTagAction::reset() {
	setHyperlinkType(REGULAR);
}

FBTextKind HtmlHrefTagAction::hyperlinkType() const {
	return myHyperlinkType;
}

void HtmlHrefTagAction::setHyperlinkType(FBTextKind hyperlinkType) {
	myHyperlinkType = hyperlinkType;
}

HtmlImageTagAction::HtmlImageTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void HtmlImageTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		bookReader().endParagraph();
		const std::string *ptr = tag.find("src");
		if (ptr != 0) {
			const std::string fileName = MiscUtil::decodeHtmlURL(*ptr);
			const ZLFile file(myReader.myBaseDirPath + fileName);
			if (file.exists()) {
				bookReader().addImageReference(fileName, 0, false);
				bookReader().addImage(fileName, new ZLFileImage(file, "", 0));
			}
		}
		bookReader().beginParagraph();
	}
}

HtmlBreakTagAction::HtmlBreakTagAction(HtmlBookReader &reader, BreakType breakType) : HtmlTagAction(reader), myBreakType(breakType) {
}

void HtmlBreakTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (myReader.myDontBreakParagraph) {
		myReader.myDontBreakParagraph = false;
		return;
	}

	if ((tag.Start && (myBreakType & BREAK_AT_START)) ||
			(!tag.Start && (myBreakType & BREAK_AT_END))) {
		bookReader().endParagraph();
		if (bookReader().isKindStackEmpty()) {
			bookReader().pushKind(REGULAR);
		}
		bookReader().beginParagraph();
	}
}

HtmlPreTagAction::HtmlPreTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void HtmlPreTagAction::run(const HtmlReader::HtmlTag &tag) {
	bookReader().endParagraph();
	myReader.myIsPreformatted = tag.Start;
	myReader.mySpaceCounter = -1;
	myReader.myBreakCounter = 0;
	if (myReader.myFormat.breakType() == PlainTextFormat::BREAK_PARAGRAPH_AT_NEW_LINE) {
		if (tag.Start) {
			bookReader().pushKind(PREFORMATTED);
		} else {
			bookReader().popKind();
		}
	}
	bookReader().beginParagraph();
}

HtmlListTagAction::HtmlListTagAction(HtmlBookReader &reader, int startIndex) : HtmlTagAction(reader), myStartIndex(startIndex) {
}

void HtmlListTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		myReader.myListNumStack.push(myStartIndex);
	} else if (!myReader.myListNumStack.empty()) {
		myReader.myListNumStack.pop();
	}
}

HtmlListItemTagAction::HtmlListItemTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void HtmlListItemTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		bookReader().endParagraph();
		bookReader().beginParagraph();
		if (!myReader.myListNumStack.empty()) {
			bookReader().addFixedHSpace(3 * myReader.myListNumStack.size());
			int &index = myReader.myListNumStack.top();
			if (index == 0) {
				myReader.addConvertedDataToBuffer("\342\200\242", 3, false);
			} else {
				const std::string number = ZLStringUtil::numberToString(index++) + ".";
				myReader.addConvertedDataToBuffer(number.data(), number.length(), false);
			}
			bookReader().addFixedHSpace(1);
			myReader.myDontBreakParagraph = true;
		}
	} else {
		myReader.myDontBreakParagraph = false;
	}
}

HtmlTableTagAction::HtmlTableTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void HtmlTableTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		myReader.myIgnoreTitles = true;
	} else {
		myReader.myIgnoreTitles = false;
	}
}

HtmlStyleTagAction::HtmlStyleTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

shared_ptr<StyleSheetParser> HtmlBookReader::createCSSParser() {
	return new StyleSheetTableParser(myBaseDirPath, myStyleSheetTable, myFontMap, 0);
}

void HtmlStyleTagAction::run(const HtmlReader::HtmlTag &tag) {
	myReader.myStyleSheetParser = tag.Start ? myReader.createCSSParser() : 0;
	/*
	if (!tag.Start) {
		myReader.myStyleSheetTable.dump();
	}
	*/
}

shared_ptr<HtmlTagAction> HtmlBookReader::createAction(const std::string &tag) {
	if (tag == "em") {
		return new HtmlControlTagAction(*this, EMPHASIS);
	} else if (tag == "strong") {
		return new HtmlControlTagAction(*this, STRONG);
	} else if (tag == "b") {
		return new HtmlControlTagAction(*this, BOLD);
	} else if (tag == "i") {
		return new HtmlControlTagAction(*this, ITALIC);
	} else if (tag == "tt") {
		return new HtmlControlTagAction(*this, CODE);
	} else if (tag == "code") {
		return new HtmlControlTagAction(*this, CODE);
	} else if (tag == "cite") {
		return new HtmlControlTagAction(*this, CITE);
	} else if (tag == "sub") {
		return new HtmlControlTagAction(*this, SUB);
	} else if (tag == "sup") {
		return new HtmlControlTagAction(*this, SUP);
	} else if (tag == "h1") {
		return new HtmlHeaderTagAction(*this, H1);
	} else if (tag == "h2") {
		return new HtmlHeaderTagAction(*this, H2);
	} else if (tag == "h3") {
		return new HtmlHeaderTagAction(*this, H3);
	} else if (tag == "h4") {
		return new HtmlHeaderTagAction(*this, H4);
	} else if (tag == "h5") {
		return new HtmlHeaderTagAction(*this, H5);
	} else if (tag == "h6") {
		return new HtmlHeaderTagAction(*this, H6);
	} else if (tag == "head") {
		return new HtmlIgnoreTagAction(*this);
	} else if (tag == "title") {
		return new HtmlIgnoreTagAction(*this);
	} else if (tag == "style") {
		return new HtmlStyleTagAction(*this);
	} else if (tag == "select") {
		return new HtmlIgnoreTagAction(*this);
	} else if (tag == "script") {
		return new HtmlIgnoreTagAction(*this);
	} else if (tag == "a") {
		return new HtmlHrefTagAction(*this);
	} else if (tag == "td") {
		//return new HtmlBreakTagAction(*this, HtmlBreakTagAction::BREAK_AT_END);
	} else if (tag == "tr") {
		return new HtmlBreakTagAction(*this, HtmlBreakTagAction::BREAK_AT_END);
	} else if (tag == "div") {
		return new HtmlBreakTagAction(*this, HtmlBreakTagAction::BREAK_AT_END);
	} else if (tag == "dt") {
		return new HtmlBreakTagAction(*this, HtmlBreakTagAction::BREAK_AT_START);
	} else if (tag == "p") {
		return new HtmlBreakTagAction(*this, HtmlBreakTagAction::BREAK_AT_START_AND_AT_END);
	} else if (tag == "br") {
		return new HtmlBreakTagAction(*this, HtmlBreakTagAction::BREAK_AT_START_AND_AT_END);
	} else if (tag == "blockquote") {
		return new HtmlBreakTagAction(*this, HtmlBreakTagAction::BREAK_AT_START_AND_AT_END);
	} else if (tag == "img") {
		return new HtmlImageTagAction(*this);
	} else if (tag == "ul") {
		return new HtmlListTagAction(*this, 0);
	} else if (tag == "menu") {
		return new HtmlListTagAction(*this, 0);
	} else if (tag == "dir") {
		return new HtmlListTagAction(*this, 0);
	} else if (tag == "ol") {
		return new HtmlListTagAction(*this, 1);
	} else if (tag == "li") {
		return new HtmlListItemTagAction(*this);
	} else if (tag == "pre") {
		if (myProcessPreTag) {
			return new HtmlPreTagAction(*this);
		}
	} else if (tag == "table") {
		return new HtmlTableTagAction(*this);
	}
	/*
	} else if (tag == "dd") {
		return 0;
	} else if (tag == "dl") {
		return 0;
	} else if (tag == "dfn") {
		return 0;
	} else if (tag == "samp") {
		return 0;
	} else if (tag == "kbd") {
		return 0;
	} else if (tag == "var") {
		return 0;
	} else if (tag == "abbr") {
		return 0;
	} else if (tag == "acronym") {
		return 0;
	} else if (tag == "q") {
		return 0;
	} else if (tag == "ins") {
		return 0;
	} else if (tag == "del") {
		return 0;
	} else if (tag == "body") {
		return 0;
	*/
	return new DummyHtmlTagAction(*this);
}

void HtmlBookReader::setBuildTableOfContent(bool build) {
	myBuildTableOfContent = build;
}

void HtmlBookReader::setProcessPreTag(bool process) {
	myProcessPreTag = process;
}

HtmlBookReader::HtmlBookReader(const std::string &baseDirectoryPath, BookModel &model, const PlainTextFormat &format, const std::string &encoding) : HtmlReader(encoding), myBookReader(model), myBaseDirPath(baseDirectoryPath), myFormat(format), myBuildTableOfContent(true), myProcessPreTag(true) {
	myFontMap = new FontMap();
}

HtmlBookReader::~HtmlBookReader() {
}

void HtmlBookReader::addConvertedDataToBuffer(const char *text, std::size_t len, bool convert) {
	if (len > 0) {
		if (myDontBreakParagraph) {
			while (len > 0 && std::isspace(*text)) {
				--len;
				++text;
			}
			if (len == 0) {
				return;
			}
		}
		if (convert) {
			myConverter->convert(myConverterBuffer, text, text + len);
			myBookReader.addData(myConverterBuffer);
			myBookReader.addContentsData(myConverterBuffer);
			myConverterBuffer.erase();
		} else {
			std::string strText(text, len);
			myBookReader.addData(strText);
			myBookReader.addContentsData(strText);
		}
		myDontBreakParagraph = false;
	}
}

void HtmlBookReader::TagData::addEntry(shared_ptr<ZLTextStyleEntry> entry) {
	if (!entry.isNull()) {
		StyleEntries.push_back(entry);
	}
}

bool HtmlBookReader::tagHandler(const HtmlTag &tag) {
	myConverter->reset();

	if (tag.Start) {
		shared_ptr<TagData> tagData = new TagData();
		tagData->addEntry(myStyleSheetTable.control(tag.Name, ""));
		const std::string *cls = tag.find("class");
		if (cls != 0) {
			tagData->addEntry(myStyleSheetTable.control("", *cls));
			tagData->addEntry(myStyleSheetTable.control(tag.Name, *cls));
		}
		myTagDataStack.push_back(tagData);
	} else if (!myTagDataStack.empty()) {
		for (int i = myTagDataStack.back()->StyleEntries.size(); i > 0; --i) {
			myBookReader.addStyleCloseEntry();
		}
		myTagDataStack.pop_back();
	}
	const std::string *id = tag.find("id");
	if (id != 0) {
		myBookReader.addHyperlinkLabel(*id);
	}
	shared_ptr<HtmlTagAction> action = myActionMap[tag.Name];
	if (action.isNull()) {
		action = createAction(tag.Name);
		myActionMap[tag.Name] = action;
	}
	action->run(tag);

	if (tag.Start) {
		for (std::vector<shared_ptr<TagData> >::const_iterator it = myTagDataStack.begin(); it != myTagDataStack.end(); ++it) {
			const unsigned char depth = it - myTagDataStack.begin() + 1;
			const std::vector<shared_ptr<ZLTextStyleEntry> > &entries = (*it)->StyleEntries;
			const bool inheritedOnly = it + 1 != myTagDataStack.end();
			for (std::vector<shared_ptr<ZLTextStyleEntry> >::const_iterator jt = entries.begin(); jt != entries.end(); ++jt) {
				shared_ptr<ZLTextStyleEntry> entry = inheritedOnly ? (*jt)->inherited() : *jt;
				myBookReader.addStyleEntry(*entry, depth);
			}
		}
	}

	return true;
}

void HtmlBookReader::preformattedCharacterDataHandler(const char *text, std::size_t len, bool convert) {
	const char *start = text;
	const char *end = text + len;

	int breakType = myFormat.breakType();
	if (breakType & PlainTextFormat::BREAK_PARAGRAPH_AT_NEW_LINE) {
		for (const char *ptr = text; ptr != end; ++ptr) {
			if (*ptr == '\n') {
				mySpaceCounter = 0;
				if (start < ptr) {
					addConvertedDataToBuffer(start, ptr - start, convert);
				} else {
					static const std::string SPACE = " ";
					myBookReader.addData(SPACE);
				}
				myBookReader.endParagraph();
				myBookReader.beginParagraph();
				start = ptr + 1;
			} else if (mySpaceCounter >= 0) {
				if (std::isspace((unsigned char)*ptr)) {
					++mySpaceCounter;
				} else {
					myBookReader.addFixedHSpace(mySpaceCounter);
					mySpaceCounter = -1;
				}
			}
		}
		addConvertedDataToBuffer(start, end - start, convert);
	} else if (breakType & PlainTextFormat::BREAK_PARAGRAPH_AT_LINE_WITH_INDENT) {
		for (const char *ptr = text; ptr != end; ++ptr) {
			if (std::isspace((unsigned char)*ptr)) {
				if (*ptr == '\n') {
					mySpaceCounter = 0;
				} else if (mySpaceCounter >= 0) {
					++mySpaceCounter;
				}
			} else {
				if (mySpaceCounter > myFormat.ignoredIndent()) {
					if (ptr - start > mySpaceCounter) {
						addConvertedDataToBuffer(start, ptr - start - mySpaceCounter, convert);
						myBookReader.endParagraph();
						myBookReader.beginParagraph();
					}
					start = ptr;
				}
				mySpaceCounter = -1;
			}
		}
		mySpaceCounter = std::max(mySpaceCounter, 0);
		if (end - start > mySpaceCounter) {
			addConvertedDataToBuffer(start, end - start - mySpaceCounter, convert);
		}
	} else if (breakType & PlainTextFormat::BREAK_PARAGRAPH_AT_EMPTY_LINE) {
		for (const char *ptr = start; ptr != end; ++ptr) {
			if (std::isspace((unsigned char)*ptr)) {
				if (*ptr == '\n') {
					++myBreakCounter;
				}
			} else {
				if (myBreakCounter > 1) {
					addConvertedDataToBuffer(start, ptr - start, convert);
					myBookReader.endParagraph();
					myBookReader.beginParagraph();
					start = ptr;
				}
				myBreakCounter = 0;
			}
		}
		addConvertedDataToBuffer(start, end - start, convert);
	}
}

bool HtmlBookReader::characterDataHandler(const char *text, std::size_t len, bool convert) {
	if (!myStyleSheetParser.isNull()) {
		myStyleSheetParser->parseString(text, len);
		return true;
	}

	if (myIgnoreDataCounter != 0) {
		return true;
	}

	if (myIsPreformatted) {
		preformattedCharacterDataHandler(text, len, convert);
		return true;
	}

	const char *ptr = text;
	const char *end = text + len;
	if (!myIsStarted) {
		for (; ptr != end; ++ptr) {
			if (!std::isspace((unsigned char)*ptr)) {
				myIsStarted = true;
				break;
			}
		}
	}
	if (myIsStarted) {
		addConvertedDataToBuffer(ptr, end - ptr, convert);
	}
	return true;
}

void HtmlBookReader::startDocumentHandler() {
	while (!myListNumStack.empty()) {
		myListNumStack.pop();
	}
	myTagDataStack.clear();
	myConverterBuffer.erase();
	myKindList.clear();

	myBookReader.reset();
	myBookReader.setMainTextModel();
	myBookReader.pushKind(REGULAR);
	myBookReader.beginParagraph();
	myIgnoreDataCounter = 0;
	myIsPreformatted = false;
	myDontBreakParagraph = false;
	for (std::map<std::string,shared_ptr<HtmlTagAction> >::const_iterator it = myActionMap.begin(); it != myActionMap.end(); ++it) {
		it->second->reset();
	}
	myIsStarted = false;
	myIgnoreTitles = false;

	myStyleSheetParser = 0;

	mySpaceCounter = -1;
	myBreakCounter = 0;
}

void HtmlBookReader::endDocumentHandler() {
	myBookReader.endParagraph();
}

void HtmlBookReader::setFileName(const std::string fileName) {
	myFileName = fileName;
}

size_t HtmlBookReader::listStackDepth() const  {
	return myListNumStack.size();
}
