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

#ifndef __BOOKREADER_H__
#define __BOOKREADER_H__

#include <vector>
#include <list>
#include <map>
#include <stack>
#include <string>

#include <ZLTextParagraph.h>

#include "FBHyperlinkType.h"
#include "FBTextKind.h"

class BookModel;
class ContentsTree;
class ZLTextModel;
class ZLInputStream;
class ZLCachedMemoryAllocator;
class ZLTextStyleEntry;
class ZLVideoEntry;
class FontEntry;

class BookReader {

public:
	BookReader(BookModel &model);
	virtual ~BookReader();

	void setMainTextModel();
	void setFootnoteTextModel(const std::string &id);
	void unsetTextModel();

	void insertEndOfSectionParagraph();
	void insertPseudoEndOfSectionParagraph();
	void insertEndOfTextParagraph();
	void insertEncryptedSectionParagraph();

	void pushKind(FBTextKind kind);
	bool popKind();
	bool isKindStackEmpty() const;

	void beginParagraph(ZLTextParagraph::Kind kind = ZLTextParagraph::TEXT_PARAGRAPH);
	void endParagraph();
	bool paragraphIsOpen() const;
	void addControl(FBTextKind kind, bool start);
	void addStyleEntry(const ZLTextStyleEntry &entry, unsigned char depth);
	void addStyleEntry(const ZLTextStyleEntry &entry, const std::vector<std::string> &fontFamilies, unsigned char depth);
	void addStyleCloseEntry();
	void addHyperlinkControl(FBTextKind kind, const std::string &label);
	void addHyperlinkLabel(const std::string &label);
	void addHyperlinkLabel(const std::string &label, int paragraphNumber);
	void addFixedHSpace(unsigned char length);

	void addImageReference(const std::string &id, short vOffset, bool isCover);
	void addImage(const std::string &id, shared_ptr<const ZLImage> image);

	void addVideoEntry(const ZLVideoEntry &entry);
	void addExtensionEntry(const std::string &action, const std::map<std::string,std::string> &data);

	void beginContentsParagraph(int referenceNumber = -1);
	void endContentsParagraph();
	bool contentsParagraphIsOpen() const;
	//void setReference(std::size_t contentsParagraphNumber, int referenceNumber);

	void addData(const std::string &data);
	void addContentsData(const std::string &data);

	void enterTitle() { myInsideTitle = true; }
	void exitTitle() { myInsideTitle = false; }

	std::string putFontEntry(const std::string &family, shared_ptr<FontEntry> fontEntry);

	const BookModel &model() const { return myModel; }

	void reset();

private:
	void insertEndParagraph(ZLTextParagraph::Kind kind);
	void flushTextBufferToParagraph();

private:
	BookModel &myModel;
	shared_ptr<ZLTextModel> myCurrentTextModel;
	std::list<shared_ptr<ZLTextModel> > myModelsWithOpenParagraphs;

	std::vector<FBTextKind> myKindStack;

	bool myContentsParagraphExists;
	std::stack<shared_ptr<ContentsTree> > myContentsTreeStack;

	bool mySectionContainsRegularContents;
	bool myInsideTitle;

	std::vector<std::string> myBuffer;

	std::string myHyperlinkReference;
	FBHyperlinkType myHyperlinkType;
	FBTextKind myHyperlinkKind;

	shared_ptr<ZLCachedMemoryAllocator> myFootnotesAllocator;
};

inline bool BookReader::contentsParagraphIsOpen() const {
	return myContentsParagraphExists;
}

#endif /* __BOOKREADER_H__ */
