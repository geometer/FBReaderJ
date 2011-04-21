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

#include <ZLImage.h>
#include <ZLFile.h>

#include "BookModel.h"
#include "BookReader.h"

#include "../formats/FormatPlugin.h"
#include "../library/Book.h"

BookModel::BookModel(const shared_ptr<Book> book) : myBook(book) {
	myBookTextModel = new ZLTextPlainModel(book->language(), 102400);
	myContentsModel = new ContentsModel(book->language());
	shared_ptr<FormatPlugin> plugin = PluginCollection::Instance().plugin(book->file(), false);
	if (!plugin.isNull()) {
		plugin->readModel(*this);
	}
}

BookModel::~BookModel() {
}

void BookModel::setHyperlinkMatcher(shared_ptr<HyperlinkMatcher> matcher) {
	myHyperlinkMatcher = matcher;
}
	
BookModel::Label BookModel::label(const std::string &id) const {
	if (!myHyperlinkMatcher.isNull()) {
		return myHyperlinkMatcher->match(myInternalHyperlinks, id);
	}

	std::map<std::string,Label>::const_iterator it = myInternalHyperlinks.find(id);
	return (it != myInternalHyperlinks.end()) ? it->second : Label(0, -1);
}

ContentsModel::ContentsModel(const std::string &language) : ZLTextTreeModel(language) {
}

void ContentsModel::setReference(const ZLTextTreeParagraph *paragraph, int reference) {
	myReferenceByParagraph[paragraph] = reference;
}

int ContentsModel::reference(const ZLTextTreeParagraph *paragraph) const {
	std::map<const ZLTextTreeParagraph*,int>::const_iterator it = myReferenceByParagraph.find(paragraph);
	return (it != myReferenceByParagraph.end()) ? it->second : -1;
}

const shared_ptr<Book> BookModel::book() const {
	return myBook;
}
