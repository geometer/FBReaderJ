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
#include <algorithm>

//#include <ZLOptions.h>
//#include <ZLOptionsDialog.h>
//#include <ZLOptionEntry.h>
#include <ZLFile.h>

#include "PlainTextFormat.h"

//#include "../../options/FBCategoryKey.h"

const std::string OPTION_Initialized = "Initialized";
const std::string OPTION_BreakType = "BreakType";
const std::string OPTION_IgnoredIndent = "IgnoredIndent";
const std::string OPTION_EmptyLinesBeforeNewSection = "EmptyLinesBeforeNewSection";
const std::string OPTION_CreateContentsTable = "CreateContentsTable";

PlainTextFormat::PlainTextFormat(const ZLFile &file) :
//	InitializedOption(FBCategoryKey::BOOKS, file.path(), OPTION_Initialized, false),
//	BreakTypeOption(FBCategoryKey::BOOKS, file.path(), OPTION_BreakType, 1),
//	IgnoredIndentOption(FBCategoryKey::BOOKS, file.path(), OPTION_IgnoredIndent, 1, 100, 1),
//	EmptyLinesBeforeNewSectionOption(FBCategoryKey::BOOKS, file.path(), OPTION_EmptyLinesBeforeNewSection, 1, 100, 1),
//	CreateContentsTableOption(FBCategoryKey::BOOKS, file.path(), OPTION_CreateContentsTable, false) {
	myInitialized(false),
	myBreakType(1),
	myIgnoredIndent(1),
	myEmptyLinesBeforeNewSection(1),
	myCreateContentsTable(false) {
}

/*PlainTextInfoPage::PlainTextInfoPage(ZLOptionsDialog &dialog, const ZLFile &file, const ZLResourceKey &key, bool showContentsEntry) : myFormat(file) {
	if (!myFormat.initialized()) {
		PlainTextFormatDetector detector;
		shared_ptr<ZLInputStream> stream = file.inputStream();
		if (!stream.isNull()) {
			detector.detect(*stream, myFormat);
		}
	}

	ZLDialogContent &tab = dialog.createTab(key);

	BreakTypeOptionEntry *breakEntry = new BreakTypeOptionEntry(*this, myFormat.BreakTypeOption);
	myIgnoredIndentEntry = new ZLSimpleSpinOptionEntry(myFormat.IgnoredIndentOption, 1);
	tab.addOption(ZLResourceKey("breakType"), breakEntry);
	tab.addOption(ZLResourceKey("ignoreIndent"), myIgnoredIndentEntry);
	breakEntry->onValueSelected(breakEntry->initialIndex());

	if (showContentsEntry) {
		CreateContentsTableOptionEntry *contentsTableEntry = new CreateContentsTableOptionEntry(*this, myFormat.CreateContentsTableOption);
		myEmptyLinesBeforeNewSectionEntry = new ZLSimpleSpinOptionEntry(myFormat.EmptyLinesBeforeNewSectionOption, 1);
		tab.addOption(ZLResourceKey("buildTOC"), contentsTableEntry);
		tab.addOption(ZLResourceKey("emptyLines"), myEmptyLinesBeforeNewSectionEntry);
		contentsTableEntry->onStateChanged(contentsTableEntry->initialState());
	}
}*/

/*PlainTextInfoPage::~PlainTextInfoPage() {
}*/

const int BUFFER_SIZE = 4096;

