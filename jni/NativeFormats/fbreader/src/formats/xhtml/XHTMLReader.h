/*
 * Copyright (C) 2004-2014 Geometer Plus <contact@geometerplus.com>
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

#ifndef __XHTMLREADER_H__
#define __XHTMLREADER_H__

#include <string>
#include <map>
#include <vector>

#include <ZLXMLReader.h>
#include <ZLVideoEntry.h>

#include "../css/StyleSheetTable.h"
#include "../css/FontMap.h"
#include "../css/StyleSheetParser.h"

class ZLFile;

class BookReader;
class XHTMLReader;

class EncryptionMap;

enum XHTMLReadingState {
	XHTML_READ_NOTHING,
	XHTML_READ_STYLE,
	XHTML_READ_BODY,
	XHTML_READ_VIDEO
};

class XHTMLTagAction {

public:
	virtual ~XHTMLTagAction();
	
	virtual void doAtStart(XHTMLReader &reader, const char **xmlattributes) = 0;
	virtual void doAtEnd(XHTMLReader &reader) = 0;
	virtual bool isEnabled(XHTMLReadingState state) = 0;

protected:
	static BookReader &bookReader(XHTMLReader &reader);	
	static const std::string &pathPrefix(XHTMLReader &reader);	
	static void beginParagraph(XHTMLReader &reader);
	static void endParagraph(XHTMLReader &reader);
};

class XHTMLReader : public ZLXMLReader {

public:
	static XHTMLTagAction *addAction(const std::string &tag, XHTMLTagAction *action);
	static XHTMLTagAction *addAction(const std::string &ns, const std::string &name, XHTMLTagAction *action);
	static void fillTagTable();

private:
	static std::map<std::string,XHTMLTagAction*> ourTagActions;
	static std::map<shared_ptr<FullNamePredicate>,XHTMLTagAction*> ourNsTagActions;

public:
	XHTMLReader(BookReader &modelReader, shared_ptr<EncryptionMap> map);

	bool readFile(const ZLFile &file, const std::string &referenceName);
	const std::string &fileAlias(const std::string &fileName) const;
	const std::string normalizedReference(const std::string &reference) const;
	void setMarkFirstImageAsCover();

private:
	XHTMLTagAction *getAction(const std::string &tag);

	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);
	void characterDataHandler(const char *text, std::size_t len);

	const std::vector<std::string> &externalDTDs() const;

	bool processNamespaces() const;

	void beginParagraph();
	void endParagraph();
	bool addTextStyleEntry(const std::string tag, const std::string aClass);
	void addTextStyleEntry(const ZLTextStyleEntry &entry);

private:
	mutable std::map<std::string,std::string> myFileNumbers;

	BookReader &myModelReader;
	shared_ptr<EncryptionMap> myEncryptionMap;
	std::string myPathPrefix;
	std::string myReferenceAlias;
	std::string myReferenceDirName;
	bool myPreformatted;
	bool myNewParagraphInProgress;
	StyleSheetTable myStyleSheetTable;
	shared_ptr<FontMap> myFontMap;
	std::vector<int> myCSSStack;
	std::vector<shared_ptr<ZLTextStyleEntry> > myStyleEntryStack;
	int myStylesToRemove;
	std::vector<bool> myDoPageBreakAfterStack;
	bool myCurrentParagraphIsEmpty;
	shared_ptr<StyleSheetSingleStyleParser> myStyleParser;
	shared_ptr<StyleSheetTableParser> myTableParser;
	std::map<std::string,shared_ptr<StyleSheetParserWithCache> > myFileParsers;
	XHTMLReadingState myReadState;
	int myBodyCounter;
	bool myMarkNextImageAsCover;
	shared_ptr<ZLVideoEntry> myVideoEntry;

	friend class XHTMLTagAction;
	friend class XHTMLTagStyleAction;
	friend class XHTMLTagLinkAction;
	friend class XHTMLTagHyperlinkAction;
	friend class XHTMLTagPreAction;
	friend class XHTMLTagParagraphAction;
	friend class XHTMLTagBodyAction;
	friend class XHTMLTagRestartParagraphAction;
	friend class XHTMLTagImageAction;
	friend class XHTMLTagVideoAction;
	friend class XHTMLTagSourceAction;
};

#endif /* __XHTMLREADER_H__ */
