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

#ifndef __ZLTEXTPARAGRAPH_H__
#define __ZLTEXTPARAGRAPH_H__

#include <map>
#include <vector>
#include <string>

#include <shared_ptr.h>

#include <ZLTextKind.h>
#include <ZLTextAlignmentType.h>
#include <ZLTextFontModifier.h>

class ZLImage;
typedef std::map<std::string,shared_ptr<const ZLImage> > ZLImageMap;

class ZLTextParagraphEntry {

public:
	enum Kind {
		TEXT_ENTRY = 1,
		IMAGE_ENTRY = 2,
		CONTROL_ENTRY = 3,
		HYPERLINK_CONTROL_ENTRY = 4,
		STYLE_ENTRY = 5,
		FIXED_HSPACE_ENTRY = 6,
		RESET_BIDI_ENTRY = 7,
	};

protected:
	ZLTextParagraphEntry();

public:
	virtual ~ZLTextParagraphEntry();

private: // disable copying
	ZLTextParagraphEntry(const ZLTextParagraphEntry &entry);
	const ZLTextParagraphEntry &operator = (const ZLTextParagraphEntry &entry);
};

class ZLTextStyleEntry : public ZLTextParagraphEntry {

public:
	enum SizeUnit {
		SIZE_UNIT_PIXEL,
		SIZE_UNIT_EM_100,
		SIZE_UNIT_EX_100,
		SIZE_UNIT_PERCENT
	};

	struct Metrics {
		Metrics(int fontSize, int fontXHeight, int fullWidth, int fullHeight);

		int FontSize;
		int FontXHeight;
		int FullWidth;
		int FullHeight;
	};

	enum Length {
		LENGTH_LEFT_INDENT = 0,
		LENGTH_RIGHT_INDENT = 1,
		LENGTH_FIRST_LINE_INDENT_DELTA = 2,
		LENGTH_SPACE_BEFORE = 3,
		LENGTH_SPACE_AFTER = 4,
		NUMBER_OF_LENGTHS = 5,
	};

private:
	struct LengthType {
		SizeUnit Unit;
		short Size;
	};

public:
	ZLTextStyleEntry();
	ZLTextStyleEntry(char *address);
	~ZLTextStyleEntry();

	bool isEmpty() const;

	bool lengthSupported(Length name) const;
	short length(Length name, const Metrics &metrics) const;
	void setLength(Length name, short length, SizeUnit unit);

	bool alignmentTypeSupported() const;
	ZLTextAlignmentType alignmentType() const;
	void setAlignmentType(ZLTextAlignmentType alignmentType);

	unsigned char supportedFontModifier() const;
	unsigned char fontModifier() const;
	void setFontModifier(ZLTextFontModifier style, bool set);

	bool fontSizeSupported() const;
	signed char fontSizeMag() const;
	void setFontSizeMag(signed char fontSizeMag);

	bool fontFamilySupported() const;
	const std::string &fontFamily() const;
	void setFontFamily(const std::string &fontFamily);

	static const int SUPPORT_ALIGNMENT_TYPE = 1 << NUMBER_OF_LENGTHS;
	static const int SUPPORT_FONT_SIZE = 1 << (NUMBER_OF_LENGTHS + 1);
	static const int SUPPORT_FONT_FAMILY = 1 << (NUMBER_OF_LENGTHS + 2);

private:
	int myMask;

	LengthType myLengths[NUMBER_OF_LENGTHS];

	ZLTextAlignmentType myAlignmentType;
	unsigned char mySupportedFontModifier;
	unsigned char myFontModifier;
	signed char myFontSizeMag;
	std::string myFontFamily;

friend class ZLTextModel;
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

class ZLTextFixedHSpaceEntry : public ZLTextParagraphEntry {

public:
	ZLTextFixedHSpaceEntry(unsigned char length);
	unsigned char length() const;

private:
	const unsigned char myLength;
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

class ZLTextHyperlinkControlEntry : public ZLTextControlEntry {

public:
	ZLTextHyperlinkControlEntry(const char *address);
	~ZLTextHyperlinkControlEntry();
	const std::string &label() const;
	const std::string &hyperlinkType() const;
	bool isHyperlink() const;

private:
	const std::string myLabel;
	const std::string myHyperlinkType;
};

class ZLTextEntry : public ZLTextParagraphEntry {

public:
	ZLTextEntry(const char *address);
	~ZLTextEntry();

