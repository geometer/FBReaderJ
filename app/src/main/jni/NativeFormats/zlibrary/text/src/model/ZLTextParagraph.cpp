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

#include <cstring>

#include <algorithm>

#include <ZLUnicodeUtil.h>
#include <ZLImage.h>

#include "ZLCachedMemoryAllocator.h"
#include "ZLTextParagraph.h"
#include "ZLTextStyleEntry.h"

const shared_ptr<ZLTextParagraphEntry> ResetBidiEntry::Instance = new ResetBidiEntry();

/*
short ZLTextStyleEntry::length(Feature featureId, const Metrics &metrics) const {
	switch (myLengths[featureId].Unit) {
		default:
		case SIZE_UNIT_PIXEL:
			return myLengths[featureId].Size;
		case SIZE_UNIT_EM_100:
			// TODO: implement
			return (myLengths[featureId].Size * metrics.FontSize + 50) / 100;
		case SIZE_UNIT_REM_100:
			return (myLengths[featureId].Size * metrics.FontSize + 50) / 100;
		case SIZE_UNIT_EX_100:
			return (myLengths[featureId].Size * metrics.FontXHeight + 50) / 100;
		case SIZE_UNIT_PERCENT:
			switch (featureId) {
				default:
				case LENGTH_MARGIN_LEFT:
				case LENGTH_MARGIN_RIGHT:
				case LENGTH_PADDING_LEFT:
				case LENGTH_PADDING_RIGHT:
				case LENGTH_FIRST_LINE_INDENT:
					return (myLengths[featureId].Size * metrics.FullWidth + 50) / 100;
				case LENGTH_SPACE_BEFORE:
				case LENGTH_SPACE_AFTER:
					return (myLengths[featureId].Size * metrics.FullHeight + 50) / 100;
				case LENGTH_FONT_SIZE:
					return (myLengths[featureId].Size * metrics.FontSize + 50) / 100;
			}
	}
}

ZLTextStyleEntry::ZLTextStyleEntry(char *address) {
	myFeatureMask = ZLCachedMemoryAllocator::readUInt16(address);
	address += 2;

	const int lengthMinusOne = ZLTextStyleEntry::NUMBER_OF_LENGTHS - 1;
	for (int i = 0; i < lengthMinusOne; i += 2) {
		ZLTextStyleEntry::LengthType &l0 = myLengths[i];
		ZLTextStyleEntry::LengthType &l1 = myLengths[i + 1];
		l0.Unit = (SizeUnit)*address++;
		l1.Unit = (SizeUnit)*address++;
		l0.Size = ZLCachedMemoryAllocator::readUInt16(address);
		address += 2;
		l1.Size = ZLCachedMemoryAllocator::readUInt16(address);
		address += 2;
	}
	if (ZLTextStyleEntry::NUMBER_OF_LENGTHS % 2) {
		ZLTextStyleEntry::LengthType &l0 = myLengths[lengthMinusOne];
		l0.Unit = (SizeUnit)*address;
		address += 2;
		l0.Size = ZLCachedMemoryAllocator::readUInt16(address);
		address += 2;
	}
	mySupportedFontModifier = *address++;
	myFontModifier = *address++;
	myAlignmentType = (ZLTextAlignmentType)*address++;
	myFontSizeMagnification = *address++;
	if (isFeatureSupported(FONT_FAMILY)) {
		const std::size_t len = ZLCachedMemoryAllocator::readUInt16(address);
		ZLUnicodeUtil::Ucs2Char *ucs2data = (ZLUnicodeUtil::Ucs2Char *)(address + 2);
		ZLUnicodeUtil::Ucs2String ucs2str(ucs2data, ucs2data + len);
		ZLUnicodeUtil::ucs2ToUtf8(myFontFamily, ucs2str);
	}
}
*/

ZLTextControlEntryPool ZLTextControlEntryPool::Pool;

shared_ptr<ZLTextParagraphEntry> ZLTextControlEntryPool::controlEntry(ZLTextKind kind, bool isStart) {
	std::map<ZLTextKind, shared_ptr<ZLTextParagraphEntry> > &entries = isStart ? myStartEntries : myEndEntries;
	std::map<ZLTextKind, shared_ptr<ZLTextParagraphEntry> >::iterator it = entries.find(kind);
	if (it != entries.end()) {
		return it->second;
	}
	shared_ptr<ZLTextParagraphEntry> entry = new ZLTextControlEntry(kind, isStart);
	entries[kind] = entry;
	return entry;
}