void PlainTextFormatDetector::detect(ZLInputStream &stream, PlainTextFormat &format) {
	if (!stream.open()) {
		return;
	}

	const unsigned int tableSize = 10;

	unsigned int lineCounter = 0;
	int emptyLineCounter = -1;
	unsigned int stringsWithLengthLessThan81Counter = 0;
	unsigned int stringIndentTable[tableSize] = { 0 };
	unsigned int emptyLinesTable[tableSize] = { 0 };
	unsigned int emptyLinesBeforeShortStringTable[tableSize] = { 0 };

	bool currentLineIsEmpty = true;
	unsigned int currentLineLength = 0;
	unsigned int currentLineIndent = 0;
	int currentNumberOfEmptyLines = -1;

	char *buffer = new char[BUFFER_SIZE];
	int length;
	char previous = 0;
	do {
		length = stream.read(buffer, BUFFER_SIZE);
		const char *end = buffer + length;
		for (const char *ptr = buffer; ptr != end; ++ptr) {
			++currentLineLength;
			if (*ptr == '\n') {
				++lineCounter;
				if (currentLineIsEmpty) {
					++emptyLineCounter;
					++currentNumberOfEmptyLines;
				} else {
					if (currentNumberOfEmptyLines >= 0) {
						int index = std::min(currentNumberOfEmptyLines, (int)tableSize - 1);
						emptyLinesTable[index]++;
						if (currentLineLength < 51) {
							emptyLinesBeforeShortStringTable[index]++;
						}
					}
					currentNumberOfEmptyLines = -1;
				}
				if (currentLineLength < 81) {
					++stringsWithLengthLessThan81Counter;
				}
				if (!currentLineIsEmpty) {
					stringIndentTable[std::min(currentLineIndent, tableSize - 1)]++;
				}

				currentLineIsEmpty = true;
				currentLineLength = 0;
				currentLineIndent = 0;
			} else if (*ptr == '\r') {
				continue;
			} else if (std::isspace((unsigned char)*ptr)) {
				if (currentLineIsEmpty) {
					++currentLineIndent;
				}
			} else {
				currentLineIsEmpty = false;
			}
			previous = *ptr;
		}
	} while (length == BUFFER_SIZE);
	delete[] buffer;

	unsigned int nonEmptyLineCounter = lineCounter - emptyLineCounter;

	{
		unsigned int indent = 0;
		unsigned int lineWithIndent = 0;
		for (; indent < tableSize; ++indent) {
			lineWithIndent += stringIndentTable[indent];
			if (lineWithIndent > 0.1 * nonEmptyLineCounter) {
				break;
			}
		}
		format.myIgnoredIndent = (indent + 1);
	}

	{
		int breakType = 0;
		breakType |= PlainTextFormat::BREAK_PARAGRAPH_AT_EMPTY_LINE;
		if (stringsWithLengthLessThan81Counter < 0.3 * nonEmptyLineCounter) {
			breakType |= PlainTextFormat::BREAK_PARAGRAPH_AT_NEW_LINE;
		} else {
			breakType |= PlainTextFormat::BREAK_PARAGRAPH_AT_LINE_WITH_INDENT;
		}
		format.myBreakType = (breakType);
	}

	{
		unsigned int max = 0;
		unsigned index;
		int emptyLinesBeforeNewSection = -1;
		for (index = 2; index < tableSize; ++index) {
			if (max < emptyLinesBeforeShortStringTable[index]) {
				max = emptyLinesBeforeShortStringTable[index];
				emptyLinesBeforeNewSection = index;
			}
		}
		if (emptyLinesBeforeNewSection > 0) {
			for (index = tableSize - 1; index > 0; --index) {
				emptyLinesTable[index - 1] += emptyLinesTable[index];
				emptyLinesBeforeShortStringTable[index - 1] += emptyLinesBeforeShortStringTable[index];
			}
			for (index = emptyLinesBeforeNewSection; index < tableSize; ++index) {
				if ((emptyLinesBeforeShortStringTable[index] > 2) &&
						(emptyLinesBeforeShortStringTable[index] > 0.7 * emptyLinesTable[index])) {
					break;
				}
			}
			emptyLinesBeforeNewSection = (index == tableSize) ? -1 : (int)index;
		}
		format.myEmptyLinesBeforeNewSection = (emptyLinesBeforeNewSection);
		format.myCreateContentsTable = (emptyLinesBeforeNewSection > 0);
	}

	format.myInitialized = (true);
}

/*BreakTypeOptionEntry::BreakTypeOptionEntry(PlainTextInfoPage &page, ZLIntegerOption &breakTypeOption) : myPage(page), myBreakTypeOption(breakTypeOption) {
}

BreakTypeOptionEntry::~BreakTypeOptionEntry() {
}

static std::vector<std::string> BREAK_TYPE_VALUES_VECTOR;

int BreakTypeOptionEntry::initialIndex() const {
	switch (myBreakTypeOption.value()) {
		case PlainTextFormat::BREAK_PARAGRAPH_AT_NEW_LINE:
			return 0;
		case PlainTextFormat::BREAK_PARAGRAPH_AT_EMPTY_LINE:
			return 1;
		case PlainTextFormat::BREAK_PARAGRAPH_AT_EMPTY_LINE | PlainTextFormat::BREAK_PARAGRAPH_AT_LINE_WITH_INDENT:
		default:
			return 2;
	}
}

const std::string &BreakTypeOptionEntry::initialValue() const {
	return values()[initialIndex()];
}

const std::vector<std::string> &BreakTypeOptionEntry::values() const {
	if (BREAK_TYPE_VALUES_VECTOR.empty()) {
		BREAK_TYPE_VALUES_VECTOR.push_back("New Line");
		BREAK_TYPE_VALUES_VECTOR.push_back("Empty Line");
		BREAK_TYPE_VALUES_VECTOR.push_back("Line With Indent");
	}
	return BREAK_TYPE_VALUES_VECTOR;
}

void BreakTypeOptionEntry::onAccept(const std::string &value) {
	if (value == values()[0]) {
		myBreakTypeOption.setValue(PlainTextFormat::BREAK_PARAGRAPH_AT_NEW_LINE);
	} else if (value == values()[1]) {
		myBreakTypeOption.setValue(PlainTextFormat::BREAK_PARAGRAPH_AT_EMPTY_LINE);
	} else if (value == values()[2]) {
		myBreakTypeOption.setValue(PlainTextFormat::BREAK_PARAGRAPH_AT_EMPTY_LINE | PlainTextFormat::BREAK_PARAGRAPH_AT_LINE_WITH_INDENT);
	}
}

void BreakTypeOptionEntry::onValueSelected(int index) {
	myPage.myIgnoredIndentEntry->setVisible(index == 2);
}*/

/*CreateContentsTableOptionEntry::CreateContentsTableOptionEntry(PlainTextInfoPage &page, ZLBooleanOption &option) : ZLSimpleBooleanOptionEntry(option), myPage(page) {
}

CreateContentsTableOptionEntry::~CreateContentsTableOptionEntry() {
}

void CreateContentsTableOptionEntry::onStateChanged(bool state) {
	myPage.myEmptyLinesBeforeNewSectionEntry->setVisible(state);
}*/
