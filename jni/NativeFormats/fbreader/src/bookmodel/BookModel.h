/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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

#ifndef __BOOKMODEL_H__
#define __BOOKMODEL_H__

#include <jni.h>

#include <map>
#include <string>

#include <ZLTextModel.h>
#include <ZLTextParagraph.h>

class ZLImage;
class Book;

class ContentsModel : public ZLTextTreeModel {

public:
	ContentsModel(const std::string &language, const std::string &directoryName, const std::string &fileExtension);
	void setReference(const ZLTextTreeParagraph *paragraph, int reference);
	int reference(const ZLTextTreeParagraph *paragraph) const;

private:
	std::map<const ZLTextTreeParagraph*,int> myReferenceByParagraph;
};

class BookModel {

public:
	struct Label {
		Label(shared_ptr<ZLTextModel> model, int paragraphNumber) : Model(model), ParagraphNumber(paragraphNumber) {}

		const shared_ptr<ZLTextModel> Model;
		const int ParagraphNumber;
	};

public:
	class HyperlinkMatcher {

	public:
		virtual Label match(const std::map<std::string,Label> &lMap, const std::string &id) const = 0;
	};

public:
	BookModel(const shared_ptr<Book> book, jobject javaModel);
	~BookModel();

	void setHyperlinkMatcher(shared_ptr<HyperlinkMatcher> matcher);

	shared_ptr<ZLTextModel> bookTextModel() const;
	shared_ptr<ZLTextModel> contentsModel() const;
	const std::map<std::string,shared_ptr<ZLTextModel> > &footnotes() const;

	Label label(const std::string &id) const;
	const std::map<std::string,Label> &internalHyperlinks() const;

	const shared_ptr<Book> book() const;

	bool flush();

private:
	const shared_ptr<Book> myBook;
	jobject myJavaModel;
	shared_ptr<ZLTextModel> myBookTextModel;
	shared_ptr<ZLTextModel> myContentsModel;
	std::map<std::string,shared_ptr<ZLTextModel> > myFootnotes;
	std::map<std::string,Label> myInternalHyperlinks;
	shared_ptr<HyperlinkMatcher> myHyperlinkMatcher;

friend class BookReader;
};

inline shared_ptr<ZLTextModel> BookModel::bookTextModel() const { return myBookTextModel; }
inline shared_ptr<ZLTextModel> BookModel::contentsModel() const { return myContentsModel; }
inline const std::map<std::string,shared_ptr<ZLTextModel> > &BookModel::footnotes() const { return myFootnotes; }
inline const std::map<std::string,BookModel::Label> &BookModel::internalHyperlinks() const { return myInternalHyperlinks; }

#endif /* __BOOKMODEL_H__ */
