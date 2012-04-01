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

#include <cstdint>
#include <cstring>
#include <algorithm>

#include <ZLibrary.h>
//#include <ZLSearchUtil.h>
//#include <ZLLanguageUtil.h>
#include <ZLUnicodeUtil.h>

#include "ZLTextModel.h"
#include "ZLTextParagraph.h"

ZLTextModel::ZLTextModel(const std::string &id, const std::string &language, const size_t rowSize,
		const std::string &directoryName, const std::string &fileExtension) :
	myId(id),
	myLanguage(language.empty() ? ZLibrary::Language() : language),
	myAllocator(new ZLCachedMemoryAllocator(rowSize, directoryName, fileExtension)),
	myLastEntryStart(0) {
}

ZLTextModel::ZLTextModel(const std::string &id, const std::string &language, shared_ptr<ZLCachedMemoryAllocator> allocator) :
	myId(id),
	myLanguage(language.empty() ? ZLibrary::Language() : language),
	myAllocator(allocator),
	myLastEntryStart(0) {
}

ZLTextModel::~ZLTextModel() {
	for (std::vector<ZLTextParagraph*>::const_iterator it = myParagraphs.begin(); it != myParagraphs.end(); ++it) {
		delete *it;
	}
}

/*
bool ZLTextModel::isRtl() const {
	return ZLLanguageUtil::isRTLLanguage(myLanguage);
}

void ZLTextModel::search(const std::string &text, size_t startIndex, size_t endIndex, bool ignoreCase) const {
	ZLSearchPattern pattern(text, ignoreCase);
	myMarks.clear();

	std::vector<ZLTextParagraph*>::const_iterator start =
		(startIndex < myParagraphs.size()) ? myParagraphs.begin() + startIndex : myParagraphs.end();
	std::vector<ZLTextParagraph*>::const_iterator end =
		(endIndex < myParagraphs.size()) ? myParagraphs.begin() + endIndex : myParagraphs.end();
	for (std::vector<ZLTextParagraph*>::const_iterator it = start; it < end; ++it) {
		int offset = 0;
		for (ZLTextParagraph::Iterator jt = **it; !jt.isEnd(); jt.next()) {
			if (jt.entryKind() == ZLTextParagraphEntry::TEXT_ENTRY) {
				const ZLTextEntry& textEntry = (ZLTextEntry&)*jt.entry();
				const char *str = textEntry.data();
				const size_t len = textEntry.dataLength();
				for (int pos = ZLSearchUtil::find(str, len, pattern); pos != -1; pos = ZLSearchUtil::find(str, len, pattern, pos + 1)) {
					myMarks.push_back(ZLTextMark(it - myParagraphs.begin(), offset + pos, pattern.length()));
				}
				offset += len;
			}
		}
	}
}

void ZLTextModel::selectParagraph(size_t index) const {
	if (index < paragraphsNumber()) {
		myMarks.push_back(ZLTextMark(index, 0, (*this)[index]->textDataLength()));
	}
}

ZLTextMark ZLTextModel::firstMark() const {
	return marks().empty() ? ZLTextMark() : marks().front();
}

ZLTextMark ZLTextModel::lastMark() const {
	return marks().empty() ? ZLTextMark() : marks().back();
}

ZLTextMark ZLTextModel::nextMark(ZLTextMark position) const {
	std::vector<ZLTextMark>::const_iterator it = std::upper_bound(marks().begin(), marks().end(), position);
	return (it != marks().end()) ? *it : ZLTextMark();
}

ZLTextMark ZLTextModel::previousMark(ZLTextMark position) const {
	if (marks().empty()) {
		return ZLTextMark();
	}
	std::vector<ZLTextMark>::const_iterator it = std::lower_bound(marks().begin(), marks().end(), position);
	if (it == marks().end()) {
		--it;
	}
	if (*it >= position) {
		if (it == marks().begin()) {
			return ZLTextMark();
		}
		--it;
	}
	return *it;
}
*/

