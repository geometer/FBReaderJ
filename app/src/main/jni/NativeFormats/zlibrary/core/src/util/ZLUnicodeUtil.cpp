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
#include <cstdlib>
#include <map>

#include <AndroidUtil.h>
#include <JniEnvelope.h>

#include <ZLibrary.h>
#include <ZLFile.h>
#include <ZLXMLReader.h>

#include "ZLUnicodeUtil.h"

struct ZLUnicodeData {
	enum SymbolType {
		LETTER_LOWERCASE,
		LETTER_UPPERCASE,
		LETTER_OTHER,
		UNKNOWN
	};

	const SymbolType Type;
	const ZLUnicodeUtil::Ucs4Char LowerCase;
	const ZLUnicodeUtil::Ucs4Char UpperCase;

	ZLUnicodeData(const SymbolType type, ZLUnicodeUtil::Ucs4Char lowerCase, ZLUnicodeUtil::Ucs4Char upperCase);
};

ZLUnicodeData::ZLUnicodeData(const SymbolType type, ZLUnicodeUtil::Ucs4Char lowerCase, ZLUnicodeUtil::Ucs4Char upperCase) : Type(type), LowerCase(lowerCase), UpperCase(upperCase) {
}

/*static std::map<ZLUnicodeUtil::Ucs4Char,ZLUnicodeData> UNICODE_TABLE;

class ZLUnicodeTableReader : public ZLXMLReader {

private:
	void startElementHandler(const char *tag, const char **attributes);
};

void ZLUnicodeTableReader::startElementHandler(const char *tag, const char **attributes) {
	static std::string SYMBOL_TAG = "symbol";
	static std::string LETTER_LOWERCASE_TYPE = "Ll";
	static std::string LETTER_UPPERCASE_TYPE = "Lu";

	if (SYMBOL_TAG == tag) {
		const char *codeS = attributeValue(attributes, "code");
		const ZLUnicodeUtil::Ucs4Char code = strtol(codeS, 0, 16);
		const char *typeS = attributeValue(attributes, "type");
		ZLUnicodeData::SymbolType type = ZLUnicodeData::UNKNOWN;
		if (LETTER_LOWERCASE_TYPE == typeS) {
			type = ZLUnicodeData::LETTER_LOWERCASE;
		} else if (LETTER_UPPERCASE_TYPE == typeS) {
			type = ZLUnicodeData::LETTER_UPPERCASE;
		} else if (typeS != 0 && *typeS == 'L') {
			type = ZLUnicodeData::LETTER_OTHER;
		}
		const char *lowerS = attributeValue(attributes, "lower");
		const ZLUnicodeUtil::Ucs4Char lower = lowerS != 0 ? std::strtol(lowerS, 0, 16) : code;
		const char *upperS = attributeValue(attributes, "upper");
		const ZLUnicodeUtil::Ucs4Char upper = upperS != 0 ? std::strtol(upperS, 0, 16) : code;
		UNICODE_TABLE.insert(std::make_pair(code, ZLUnicodeData(type, lower, upper)));
	}
}

static void initUnicodeTable() {
	static bool inProgress = false;
	if (!inProgress && UNICODE_TABLE.empty()) {
		inProgress = true;
		ZLUnicodeTableReader reader;
		reader.readDocument(ZLFile(ZLibrary::ZLibraryDirectory() + ZLibrary::FileNameDelimiter + "unicode.xml"));
		inProgress = false;
	}
}
*/

bool ZLUnicodeUtil::isUtf8String(const char *str, int len) {
	const char *last = str + len;
	int nonLeadingCharsCounter = 0;
	for (; str < last; ++str) {
		if (nonLeadingCharsCounter == 0) {
			if ((*str & 0x80) != 0) {
				if ((*str & 0xE0) == 0xC0) {
					nonLeadingCharsCounter = 1;
				} else if ((*str & 0xF0) == 0xE0) {
					nonLeadingCharsCounter = 2;
				} else if ((*str & 0xF8) == 0xF0) {
					nonLeadingCharsCounter = 3;
				} else {
					return false;
				}
			}
		} else {
			if ((*str & 0xC0) != 0x80) {
				return false;
			}
			--nonLeadingCharsCounter;
		}
	}
	return nonLeadingCharsCounter == 0;
}

