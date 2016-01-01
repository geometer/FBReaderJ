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

#include "TxtBookReader.h"
#include "../../bookmodel/BookModel.h"

TxtBookReader::TxtBookReader(BookModel &model, const PlainTextFormat &format, const std::string &encoding) : TxtReader(encoding), BookReader(model), myFormat(format) {
}

void TxtBookReader::internalEndParagraph() {
	if (!myLastLineIsEmpty) {
		//myLineFeedCounter = 0;
		myLineFeedCounter = -1; /* Fixed by Hatred: zero value was break LINE INDENT formater -
		                           second line print with indent like new paragraf */
	}
	myLastLineIsEmpty = true;
	endParagraph();
}

bool TxtBookReader::characterDataHandler(std::string &str) {
	const char *ptr = str.data();
	const char *end = ptr + str.length();
	for (; ptr != end; ++ptr) {
		if (std::isspace((unsigned char)*ptr)) {
			if (*ptr != '\t') {
				++mySpaceCounter;
			} else {
				mySpaceCounter += myFormat.ignoredIndent() + 1; // TODO: implement single option in PlainTextFormat
			}
		} else {
			myLastLineIsEmpty = false;
			break;
		}
	}
	if (ptr != end) {
		if ((myFormat.breakType() & PlainTextFormat::BREAK_PARAGRAPH_AT_LINE_WITH_INDENT) &&
				myNewLine && (mySpaceCounter > myFormat.ignoredIndent())) {
			internalEndParagraph();
			beginParagraph();
		}
		addData(str);
		if (myInsideContentsParagraph) {
			addContentsData(str);
		}
		myNewLine = false;
	}
	return true;
}

bool TxtBookReader::newLineHandler() {
	if (!myLastLineIsEmpty) {
		myLineFeedCounter = -1;
	}
	myLastLineIsEmpty = true;
	++myLineFeedCounter;
	myNewLine = true;
	mySpaceCounter = 0;
	bool paragraphBreak =
		(myFormat.breakType() & PlainTextFormat::BREAK_PARAGRAPH_AT_NEW_LINE) ||
		((myFormat.breakType() & PlainTextFormat::BREAK_PARAGRAPH_AT_EMPTY_LINE) && (myLineFeedCounter > 0));

	if (myFormat.createContentsTable()) {
//		if (!myInsideContentsParagraph && (myLineFeedCounter == myFormat.emptyLinesBeforeNewSection() + 1)) {
			/* Fixed by Hatred: remove '+ 1' for emptyLinesBeforeNewSection, it looks like very strange
				 when we should point count of empty string decrised by 1 in settings dialog */
		if (!myInsideContentsParagraph && (myLineFeedCounter == myFormat.emptyLinesBeforeNewSection())) {
			myInsideContentsParagraph = true;
			internalEndParagraph();
			insertEndOfSectionParagraph();
			beginContentsParagraph();
			enterTitle();
			pushKind(SECTION_TITLE);
			beginParagraph();
			paragraphBreak = false;
		}
		if (myInsideContentsParagraph && (myLineFeedCounter == 1)) {
			exitTitle();
			endContentsParagraph();
			popKind();
			myInsideContentsParagraph = false;
			paragraphBreak = true;
		}
	}

	if (paragraphBreak) {
		internalEndParagraph();
		beginParagraph();
	}
	return true;
}

void TxtBookReader::startDocumentHandler() {
	setMainTextModel();
	pushKind(REGULAR);
	beginParagraph();
	myLineFeedCounter = 0;
	myInsideContentsParagraph = false;
	enterTitle();
	myLastLineIsEmpty = true;
	myNewLine = true;
	mySpaceCounter = 0;
}

void TxtBookReader::endDocumentHandler() {
	internalEndParagraph();
}
