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

#ifndef __HTMLBOOKREADER_H__
#define __HTMLBOOKREADER_H__

#include <stack>
#include <vector>

#include <shared_ptr.h>

#include <FontMap.h>

#include "HtmlReader.h"
#include "../../bookmodel/BookReader.h"
#include "../css/StyleSheetTable.h"

class BookModel;
class PlainTextFormat;
class StyleSheetParser;
class ZLTextStyleEntry;

class HtmlTagAction;

class HtmlBookReader : public HtmlReader {

public:
	struct TagData {
		std::vector<shared_ptr<ZLTextStyleEntry> > StyleEntries;

		void addEntry(shared_ptr<ZLTextStyleEntry> entry);
	};

public:
	HtmlBookReader(const std::string &baseDirectoryPath, BookModel &model, const PlainTextFormat &format, const std::string &encoding);
	~HtmlBookReader();
	void setFileName(const std::string fileName);
	shared_ptr<StyleSheetParser> createCSSParser();
	size_t listStackDepth() const;

protected:
	virtual shared_ptr<HtmlTagAction> createAction(const std::string &tag);
	void setBuildTableOfContent(bool build);
	void setProcessPreTag(bool process);

protected:
	void startDocumentHandler();
	void endDocumentHandler();
	bool tagHandler(const HtmlTag &tag);
	bool characterDataHandler(const char *text, std::size_t len, bool convert);

private:
	void preformattedCharacterDataHandler(const char *text, std::size_t len, bool convert);
	void addConvertedDataToBuffer(const char *text, std::size_t len, bool convert);

protected:
	BookReader myBookReader;
	std::string myBaseDirPath;

private:
	const PlainTextFormat &myFormat;
	int myIgnoreDataCounter;
	bool myIsPreformatted;
	bool myDontBreakParagraph;

	bool myIsStarted;
	bool myBuildTableOfContent;
	bool myProcessPreTag;
	bool myIgnoreTitles;
	std::stack<int> myListNumStack;
	std::vector<shared_ptr<TagData> > myTagDataStack;

	StyleSheetTable myStyleSheetTable;
	shared_ptr<StyleSheetParser> myStyleSheetParser;
	shared_ptr<FontMap> myFontMap;

	int mySpaceCounter;
	int myBreakCounter;
	std::string myConverterBuffer;

	std::map<std::string,shared_ptr<HtmlTagAction> > myActionMap;
	std::vector<FBTextKind> myKindList;

	std::string myFileName;

	friend class HtmlTagAction;
	friend class HtmlControlTagAction;
	friend class HtmlHeaderTagAction;
	friend class HtmlIgnoreTagAction;
	friend class HtmlHrefTagAction;
	friend class HtmlImageTagAction;
	friend class HtmlBreakTagAction;
	friend class HtmlPreTagAction;
	friend class HtmlListTagAction;
	friend class HtmlListItemTagAction;
	friend class HtmlTableTagAction;
	friend class HtmlStyleTagAction;
};

#endif /* __HTMLBOOKREADER_H__ */