bool ZLUnicodeUtil::isUtf8String(const std::string &str) {
	return isUtf8String(str.data(), str.length());
}

void ZLUnicodeUtil::cleanUtf8String(std::string &str) {
	int charLength = 0;
	int processed = 0;
	for (std::string::iterator it = str.begin(); it != str.end();) {
		if (charLength == processed) {
			if ((*it & 0x80) == 0) {
				++it;
			} else if ((*it & 0xE0) == 0xC0) {
				charLength = 2;
				processed = 1;
				++it;
			} else if ((*it & 0xF0) == 0xE0) {
				charLength = 3;
				processed = 1;
				++it;
			} else if ((*it & 0xF8) == 0xF0) {
				charLength = 4;
				processed = 1;
				++it;
			} else {
				it = str.erase(it);
			}
		} else if ((*it & 0xC0) == 0x80) {
			++processed;
			++it;
		} else {
			it -= processed;
			do {
				it = str.erase(it);
			} while (--processed);
			charLength = 0;
		}
	}
}

int ZLUnicodeUtil::utf8Length(const char *str, int len) {
	const char *last = str + len;
	int counter = 0;
	while (str < last) {
		if ((*str & 0x80) == 0) {
			++str;
		} else if ((*str & 0x20) == 0) {
			str += 2;
		} else if ((*str & 0x10) == 0) {
			str += 3;
		} else {
			str += 4;
		}
		++counter;
	}
	return counter;
}

int ZLUnicodeUtil::utf8Length(const std::string &str) {
	return utf8Length(str.data(), str.length());
}

int ZLUnicodeUtil::length(const char *str, int utf8Length) {
	const char *ptr = str;
	for (int i = 0; i < utf8Length; ++i) {
		if ((*ptr & 0x80) == 0) {
			++ptr;
		} else if ((*ptr & 0x20) == 0) {
			ptr += 2;
		} else if ((*ptr & 0x10) == 0) {
			ptr += 3;
		} else {
			ptr += 4;
		}
	}
	return ptr - str;
}

int ZLUnicodeUtil::length(const std::string &str, int utf8Length) {
	return length(str.data(), utf8Length);
}

void ZLUnicodeUtil::utf8ToUcs4(Ucs4String &to, const char *from, int length, int toLength) {
	to.clear();
	if (toLength < 0) {
		toLength = utf8Length(from, length);
	}
	to.reserve(toLength);
	const char *last = from + length;
	for (const char *ptr = from; ptr < last;) {
		if ((*ptr & 0x80) == 0) {
			to.push_back(*ptr);
			++ptr;
		} else if ((*ptr & 0x20) == 0) {
			Ucs4Char ch = *ptr & 0x1f;
			++ptr;
			ch <<= 6;
			ch += *ptr & 0x3f;
			to.push_back(ch);
			++ptr;
		} else if ((*ptr & 0x10) == 0) {
			Ucs4Char ch = *ptr & 0x0f;
			++ptr;
			ch <<= 6;
			ch += *ptr & 0x3f;
			++ptr;
			ch <<= 6;
			ch += *ptr & 0x3f;
			to.push_back(ch);
			++ptr;
		} else {
			// symbol number is > 0xffff :(
			to.push_back('X');
			ptr += 4;
		}
	}
}

void ZLUnicodeUtil::utf8ToUcs4(Ucs4String &to, const std::string &from, int toLength) {
	utf8ToUcs4(to, from.data(), from.length(), toLength);
}

