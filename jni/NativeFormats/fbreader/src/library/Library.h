/*
 * Copyright (C) 2004-2010 Geometer Plus <contact@geometerplus.com>
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

#ifndef __LIBRARY_H__
#define __LIBRARY_H__

#include <string>
#include <vector>
#include <set>
#include <map>

#include <shared_ptr.h>

#include <ZLOptions.h>

#include "Book.h"
#include "Author.h"
#include "Tag.h"
#include "Lists.h"

class Library {

public:
	static Library &Instance();

private:
	static shared_ptr<Library> ourInstance;
	static const size_t MaxRecentListSize;

public:
	ZLStringOption PathOption;
	ZLBooleanOption ScanSubdirsOption;
	ZLBooleanOption CollectAllBooksOption;

private:
	Library();

public:
	const AuthorList &authors() const;
	const TagList &tags() const;
	const BookList &books(shared_ptr<Author> author) const;
	const BookList &books(shared_ptr<Tag> tag) const;
	const BookList &recentBooks() const;

	enum RemoveType {
		REMOVE_DONT_REMOVE = 0,
		REMOVE_FROM_LIBRARY = 1,
		REMOVE_FROM_DISK = 2,
		REMOVE_FROM_LIBRARY_AND_DISK = REMOVE_FROM_LIBRARY | REMOVE_FROM_DISK
	};
		
	RemoveType canRemove(shared_ptr<Book> book) const;

	void collectSeriesTitles(shared_ptr<Author> author, std::set<std::string> &titles) const;

	size_t revision() const;

	void addBook(shared_ptr<Book> book);
	void removeBook(shared_ptr<Book> book);
	void updateBook(shared_ptr<Book> book);
	void addBookToRecentList(shared_ptr<Book> book);

	void replaceAuthor(shared_ptr<Author> from, shared_ptr<Author> to);

	bool hasBooks(shared_ptr<Tag> tag) const;
	bool hasSubtags(shared_ptr<Tag> tag) const;
	void removeTag(shared_ptr<Tag> tag, bool includeSubTags);
	void renameTag(shared_ptr<Tag> from, shared_ptr<Tag> to, bool includeSubTags);
	void cloneTag(shared_ptr<Tag> from, shared_ptr<Tag> to, bool includeSubTags);

private:
	void collectDirNames(std::set<std::string> &names) const;
	void collectBookFileNames(std::set<std::string> &bookFileNames) const;

	void synchronize() const;

	void rebuildBookSet() const;
	void rebuildMaps() const;

	void insertIntoBookSet(shared_ptr<Book> book) const;

private:
	mutable BookSet myBooks;
	mutable BookSet myExternalBooks;

	mutable AuthorList myAuthors;
	mutable TagList myTags;
	typedef std::map<shared_ptr<Author>,BookList,AuthorComparator> BooksByAuthor;
	mutable BooksByAuthor myBooksByAuthor;
	typedef std::map<shared_ptr<Tag>,BookList,TagComparator> BooksByTag;
	mutable BooksByTag myBooksByTag;
	mutable BookList myRecentBooks;

	mutable std::string myPath;
	mutable bool myScanSubdirs;

	enum BuildMode {
		BUILD_NOTHING = 0,
		BUILD_UPDATE_BOOKS_INFO = 1 << 0,
		BUILD_COLLECT_FILES_INFO = 1 << 1,
		BUILD_ALL = 0x03
	};
	mutable BuildMode myBuildMode;
	mutable size_t myRevision;

friend class LibrarySynchronizer;
};

#endif /* __LIBRARY_H__ */