void ZLTextModel::addParagraphInternal(ZLTextParagraph *paragraph) {
	const size_t dataSize = myAllocator->blocksNumber();
	const size_t bytesOffset = myAllocator->currentBytesOffset();

	myStartEntryIndices.push_back((dataSize == 0) ? 0 : (dataSize - 1));
	myStartEntryOffsets.push_back(bytesOffset / 2); // offset in words for future use in Java
	myParagraphLengths.push_back(0);
	myTextSizes.push_back(myTextSizes.empty() ? 0 : myTextSizes.back());
	myParagraphKinds.push_back(paragraph->kind());

	myParagraphs.push_back(paragraph);
	myLastEntryStart = 0;
}

ZLTextTreeModel::ZLTextTreeModel(const std::string &id, const std::string &language,
		const std::string &directoryName, const std::string &fileExtension) :
	ZLTextModel(id, language, 8192, directoryName, fileExtension) {
	myRoot = new ZLTextTreeParagraph();
	myRoot->open(true);
}

ZLTextTreeModel::~ZLTextTreeModel() {
	delete myRoot;
}

ZLTextTreeParagraph *ZLTextTreeModel::createParagraph(ZLTextTreeParagraph *parent) {
	if (parent == 0) {
		parent = myRoot;
	}
	ZLTextTreeParagraph *tp = new ZLTextTreeParagraph(parent);
	addParagraphInternal(tp);
	return tp;
}

/*
void ZLTextTreeModel::search(const std::string &text, size_t startIndex, size_t endIndex, bool ignoreCase) const {
	ZLTextModel::search(text, startIndex, endIndex, ignoreCase);
	for (std::vector<ZLTextMark>::const_iterator it = marks().begin(); it != marks().end(); ++it) {
		((ZLTextTreeParagraph*)(*this)[it->ParagraphIndex])->openTree();
	}
}

void ZLTextTreeModel::selectParagraph(size_t index) const {
	if (index < paragraphsNumber()) {
		ZLTextModel::selectParagraph(index);
		((ZLTextTreeParagraph*)(*this)[index])->openTree();
	}
}
*/

ZLTextPlainModel::ZLTextPlainModel(const std::string &id, const std::string &language, const size_t rowSize,
		const std::string &directoryName, const std::string &fileExtension) :
	ZLTextModel(id, language, rowSize, directoryName, fileExtension) {
}

ZLTextPlainModel::ZLTextPlainModel(const std::string &id, const std::string &language, shared_ptr<ZLCachedMemoryAllocator> allocator) :
	ZLTextModel(id, language, allocator) {
}

void ZLTextPlainModel::createParagraph(ZLTextParagraph::Kind kind) {
	ZLTextParagraph *paragraph = (kind == ZLTextParagraph::TEXT_PARAGRAPH) ? new ZLTextParagraph() : new ZLTextSpecialParagraph(kind);
	addParagraphInternal(paragraph);
}

void ZLTextModel::addText(const std::string &text) {
	ZLUnicodeUtil::Ucs2String ucs2str;
	ZLUnicodeUtil::utf8ToUcs2(ucs2str, text);
	const size_t len = ucs2str.size();

	if (myLastEntryStart != 0 && *myLastEntryStart == ZLTextParagraphEntry::TEXT_ENTRY) {
		const size_t oldLen = ZLCachedMemoryAllocator::readUInt32(myLastEntryStart + 2);
		const size_t newLen = oldLen + len;
		myLastEntryStart = myAllocator->reallocateLast(myLastEntryStart, 2 * newLen + 6);
		ZLCachedMemoryAllocator::writeUInt32(myLastEntryStart + 2, newLen);
		memcpy(myLastEntryStart + 6 + oldLen, &ucs2str.front(), 2 * newLen);
	} else {
		myLastEntryStart = myAllocator->allocate(2 * len + 6);
		*myLastEntryStart = ZLTextParagraphEntry::TEXT_ENTRY;
		*(myLastEntryStart + 1) = 0;
		ZLCachedMemoryAllocator::writeUInt32(myLastEntryStart + 2, len);
		memcpy(myLastEntryStart + 6, &ucs2str.front(), 2 * len);
		myParagraphs.back()->addEntry(myLastEntryStart);
		++myParagraphLengths.back();
	}
	myTextSizes.back() += len;
}