void ZLUnicodeUtil::utf8ToUcs2(Ucs2String &to, const char *from, int length, int toLength) {
	to.clear();
	if (toLength < 0) {
		toLength = utf8Length(from, length);
	}
	to.reserve(toLength);
	const char *last = from + length;
	for (const char *ptr = from; ptr < last;) {
		if ((*ptr & 0x80) == 0) {
			to.push_back(*ptr);
			++ptr;
		} else if ((*ptr & 0x20) == 0) {
			Ucs2Char ch = *ptr & 0x1f;
			++ptr;
			ch <<= 6;
			ch += *ptr & 0x3f;
			to.push_back(ch);
			++ptr;
		} else if ((*ptr & 0x10) == 0) {
			Ucs2Char ch = *ptr & 0x0f;
			++ptr;
			ch <<= 6;
			ch += *ptr & 0x3f;
			++ptr;
			ch <<= 6;
			ch += *ptr & 0x3f;
			to.push_back(ch);
			++ptr;
		} else {
			// symbol number is > 0xffff :(
			to.push_back('X');
			ptr += 4;
		}
	}
}

void ZLUnicodeUtil::utf8ToUcs2(Ucs2String &to, const std::string &from, int toLength) {
	utf8ToUcs2(to, from.data(), from.length(), toLength);
}

std::size_t ZLUnicodeUtil::firstChar(Ucs4Char &ch, const std::string &utf8String) {
	return firstChar(ch, utf8String.c_str());
}

std::size_t ZLUnicodeUtil::firstChar(Ucs4Char &ch, const char *utf8String) {
	if ((*utf8String & 0x80) == 0) {
		ch = *utf8String;
		return 1;
	} else if ((*utf8String & 0x20) == 0) {
		ch = *utf8String & 0x1f;
		ch <<= 6;
		ch += *(utf8String + 1) & 0x3f;
		return 2;
	} else {
		ch = *utf8String & 0x0f;
		ch <<= 6;
		ch += *(utf8String + 1) & 0x3f;
		ch <<= 6;
		ch += *(utf8String + 2) & 0x3f;
		return 3;
	}
}

std::size_t ZLUnicodeUtil::lastChar(Ucs4Char &ch, const std::string &utf8String) {
	return lastChar(ch, utf8String.data() + utf8String.length());
}

std::size_t ZLUnicodeUtil::lastChar(Ucs4Char &ch, const char *utf8String) {
	const char *ptr = utf8String - 1;
	while ((*ptr & 0xC0) == 0x80) {
		--ptr;
	}
	return utf8String - ptr;
}

int ZLUnicodeUtil::ucs4ToUtf8(char *to, Ucs4Char ch) {
	if (ch < 0x80) {
		*to = (char)ch;
		return 1;
	} else if (ch < 0x800) {
		*to = (char)(0xC0 | (ch >> 6));
		*(to + 1) = (char)(0x80 | (ch & 0x3F));
		return 2;
	} else {
		*to = (char)(0xE0 | ch >> 12);
		*(to + 1) = (char)(0x80 | ((ch >> 6) & 0x3F));
		*(to + 2) = (char)(0x80 | (ch & 0x3F));
		return 3;
	}
}

void ZLUnicodeUtil::ucs4ToUtf8(std::string &to, const Ucs4String &from, int toLength) {
	char buffer[3];
	to.erase();
	if (toLength > 0) {
		to.reserve(toLength);
	}
	for (Ucs4String::const_iterator it = from.begin(); it != from.end(); ++it) {
		to.append(buffer, ucs4ToUtf8(buffer, *it));
	}
}

int ZLUnicodeUtil::ucs2ToUtf8(char *to, Ucs2Char ch) {
	if (ch < 0x80) {
		*to = (char)ch;
		return 1;
	} else if (ch < 0x800) {
		*to = (char)(0xC0 | (ch >> 6));
		*(to + 1) = (char)(0x80 | (ch & 0x3F));
		return 2;
	} else {
		*to = (char)(0xE0 | ch >> 12);
		*(to + 1) = (char)(0x80 | ((ch >> 6) & 0x3F));
		*(to + 2) = (char)(0x80 | (ch & 0x3F));
		return 3;
	}
}

void ZLUnicodeUtil::ucs2ToUtf8(std::string &to, const Ucs2String &from, int toLength) {
	char buffer[3];
	to.erase();
	if (toLength > 0) {
		to.reserve(toLength);
	}
	for (Ucs2String::const_iterator it = from.begin(); it != from.end(); ++it) {
		to.append(buffer, ucs2ToUtf8(buffer, *it));
	}
}