/*
ZLTextHyperlinkControlEntry::ZLTextHyperlinkControlEntry(const char *address) : ZLTextControlEntry((ZLTextKind)*address, true), myHyperlinkType((ZLHyperlinkType)*(address + 1)) {
	const std::size_t len = ZLCachedMemoryAllocator::readUInt16(address + 2);
	ZLUnicodeUtil::Ucs2Char *ucs2data = (ZLUnicodeUtil::Ucs2Char *)(address + 4);
	ZLUnicodeUtil::Ucs2String ucs2str(ucs2data, ucs2data + len);
	ZLUnicodeUtil::ucs2ToUtf8(myLabel, ucs2str);
}

ZLTextEntry::ZLTextEntry(const char *address) {
	const std::size_t len = ZLCachedMemoryAllocator::readUInt32(address);
	ZLUnicodeUtil::Ucs2Char *ucs2data = (ZLUnicodeUtil::Ucs2Char *)(address + 4);
	ZLUnicodeUtil::Ucs2String ucs2str(ucs2data, ucs2data + len);
	ZLUnicodeUtil::ucs2ToUtf8(myText, ucs2str);
}

ImageEntry::ImageEntry(const char *address) {
	myVOffset = ZLCachedMemoryAllocator::readUInt16(address);
	const std::size_t len = ZLCachedMemoryAllocator::readUInt16(address + 2);
	ZLUnicodeUtil::Ucs2Char *ucs2data = (ZLUnicodeUtil::Ucs2Char *)(address + 4);
	ZLUnicodeUtil::Ucs2String ucs2str(ucs2data, ucs2data + len);
	ZLUnicodeUtil::ucs2ToUtf8(myId, ucs2str);
}

const shared_ptr<ZLTextParagraphEntry> ZLTextParagraph::Iterator::entry() const {
	if (myEntry.isNull()) {
		switch (*myPointer) {
			case ZLTextParagraphEntry::TEXT_ENTRY:
				myEntry = new ZLTextEntry(myPointer + 2);
				break;
			case ZLTextParagraphEntry::CONTROL_ENTRY:
				myEntry = ZLTextControlEntryPool::Pool.controlEntry(
					(ZLTextKind)*(myPointer + 2), *(myPointer + 3) != 0);
				break;
			case ZLTextParagraphEntry::HYPERLINK_CONTROL_ENTRY:
				myEntry = new ZLTextHyperlinkControlEntry(myPointer + 2);
				break;
			case ZLTextParagraphEntry::IMAGE_ENTRY:
				myEntry = new ImageEntry(myPointer + 2);
				break;
			case ZLTextParagraphEntry::STYLE_CSS_ENTRY:
			case ZLTextParagraphEntry::STYLE_OTHER_ENTRY:
				myEntry = new ZLTextStyleEntry(myPointer + 2);
				break;
			case ZLTextParagraphEntry::FIXED_HSPACE_ENTRY:
				myEntry = new ZLTextFixedHSpaceEntry(*(myPointer + 2));
				break;
			case ZLTextParagraphEntry::RESET_BIDI_ENTRY:
				myEntry = ResetBidiEntry::Instance;
				break;
		}
	}
	return myEntry;
}

void ZLTextParagraph::Iterator::next() {
	++myIndex;
	myEntry = 0;
	if (myIndex != myEndIndex) {
		switch (*myPointer) {
			case ZLTextParagraphEntry::TEXT_ENTRY:
			{
				const std::size_t len = ZLCachedMemoryAllocator::readUInt32(myPointer + 2);
				myPointer += len * 2 + 6;
				break;
			}
			case ZLTextParagraphEntry::CONTROL_ENTRY:
				myPointer += 4;
				break;
			case ZLTextParagraphEntry::HYPERLINK_CONTROL_ENTRY:
			{
				const std::size_t len = ZLCachedMemoryAllocator::readUInt16(myPointer + 4);
				myPointer += len * 2 + 6;
				break;
			}
			case ZLTextParagraphEntry::IMAGE_ENTRY:
			{
				const std::size_t len = ZLCachedMemoryAllocator::readUInt16(myPointer + 4);
				myPointer += len * 2 + 6;
				break;
			}
			case ZLTextParagraphEntry::STYLE_ENTRY:
			{
				unsigned int mask = ZLCachedMemoryAllocator::readUInt32(myPointer + 2);
				bool withFontFamily = (mask & (1 << ZLTextStyleEntry::FONT_FAMILY)) != 0;

				myPointer += 10 + 2 * (ZLTextStyleEntry::NUMBER_OF_LENGTHS +
						(ZLTextStyleEntry::NUMBER_OF_LENGTHS + 1) / 2);
				if (withFontFamily) {
					const std::size_t len = ZLCachedMemoryAllocator::readUInt16(myPointer);
					myPointer += 2 + 2 * len;
				}
				break;
			}
			case ZLTextParagraphEntry::FIXED_HSPACE_ENTRY:
				myPointer += 4;
				break;
			case ZLTextParagraphEntry::RESET_BIDI_ENTRY:
				myPointer += 2;
				break;
		}
		if (*myPointer == 0) {
			std::memcpy(&myPointer, myPointer + 1, sizeof(char*));
		}
	}
}

std::size_t ZLTextParagraph::textDataLength() const {
	std::size_t len = 0;
	for (Iterator it = *this; !it.isEnd(); it.next()) {
		if (it.entryKind() == ZLTextParagraphEntry::TEXT_ENTRY) {
			len += ((ZLTextEntry&)*it.entry()).dataLength();
		}
	}
	return len;
}

std::size_t ZLTextParagraph::characterNumber() const {
	std::size_t len = 0;
	for (Iterator it = *this; !it.isEnd(); it.next()) {
		switch (it.entryKind()) {
			case ZLTextParagraphEntry::TEXT_ENTRY:
			{
				const ZLTextEntry &entry = (ZLTextEntry&)*it.entry();
				len += ZLUnicodeUtil::utf8Length(entry.data(), entry.dataLength());
				break;
			}
			case ZLTextParagraphEntry::IMAGE_ENTRY:
				len += 100;
				break;
			default:
				break;
		}
	}
	return len;
}
*/