	size_t dataLength() const;
	const char *data() const;
 
private:
	const char *myAddress;
};

class ImageEntry : public ZLTextParagraphEntry {

public:
	ImageEntry(const std::string &id, const ZLImageMap *imageMap, short vOffset);
	~ImageEntry();
	const std::string &id() const;
	shared_ptr<const ZLImage> image() const;
	short vOffset() const;

private:
	const std::string myId;
	const ZLImageMap *myMap;
	const short myVOffset;
};

class ResetBidiEntry : public ZLTextParagraphEntry {

public:
	static const shared_ptr<ZLTextParagraphEntry> Instance;

private:
	ResetBidiEntry();
};

class ZLTextParagraph {

public:
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
		size_t myIndex;
		size_t myEndIndex;
		mutable shared_ptr<ZLTextParagraphEntry> myEntry;
	};

	enum Kind {
		TEXT_PARAGRAPH,
		TREE_PARAGRAPH,
		EMPTY_LINE_PARAGRAPH,
		BEFORE_SKIP_PARAGRAPH,
		AFTER_SKIP_PARAGRAPH,
		END_OF_SECTION_PARAGRAPH,
		END_OF_TEXT_PARAGRAPH,
	};

protected:
	ZLTextParagraph();

public:
	virtual ~ZLTextParagraph();
	virtual Kind kind() const;

	size_t entryNumber() const;

	size_t textDataLength() const;
	size_t characterNumber() const;

private:
	void addEntry(char *address);

private:
	char *myFirstEntryAddress;
	size_t myEntryNumber;

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

class ZLTextTreeParagraph : public ZLTextParagraph {

public:
	ZLTextTreeParagraph(ZLTextTreeParagraph *parent = 0);
	~ZLTextTreeParagraph();
	Kind kind() const;

	bool isOpen() const;
	void open(bool o);
	void openTree();
	int depth() const;
	ZLTextTreeParagraph *parent();
	const ZLTextTreeParagraph *parent() const;
	const std::vector<ZLTextTreeParagraph*> &children() const;
	int fullSize() const;

