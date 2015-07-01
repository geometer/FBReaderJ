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

#include <algorithm>
#include <cctype>

#include <ZLInputStream.h>
#include <ZLXMLReader.h>
#include <ZLFile.h>
#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>

#include "HtmlReader.h"
#include "HtmlEntityCollection.h"

HtmlReader::HtmlReader(const std::string &encoding) : EncodedTextReader(encoding) {
}

HtmlReader::~HtmlReader() {
}

void HtmlReader::setTag(HtmlTag &tag, const std::string &name) {
	tag.Attributes.clear();

	if (name.length() == 0) {
		tag.Name = name;
		return;
	}

	tag.Start = name[0] != '/';
	if (tag.Start) {
		tag.Name = name;
	} else {
		tag.Name = name.substr(1);
	}

	ZLStringUtil::asciiToLowerInline(tag.Name);
}

enum ParseState {
	PS_TEXT,
	PS_TAGSTART,
	PS_TAGNAME,
	PS_WAIT_END_OF_TAG,
	PS_ATTRIBUTENAME,
	PS_ATTRIBUTEVALUE,
	PS_SKIPTAG,
	PS_COMMENT,
	PS_SPECIAL,
	PS_SPECIAL_IN_ATTRIBUTEVALUE,
};

enum SpecialType {
	ST_UNKNOWN,
	ST_NUM,
	ST_NAME,
	ST_DEC,
	ST_HEX
};

static bool allowSymbol(SpecialType type, char ch) {
	return
		(type == ST_NAME && std::isalpha(ch)) ||
		(type == ST_DEC && std::isdigit(ch)) ||
		(type == ST_HEX && std::isxdigit(ch));
}

static int specialSymbolNumber(SpecialType type, const std::string &txt) {
	char *end = 0;
	switch (type) {
		case ST_NAME:
			return HtmlEntityCollection::symbolNumber(txt);
		case ST_DEC:
			return std::strtol(txt.c_str() + 1, &end, 10);
		case ST_HEX:
			return std::strtol(txt.c_str() + 2, &end, 16);
		default:
			return 0;
	}
}

void HtmlReader::appendString(std::string &to, std::string &from) {
	if (myConverter.isNull()) {
		to += from;
	} else {
		myConverter->convert(to, from);
		myConverter->reset();
	}
	from.erase();
}

