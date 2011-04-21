/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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

#include <ZLImage.h>
#include <ZLLogger.h>

#include "BookReader.h"
#include "BookModel.h"

#include "../library/Book.h"

BookReader::BookReader(BookModel &model) : myModel(model) {
	myCurrentTextModel = 0;
	myLastTOCParagraphIsEmpty = false;

	myTextParagraphExists = false;
	myContentsParagraphExists = false;

	myInsideTitle = false;
	mySectionContainsRegularContents = false;
}

BookReader::~BookReader() {
}

void BookReader::setMainTextModel() {
	myCurrentTextModel = myModel.myBookTextModel;
}

void BookReader::setFootnoteTextModel(const std::string &id) {
	std::map<std::string,shared_ptr<ZLTextModel> >::iterator it = myModel.myFootnotes.find(id);
	if (it != myModel.myFootnotes.end()) {
		myCurrentTextModel = (*it).second;
	} else {
		myCurrentTextModel = new ZLTextPlainModel(myModel.myBookTextModel->language(), 8192);
		myModel.myFootnotes.insert(std::make_pair(id, myCurrentTextModel));
	}
}

void BookReader::unsetTextModel() {
	myCurrentTextModel = 0;
}

void BookReader::pushKind(FBTextKind kind) {
	myKindStack.push_back(kind);
}

bool BookReader::popKind() {
	if (!myKindStack.empty()) {
		myKindStack.pop_back();
		return true;
	}
	return false;
}

bool BookReader::isKindStackEmpty() const {
	return myKindStack.empty();
}

void BookReader::beginParagraph(ZLTextParagraph::Kind kind) {
	if (myCurrentTextModel != 0) {
		((ZLTextPlainModel&)*myCurrentTextModel).createParagraph(kind);
		for (std::vector<FBTextKind>::const_iterator it = myKindStack.begin(); it != myKindStack.end(); ++it) {
			myCurrentTextModel->addControl(*it, true);
		}
		if (!myHyperlinkReference.empty()) {
			myCurrentTextModel->addHyperlinkControl(myHyperlinkKind, myHyperlinkReference, myHyperlinkType);
		}
		myTextParagraphExists = true;
	}
}

void BookReader::endParagraph() {
	if (myTextParagraphExists) {
		flushTextBufferToParagraph();
		myTextParagraphExists = false;
	}
}

void BookReader::addControl(FBTextKind kind, bool start) {
	if (myTextParagraphExists) {
		flushTextBufferToParagraph();
		myCurrentTextModel->addControl(kind, start);
	}
	if (!start && !myHyperlinkReference.empty() && (kind == myHyperlinkKind)) {
		myHyperlinkReference.erase();
	}
}

void BookReader::addFixedHSpace(unsigned char length) {
	if (myTextParagraphExists) {
		myCurrentTextModel->addFixedHSpace(length);
	}
}

void BookReader::addControl(const ZLTextStyleEntry &entry) {
	if (myTextParagraphExists) {
		flushTextBufferToParagraph();
		myCurrentTextModel->addControl(entry);
	}
}

void BookReader::addHyperlinkControl(FBTextKind kind, const std::string &label) {
	myHyperlinkKind = kind;
	switch (myHyperlinkKind) {
		case INTERNAL_HYPERLINK:
		case FOOTNOTE:
			myHyperlinkType = "internal";
			break;
		case EXTERNAL_HYPERLINK:
			myHyperlinkType = "external";
			break;
		case BOOK_HYPERLINK:
			myHyperlinkType = "book";
			break;
		default:
			myHyperlinkType.erase();
			break;
	}
	ZLLogger::Instance().println(
		"hyperlink",
		" + control (" + myHyperlinkType + "): " + label
	);
	if (myTextParagraphExists) {
		flushTextBufferToParagraph();
		myCurrentTextModel->addHyperlinkControl(kind, label, myHyperlinkType);
	}
	myHyperlinkReference = label;
}

void BookReader::addHyperlinkLabel(const std::string &label) {
	if (!myCurrentTextModel.isNull()) {
		int paragraphNumber = myCurrentTextModel->paragraphsNumber();
		if (myTextParagraphExists) {
			--paragraphNumber;
		}
		addHyperlinkLabel(label, paragraphNumber);
	}
}