void ZLTextModel::addText(const std::vector<std::string> &text) {
	if (text.size() == 0) {
		return;
	}
	size_t fullLength = 0;
	for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
		fullLength += ZLUnicodeUtil::utf8Length(*it);
	}

	ZLUnicodeUtil::Ucs2String ucs2str;
	if (myLastEntryStart != 0 && *myLastEntryStart == ZLTextParagraphEntry::TEXT_ENTRY) {
		const size_t oldLen = ZLCachedMemoryAllocator::readUInt32(myLastEntryStart + 2);
		const size_t newLen = oldLen + fullLength;
		myLastEntryStart = myAllocator->reallocateLast(myLastEntryStart, 2 * newLen + 6);
		ZLCachedMemoryAllocator::writeUInt32(myLastEntryStart + 2, newLen);
		size_t offset = 6 + oldLen;
		for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
			ZLUnicodeUtil::utf8ToUcs2(ucs2str, *it);
			const size_t len = 2 * ucs2str.size();
			memcpy(myLastEntryStart + offset, &ucs2str.front(), len);
			offset += len;
			ucs2str.clear();
		}
	} else {
		myLastEntryStart = myAllocator->allocate(2 * fullLength + 6);
		*myLastEntryStart = ZLTextParagraphEntry::TEXT_ENTRY;
		*(myLastEntryStart + 1) = 0;
		ZLCachedMemoryAllocator::writeUInt32(myLastEntryStart + 2, fullLength);
		size_t offset = 6;
		for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
			ZLUnicodeUtil::utf8ToUcs2(ucs2str, *it);
			const size_t len = 2 * ucs2str.size();
			memcpy(myLastEntryStart + offset, &ucs2str.front(), len);
			offset += len;
			ucs2str.clear();
		}
		myParagraphs.back()->addEntry(myLastEntryStart);
		++myParagraphLengths.back();
	}
	myTextSizes.back() += fullLength;
}

