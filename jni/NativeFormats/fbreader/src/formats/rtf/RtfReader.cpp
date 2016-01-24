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
#include <cctype>

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLUnicodeUtil.h>

#include "RtfReader.h"

std::map<std::string, RtfCommand*> RtfReader::ourKeywordMap;

static const int rtfStreamBufferSize = 4096;

RtfReader::RtfReader(const std::string &encoding) : EncodedTextReader(encoding) {
}

RtfReader::~RtfReader() {
}

RtfCommand::~RtfCommand() {
}

void RtfDummyCommand::run(RtfReader&, int*) const {
}

void RtfNewParagraphCommand::run(RtfReader &reader, int*) const {
	reader.newParagraph();
}

RtfFontPropertyCommand::RtfFontPropertyCommand(RtfReader::FontProperty property) : myProperty(property) {
}

void RtfFontPropertyCommand::run(RtfReader &reader, int *parameter) const {
	const bool start = (parameter == 0) || (*parameter != 0);
	switch (myProperty) {
		case RtfReader::FONT_BOLD:
			if (reader.myState.Bold != start) {
				reader.myState.Bold = start;
				reader.setFontProperty(RtfReader::FONT_BOLD);
			}
			break;
		case RtfReader::FONT_ITALIC:
			if (reader.myState.Italic != start) {
				reader.myState.Italic = start;
				reader.setFontProperty(RtfReader::FONT_ITALIC);
			}
			break;
		case RtfReader::FONT_UNDERLINED:
			if (reader.myState.Underlined != start) {
				reader.myState.Underlined = start;
				reader.setFontProperty(RtfReader::FONT_UNDERLINED);
			}
			break;
	}
}

RtfAlignmentCommand::RtfAlignmentCommand(ZLTextAlignmentType alignment) : myAlignment(alignment) {
}

void RtfAlignmentCommand::run(RtfReader &reader, int*) const {
	if (reader.myState.Alignment != myAlignment) {
		reader.myState.Alignment = myAlignment;
		reader.setAlignment();
	}
}

RtfCharCommand::RtfCharCommand(const std::string &chr) : myChar(chr) {
}

void RtfCharCommand::run(RtfReader &reader, int*) const {
	reader.processCharData(myChar.data(), myChar.length(), false);
}

RtfDestinationCommand::RtfDestinationCommand(RtfReader::DestinationType destination) : myDestination(destination) {
}

void RtfDestinationCommand::run(RtfReader &reader, int*) const {
	if (reader.myState.Destination == myDestination) {
		return;
	}
	reader.myState.Destination = myDestination;
	if (myDestination == RtfReader::DESTINATION_PICTURE) {
		reader.myState.ReadDataAsHex = true;
		reader.myNextImageMimeType.clear();
	}
	reader.switchDestination(myDestination, true);
}

void RtfStyleCommand::run(RtfReader &reader, int*) const {
	if (reader.myState.Destination == RtfReader::DESTINATION_STYLESHEET) {
		//std::cerr << "Add style index: " << val << "\n";

		//sprintf(style_attributes[0], "%i", val);
	} else /*if (myState.Destination == rdsContent)*/ {
		//std::cerr << "Set style index: " << val << "\n";

		//sprintf(style_attributes[0], "%i", val);
	}
}

void RtfCodepageCommand::run(RtfReader &reader, int *parameter) const {
	if (parameter != 0) {
		reader.setEncoding(*parameter);
	}
}

void RtfSpecialCommand::run(RtfReader &reader, int*) const {
	reader.mySpecialMode = true;
}

RtfPictureCommand::RtfPictureCommand(const std::string &mimeType) : myMimeType(mimeType) {
}

void RtfPictureCommand::run(RtfReader &reader, int*) const {
	reader.myNextImageMimeType = myMimeType;
}

void RtfFontResetCommand::run(RtfReader &reader, int*) const {
	if (reader.myState.Bold) {
		reader.myState.Bold = false;
		reader.setFontProperty(RtfReader::FONT_BOLD);
	}
	if (reader.myState.Italic) {
		reader.myState.Italic = false;
		reader.setFontProperty(RtfReader::FONT_ITALIC);
	}
	if (reader.myState.Underlined) {
		reader.myState.Underlined = false;
		reader.setFontProperty(RtfReader::FONT_UNDERLINED);
	}
}

