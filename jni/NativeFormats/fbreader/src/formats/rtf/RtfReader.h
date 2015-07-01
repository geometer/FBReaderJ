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

#ifndef __RTFREADER_H__
#define __RTFREADER_H__

#include <string>
#include <map>
#include <stack>

#include <ZLEncodingConverter.h>

#include <ZLTextAlignmentType.h>

#include "../EncodedTextReader.h"

class ZLFile;
class ZLInputStream;
class RtfCommand;

class RtfReader : public EncodedTextReader {

private:
	static void fillKeywordMap();
	static void addAction(const std::string &tag, RtfCommand *command);

private:
	static std::map<std::string, RtfCommand*> ourKeywordMap;

protected:
	RtfReader(const std::string &encoding);
	virtual ~RtfReader();

public:
	virtual bool readDocument(const ZLFile &file);

protected:
	enum DestinationType {
		DESTINATION_NONE,
		DESTINATION_SKIP,
		DESTINATION_INFO,
		DESTINATION_TITLE,
		DESTINATION_AUTHOR,
		DESTINATION_PICTURE,
		DESTINATION_STYLESHEET,
		DESTINATION_FOOTNOTE,
	};

	enum FontProperty {
		FONT_BOLD,
		FONT_ITALIC,
		FONT_UNDERLINED
	};

	virtual void addCharData(const char *data, std::size_t len, bool convert) = 0;
	virtual void insertImage(const std::string &mimeType, const std::string &fileName, std::size_t startOffset, std::size_t size) = 0;
	virtual void setEncoding(int code) = 0;
	virtual void switchDestination(DestinationType destination, bool on) = 0;
	virtual void setAlignment() = 0;
	virtual void setFontProperty(FontProperty property) = 0;
	virtual void newParagraph() = 0;

	void interrupt();

private:
	bool parseDocument();
	void processKeyword(const std::string &keyword, int *parameter = 0);
	void processCharData(const char *data, std::size_t len, bool convert = true);
	void processUnicodeCharacter(int character);

protected:
	struct RtfReaderState {
		bool Bold;
		bool Italic;
		bool Underlined;
		ZLTextAlignmentType Alignment;
		DestinationType Destination;

		bool ReadDataAsHex;
	};

	RtfReaderState myState;

private:
	bool mySpecialMode;

	std::string myFileName;
	shared_ptr<ZLInputStream> myStream;
	char *myStreamBuffer;

	std::stack<RtfReaderState> myStateStack;

	int myBinaryDataSize;
	std::string myNextImageMimeType;

	int myIsInterrupted;

friend class RtfNewParagraphCommand;
friend class RtfFontPropertyCommand;
friend class RtfAlignmentCommand;
friend class RtfCharCommand;
friend class RtfDestinationCommand;
friend class RtfStyleCommand;
friend class RtfSpecialCommand;
friend class RtfPictureCommand;
friend class RtfFontResetCommand;
friend class RtfCodepageCommand;
};

class RtfCommand {
protected:
	virtual ~RtfCommand();

public:
	virtual void run(RtfReader &reader, int *parameter) const = 0;
};

class RtfDummyCommand : public RtfCommand {
public:
	void run(RtfReader &reader, int *parameter) const;
};

class RtfNewParagraphCommand : public RtfCommand {
public:
	void run(RtfReader &reader, int *parameter) const;
};

class RtfFontPropertyCommand : public RtfCommand {

public:
	RtfFontPropertyCommand(RtfReader::FontProperty property);
	void run(RtfReader &reader, int *parameter) const;

private:
	RtfReader::FontProperty myProperty;
};

class RtfAlignmentCommand : public RtfCommand {
public:
	RtfAlignmentCommand(ZLTextAlignmentType alignment);
	void run(RtfReader &reader, int *parameter) const;

private:
	ZLTextAlignmentType myAlignment;
};

class RtfCharCommand : public RtfCommand {
public:
	RtfCharCommand(const std::string &chr);
	void run(RtfReader &reader, int *parameter) const;

private:
	std::string myChar;
};

class RtfDestinationCommand : public RtfCommand {
public:
	RtfDestinationCommand(RtfReader::DestinationType dest);
	void run(RtfReader &reader, int *parameter) const;

private:
	RtfReader::DestinationType myDestination;
};

class RtfStyleCommand : public RtfCommand {
public:
	void run(RtfReader &reader, int *parameter) const;
};

class RtfSpecialCommand : public RtfCommand {
	void run(RtfReader &reader, int *parameter) const;
};

class RtfPictureCommand : public RtfCommand {
public:
	RtfPictureCommand(const std::string &mimeType);
	void run(RtfReader &reader, int *parameter) const;

private:
	const std::string myMimeType;
};

class RtfFontResetCommand : public RtfCommand {
public:
	void run(RtfReader &reader, int *parameter) const;
};

class RtfCodepageCommand : public RtfCommand {
public:
	void run(RtfReader &reader, int *parameter) const;
};

#endif /* __RTFREADER_H__ */