void HtmlReader::readDocument(ZLInputStream &stream) {
	if (!stream.open()) {
		return;
	}

	startDocumentHandler();

	ParseState state = PS_TEXT;
	SpecialType state_special = ST_UNKNOWN;
	std::string currentString;
	std::string attributeValueString;
	std::string specialString;
	int quotationCounter = 0;
	HtmlTag currentTag;
	char endOfComment[2] = "\0";

	const std::size_t BUFSIZE = 2048;
	char *buffer = new char[BUFSIZE];
	std::size_t length;
	std::size_t offset = 0;
	do {
		length = stream.read(buffer, BUFSIZE);
		char *start = buffer;
		char *endOfBuffer = buffer + length;
		for (char *ptr = buffer; ptr < endOfBuffer; ++ptr) {
			switch (state) {
				case PS_TEXT:
					if (*ptr == '<') {
						if (!characterDataHandler(start, ptr - start, true)) {
							goto endOfProcessing;
						}
						start = ptr + 1;
						state = PS_TAGSTART;
						currentTag.Offset = offset + (ptr - buffer);
					}
					if (*ptr == '&') {
						if (!characterDataHandler(start, ptr - start, true)) {
							goto endOfProcessing;
						}
						start = ptr + 1;
						state = PS_SPECIAL;
						state_special = ST_UNKNOWN;
					}
					break;
				case PS_SPECIAL:
				case PS_SPECIAL_IN_ATTRIBUTEVALUE:
					if (state_special == ST_UNKNOWN) {
						if (*ptr == '#') {
							state_special = ST_NUM;
						} else if (std::isalpha(*ptr)) {
							state_special = ST_NAME;
						} else {
							start = ptr;
							state = (state == PS_SPECIAL) ? PS_TEXT : PS_ATTRIBUTEVALUE;
						}
					} else if (state_special == ST_NUM) {
						if (*ptr == 'x') {
							state_special = ST_HEX;
						} else if (std::isdigit(*ptr)) {
							state_special = ST_DEC;
						} else {
							start = ptr;
							state = (state == PS_SPECIAL) ? PS_TEXT : PS_ATTRIBUTEVALUE;
						}
					} else {
						if (*ptr == ';') {
							specialString.append(start, ptr - start);
							const int number = specialSymbolNumber(state_special, specialString);
							if (128 <= number && number <= 159) {
								char ch = number;
								if (state == PS_SPECIAL) {
									characterDataHandler(&ch, 1, true);
								} else {
									myConverter->convert(attributeValueString, &ch, &ch + 1);
								}
							} else if (number != 0) {
								char buffer[4];
								int len = ZLUnicodeUtil::ucs4ToUtf8(buffer, number);
								if (state == PS_SPECIAL) {
									characterDataHandler(buffer, len, false);
								} else {
									attributeValueString.append(buffer, len);
								}
							} else {
								specialString = "&" + specialString + ";";
								if (state == PS_SPECIAL) {
									characterDataHandler(specialString.c_str(), specialString.length(), false);
								} else {
									attributeValueString += specialString;
								}
							}
							specialString.erase();
							start = ptr + 1;
							state = (state == PS_SPECIAL) ? PS_TEXT : PS_ATTRIBUTEVALUE;
						} else if (!allowSymbol(state_special, *ptr)) {
							start = ptr;
							state = (state == PS_SPECIAL) ? PS_TEXT : PS_ATTRIBUTEVALUE;
						}
					}
					break;
				case PS_TAGSTART:
					state = *ptr == '!' ? PS_COMMENT : PS_TAGNAME;
					break;
				case PS_COMMENT:
					if (endOfComment[0] == '\0' && *ptr != '-') {
						state = PS_TAGNAME;
					} else if (endOfComment[0] == '-' && endOfComment[1] == '-' && *ptr == '>') {
						start = ptr + 1;
						state = PS_TEXT;
						endOfComment[0] = '\0';
						endOfComment[1] = '\0';
					} else {
						endOfComment[0] = endOfComment[1];
						endOfComment[1] = *ptr;
					}
					break;
				case PS_WAIT_END_OF_TAG:
					if (*ptr == '>') {
						start = ptr + 1;
						state = PS_TEXT;
					}
					break;
				case PS_TAGNAME:
					if (*ptr == '>' || *ptr == '/' || std::isspace((unsigned char)*ptr)) {
						currentString.append(start, ptr - start);
						start = ptr + 1;
						setTag(currentTag, currentString);
						currentString.erase();
						if (currentTag.Name == "") {
							state = *ptr == '>' ? PS_TEXT : PS_SKIPTAG;
						} else {
							if (*ptr == '>') {
								if (!tagHandler(currentTag)) {
									goto endOfProcessing;
								}
								state = PS_TEXT;
							} else if (*ptr == '/') {
								if (!tagHandler(currentTag)) {
									goto endOfProcessing;
								}
								currentTag.Start = false;
								if (!tagHandler(currentTag)) {
									goto endOfProcessing;
								}
								state = PS_WAIT_END_OF_TAG;
							} else {
								state = PS_ATTRIBUTENAME;
							}
						}
					}
					break;
				case PS_ATTRIBUTENAME:
					if (*ptr == '>' || *ptr == '/' || *ptr == '=' || std::isspace((unsigned char)*ptr)) {
						if (ptr != start || !currentString.empty()) {
							currentString.append(start, ptr - start);
							ZLStringUtil::asciiToLowerInline(currentString);
							currentTag.addAttribute(currentString);
							currentString.erase();
						}
						start = ptr + 1;
						if (*ptr == '>') {
							if (!tagHandler(currentTag)) {
								goto endOfProcessing;
							}
							state = PS_TEXT;
						} else if (*ptr == '/') {
							if (!tagHandler(currentTag)) {
								goto endOfProcessing;
							}
							currentTag.Start = false;
							if (!tagHandler(currentTag)) {
								goto endOfProcessing;
							}
							state = PS_WAIT_END_OF_TAG;
						} else {
							state = (*ptr == '=') ? PS_ATTRIBUTEVALUE : PS_ATTRIBUTENAME;
						}
					}
					break;
				case PS_ATTRIBUTEVALUE:
					if (*ptr == '"') {
						if (((ptr == start) && currentString.empty()) || (quotationCounter > 0)) {
							++quotationCounter;
						}
					} else if (*ptr == '&') {
						currentString.append(start, ptr - start);
						start = ptr + 1;
						appendString(attributeValueString, currentString);
						state = PS_SPECIAL_IN_ATTRIBUTEVALUE;
						state_special = ST_UNKNOWN;
					} else if (quotationCounter != 1 && (*ptr == '>' || *ptr == '/' || std::isspace((unsigned char)*ptr))) {
						if (ptr != start || !currentString.empty()) {
							currentString.append(start, ptr - start);
							appendString(attributeValueString, currentString);
							if (attributeValueString[0] == '"') {
								attributeValueString = attributeValueString.substr(1, attributeValueString.length() - 2);
							}
							currentTag.setLastAttributeValue(attributeValueString);
							attributeValueString.erase();
							quotationCounter = 0;
						}
						start = ptr + 1;
						if (*ptr == '>') {
							if (!tagHandler(currentTag)) {
								goto endOfProcessing;
							}
							state = PS_TEXT;
						} else if (*ptr == '/') {
							if (!tagHandler(currentTag)) {
								goto endOfProcessing;
							}
							currentTag.Start = false;
							if (!tagHandler(currentTag)) {
								goto endOfProcessing;
							}
							state = PS_WAIT_END_OF_TAG;
						} else {
							state = PS_ATTRIBUTENAME;
						}
					}
					break;
				case PS_SKIPTAG:
					if (*ptr == '>') {
						start = ptr + 1;
						state = PS_TEXT;
					}
					break;
			}
		}
		if (start != endOfBuffer) {
			switch (state) {
				case PS_TEXT:
					if (!characterDataHandler(start, endOfBuffer - start, true)) {
						goto endOfProcessing;
					}
					break;
				case PS_TAGNAME:
				case PS_ATTRIBUTENAME:
				case PS_ATTRIBUTEVALUE:
					currentString.append(start, endOfBuffer - start);
					break;
				case PS_SPECIAL:
				case PS_SPECIAL_IN_ATTRIBUTEVALUE:
					specialString.append(start, endOfBuffer - start);
					break;
				case PS_TAGSTART:
				case PS_SKIPTAG:
				case PS_COMMENT:
				case PS_WAIT_END_OF_TAG:
					break;
			}
		}
		offset += length;
	} while (length == BUFSIZE);
endOfProcessing:
	delete[] buffer;

	endDocumentHandler();

	stream.close();
}

const std::string *HtmlReader::HtmlTag::find(const std::string &name) const {
	for (unsigned int i = 0; i < Attributes.size(); ++i) {
		if (Attributes[i].Name == name) {
			return &Attributes[i].Value;
		}
	}
	return 0;
}