void RtfReader::addAction(const std::string &tag, RtfCommand *command) {
	ourKeywordMap.insert(std::make_pair(tag, command));
}

void RtfReader::fillKeywordMap() {
	if (ourKeywordMap.empty()) {
		addAction("*",	new RtfSpecialCommand());
		addAction("ansicpg",	new RtfCodepageCommand());

		static const char *keywordsToSkip[] = {"buptim", "colortbl", "comment", "creatim", "doccomm", "fonttbl", "footer", "footerf", "footerl", "footerr", "ftncn", "ftnsep", "ftnsepc", "header", "headerf", "headerl", "headerr", "keywords", "operator", "printim", "private1", "revtim", "rxe", "subject", "tc", "txe", "xe", 0};
		RtfCommand *skipCommand = new RtfDestinationCommand(RtfReader::DESTINATION_SKIP);
		for (const char **i = keywordsToSkip; *i != 0; ++i) {
			addAction(*i,	skipCommand);
		}
		addAction("shppict",	new RtfDummyCommand());
		addAction("info",	new RtfDestinationCommand(RtfReader::DESTINATION_INFO));
		addAction("title",	new RtfDestinationCommand(RtfReader::DESTINATION_TITLE));
		addAction("author",	new RtfDestinationCommand(RtfReader::DESTINATION_AUTHOR));
		addAction("pict",	new RtfDestinationCommand(RtfReader::DESTINATION_PICTURE));
		addAction("stylesheet",	new RtfDestinationCommand(RtfReader::DESTINATION_STYLESHEET));
		addAction("footnote",	new RtfDestinationCommand(RtfReader::DESTINATION_FOOTNOTE));

		RtfCommand *newParagraphCommand = new RtfNewParagraphCommand();
		addAction("\n",	newParagraphCommand);
		addAction("\r",	newParagraphCommand);
		addAction("par",	newParagraphCommand);

		addAction("\x09",	new RtfCharCommand("\x09"));
		addAction("_",	new RtfCharCommand("-"));
		addAction("\\",	new RtfCharCommand("\\"));
		addAction("{",	new RtfCharCommand("{"));
		addAction("}",	new RtfCharCommand("}"));
		addAction("bullet",	new RtfCharCommand("\xE2\x80\xA2"));		 // &bull;
		addAction("endash",	new RtfCharCommand("\xE2\x80\x93"));		 // &ndash;
		addAction("emdash",	new RtfCharCommand("\xE2\x80\x94"));		 // &mdash;
		addAction("~",	new RtfCharCommand("\xC0\xA0"));					// &nbsp;
		addAction("enspace",	new RtfCharCommand("\xE2\x80\x82"));		// &emsp;
		addAction("emspace",	new RtfCharCommand("\xE2\x80\x83"));		// &ensp;
		addAction("lquote",	new RtfCharCommand("\xE2\x80\x98"));		 // &lsquo;
		addAction("rquote",	new RtfCharCommand("\xE2\x80\x99"));		 // &rsquo;
		addAction("ldblquote",	new RtfCharCommand("\xE2\x80\x9C"));	// &ldquo;
		addAction("rdblquote",	new RtfCharCommand("\xE2\x80\x9D"));	// &rdquo;

		addAction("jpegblip",	new RtfPictureCommand("image/jpeg"));
		addAction("pngblip",	new RtfPictureCommand("image/png"));

		addAction("s",	new RtfStyleCommand());

		addAction("qc",	new RtfAlignmentCommand(ALIGN_CENTER));
		addAction("ql",	new RtfAlignmentCommand(ALIGN_LEFT));
		addAction("qr",	new RtfAlignmentCommand(ALIGN_RIGHT));
		addAction("qj",	new RtfAlignmentCommand(ALIGN_JUSTIFY));
		addAction("pard",	new RtfAlignmentCommand(ALIGN_UNDEFINED));

		addAction("b",	new RtfFontPropertyCommand(RtfReader::FONT_BOLD));
		addAction("i",	new RtfFontPropertyCommand(RtfReader::FONT_ITALIC));
		addAction("u",	new RtfFontPropertyCommand(RtfReader::FONT_UNDERLINED));
		addAction("plain",	new RtfFontResetCommand());
	}
}