	void removeFromParent();

private:
	void addChild(ZLTextTreeParagraph *child);

private:
	bool myIsOpen;
	int myDepth;
	ZLTextTreeParagraph *myParent;
	std::vector<ZLTextTreeParagraph*> myChildren;
};

inline ZLTextParagraphEntry::ZLTextParagraphEntry() {}
inline ZLTextParagraphEntry::~ZLTextParagraphEntry() {}

inline ZLTextStyleEntry::ZLTextStyleEntry() : myMask(0), mySupportedFontModifier(0), myFontModifier(0) {}
inline ZLTextStyleEntry::~ZLTextStyleEntry() {}

inline ZLTextStyleEntry::Metrics::Metrics(int fontSize, int fontXHeight, int fullWidth, int fullHeight) : FontSize(fontSize), FontXHeight(fontXHeight), FullWidth(fullWidth), FullHeight(fullHeight) {}

inline bool ZLTextStyleEntry::isEmpty() const { return myMask == 0; }

inline bool ZLTextStyleEntry::lengthSupported(Length name) const { return (myMask & (1 << name)) != 0; }
inline void ZLTextStyleEntry::setLength(Length name, short length, SizeUnit unit) {
	myLengths[name].Size = length;
	myLengths[name].Unit = unit;
	myMask |= 1 << name;
}

inline bool ZLTextStyleEntry::alignmentTypeSupported() const { return (myMask & SUPPORT_ALIGNMENT_TYPE) == SUPPORT_ALIGNMENT_TYPE; }
inline ZLTextAlignmentType ZLTextStyleEntry::alignmentType() const { return myAlignmentType; }
inline void ZLTextStyleEntry::setAlignmentType(ZLTextAlignmentType alignmentType) { myAlignmentType = alignmentType; myMask |= SUPPORT_ALIGNMENT_TYPE; }

inline unsigned char ZLTextStyleEntry::supportedFontModifier() const { return mySupportedFontModifier; }
inline unsigned char ZLTextStyleEntry::fontModifier() const { return myFontModifier; }
inline void ZLTextStyleEntry::setFontModifier(ZLTextFontModifier style, bool set) {
	if (set) {
		myFontModifier |= style;
	} else {
		myFontModifier &= ~style;
	}
	mySupportedFontModifier |= style;
}

inline bool ZLTextStyleEntry::fontSizeSupported() const { return (myMask & SUPPORT_FONT_SIZE) == SUPPORT_FONT_SIZE; }
inline signed char ZLTextStyleEntry::fontSizeMag() const { return myFontSizeMag; }
inline void ZLTextStyleEntry::setFontSizeMag(signed char fontSizeMag) { myFontSizeMag = fontSizeMag; myMask |= SUPPORT_FONT_SIZE; }

inline bool ZLTextStyleEntry::fontFamilySupported() const { return (myMask & SUPPORT_FONT_FAMILY) == SUPPORT_FONT_FAMILY; }
inline const std::string &ZLTextStyleEntry::fontFamily() const { return myFontFamily; }
inline void ZLTextStyleEntry::setFontFamily(const std::string &fontFamily) { myFontFamily = fontFamily; myMask |= SUPPORT_FONT_FAMILY; }

inline ZLTextControlEntry::ZLTextControlEntry(ZLTextKind kind, bool isStart) : myKind(kind), myStart(isStart) {}
inline ZLTextControlEntry::~ZLTextControlEntry() {}
inline ZLTextKind ZLTextControlEntry::kind() const { return myKind; }
inline bool ZLTextControlEntry::isStart() const { return myStart; }
inline bool ZLTextControlEntry::isHyperlink() const { return false; }

inline ZLTextFixedHSpaceEntry::ZLTextFixedHSpaceEntry(unsigned char length) : myLength(length) {}
inline unsigned char ZLTextFixedHSpaceEntry::length() const { return myLength; }

inline ZLTextControlEntryPool::ZLTextControlEntryPool() {}
inline ZLTextControlEntryPool::~ZLTextControlEntryPool() {}

inline ZLTextHyperlinkControlEntry::ZLTextHyperlinkControlEntry(const char *address) : ZLTextControlEntry((ZLTextKind)*address, true), myLabel(address + 1), myHyperlinkType(address + myLabel.length() + 2) {}
inline ZLTextHyperlinkControlEntry::~ZLTextHyperlinkControlEntry() {}
inline const std::string &ZLTextHyperlinkControlEntry::label() const { return myLabel; }
inline const std::string &ZLTextHyperlinkControlEntry::hyperlinkType() const { return myHyperlinkType; }
inline bool ZLTextHyperlinkControlEntry::isHyperlink() const { return true; }

inline ZLTextEntry::ZLTextEntry(const char *address) : myAddress(address) {}
inline ZLTextEntry::~ZLTextEntry() {}
inline const char *ZLTextEntry::data() const { return myAddress + sizeof(size_t); }

inline ImageEntry::ImageEntry(const std::string &id, const ZLImageMap *imageMap, short vOffset) : myId(id), myMap(imageMap), myVOffset(vOffset) {}
inline ImageEntry::~ImageEntry() {}
inline const std::string &ImageEntry::id() const { return myId; }
inline short ImageEntry::vOffset() const { return myVOffset; }

inline ZLTextParagraph::ZLTextParagraph() : myEntryNumber(0) {}
inline ZLTextParagraph::~ZLTextParagraph() {}
inline ZLTextParagraph::Kind ZLTextParagraph::kind() const { return TEXT_PARAGRAPH; }
inline size_t ZLTextParagraph::entryNumber() const { return myEntryNumber; }
inline void ZLTextParagraph::addEntry(char *address) { if (myEntryNumber == 0) myFirstEntryAddress = address; ++myEntryNumber; }

inline ZLTextParagraph::Iterator::Iterator(const ZLTextParagraph &paragraph) : myPointer(paragraph.myFirstEntryAddress), myIndex(0), myEndIndex(paragraph.entryNumber()) {}
inline ZLTextParagraph::Iterator::~Iterator() {}
inline bool ZLTextParagraph::Iterator::isEnd() const { return myIndex == myEndIndex; }
inline ZLTextParagraphEntry::Kind ZLTextParagraph::Iterator::entryKind() const { return (ZLTextParagraphEntry::Kind)*myPointer; }

inline ZLTextSpecialParagraph::ZLTextSpecialParagraph(Kind kind) : myKind(kind) {}
inline ZLTextSpecialParagraph::~ZLTextSpecialParagraph() {}
inline ZLTextParagraph::Kind ZLTextSpecialParagraph::kind() const { return myKind; }

inline ZLTextTreeParagraph::~ZLTextTreeParagraph() {}
inline ZLTextParagraph::Kind ZLTextTreeParagraph::kind() const { return TREE_PARAGRAPH; }
inline bool ZLTextTreeParagraph::isOpen() const { return myIsOpen; }
inline void ZLTextTreeParagraph::open(bool o) { myIsOpen = o; }
inline int ZLTextTreeParagraph::depth() const { return myDepth; }
inline ZLTextTreeParagraph *ZLTextTreeParagraph::parent() { return myParent; }
inline const ZLTextTreeParagraph *ZLTextTreeParagraph::parent() const { return myParent; }
inline const std::vector<ZLTextTreeParagraph*> &ZLTextTreeParagraph::children() const { return myChildren; }
inline void ZLTextTreeParagraph::addChild(ZLTextTreeParagraph *child) { myChildren.push_back(child); }

#endif /* __ZLTEXTPARAGRAPH_H__ */
