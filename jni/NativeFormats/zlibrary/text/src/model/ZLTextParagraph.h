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

#ifndef __ZLTEXTPARAGRAPH_H__
#define __ZLTEXTPARAGRAPH_H__

#include <map>
#include <vector>
#include <string>

#include <shared_ptr.h>

#include <ZLHyperlinkType.h>
#include <ZLTextKind.h>
#include <ZLTextAlignmentType.h>

class ZLImage;
typedef std::map<std::string,shared_ptr<const ZLImage> > ZLImageMap;

class ZLTextParagraphEntry {

public:
	enum Kind {
		TEXT_ENTRY = 1,
		IMAGE_ENTRY = 2,
		CONTROL_ENTRY = 3,
		HYPERLINK_CONTROL_ENTRY = 4,
		STYLE_CSS_ENTRY = 5,
		STYLE_OTHER_ENTRY = 6,
		STYLE_CLOSE_ENTRY = 7,
		FIXED_HSPACE_ENTRY = 8,
		RESET_BIDI_ENTRY = 9,
		AUDIO_ENTRY = 10,
		VIDEO_ENTRY = 11,
		EXTENSION_ENTRY = 12,
	};

protected:
	ZLTextParagraphEntry();

public:
	virtual ~ZLTextParagraphEntry();

private: // disable copying
	ZLTextParagraphEntry(const ZLTextParagraphEntry &entry);
	const ZLTextParagraphEntry &operator = (const ZLTextParagraphEntry &entry);
};

class ZLTextControlEntry : public ZLTextParagraphEntry {

protected:
	ZLTextControlEntry(ZLTextKind kind, bool isStart);

public:
	virtual ~ZLTextControlEntry();
	ZLTextKind kind() const;
	bool isStart() const;
	virtual bool isHyperlink() const;

private:
	ZLTextKind myKind;
	bool myStart;

friend class ZLTextControlEntryPool;
};

class ZLTextControlEntryPool {

public:
	static ZLTextControlEntryPool Pool;

public:
	ZLTextControlEntryPool();
	~ZLTextControlEntryPool();
	shared_ptr<ZLTextParagraphEntry> controlEntry(ZLTextKind kind, bool isStart);

private:
	std::map<ZLTextKind, shared_ptr<ZLTextParagraphEntry> > myStartEntries;
	std::map<ZLTextKind, shared_ptr<ZLTextParagraphEntry> > myEndEntries;
};

class ZLTextFixedHSpaceEntry : public ZLTextParagraphEntry {

public:
	ZLTextFixedHSpaceEntry(unsigned char length);
	unsigned char length() const;

private:
	const unsigned char myLength;
};

class ZLTextHyperlinkControlEntry : public ZLTextControlEntry {

public:
	//ZLTextHyperlinkControlEntry(const char *address);
	~ZLTextHyperlinkControlEntry();
	const std::string &label() const;
	ZLHyperlinkType hyperlinkType() const;
	bool isHyperlink() const;

private:
	std::string myLabel;
	ZLHyperlinkType myHyperlinkType;
};

class ZLTextEntry : public ZLTextParagraphEntry {

public:
	//ZLTextEntry(const char *address);
	~ZLTextEntry();

	std::size_t dataLength() const;
	const char *data() const;

private:
	std::string myText;
};

class ImageEntry : public ZLTextParagraphEntry {

public:
	//ImageEntry(const char *address);
	~ImageEntry();
	const std::string &id() const;
	short vOffset() const;

private:
	std::string myId;
	short myVOffset;
};

class ResetBidiEntry : public ZLTextParagraphEntry {

public:
	static const shared_ptr<ZLTextParagraphEntry> Instance;

private:
	ResetBidiEntry();
};

class ExtensionEntry : public ZLTextParagraphEntry {

public:
	~ExtensionEntry();
	const std::string &action() const;
	const std::string &data() const;

private:
	std::string myAction;
	std::string myData;
};

class ZLTextParagraph {

public:
/*
	class Iterator {

	public:
		Iterator(const ZLTextParagraph &paragraph);
		~Iterator();

		bool isEnd() const;
		void next();
		const shared_ptr<ZLTextParagraphEntry> entry() const;
		ZLTextParagraphEntry::Kind entryKind() const;

	private:
		char *myPointer;
		std::size_t myIndex;
		std::size_t myEndIndex;
		mutable shared_ptr<ZLTextParagraphEntry> myEntry;
	};
*/