void BookReader::addHyperlinkLabel(const std::string &label, int paragraphNumber) {
	ZLLogger::Instance().println(
		"hyperlink",
		" + label: " + label
	);
	myModel.myInternalHyperlinks.insert(std::make_pair(
		label, BookModel::Label(myCurrentTextModel, paragraphNumber)
	));
}

void BookReader::addData(const std::string &data) {
	if (!data.empty() && myTextParagraphExists) {
		if (!myInsideTitle) {
			mySectionContainsRegularContents = true;
		}
		myBuffer.push_back(data);
	}
}

void BookReader::addContentsData(const std::string &data) {
	if (!data.empty() && !myTOCStack.empty()) {
		myContentsBuffer.push_back(data);
	}
}

void BookReader::flushTextBufferToParagraph() {
	myCurrentTextModel->addText(myBuffer);
	myBuffer.clear();
}

void BookReader::addImage(const std::string &id, shared_ptr<const ZLImage> image) {
	myModel.myImages[id] = image;
}

void BookReader::insertEndParagraph(ZLTextParagraph::Kind kind) {
	if ((myCurrentTextModel != 0) && mySectionContainsRegularContents) {
		size_t size = myCurrentTextModel->paragraphsNumber();
		if ((size > 0) && (((*myCurrentTextModel)[(size_t)-1])->kind() != kind)) {
			((ZLTextPlainModel&)*myCurrentTextModel).createParagraph(kind);
			mySectionContainsRegularContents = false;
		}
	}
}

void BookReader::insertEndOfSectionParagraph() {
	insertEndParagraph(ZLTextParagraph::END_OF_SECTION_PARAGRAPH);
}

void BookReader::insertEndOfTextParagraph() {
	insertEndParagraph(ZLTextParagraph::END_OF_TEXT_PARAGRAPH);
}

void BookReader::addImageReference(const std::string &id, short vOffset) {
	if (myCurrentTextModel != 0) {
		mySectionContainsRegularContents = true;
		if (myTextParagraphExists) {
			flushTextBufferToParagraph();
			myCurrentTextModel->addImage(id, myModel.imageMap(), vOffset);
		} else {
			beginParagraph();
			myCurrentTextModel->addControl(IMAGE, true);
			myCurrentTextModel->addImage(id, myModel.imageMap(), vOffset);
			myCurrentTextModel->addControl(IMAGE, false);
			endParagraph();
		}
	}
}

void BookReader::beginContentsParagraph(int referenceNumber) {
	if (myCurrentTextModel == myModel.myBookTextModel) {
		ContentsModel &contentsModel = (ContentsModel&)*myModel.myContentsModel;
		if (referenceNumber == -1) {
			referenceNumber = myCurrentTextModel->paragraphsNumber();
		}
		ZLTextTreeParagraph *peek = myTOCStack.empty() ? 0 : myTOCStack.top();
		if (!myContentsBuffer.empty()) {
			contentsModel.addText(myContentsBuffer);
			myContentsBuffer.clear();
			myLastTOCParagraphIsEmpty = false;
		}
		if (myLastTOCParagraphIsEmpty) {
			contentsModel.addText("...");
		}
		ZLTextTreeParagraph *para = contentsModel.createParagraph(peek);
		contentsModel.addControl(CONTENTS_TABLE_ENTRY, true);
		contentsModel.setReference(para, referenceNumber);
		myTOCStack.push(para);
		myLastTOCParagraphIsEmpty = true;
		myContentsParagraphExists = true;
	}
}

void BookReader::endContentsParagraph() {
	if (!myTOCStack.empty()) {
		ContentsModel &contentsModel = (ContentsModel&)*myModel.myContentsModel;
		if (!myContentsBuffer.empty()) {
			contentsModel.addText(myContentsBuffer);
			myContentsBuffer.clear();
			myLastTOCParagraphIsEmpty = false;
		}
		if (myLastTOCParagraphIsEmpty) {
			contentsModel.addText("...");
			myLastTOCParagraphIsEmpty = false;
		}
		myTOCStack.pop();
	}
	myContentsParagraphExists = false;
}

void BookReader::setReference(size_t contentsParagraphNumber, int referenceNumber) {
	ContentsModel &contentsModel = (ContentsModel&)*myModel.myContentsModel;
	if (contentsParagraphNumber >= contentsModel.paragraphsNumber()) {
		return;
	}
	contentsModel.setReference((const ZLTextTreeParagraph*)contentsModel[contentsParagraphNumber], referenceNumber);
}

void BookReader::reset() {
	myKindStack.clear();
}
