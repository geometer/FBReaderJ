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

#include <cstdint>
#include <cstring>
#include <algorithm>

#include <ZLibrary.h>
//#include <ZLSearchUtil.h>
//#include <ZLLanguageUtil.h>
#include <ZLUnicodeUtil.h>
//#include <ZLStringUtil.h>
//#include <ZLLogger.h>
#include <FontManager.h>

#include "ZLTextModel.h"
#include "ZLTextParagraph.h"
#include "ZLTextStyleEntry.h"
#include "ZLVideoEntry.h"

ZLTextModel::ZLTextModel(const std::string &id, const std::string &language, const std::size_t rowSize,
		const std::string &directoryName, const std::string &fileExtension, FontManager &fontManager) :
	myId(id),
	myLanguage(language.empty() ? ZLibrary::Language() : language),
	myAllocator(new ZLCachedMemoryAllocator(rowSize, directoryName, fileExtension)),
	myLastEntryStart(0),
	myFontManager(fontManager) {
}

ZLTextModel::ZLTextModel(const std::string &id, const std::string &language, shared_ptr<ZLCachedMemoryAllocator> allocator, FontManager &fontManager) :
	myId(id),
	myLanguage(language.empty() ? ZLibrary::Language() : language),
	myAllocator(allocator),
	myLastEntryStart(0),
	myFontManager(fontManager) {
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

void ZLTextModel::search(const std::string &text, std::size_t startIndex, std::size_t endIndex, bool ignoreCase) const {
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
				const std::size_t len = textEntry.dataLength();
				for (int pos = ZLSearchUtil::find(str, len, pattern); pos != -1; pos = ZLSearchUtil::find(str, len, pattern, pos + 1)) {
					myMarks.push_back(ZLTextMark(it - myParagraphs.begin(), offset + pos, pattern.length()));
				}
				offset += len;
			}
		}
	}
}

void ZLTextModel::selectParagraph(std::size_t index) const {
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
	const std::size_t dataSize = myAllocator->blocksNumber();
	const std::size_t bytesOffset = myAllocator->currentBytesOffset();

	myStartEntryIndices.push_back((dataSize == 0) ? 0 : (dataSize - 1));
	myStartEntryOffsets.push_back(bytesOffset / 2); // offset in words for future use in Java
	myParagraphLengths.push_back(0);
	myTextSizes.push_back(myTextSizes.empty() ? 0 : myTextSizes.back());
	myParagraphKinds.push_back(paragraph->kind());

	myParagraphs.push_back(paragraph);
	myLastEntryStart = 0;
}

ZLTextPlainModel::ZLTextPlainModel(const std::string &id, const std::string &language, const std::size_t rowSize,
		const std::string &directoryName, const std::string &fileExtension, FontManager &fontManager) :
	ZLTextModel(id, language, rowSize, directoryName, fileExtension, fontManager) {
}

ZLTextPlainModel::ZLTextPlainModel(const std::string &id, const std::string &language, shared_ptr<ZLCachedMemoryAllocator> allocator, FontManager &fontManager) :
	ZLTextModel(id, language, allocator, fontManager) {
}

void ZLTextPlainModel::createParagraph(ZLTextParagraph::Kind kind) {
	ZLTextParagraph *paragraph = (kind == ZLTextParagraph::TEXT_PARAGRAPH) ? new ZLTextParagraph() : new ZLTextSpecialParagraph(kind);
	addParagraphInternal(paragraph);
}

void ZLTextModel::addText(const std::string &text) {
	ZLUnicodeUtil::Ucs2String ucs2str;
	ZLUnicodeUtil::utf8ToUcs2(ucs2str, text);
	const std::size_t len = ucs2str.size();

	if (myLastEntryStart != 0 && *myLastEntryStart == ZLTextParagraphEntry::TEXT_ENTRY) {
		const std::size_t oldLen = ZLCachedMemoryAllocator::readUInt32(myLastEntryStart + 2);
		const std::size_t newLen = oldLen + len;
		myLastEntryStart = myAllocator->reallocateLast(myLastEntryStart, 2 * newLen + 6);
		ZLCachedMemoryAllocator::writeUInt32(myLastEntryStart + 2, newLen);
		std::memcpy(myLastEntryStart + 6 + oldLen, &ucs2str.front(), 2 * newLen);
	} else {
		myLastEntryStart = myAllocator->allocate(2 * len + 6);
		*myLastEntryStart = ZLTextParagraphEntry::TEXT_ENTRY;
		*(myLastEntryStart + 1) = 0;
		ZLCachedMemoryAllocator::writeUInt32(myLastEntryStart + 2, len);
		std::memcpy(myLastEntryStart + 6, &ucs2str.front(), 2 * len);
		myParagraphs.back()->addEntry(myLastEntryStart);
		++myParagraphLengths.back();
	}
	myTextSizes.back() += len;
}

