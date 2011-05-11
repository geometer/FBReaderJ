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

#include <string.h>

#include <algorithm>

#include <ZLUnicodeUtil.h>
#include <ZLImage.h>

#include "ZLTextParagraph.h"


const shared_ptr<ZLTextParagraphEntry> ResetBidiEntry::Instance = new ResetBidiEntry();

/*short ZLTextStyleEntry::length(Length name, const Metrics &metrics) const {
	switch (myLengths[name].Unit) {
		default:
		case SIZE_UNIT_PIXEL:
			return myLengths[name].Size;
		case SIZE_UNIT_EM_100:
			return (myLengths[name].Size * metrics.FontSize + 50) / 100;
		case SIZE_UNIT_EX_100:
			return (myLengths[name].Size * metrics.FontXHeight + 50) / 100;
		case SIZE_UNIT_PERCENT:
			switch (name) {
				default:
				case LENGTH_LEFT_INDENT:
				case LENGTH_RIGHT_INDENT:
				case LENGTH_FIRST_LINE_INDENT_DELTA:
					return (myLengths[name].Size * metrics.FullWidth + 50) / 100;
				case LENGTH_SPACE_BEFORE:
				case LENGTH_SPACE_AFTER:
					return (myLengths[name].Size * metrics.FullHeight + 50) / 100;
			}
	}
}

ZLTextStyleEntry::ZLTextStyleEntry(char *address) {
	memcpy(&myMask, address, sizeof(int));
	address += sizeof(int);
	for (int i = 0; i < NUMBER_OF_LENGTHS; ++i) {
		myLengths[i].Unit = (SizeUnit)*address++;
		memcpy(&myLengths[i].Size, address, sizeof(short));
		address += sizeof(short);
	}
	mySupportedFontModifier = *address++;
	myFontModifier = *address++;
	myAlignmentType = (ZLTextAlignmentType)*address++;
	myFontSizeMag = (signed char)*address++;
	if (fontFamilySupported()) {
		myFontFamily = address;
	}
}*/

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

ZLTextHyperlinkControlEntry::ZLTextHyperlinkControlEntry(const char *address) : ZLTextControlEntry((ZLTextKind)*address, true), myHyperlinkType((ZLHyperlinkType)*(address + 1)) {
	uint16_t len;
	memcpy(&len, address + 2, 2);
	ZLUnicodeUtil::Ucs2Char *ucs2data = (ZLUnicodeUtil::Ucs2Char *)(address + 4);
	ZLUnicodeUtil::Ucs2String label(ucs2data, ucs2data + len);
	ZLUnicodeUtil::ucs2ToUtf8(myLabel, label);
}

ZLTextEntry::ZLTextEntry(const char *address) {
	uint32_t len;
	memcpy(&len, address, 4);
	ZLUnicodeUtil::Ucs2Char *ucs2data = (ZLUnicodeUtil::Ucs2Char *)(address + 4);
	ZLUnicodeUtil::Ucs2String ucs2str(ucs2data, ucs2data + len);
	ZLUnicodeUtil::ucs2ToUtf8(myText, ucs2str);
}

ImageEntry::ImageEntry(const char *address) {
	int16_t vOffset;
	memcpy(&vOffset, address, 2);
	myVOffset = vOffset;

	uint16_t len;
	memcpy(&len, address + 2, 2);
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
			{
				ZLTextKind kind = (ZLTextKind)*(myPointer + 2);
				bool isStart = *(myPointer + 3) != 0;
				myEntry = ZLTextControlEntryPool::Pool.controlEntry(kind, isStart);
				break;
			}
			case ZLTextParagraphEntry::HYPERLINK_CONTROL_ENTRY:
				myEntry = new ZLTextHyperlinkControlEntry(myPointer + 2);
				break;
			case ZLTextParagraphEntry::IMAGE_ENTRY:
				myEntry = new ImageEntry(myPointer + 2);
				break;
			/*case ZLTextParagraphEntry::STYLE_ENTRY:
				myEntry = new ZLTextStyleEntry(myPointer + 1);
				break;*/
			case ZLTextParagraphEntry::FIXED_HSPACE_ENTRY:
				myEntry = new ZLTextFixedHSpaceEntry((unsigned char)*(myPointer + 2));
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
				uint32_t len;
				memcpy(&len, myPointer + 2, 4);
				myPointer += len * 2 + 6;
				break;
			}
			case ZLTextParagraphEntry::CONTROL_ENTRY:
				myPointer += 4;
				break;
			case ZLTextParagraphEntry::HYPERLINK_CONTROL_ENTRY:
			{
				uint16_t len;
				memcpy(&len, myPointer + 4, 2);
				myPointer += len * 2 + 6;
				break;
			}
			case ZLTextParagraphEntry::IMAGE_ENTRY:
			{
				uint16_t len;
				memcpy(&len, myPointer + 4, 2);
				myPointer += len * 2 + 6;
				break;
			}
			/*case ZLTextParagraphEntry::STYLE_ENTRY:
			{
				int mask;
				memcpy(&mask, myPointer + 1, sizeof(int));
				bool withFontFamily = (mask & ZLTextStyleEntry::SUPPORT_FONT_FAMILY) == ZLTextStyleEntry::SUPPORT_FONT_FAMILY;
				myPointer += sizeof(int) + ZLTextStyleEntry::NUMBER_OF_LENGTHS * (sizeof(short) + 1) + 5;
				if (withFontFamily) {
					while (*myPointer != '\0') {
						++myPointer;
					}
					++myPointer;
				}
				break;
			}*/
			case ZLTextParagraphEntry::FIXED_HSPACE_ENTRY:
				myPointer += 4;
				break;
			case ZLTextParagraphEntry::RESET_BIDI_ENTRY:
				myPointer += 2;
				break;
		}
		if (*myPointer == 0) {
			memcpy(&myPointer, myPointer + 1, sizeof(char*));
		}
	}
}

size_t ZLTextParagraph::textDataLength() const {
	size_t len = 0;
	for (Iterator it = *this; !it.isEnd(); it.next()) {
		if (it.entryKind() == ZLTextParagraphEntry::TEXT_ENTRY) {
			len += ((ZLTextEntry&)*it.entry()).dataLength();
		}
	}
	return len;
}

size_t ZLTextParagraph::characterNumber() const {
	size_t len = 0;
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


ZLTextTreeParagraph::ZLTextTreeParagraph(ZLTextTreeParagraph *parent) : myIsOpen(false), myParent(parent) {
	if (parent != 0) {
		parent->addChild(this);
		myDepth = parent->myDepth + 1;
	} else {
		myDepth = 0;
	}
}

void ZLTextTreeParagraph::openTree() {
	for (ZLTextTreeParagraph *p = parent(); p != 0; p = p->parent()) {
		p->open(true);
	}
}

void ZLTextTreeParagraph::removeFromParent() {
	if (myParent != 0) {
		myParent->myChildren.erase(std::find(myParent->myChildren.begin(), myParent->myChildren.end(), this));
	}
}

int ZLTextTreeParagraph::fullSize() const {
	int size = 1;
	for (std::vector<ZLTextTreeParagraph*>::const_iterator it = myChildren.begin(); it != myChildren.end(); ++it) {
		size += (*it)->fullSize();
	}
	return size;
}
