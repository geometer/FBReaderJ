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

#include <cctype>

#include <ZLInputStream.h>

#include "TxtReader.h"

TxtReader::TxtReader(const std::string &encoding) : EncodedTextReader(encoding) {
}

TxtReader::~TxtReader() {
}

void TxtReader::readDocument(ZLInputStream &stream) {
	if (!stream.open()) {
		return;
	}

	startDocumentHandler();

	const size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::string str;
	size_t length;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		const char *end = buffer + length;
		for (char *ptr = start; ptr != end; ++ptr) {
			if (*ptr == '\n' || *ptr == '\r') {
				bool skipNewLine = false;
				if (*ptr == '\r' && (ptr + 1) != end && *(ptr + 1) == '\n') {
					skipNewLine = true;
					*ptr = '\n';
				}
				if (start != ptr) {
					str.erase();
					myConverter->convert(str, start, ptr + 1);
					characterDataHandler(str);
				}
				if (skipNewLine) {
					++ptr;
				}
				start = ptr + 1;
				newLineHandler();
			} else if (isspace((unsigned char)*ptr)) {
				if (*ptr != '\t') {
					*ptr = ' ';
				}
			} else {
			}
		}
		if (start != end) {
			str.erase();
			myConverter->convert(str, start, end);
			characterDataHandler(str);
		}
	} while (length == BUFSIZE);
	delete[] buffer;

	endDocumentHandler();

	stream.close();
}
