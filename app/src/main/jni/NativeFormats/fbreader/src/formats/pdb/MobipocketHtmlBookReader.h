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

#ifndef __MOBIPOCKETHTMLBOOKREADER_H__
#define __MOBIPOCKETHTMLBOOKREADER_H__

#include <set>

#include "../html/HtmlBookReader.h"

class MobipocketHtmlBookReader : public HtmlBookReader {

public:
	MobipocketHtmlBookReader(const ZLFile &file, BookModel &model, const PlainTextFormat &format, const std::string &encoding);
	void readDocument(ZLInputStream &stream);

private:
	void startDocumentHandler();
	bool tagHandler(const HtmlTag &tag);
	bool characterDataHandler(const char *text, size_t len, bool convert);
	shared_ptr<HtmlTagAction> createAction(const std::string &tag);

public:
	class TOCReader {

	public:
		struct Entry {
			std::string Text;
			size_t Level;

			Entry();
			Entry(const std::string &text, size_t level);
		};
	
	public:
		TOCReader(MobipocketHtmlBookReader &reader);
		void reset();

		void addReference(size_t position, const std::string &text, size_t level);

		void setStartOffset(size_t position);
		void setEndOffset(size_t position);

		bool rangeContainsPosition(size_t position);

		void startReadEntry(size_t position);
		void endReadEntry(size_t level);
		void appendText(const char *text, size_t len);

		const std::map<size_t,Entry> &entries() const;

	private:	
		MobipocketHtmlBookReader &myReader;

		std::map<size_t,Entry> myEntries;

		bool myIsActive;
		size_t myStartOffset;
		size_t myEndOffset;

		size_t myCurrentReference;
		std::string myCurrentEntryText;
	};

private:
	std::set<int> myImageIndexes;
	const std::string myFileName;

	std::vector<std::pair<size_t,size_t> > myPositionToParagraphMap;
	std::set<size_t> myFileposReferences;
	bool myInsideGuide;
	TOCReader myTocReader;

friend class MobipocketHtmlImageTagAction;
friend class MobipocketHtmlHrefTagAction;
friend class MobipocketHtmlGuideTagAction;
friend class MobipocketHtmlReferenceTagAction;
friend class MobipocketHtmlPagebreakTagAction;
friend class TOCReader;
};

#endif /* __MOBIPOCKETHTMLBOOKREADER_H__ */
