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

#include "ZLTextStyleEntry.h"

shared_ptr<ZLTextStyleEntry> ZLTextStyleEntry::start() const {
	ZLTextStyleEntry *clone = new ZLTextStyleEntry(myEntryKind);
	clone->myFeatureMask = myFeatureMask & ~(1 << LENGTH_SPACE_AFTER);
	for (int i = 0; i < NUMBER_OF_LENGTHS; ++i) {
		clone->myLengths[i] = myLengths[i];
	}
	clone->myAlignmentType = myAlignmentType;
	clone->mySupportedFontModifier = mySupportedFontModifier;
	clone->myFontModifier = myFontModifier;
	clone->myFontFamilies = myFontFamilies;
	clone->myVerticalAlignCode = myVerticalAlignCode;
	return clone;
}

shared_ptr<ZLTextStyleEntry> ZLTextStyleEntry::end() const {
	if ((myFeatureMask & (1 << LENGTH_SPACE_AFTER)) == 0) {
		return 0;
	}
	ZLTextStyleEntry *clone = new ZLTextStyleEntry(myEntryKind);
	clone->myFeatureMask = 1 << LENGTH_SPACE_AFTER;
	clone->myLengths[LENGTH_SPACE_AFTER] = myLengths[LENGTH_SPACE_AFTER];
	return clone;
}

shared_ptr<ZLTextStyleEntry> ZLTextStyleEntry::inherited() const {
	ZLTextStyleEntry *clone = new ZLTextStyleEntry(myEntryKind);
	static const unsigned short skip =
		//(1 << LENGTH_MARGIN_LEFT) |
		//(1 << LENGTH_MARGIN_RIGHT) |
		//(1 << LENGTH_PADDING_LEFT) |
		//(1 << LENGTH_PADDING_RIGHT) |
		(1 << LENGTH_SPACE_BEFORE) |
		(1 << LENGTH_SPACE_AFTER);
	clone->myFeatureMask = myFeatureMask & ~skip;
	for (int i = 0; i < NUMBER_OF_LENGTHS; ++i) {
		clone->myLengths[i] = myLengths[i];
	}
	clone->myAlignmentType = myAlignmentType;
	clone->mySupportedFontModifier = mySupportedFontModifier;
	clone->myFontModifier = myFontModifier;
	clone->myFontFamilies = myFontFamilies;
	clone->myVerticalAlignCode = myVerticalAlignCode;
	return clone;
}