	enum Kind {
		TEXT_PARAGRAPH = 0,
		TREE_PARAGRAPH = 1,
		EMPTY_LINE_PARAGRAPH = 2,
		BEFORE_SKIP_PARAGRAPH = 3,
		AFTER_SKIP_PARAGRAPH = 4,
		END_OF_SECTION_PARAGRAPH = 5,
		PSEUDO_END_OF_SECTION_PARAGRAPH = 6,
		END_OF_TEXT_PARAGRAPH = 7,
		ENCRYPTED_SECTION_PARAGRAPH = 8,
	};

protected:
	ZLTextParagraph();

public:
	virtual ~ZLTextParagraph();
	virtual Kind kind() const;

	std::size_t entryNumber() const;

	//std::size_t textDataLength() const;
	//std::size_t characterNumber() const;

private:
	void addEntry(char *address);

private:
	char *myFirstEntryAddress;
	std::size_t myEntryNumber;

friend class Iterator;
friend class ZLTextModel;
friend class ZLTextPlainModel;
};

class ZLTextSpecialParagraph : public ZLTextParagraph {

private:
	ZLTextSpecialParagraph(Kind kind);

public:
	~ZLTextSpecialParagraph();
	Kind kind() const;

private:
	Kind myKind;

friend class ZLTextPlainModel;
};

inline ZLTextParagraphEntry::ZLTextParagraphEntry() {}
inline ZLTextParagraphEntry::~ZLTextParagraphEntry() {}

inline ZLTextControlEntry::ZLTextControlEntry(ZLTextKind kind, bool isStart) : myKind(kind), myStart(isStart) {}
inline ZLTextControlEntry::~ZLTextControlEntry() {}
inline ZLTextKind ZLTextControlEntry::kind() const { return myKind; }
inline bool ZLTextControlEntry::isStart() const { return myStart; }
inline bool ZLTextControlEntry::isHyperlink() const { return false; }

inline ZLTextFixedHSpaceEntry::ZLTextFixedHSpaceEntry(unsigned char length) : myLength(length) {}
inline unsigned char ZLTextFixedHSpaceEntry::length() const { return myLength; }

inline ZLTextControlEntryPool::ZLTextControlEntryPool() {}
inline ZLTextControlEntryPool::~ZLTextControlEntryPool() {}

inline ZLTextHyperlinkControlEntry::~ZLTextHyperlinkControlEntry() {}
inline const std::string &ZLTextHyperlinkControlEntry::label() const { return myLabel; }
inline ZLHyperlinkType ZLTextHyperlinkControlEntry::hyperlinkType() const { return myHyperlinkType; }
inline bool ZLTextHyperlinkControlEntry::isHyperlink() const { return true; }

inline ZLTextEntry::~ZLTextEntry() {}
inline const char *ZLTextEntry::data() const { return myText.data(); }
inline std::size_t ZLTextEntry::dataLength() const { return myText.length(); }

inline ImageEntry::~ImageEntry() {}
inline const std::string &ImageEntry::id() const { return myId; }
inline short ImageEntry::vOffset() const { return myVOffset; }

inline ResetBidiEntry::ResetBidiEntry() {}

inline ExtensionEntry::~ExtensionEntry() {}
inline const std::string &ExtensionEntry::action() const { return myAction; }
inline const std::string &ExtensionEntry::data() const { return myData; }

inline ZLTextParagraph::ZLTextParagraph() : myEntryNumber(0) {}
inline ZLTextParagraph::~ZLTextParagraph() {}
inline ZLTextParagraph::Kind ZLTextParagraph::kind() const { return TEXT_PARAGRAPH; }
inline std::size_t ZLTextParagraph::entryNumber() const { return myEntryNumber; }
inline void ZLTextParagraph::addEntry(char *address) { if (myEntryNumber == 0) myFirstEntryAddress = address; ++myEntryNumber; }

//inline ZLTextParagraph::Iterator::Iterator(const ZLTextParagraph &paragraph) : myPointer(paragraph.myFirstEntryAddress), myIndex(0), myEndIndex(paragraph.entryNumber()) {}
//inline ZLTextParagraph::Iterator::~Iterator() {}
//inline bool ZLTextParagraph::Iterator::isEnd() const { return myIndex == myEndIndex; }
//inline ZLTextParagraphEntry::Kind ZLTextParagraph::Iterator::entryKind() const { return (ZLTextParagraphEntry::Kind)*myPointer; }

inline ZLTextSpecialParagraph::ZLTextSpecialParagraph(Kind kind) : myKind(kind) {}
inline ZLTextSpecialParagraph::~ZLTextSpecialParagraph() {}
inline ZLTextParagraph::Kind ZLTextSpecialParagraph::kind() const { return myKind; }

#endif /* __ZLTEXTPARAGRAPH_H__ */