bool RtfReader::parseDocument() {
	enum {
		READ_NORMAL_DATA,
		READ_BINARY_DATA,
		READ_HEX_SYMBOL,
		READ_KEYWORD,
		READ_KEYWORD_PARAMETER,
		READ_END_OF_FILE
	} parserState = READ_NORMAL_DATA;

	std::string keyword;
	std::string parameterString;
	std::string hexString;
	int imageStartOffset = -1;

	while (!myIsInterrupted) {
		const char *ptr = myStreamBuffer;
		const char *end = myStreamBuffer + myStream->read(myStreamBuffer, rtfStreamBufferSize);
		if (ptr == end) {
			break;
		}
		const char *dataStart = ptr;
		bool readNextChar = true;
		while (ptr != end) {
			switch (parserState) {
				case READ_END_OF_FILE:
					if (*ptr != '}' && !std::isspace(*ptr)) {
						return false;
					}
					break;
				case READ_BINARY_DATA:
					// TODO: optimize
					processCharData(ptr, 1);
					--myBinaryDataSize;
					if (myBinaryDataSize == 0) {
						parserState = READ_NORMAL_DATA;
					}
					break;
				case READ_NORMAL_DATA:
					switch (*ptr) {
						case '{':
							if (ptr > dataStart) {
								processCharData(dataStart, ptr - dataStart);
							}
							dataStart = ptr + 1;
							myStateStack.push(myState);
							myState.ReadDataAsHex = false;
							break;
						case '}':
						{
							if (ptr > dataStart) {
								processCharData(dataStart, ptr - dataStart);
							}
							dataStart = ptr + 1;

							if (imageStartOffset >= 0) {
								if (!myNextImageMimeType.empty()) {
									const int imageSize = myStream->offset() + (ptr - end) - imageStartOffset;
									insertImage(myNextImageMimeType, myFileName, imageStartOffset, imageSize);
								}
								imageStartOffset = -1;
							}

							if (myStateStack.empty()) {
								parserState = READ_END_OF_FILE;
								break;
							}

							if (myState.Destination != myStateStack.top().Destination) {
								switchDestination(myState.Destination, false);
								switchDestination(myStateStack.top().Destination, true);
							}

							bool oldItalic = myState.Italic;
							bool oldBold = myState.Bold;
							bool oldUnderlined = myState.Underlined;
							ZLTextAlignmentType oldAlignment = myState.Alignment;
							myState = myStateStack.top();
							myStateStack.pop();

							if (myState.Italic != oldItalic) {
								setFontProperty(RtfReader::FONT_ITALIC);
							}
							if (myState.Bold != oldBold) {
								setFontProperty(RtfReader::FONT_BOLD);
							}
							if (myState.Underlined != oldUnderlined) {
								setFontProperty(RtfReader::FONT_UNDERLINED);
							}
							if (myState.Alignment != oldAlignment) {
								setAlignment();
							}

							break;
						}
						case '\\':
							if (ptr > dataStart) {
								processCharData(dataStart, ptr - dataStart);
							}
							dataStart = ptr + 1;
							keyword.erase();
							parserState = READ_KEYWORD;
							break;
						case 0x0d:
						case 0x0a:			// cr and lf are noise characters...
							if (ptr > dataStart) {
								processCharData(dataStart, ptr - dataStart);
							}
							dataStart = ptr + 1;
							break;
						default:
							if (myState.ReadDataAsHex) {
								if (imageStartOffset == -1) {
									imageStartOffset = myStream->offset() + (ptr - end);
								}
							}
							break;
					}
					break;
				case READ_HEX_SYMBOL:
					hexString += *ptr;
					if (hexString.size() == 2) {
						char ch = std::strtol(hexString.c_str(), 0, 16);
						hexString.erase();
						processCharData(&ch, 1);
						parserState = READ_NORMAL_DATA;
						dataStart = ptr + 1;
					}
					break;
				case READ_KEYWORD:
					if (!std::isalpha(*ptr)) {
						if (ptr == dataStart && keyword.empty()) {
							if (*ptr == '\'') {
								parserState = READ_HEX_SYMBOL;
							} else {
								keyword = *ptr;
								processKeyword(keyword);
								parserState = READ_NORMAL_DATA;
							}
							dataStart = ptr + 1;
						} else {
							keyword.append(dataStart, ptr - dataStart);
							if (*ptr == '-' || std::isdigit(*ptr)) {
								dataStart = ptr;
								parserState = READ_KEYWORD_PARAMETER;
							} else {
								readNextChar = *ptr == ' ';
								processKeyword(keyword);
								parserState = READ_NORMAL_DATA;
								dataStart = readNextChar ? ptr + 1 : ptr;
							}
						}
					}
					break;
				case READ_KEYWORD_PARAMETER:
					if (!std::isdigit(*ptr)) {
						parameterString.append(dataStart, ptr - dataStart);
						int parameter = std::atoi(parameterString.c_str());
						parameterString.erase();
						readNextChar = *ptr == ' ';
						if (keyword == "bin" && parameter > 0) {
							myBinaryDataSize = parameter;
							parserState = READ_BINARY_DATA;
						} else if (keyword == "u") {
							// TODO: implement commands of form "\ucL\uN" (insert symbol N + skip L bytes)
							processUnicodeCharacter(parameter);
							readNextChar &= *ptr != '\\';
							parserState = READ_NORMAL_DATA;
						} else {
							processKeyword(keyword, &parameter);
							parserState = READ_NORMAL_DATA;
						}
						dataStart = readNextChar ? ptr + 1 : ptr;
					}
					break;
			}
			if (readNextChar) {
				++ptr;
			} else {
				readNextChar = true;
			}
		}
		if (dataStart < end) {
			switch (parserState) {
				case READ_NORMAL_DATA:
					processCharData(dataStart, end - dataStart);
				case READ_KEYWORD:
					keyword.append(dataStart, end - dataStart);
					break;
				case READ_KEYWORD_PARAMETER:
					parameterString.append(dataStart, end - dataStart);
					break;
				default:
					break;
			}
		}
	}

	return myIsInterrupted || myStateStack.empty();
}