void ZLTextModel::addText(const std::vector<std::string> &text) {
	if (text.size() == 0) {
		return;
	}
	std::size_t fullLength = 0;
	for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
		fullLength += ZLUnicodeUtil::utf8Length(*it);
	}

	ZLUnicodeUtil::Ucs2String ucs2str;
	if (myLastEntryStart != 0 && *myLastEntryStart == ZLTextParagraphEntry::TEXT_ENTRY) {
		const std::size_t oldLen = ZLCachedMemoryAllocator::readUInt32(myLastEntryStart + 2);
		const std::size_t newLen = oldLen + fullLength;
		myLastEntryStart = myAllocator->reallocateLast(myLastEntryStart, 2 * newLen + 6);
		ZLCachedMemoryAllocator::writeUInt32(myLastEntryStart + 2, newLen);
		std::size_t offset = 6 + oldLen;
		for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
			ZLUnicodeUtil::utf8ToUcs2(ucs2str, *it);
			const std::size_t len = 2 * ucs2str.size();
			std::memcpy(myLastEntryStart + offset, &ucs2str.front(), len);
			offset += len;
			ucs2str.clear();
		}
	} else {
		myLastEntryStart = myAllocator->allocate(2 * fullLength + 6);
		*myLastEntryStart = ZLTextParagraphEntry::TEXT_ENTRY;
		*(myLastEntryStart + 1) = 0;
		ZLCachedMemoryAllocator::writeUInt32(myLastEntryStart + 2, fullLength);
		std::size_t offset = 6;
		for (std::vector<std::string>::const_iterator it = text.begin(); it != text.end(); ++it) {
			ZLUnicodeUtil::utf8ToUcs2(ucs2str, *it);
			const std::size_t len = 2 * ucs2str.size();
			std::memcpy(myLastEntryStart + offset, &ucs2str.front(), len);
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

//static int EntryCount = 0;
//static int EntryLen = 0;

void ZLTextModel::addStyleEntry(const ZLTextStyleEntry &entry, unsigned char depth) {
	addStyleEntry(entry, entry.fontFamilies(), depth);
}

void ZLTextModel::addStyleEntry(const ZLTextStyleEntry &entry, const std::vector<std::string> &fontFamilies, unsigned char depth) {
	// +++ calculating entry size
	std::size_t len = 4; // entry type + feature mask
	for (int i = 0; i < ZLTextStyleEntry::NUMBER_OF_LENGTHS; ++i) {
		if (entry.isFeatureSupported((ZLTextStyleEntry::Feature)i)) {
			len += 4; // each supported length
		}
	}
	if (entry.isFeatureSupported(ZLTextStyleEntry::ALIGNMENT_TYPE) ||
			entry.isFeatureSupported(ZLTextStyleEntry::NON_LENGTH_VERTICAL_ALIGN)) {
		len += 2;
	}
	if (entry.isFeatureSupported(ZLTextStyleEntry::FONT_FAMILY)) {
		len += 2;
	}
	if (entry.isFeatureSupported(ZLTextStyleEntry::FONT_STYLE_MODIFIER)) {
		len += 2;
	}
	// --- calculating entry size

/*
	EntryCount += 1;
	EntryLen += len;
	std::string debug = "style entry counter: ";
	ZLStringUtil::appendNumber(debug, EntryCount);
	debug += "/";
	ZLStringUtil::appendNumber(debug, EntryLen);
	ZLLogger::Instance().println(ZLLogger::DEFAULT_CLASS, debug);
*/

	// +++ writing entry
	myLastEntryStart = myAllocator->allocate(len);
	char *address = myLastEntryStart;

	*address++ = entry.entryKind();
	*address++ = depth;
	address = ZLCachedMemoryAllocator::writeUInt16(address, entry.myFeatureMask);

	for (int i = 0; i < ZLTextStyleEntry::NUMBER_OF_LENGTHS; ++i) {
		if (entry.isFeatureSupported((ZLTextStyleEntry::Feature)i)) {
			const ZLTextStyleEntry::LengthType &len = entry.myLengths[i];
			address = ZLCachedMemoryAllocator::writeUInt16(address, len.Size);
			*address++ = len.Unit;
			*address++ = 0;
		}
	}
	if (entry.isFeatureSupported(ZLTextStyleEntry::ALIGNMENT_TYPE) ||
			entry.isFeatureSupported(ZLTextStyleEntry::NON_LENGTH_VERTICAL_ALIGN)) {
		*address++ = entry.myAlignmentType;
		*address++ = entry.myVerticalAlignCode;
	}
	if (entry.isFeatureSupported(ZLTextStyleEntry::FONT_FAMILY)) {
		address = ZLCachedMemoryAllocator::writeUInt16(address, myFontManager.familyListIndex(fontFamilies));
	}
	if (entry.isFeatureSupported(ZLTextStyleEntry::FONT_STYLE_MODIFIER)) {
		*address++ = entry.mySupportedFontModifier;
		*address++ = entry.myFontModifier;
	}
	// --- writing entry

	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::addStyleCloseEntry() {
	myLastEntryStart = myAllocator->allocate(2);
	char *address = myLastEntryStart;

	*address++ = ZLTextParagraphEntry::STYLE_CLOSE_ENTRY;
	*address++ = 0;

	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::addHyperlinkControl(ZLTextKind textKind, ZLHyperlinkType hyperlinkType, const std::string &label) {
	ZLUnicodeUtil::Ucs2String ucs2label;
	ZLUnicodeUtil::utf8ToUcs2(ucs2label, label);

	const std::size_t len = ucs2label.size() * 2;

	myLastEntryStart = myAllocator->allocate(len + 6);
	*myLastEntryStart = ZLTextParagraphEntry::HYPERLINK_CONTROL_ENTRY;
	*(myLastEntryStart + 1) = 0;
	*(myLastEntryStart + 2) = textKind;
	*(myLastEntryStart + 3) = hyperlinkType;
	ZLCachedMemoryAllocator::writeUInt16(myLastEntryStart + 4, ucs2label.size());
	std::memcpy(myLastEntryStart + 6, &ucs2label.front(), len);
	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::addImage(const std::string &id, short vOffset, bool isCover) {
	ZLUnicodeUtil::Ucs2String ucs2id;
	ZLUnicodeUtil::utf8ToUcs2(ucs2id, id);

	const std::size_t len = ucs2id.size() * 2;

	myLastEntryStart = myAllocator->allocate(len + 8);
	*myLastEntryStart = ZLTextParagraphEntry::IMAGE_ENTRY;
	*(myLastEntryStart + 1) = 0;
	ZLCachedMemoryAllocator::writeUInt16(myLastEntryStart + 2, vOffset);
	ZLCachedMemoryAllocator::writeUInt16(myLastEntryStart + 4, ucs2id.size());
	std::memcpy(myLastEntryStart + 6, &ucs2id.front(), len);
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

void ZLTextModel::addVideoEntry(const ZLVideoEntry &entry) {
	const std::map<std::string,std::string> &sources = entry.sources();

	std::size_t len = 4;
	for (std::map<std::string,std::string>::const_iterator it = sources.begin(); it != sources.end(); ++it) {
		len += 2 * (ZLUnicodeUtil::utf8Length(it->first) + ZLUnicodeUtil::utf8Length(it->second)) + 4;
	}

	myLastEntryStart = myAllocator->allocate(len);
	*myLastEntryStart = ZLTextParagraphEntry::VIDEO_ENTRY;
	*(myLastEntryStart + 1) = 0;
	char *p = ZLCachedMemoryAllocator::writeUInt16(myLastEntryStart + 2, sources.size());
	for (std::map<std::string,std::string>::const_iterator it = sources.begin(); it != sources.end(); ++it) {
		ZLUnicodeUtil::Ucs2String first;
		ZLUnicodeUtil::utf8ToUcs2(first, it->first);
		p = ZLCachedMemoryAllocator::writeString(p, first);
		ZLUnicodeUtil::Ucs2String second;
		ZLUnicodeUtil::utf8ToUcs2(second, it->second);
		p = ZLCachedMemoryAllocator::writeString(p, second);
	}

	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::addExtensionEntry(const std::string &action, const std::map<std::string,std::string> &data) {
	std::size_t fullLength = 2;                                      // entry type + map size
	fullLength += 2 + ZLUnicodeUtil::utf8Length(action) * 2;         // action name
	for (std::map<std::string,std::string>::const_iterator it = data.begin(); it != data.end(); ++it) {
		fullLength += 2 + ZLUnicodeUtil::utf8Length(it->first) * 2;    // data key
		fullLength += 2 + ZLUnicodeUtil::utf8Length(it->second) * 2;   // data value
	}

	myLastEntryStart = myAllocator->allocate(fullLength);
	*myLastEntryStart = ZLTextParagraphEntry::EXTENSION_ENTRY;
	*(myLastEntryStart + 1) = data.size();

	char *p = myLastEntryStart + 2;
	ZLUnicodeUtil::Ucs2String ucs2action;
	ZLUnicodeUtil::utf8ToUcs2(ucs2action, action);
	p = ZLCachedMemoryAllocator::writeString(p, ucs2action);

	for (std::map<std::string,std::string>::const_iterator it = data.begin(); it != data.end(); ++it) {
		ZLUnicodeUtil::Ucs2String key;
		ZLUnicodeUtil::utf8ToUcs2(key, it->first);
		p = ZLCachedMemoryAllocator::writeString(p, key);
		ZLUnicodeUtil::Ucs2String value;
		ZLUnicodeUtil::utf8ToUcs2(value, it->second);
		p = ZLCachedMemoryAllocator::writeString(p, value);
	}

	myParagraphs.back()->addEntry(myLastEntryStart);
	++myParagraphLengths.back();
}

void ZLTextModel::flush() {
	myAllocator->flush();
}
