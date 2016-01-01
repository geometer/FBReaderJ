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

#include <cstdlib>

#include <ZLFile.h>
#include <ZLStringUtil.h>

#include "MiscUtil.h"

FBTextKind MiscUtil::referenceType(const std::string &link) {
	std::string lowerCasedLink = link;
	const bool isFileReference =
		ZLStringUtil::stringStartsWith(lowerCasedLink, "http://") ||
		ZLStringUtil::stringStartsWith(lowerCasedLink, "https://") ||
		ZLStringUtil::stringStartsWith(lowerCasedLink, "ftp://");

	if (!isFileReference) {
		return
			ZLStringUtil::stringStartsWith(lowerCasedLink, "mailto:") ||
			ZLStringUtil::stringStartsWith(lowerCasedLink, "fbreader-action:") ||
			ZLStringUtil::stringStartsWith(lowerCasedLink, "com-fbreader-action:")
				? EXTERNAL_HYPERLINK : INTERNAL_HYPERLINK;
	}
	/*static const std::string FeedBooksPrefix0 = "http://feedbooks.com/book/stanza/";
	static const std::string FeedBooksPrefix1 = "http://www.feedbooks.com/book/stanza/";
	bool isBookHyperlink =
		ZLStringUtil::stringStartsWith(lowerCasedLink, FeedBooksPrefix0) ||
		ZLStringUtil::stringStartsWith(lowerCasedLink, FeedBooksPrefix1) ||
		ZLStringUtil::stringEndsWith(lowerCasedLink, ".epub") ||
		ZLStringUtil::stringEndsWith(lowerCasedLink, ".mobi") ||
		ZLStringUtil::stringEndsWith(lowerCasedLink, ".chm") ||
		ZLStringUtil::stringEndsWith(lowerCasedLink, ".fb2");
	return isBookHyperlink ? BOOK_HYPERLINK : EXTERNAL_HYPERLINK;*/
	return EXTERNAL_HYPERLINK;
}

std::string MiscUtil::htmlDirectoryPrefix(const std::string &fileName) {
	ZLFile file(fileName);
	std::string shortName = file.name(false);
	std::string path = file.path();
	int index = -1;
	if ((path.length() > shortName.length()) &&
			(path[path.length() - shortName.length() - 1] == ':')) {
		index = shortName.rfind('/');
	}
	return path.substr(0, path.length() - shortName.length() + index + 1);
}

std::string MiscUtil::htmlFileName(const std::string &fileName) {
	ZLFile file(fileName);
	std::string shortName = file.name(false);
	std::string path = file.path();
	int index = -1;
	if ((path.length() > shortName.length()) &&
			(path[path.length() - shortName.length() - 1] == ':')) {
		index = shortName.rfind('/');
	}
	return path.substr(path.length() - shortName.length() + index + 1);
}

std::string MiscUtil::decodeHtmlURL(const std::string &encoded) {
	char buffer[3];
	buffer[2] = '\0';

	std::string decoded;
	const int len = encoded.length();
	decoded.reserve(len);
	for (int i = 0; i < len; i++) {
		if ((encoded[i] == '%') && (i < len - 2)) {
			buffer[0] = *(encoded.data() + i + 1);
			buffer[1] = *(encoded.data() + i + 2);
			decoded += (char)std::strtol(buffer, 0, 16);
			i += 2;
		} else {
			decoded += encoded[i];
		}
	}
	return decoded;
}