void RtfReader::processKeyword(const std::string &keyword, int *parameter) {
	const bool wasSpecialMode = mySpecialMode;
	mySpecialMode = false;
	if (myState.Destination == RtfReader::DESTINATION_SKIP) {
		return;
	}

	std::map<std::string, RtfCommand*>::const_iterator it = ourKeywordMap.find(keyword);

	if (it == ourKeywordMap.end()) {
		if (wasSpecialMode) {
			myState.Destination = RtfReader::DESTINATION_SKIP;
		}
		return;
	}

	it->second->run(*this, parameter);
}

void RtfReader::processUnicodeCharacter(int character) {
	static char buffer[8];
	const int len = ZLUnicodeUtil::ucs4ToUtf8(buffer, character);
	processCharData(buffer, len, false);
}

void RtfReader::processCharData(const char *data, std::size_t len, bool convert) {
	if (myState.Destination != RtfReader::DESTINATION_SKIP) {
		addCharData(data, len, convert);
	}
}

void RtfReader::interrupt() {
	myIsInterrupted = true;
}

bool RtfReader::readDocument(const ZLFile &file) {
	myFileName = file.path();
	myStream = file.inputStream();
	if (myStream.isNull() || !myStream->open()) {
			return false;
	}

	fillKeywordMap();

	myStreamBuffer = new char[rtfStreamBufferSize];

	myIsInterrupted = false;

	mySpecialMode = false;

	myState.Alignment = ALIGN_UNDEFINED;
	myState.Italic = false;
	myState.Bold = false;
	myState.Underlined = false;
	myState.Destination = RtfReader::DESTINATION_NONE;
	myState.ReadDataAsHex = false;

	bool code = parseDocument();

	while (!myStateStack.empty()) {
		myStateStack.pop();
	}

	delete[] myStreamBuffer;
	myStream->close();

	return code;
}