/*
bool ZLUnicodeUtil::isLetter(Ucs4Char ch) {
	initUnicodeTable();
	std::map<ZLUnicodeUtil::Ucs4Char,ZLUnicodeData>::const_iterator it = UNICODE_TABLE.find(ch);
	if (it == UNICODE_TABLE.end()) {
		return false;
	}
	switch (it->second.Type) {
		case ZLUnicodeData::LETTER_LOWERCASE:
		case ZLUnicodeData::LETTER_UPPERCASE:
		case ZLUnicodeData::LETTER_OTHER:
			return true;
		default:
			return false;
	}
}
*/

bool ZLUnicodeUtil::isSpace(Ucs4Char ch) {
	return
		((9 <= ch) && (ch <= 13)) ||
		(ch == 32) ||
		//(ch == 160) ||
		(ch == 5760) ||
		((8192 <= ch) && (ch <= 8203)) ||
		(ch == 8232) ||
		(ch == 8233) ||
		(ch == 8239) ||
		(ch == 8287) ||
		(ch == 12288);
}

ZLUnicodeUtil::Breakable ZLUnicodeUtil::isBreakable(Ucs4Char c) {
	if (c <= 0x2000) {
		return NO_BREAKABLE;
	}

	if (((c < 0x2000) || (c > 0x2006)) &&
			((c < 0x2008) || (c > 0x2046)) &&
			((c < 0x207D) || (c > 0x207E)) &&
			((c < 0x208D) || (c > 0x208E)) &&
			((c < 0x2329) || (c > 0x232A)) &&
			((c < 0x3001) || (c > 0x3003)) &&
			((c < 0x3008) || (c > 0x3011)) &&
			((c < 0x3014) || (c > 0x301F)) &&
			((c < 0xFD3E) || (c > 0xFD3F)) &&
			((c < 0xFE30) || (c > 0xFE44)) &&
			((c < 0xFE49) || (c > 0xFE52)) &&
			((c < 0xFE54) || (c > 0xFE61)) &&
			((c < 0xFE6A) || (c > 0xFE6B)) &&
			((c < 0xFF01) || (c > 0xFF03)) &&
			((c < 0xFF05) || (c > 0xFF0A)) &&
			((c < 0xFF0C) || (c > 0xFF0F)) &&
			((c < 0xFF1A) || (c > 0xFF1B)) &&
			((c < 0xFF1F) || (c > 0xFF20)) &&
			((c < 0xFF3B) || (c > 0xFF3D)) &&
			((c < 0xFF61) || (c > 0xFF65)) &&
			(c != 0xFE63) &&
			(c != 0xFE68) &&
			(c != 0x3030) &&
			(c != 0x30FB) &&
			(c != 0xFF3F) &&
			(c != 0xFF5B) &&
			(c != 0xFF5D)) {
		return NO_BREAKABLE;
	}

	if (((c >= 0x201A) && (c <= 0x201C)) ||
			((c >= 0x201E) && (c <= 0x201F))) {
		return BREAKABLE_BEFORE;
	}
	switch (c) {
		case 0x2018: case 0x2039: case 0x2045:
		case 0x207D: case 0x208D: case 0x2329:
		case 0x3008: case 0x300A: case 0x300C:
		case 0x300E: case 0x3010: case 0x3014:
		case 0x3016: case 0x3018: case 0x301A:
		case 0x301D: case 0xFD3E: case 0xFE35:
		case 0xFE37: case 0xFE39: case 0xFE3B:
		case 0xFE3D: case 0xFE3F: case 0xFE41:
		case 0xFE43: case 0xFE59: case 0xFE5B:
		case 0xFE5D: case 0xFF08: case 0xFF3B:
		case 0xFF5B: case 0xFF62:
			return BREAKABLE_BEFORE;
	}
	return BREAKABLE_AFTER;
}

