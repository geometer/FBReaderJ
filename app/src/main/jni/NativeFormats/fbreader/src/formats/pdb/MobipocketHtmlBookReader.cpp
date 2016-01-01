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

#include <algorithm>

#include <ZLFile.h>
#include <ZLFileImage.h>
#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>

#include "MobipocketHtmlBookReader.h"
#include "PalmDocStream.h"
#include "../html/HtmlTagActions.h"
#include "../../bookmodel/BookModel.h"

class MobipocketHtmlImageTagAction : public HtmlTagAction {

public:
	MobipocketHtmlImageTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class MobipocketHtmlHrTagAction : public HtmlTagAction {

public:
	MobipocketHtmlHrTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class MobipocketHtmlHrefTagAction : public HtmlHrefTagAction {

public:
	MobipocketHtmlHrefTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class MobipocketHtmlGuideTagAction : public HtmlTagAction {

public:
	MobipocketHtmlGuideTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class MobipocketHtmlReferenceTagAction : public HtmlTagAction {

public:
	MobipocketHtmlReferenceTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class MobipocketHtmlPagebreakTagAction : public HtmlTagAction {

public:
	MobipocketHtmlPagebreakTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

MobipocketHtmlImageTagAction::MobipocketHtmlImageTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void MobipocketHtmlImageTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		int index = -1;
		for (unsigned int i = 0; i < tag.Attributes.size() && index < 0; ++i) {
			const std::string aName = tag.Attributes[i].Name;
			if (aName == "recindex") {
				index = ZLStringUtil::parseDecimal(tag.Attributes[i].Value, -1);
			} else if (aName == "src") {
				static const std::string KINDLE_EMBED_PREFIX = "kindle:embed:";
				std::string aValue = tag.Attributes[i].Value;
				if (ZLStringUtil::stringStartsWith(aValue, KINDLE_EMBED_PREFIX)) {
					aValue = aValue.substr(KINDLE_EMBED_PREFIX.length());
					const size_t q = aValue.find('?');
					if (q != std::string::npos) {
						aValue = aValue.substr(0, q);
					}
					index = ZLStringUtil::parseHex(aValue, -1);
				}
			}
		}
		if (index >= 0) {
			((MobipocketHtmlBookReader&)myReader).myImageIndexes.insert(index);
			bool stopParagraph = bookReader().paragraphIsOpen();
			if (stopParagraph) {
				bookReader().endParagraph();
			}
			bookReader().addImageReference(ZLStringUtil::numberToString(index), 0, false);
			if (stopParagraph) {
				bookReader().beginParagraph();
			}
		}
	}
}

MobipocketHtmlHrTagAction::MobipocketHtmlHrTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void MobipocketHtmlHrTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		if (bookReader().contentsParagraphIsOpen()) {
			bookReader().endContentsParagraph();
			bookReader().exitTitle();
		}
		bookReader().insertEndOfSectionParagraph();
	}
}

MobipocketHtmlHrefTagAction::MobipocketHtmlHrefTagAction(HtmlBookReader &reader) : HtmlHrefTagAction(reader) {
}

MobipocketHtmlPagebreakTagAction::MobipocketHtmlPagebreakTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void MobipocketHtmlPagebreakTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (tag.Start) {
		if (bookReader().contentsParagraphIsOpen()) {
			bookReader().endContentsParagraph();
			bookReader().exitTitle();
		}
		bookReader().insertEndOfSectionParagraph();
	}
}

MobipocketHtmlBookReader::TOCReader::TOCReader(MobipocketHtmlBookReader &reader) : myReader(reader) {
	reset();
}

void MobipocketHtmlBookReader::TOCReader::reset() {
	myEntries.clear();

	myIsActive = false;
	myStartOffset = (size_t)-1;
	myEndOffset = (size_t)-1;
	myCurrentEntryText.erase();
}

bool MobipocketHtmlBookReader::TOCReader::rangeContainsPosition(size_t position) {
	return myStartOffset <= position && myEndOffset > position;
}

void MobipocketHtmlBookReader::TOCReader::startReadEntry(size_t position) {
	myCurrentReference = position;
	myIsActive = true;
}

void MobipocketHtmlBookReader::TOCReader::endReadEntry(size_t level) {
	if (myIsActive && !myCurrentEntryText.empty()) {
		std::string converted;
		myReader.myConverter->convert(converted, myCurrentEntryText);
		myReader.myConverter->reset();
		myEntries[myCurrentReference] = Entry(converted, level);
		myCurrentEntryText.erase();
	}
	myIsActive = false;
}

void MobipocketHtmlBookReader::TOCReader::appendText(const char *text, size_t len) {
	if (myIsActive) {
		myCurrentEntryText.append(text, len);
	}
}

MobipocketHtmlBookReader::TOCReader::Entry::Entry() : Level(0) {
}

MobipocketHtmlBookReader::TOCReader::Entry::Entry(const std::string &text, size_t level) : Text(text), Level(level) {
}

void MobipocketHtmlBookReader::TOCReader::addReference(size_t position, const std::string &text, size_t level) {
	myEntries[position] = Entry(text, level);
	if (rangeContainsPosition(position)) {
		setEndOffset(position);
	}
}

void MobipocketHtmlBookReader::TOCReader::setStartOffset(size_t position) {
	myStartOffset = position;
	std::map<size_t,Entry>::const_iterator it = myEntries.lower_bound(position);
	if (it != myEntries.end()) {
		++it;
		if (it != myEntries.end()) {
			myEndOffset = it->first;
		}
	}
}

void MobipocketHtmlBookReader::TOCReader::setEndOffset(size_t position) {
	myEndOffset = position;
}

const std::map<size_t,MobipocketHtmlBookReader::TOCReader::Entry> &MobipocketHtmlBookReader::TOCReader::entries() const {
	return myEntries;
}

void MobipocketHtmlHrefTagAction::run(const HtmlReader::HtmlTag &tag) {
	MobipocketHtmlBookReader &reader = (MobipocketHtmlBookReader&)myReader;
	if (tag.Start) {
		const std::string *filepos = tag.find("filepos");
		if (filepos != 0 && !filepos->empty()) {
			std::string label = "&";
			const int intValue = ZLStringUtil::parseDecimal(*filepos, -1);
			if (intValue > 0) {
				if (reader.myTocReader.rangeContainsPosition(tag.Offset)) {
					reader.myTocReader.startReadEntry(intValue);
					if (reader.myTocReader.rangeContainsPosition(intValue)) {
						reader.myTocReader.setEndOffset(intValue);
					}
				}
				reader.myFileposReferences.insert(intValue);
				ZLStringUtil::appendNumber(label, intValue);
				setHyperlinkType(INTERNAL_HYPERLINK);
				bookReader().addHyperlinkControl(INTERNAL_HYPERLINK, label);
				return;
			}
		}
	} else {
		reader.myTocReader.endReadEntry(reader.listStackDepth());
	}
	HtmlHrefTagAction::run(tag);
}

MobipocketHtmlGuideTagAction::MobipocketHtmlGuideTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void MobipocketHtmlGuideTagAction::run(const HtmlReader::HtmlTag &tag) {
	MobipocketHtmlBookReader &reader = (MobipocketHtmlBookReader&)myReader;
	reader.myInsideGuide = tag.Start;
}

MobipocketHtmlReferenceTagAction::MobipocketHtmlReferenceTagAction(HtmlBookReader &reader) : HtmlTagAction(reader) {
}

void MobipocketHtmlReferenceTagAction::run(const HtmlReader::HtmlTag &tag) {
	if (!tag.Start) {
		return;
	}

	MobipocketHtmlBookReader &reader = (MobipocketHtmlBookReader&)myReader;
	if (reader.myInsideGuide) {
		std::string title;
		std::string filepos;
		bool isTocReference = false;
		for (size_t i = 0; i < tag.Attributes.size(); ++i) {
			const std::string &name = tag.Attributes[i].Name;
			const std::string &value = tag.Attributes[i].Value;
			if (name == "title") {
				title = value;
			} else if (name == "filepos") {
				filepos = value;
			} else if (name == "type" && ZLUnicodeUtil::toLower(value) == "toc") {
				isTocReference = true;
			}
		}
		if (!title.empty() && !filepos.empty()) {
			const int position = ZLStringUtil::parseDecimal(filepos, -1);
			if (position > 0) {
				reader.myTocReader.addReference(position, title, reader.listStackDepth());
				if (isTocReference) {
					reader.myTocReader.setStartOffset(position);
				}
			}
		}
	}
}

shared_ptr<HtmlTagAction> MobipocketHtmlBookReader::createAction(const std::string &tag) {
	if (tag == "img") {
		return new MobipocketHtmlImageTagAction(*this);
	} else if (tag == "hr") {
		return new MobipocketHtmlHrTagAction(*this);
	} else if (tag == "a") {
		return new MobipocketHtmlHrefTagAction(*this);
	} else if (tag == "guide") {
		return new MobipocketHtmlGuideTagAction(*this);
	} else if (tag == "reference") {
		return new MobipocketHtmlReferenceTagAction(*this);
	} else if (tag == "mbp:pagebreak") {
		return new MobipocketHtmlPagebreakTagAction(*this);
	}
	return HtmlBookReader::createAction(tag);
}

void MobipocketHtmlBookReader::startDocumentHandler() {
	HtmlBookReader::startDocumentHandler();
	myInsideGuide = false;
	myFileposReferences.clear();
	myPositionToParagraphMap.clear();
	myTocReader.reset();
}

bool MobipocketHtmlBookReader::tagHandler(const HtmlTag &tag) {
	if (tag.Start) {
		size_t paragraphNumber = myBookReader.model().bookTextModel()->paragraphsNumber();
		if (myBookReader.paragraphIsOpen()) {
			--paragraphNumber;
		}
		myPositionToParagraphMap.push_back(std::make_pair(tag.Offset, paragraphNumber));
	}
	return HtmlBookReader::tagHandler(tag);
}

MobipocketHtmlBookReader::MobipocketHtmlBookReader(const ZLFile &file, BookModel &model, const PlainTextFormat &format, const std::string &encoding) : HtmlBookReader("", model, format, encoding), myFileName(file.path()), myTocReader(*this) {
	setBuildTableOfContent(false);
	setProcessPreTag(false);
}

bool MobipocketHtmlBookReader::characterDataHandler(const char *text, size_t len, bool convert) {
	myTocReader.appendText(text, len);
	return HtmlBookReader::characterDataHandler(text, len, convert);
}

void MobipocketHtmlBookReader::readDocument(ZLInputStream &stream) {
	HtmlBookReader::readDocument(stream);

	PalmDocStream &pdStream = (PalmDocStream&)stream;

	for (std::set<int>::const_iterator it = myImageIndexes.begin(); it != myImageIndexes.end(); ++it) {
		std::pair<int,int> imageLocation = pdStream.imageLocation(pdStream.header(), *it - 1);
		if (imageLocation.first > 0 && imageLocation.second > 0) {
			myBookReader.addImage(
				ZLStringUtil::numberToString(*it),
				new ZLFileImage(ZLFile(myFileName), "", imageLocation.first, imageLocation.second)
			);
		}
	}

	std::vector<std::pair<size_t,size_t> >::const_iterator jt = myPositionToParagraphMap.begin();
	for (std::set<size_t>::const_iterator it = myFileposReferences.begin(); it != myFileposReferences.end(); ++it) {
		while (jt != myPositionToParagraphMap.end() && jt->first < *it) {
			++jt;
		}
		if (jt == myPositionToParagraphMap.end()) {
			break;
		}
		std::string label = "&";
		ZLStringUtil::appendNumber(label, *it);
		myBookReader.addHyperlinkLabel(label, jt->second);
	}

	jt = myPositionToParagraphMap.begin();
	const std::map<size_t,TOCReader::Entry> &entries = myTocReader.entries();
	int level = 0;
	for (std::map<size_t,TOCReader::Entry>::const_iterator it = entries.begin(); it != entries.end(); ++it) {
		while (jt != myPositionToParagraphMap.end() && jt->first < it->first) {
			++jt;
		}
		if (jt == myPositionToParagraphMap.end()) {
			break;
		}
		for (; level >= (int)it->second.Level; --level) {
			myBookReader.endContentsParagraph();
		}
		myBookReader.beginContentsParagraph(jt->second);
		myBookReader.addContentsData(it->second.Text);
		level = it->second.Level;
	}
	for (; level >= 0; --level) {
		myBookReader.endContentsParagraph();
	}
}