void ZLTextModel::addFixedHSpace(unsigned char length) {
	myLastEntryStart = myAllocator->allocate(4);
	*myLastEntryStart = ZLTextParagraphEntry::FIXED_HSPACE_ENTRY;
	*(myLastEntryStart + 1) = 0;
	*(myLastEntryStart + 2) = length;
	*(myLastEntryStart + 3) = 0;
	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::addControl(ZLTextKind textKind, bool isStart) {
	myLastEntryStart = myAllocator->allocate(4);
	*myLastEntryStart = ZLTextParagraphEntry::CONTROL_ENTRY;
	*(myLastEntryStart + 1) = 0;
	*(myLastEntryStart + 2) = textKind;
	*(myLastEntryStart + 3) = isStart ? 1 : 0;
	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::addControl(const ZLTextStyleEntry &entry) {
	/*
	size_t len = 10 + 2 * (ZLTextStyleEntry::NUMBER_OF_LENGTHS +
			(ZLTextStyleEntry::NUMBER_OF_LENGTHS + 1) / 2);

	ZLUnicodeUtil::Ucs2String fontFamily;
	size_t fontFamilyLen = 0;
	if (entry.fontFamilySupported()) {
		ZLUnicodeUtil::utf8ToUcs2(fontFamily, entry.fontFamily());
		fontFamilyLen = fontFamily.size() * 2;
		len += 2 + fontFamilyLen;
	}

	myLastEntryStart = myAllocator->allocate(len);
	char *address = myLastEntryStart;

	*address++ = ZLTextParagraphEntry::STYLE_ENTRY;
	*address++ = 0;

	ZLCachedMemoryAllocator::writeUInt32(address, entry.myMask);
	address += 4;*/

	// Pack myLengths array so:
	//
	//  1) for every two elements there is a word with those Units (two Units
	//     in two bytes) followed by two words with those Sizes;
	//
	//  2) if there is one last element (without a pair) => then another one
	//     word is appended with only one Unit (in the first byte) followed by
	//     a word containing corresponding Size.
	//
	/*const int lengthMinusOne = ZLTextStyleEntry::NUMBER_OF_LENGTHS - 1;
	for (int i = 0; i < lengthMinusOne; i += 2) {
		const ZLTextStyleEntry::LengthType &l0 = entry.myLengths[i];
		const ZLTextStyleEntry::LengthType &l1 = entry.myLengths[i + 1];
		*address++ = l0.Unit;
		*address++ = l1.Unit;
		ZLCachedMemoryAllocator::writeUInt16(address, l0.Size);
		address += 2;
		ZLCachedMemoryAllocator::writeUInt16(address, l1.Size);
		address += 2;
	}
	if (ZLTextStyleEntry::NUMBER_OF_LENGTHS % 2) {
		const ZLTextStyleEntry::LengthType &l0 = entry.myLengths[lengthMinusOne];
		*address++ = l0.Unit;
		*address++ = 0;
		ZLCachedMemoryAllocator::writeUInt16(address, l0.Size);
		address += 2;
	}
	*address++ = entry.mySupportedFontModifier;
	*address++ = entry.myFontModifier;
	*address++ = entry.myAlignmentType;
	*address++ = entry.myFontSizeMag;
	if (entry.fontFamilySupported()) {
		ZLCachedMemoryAllocator::writeUInt16(address, fontFamily.size());
		memcpy(address + 2, &fontFamily.front(), fontFamilyLen);
	}
	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();*/
}

void ZLTextModel::addHyperlinkControl(ZLTextKind textKind, ZLHyperlinkType hyperlinkType, const std::string &label) {
	ZLUnicodeUtil::Ucs2String ucs2label;
	ZLUnicodeUtil::utf8ToUcs2(ucs2label, label);

	const size_t len = ucs2label.size() * 2;

	myLastEntryStart = myAllocator->allocate(len + 6);
	*myLastEntryStart = ZLTextParagraphEntry::HYPERLINK_CONTROL_ENTRY;
	*(myLastEntryStart + 1) = 0;
	*(myLastEntryStart + 2) = textKind;
	*(myLastEntryStart + 3) = hyperlinkType;
	ZLCachedMemoryAllocator::writeUInt16(myLastEntryStart + 4, ucs2label.size());
	memcpy(myLastEntryStart + 6, &ucs2label.front(), len);
	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::addImage(const std::string &id, short vOffset, bool isCover) {
	ZLUnicodeUtil::Ucs2String ucs2id;
	ZLUnicodeUtil::utf8ToUcs2(ucs2id, id);

	const size_t len = ucs2id.size() * 2;

	myLastEntryStart = myAllocator->allocate(len + 8);
	*myLastEntryStart = ZLTextParagraphEntry::IMAGE_ENTRY;
	*(myLastEntryStart + 1) = 0;
	ZLCachedMemoryAllocator::writeUInt16(myLastEntryStart + 2, vOffset);
	ZLCachedMemoryAllocator::writeUInt16(myLastEntryStart + 4, ucs2id.size());
	memcpy(myLastEntryStart + 6, &ucs2id.front(), len);
	ZLCachedMemoryAllocator::writeUInt16(myLastEntryStart + 6 + len, isCover ? 1 : 0);
	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::addBidiReset() {
	myLastEntryStart = myAllocator->allocate(2);
	*myLastEntryStart = ZLTextParagraphEntry::RESET_BIDI_ENTRY;
	*(myLastEntryStart + 1) = 0;
	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::flush() {
	myAllocator->flush();
}