/*
ZLUnicodeUtil::Ucs4Char ZLUnicodeUtil::toLower(Ucs4Char ch) {
	initUnicodeTable();
	std::map<ZLUnicodeUtil::Ucs4Char,ZLUnicodeData>::const_iterator it = UNICODE_TABLE.find(ch);
	return (it != UNICODE_TABLE.end()) ? it->second.LowerCase : ch;
}

void ZLUnicodeUtil::toLower(Ucs4String &str) {
	for (Ucs4String::iterator it = str.begin(); it != str.end(); ++it) {
		*it = toLower(*it);
	}
}
*/

std::string ZLUnicodeUtil::toLower(const std::string &utf8String) {
	/*
	Ucs4String ucs4String;
	utf8ToUcs4(ucs4String, utf8String);

	toLower(ucs4String);

	std::string result;
	ucs4ToUtf8(result, ucs4String, utf8String.length());
	return result;
	*/
	if (utf8String.empty()) {
		return utf8String;
	}

	bool isAscii = true;
	const int size = utf8String.size();
	for (int i = size - 1; i >= 0; --i) {
		if ((utf8String[i] & 0x80) != 0) {
			isAscii = false;
			break;
		}
	}
	if (isAscii) {
		std::string result(size, ' ');
		for (int i = size - 1; i >= 0; --i) {
			result[i] = std::tolower(utf8String[i]);
		}
		return result;
	}
	JNIEnv *env = AndroidUtil::getEnv();
	jstring javaString = AndroidUtil::createJavaString(env, utf8String);
	jstring lowerCased = AndroidUtil::Method_java_lang_String_toLowerCase->callForJavaString(javaString);
	if (javaString == lowerCased) {
		env->DeleteLocalRef(lowerCased);
		env->DeleteLocalRef(javaString);
		return utf8String;
	} else {
		const std::string result = AndroidUtil::fromJavaString(env, lowerCased);
		env->DeleteLocalRef(lowerCased);
		env->DeleteLocalRef(javaString);
		return result;
	}
}

/*
ZLUnicodeUtil::Ucs4Char ZLUnicodeUtil::toUpper(Ucs4Char ch) {
	initUnicodeTable();
	std::map<ZLUnicodeUtil::Ucs4Char,ZLUnicodeData>::const_iterator it = UNICODE_TABLE.find(ch);
	return (it != UNICODE_TABLE.end()) ? it->second.UpperCase : ch;
}

void ZLUnicodeUtil::toUpper(Ucs4String &str) {
	for (Ucs4String::iterator it = str.begin(); it != str.end(); ++it) {
		*it = toUpper(*it);
	}
}
*/

std::string ZLUnicodeUtil::toUpper(const std::string &utf8String) {
	/*
	Ucs4String ucs4String;
	utf8ToUcs4(ucs4String, utf8String);

	toUpper(ucs4String);

	std::string result;
	ucs4ToUtf8(result, ucs4String, utf8String.length());
	return result;
	*/
	if (utf8String.empty()) {
		return utf8String;
	}

	JNIEnv *env = AndroidUtil::getEnv();
	jstring javaString = AndroidUtil::createJavaString(env, utf8String);
	jstring upperCased = AndroidUtil::Method_java_lang_String_toUpperCase->callForJavaString(javaString);
	if (javaString == upperCased) {
		env->DeleteLocalRef(upperCased);
		env->DeleteLocalRef(javaString);
		return utf8String;
	} else {
		const std::string result = AndroidUtil::fromJavaString(env, upperCased);
		env->DeleteLocalRef(upperCased);
		env->DeleteLocalRef(javaString);
		return result;
	}
}

void ZLUnicodeUtil::utf8Trim(std::string &utf8String) {
	std::size_t counter = 0;
	std::size_t length = utf8String.length();
	Ucs4Char chr;
	while (counter < length) {
		const std::size_t l = firstChar(chr, utf8String.data() + counter);
		if (isSpace(chr)) {
			counter += l;
		} else {
			break;
		}
	}
	utf8String.erase(0, counter);
	length -= counter;

	std::size_t r_counter = length;
	while (r_counter > 0) {
		const std::size_t l = lastChar(chr, utf8String.data() + r_counter);
		if (isSpace(chr)) {
			r_counter -= l;
		} else {
			break;
		}
	}
	utf8String.erase(r_counter, length - r_counter);
}
