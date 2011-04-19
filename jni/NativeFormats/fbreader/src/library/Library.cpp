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

#include <queue>
#include <algorithm>

#include <ZLibrary.h>
#include <ZLStringUtil.h>
#include <ZLFile.h>
#include <ZLDir.h>
#include <ZLDialogManager.h>

#include "Library.h"
#include "Book.h"
#include "Author.h"
#include "Tag.h"

#include "../formats/FormatPlugin.h"

#include "../database/booksdb/BooksDBUtil.h"
#include "../database/booksdb/BooksDB.h"

shared_ptr<Library> Library::ourInstance;
const size_t Library::MaxRecentListSize = 10;

Library &Library::Instance() {
	if (ourInstance.isNull()) {
		ourInstance = new Library();
	}
	return *ourInstance;
}

static const std::string OPTIONS = "Options";

Library::Library() :
	PathOption(ZLCategoryKey::CONFIG, OPTIONS, "BookPath", ""),
	ScanSubdirsOption(ZLCategoryKey::CONFIG, OPTIONS, "ScanSubdirs", false),
	CollectAllBooksOption(ZLCategoryKey::CONFIG, OPTIONS, "CollectAllBooks", false),
	myBuildMode(BUILD_ALL),
	myRevision(0) {
	BooksDBUtil::getRecentBooks(myRecentBooks);
}

void Library::collectBookFileNames(std::set<std::string> &bookFileNames) const {
	std::set<std::string> dirs;
	collectDirNames(dirs);

	while (!dirs.empty()) {
		std::string dirname = *dirs.begin();
		dirs.erase(dirs.begin());
		
		ZLFile dirfile(dirname);
		std::vector<std::string> files;
		bool inZip = false;

		shared_ptr<ZLDir> dir = dirfile.directory();
		if (dir.isNull()) {
			continue;
		}

		if (dirfile.extension() == "zip") {
			ZLFile phys(dirfile.physicalFilePath());
			if (!BooksDBUtil::checkInfo(phys)) {
				BooksDBUtil::resetZipInfo(phys);
				BooksDBUtil::saveInfo(phys);
			}
			BooksDBUtil::listZipEntries(dirfile, files);
			inZip = true;
		} else {
			dir->collectFiles(files, true);
		}
		if (!files.empty()) {
			const bool collectBookWithoutMetaInfo = CollectAllBooksOption.value();
			for (std::vector<std::string>::const_iterator jt = files.begin(); jt != files.end(); ++jt) {
				const std::string fileName = (inZip) ? (*jt) : (dir->itemPath(*jt));
				ZLFile file(fileName);
				if (PluginCollection::Instance().plugin(file, !collectBookWithoutMetaInfo) != 0) {
					bookFileNames.insert(fileName);
				// TODO: zip -> any archive
				} else if (file.extension() == "zip") {
					if (myScanSubdirs || !inZip) {
						dirs.insert(fileName);
					}
				}
			}
		}
	}
}

void Library::rebuildBookSet() const {
	myBooks.clear();
	myExternalBooks.clear();
	
	std::map<std::string, shared_ptr<Book> > booksMap;
	BooksDBUtil::getBooks(booksMap);

	std::set<std::string> fileNamesSet;
	collectBookFileNames(fileNamesSet);

	// collect books from book path
	for (std::set<std::string>::iterator it = fileNamesSet.begin(); it != fileNamesSet.end(); ++it) {
		std::map<std::string, shared_ptr<Book> >::iterator jt = booksMap.find(*it);
		if (jt == booksMap.end()) {
			insertIntoBookSet(BooksDBUtil::getBook(*it));
		} else {
			insertIntoBookSet(jt->second);
			booksMap.erase(jt);
		}
	}

	// other books from our database
	for (std::map<std::string, shared_ptr<Book> >::iterator jt = booksMap.begin(); jt != booksMap.end(); ++jt) {
		shared_ptr<Book> book = jt->second;
		if (!book.isNull()) {
			if (BooksDB::Instance().checkBookList(*book)) {
				insertIntoBookSet(book);
				myExternalBooks.insert(book);
			}
		}
	}
}

size_t Library::revision() const {
	if (myBuildMode == BUILD_NOTHING &&
			(myScanSubdirs != ScanSubdirsOption.value() ||
			 myPath != PathOption.value())) {
		myPath = PathOption.value();
		myScanSubdirs = ScanSubdirsOption.value();
		myBuildMode = BUILD_ALL;
	}

	return (myBuildMode == BUILD_NOTHING) ? myRevision : myRevision + 1;
}

class LibrarySynchronizer : public ZLRunnable {

public:
	LibrarySynchronizer(Library::BuildMode mode);

private:
	void run();

private:
	const Library::BuildMode myBuildMode;
};

LibrarySynchronizer::LibrarySynchronizer(Library::BuildMode mode) : myBuildMode(mode) {
}

void LibrarySynchronizer::run() {
	Library &library = Library::Instance();

	if (myBuildMode & Library::BUILD_COLLECT_FILES_INFO) {
		library.rebuildBookSet();
	}

	if (myBuildMode & Library::BUILD_UPDATE_BOOKS_INFO) {
		library.rebuildMaps();
	}
}

void Library::synchronize() const {
	if (myScanSubdirs != ScanSubdirsOption.value() ||
			myPath != PathOption.value()) {
		myPath = PathOption.value();
		myScanSubdirs = ScanSubdirsOption.value();
		myBuildMode = BUILD_ALL;
	}

	if (myBuildMode == BUILD_NOTHING) {
		return;
	}

	LibrarySynchronizer synchronizer(myBuildMode);
	myBuildMode = BUILD_NOTHING;
	ZLDialogManager::Instance().wait(ZLResourceKey("loadingBookList"), synchronizer);

	++myRevision;
}

void Library::rebuildMaps() const {
	myAuthors.clear();
	myBooksByAuthor.clear();
	myTags.clear();
	myBooksByTag.clear();

	for (BookSet::const_iterator it = myBooks.begin(); it != myBooks.end(); ++it) {
		if ((*it).isNull()) {
			continue;
		}

		const AuthorList &bookAuthors = (*it)->authors();
		if (bookAuthors.empty()) {
			myBooksByAuthor[0].push_back(*it);
		} else {
			for(AuthorList::const_iterator jt = bookAuthors.begin(); jt != bookAuthors.end(); ++jt) {
				myBooksByAuthor[*jt].push_back(*it);
			}
		}

		const TagList &bookTags = (*it)->tags();
		if (bookTags.empty()) {
			myBooksByTag[0].push_back(*it);
		} else {
			for(TagList::const_iterator kt = bookTags.begin(); kt != bookTags.end(); ++kt) {
				myBooksByTag[*kt].push_back(*it);
			}
		}
	}
	for (BooksByAuthor::iterator mit = myBooksByAuthor.begin(); mit != myBooksByAuthor.end(); ++mit) {
		myAuthors.push_back(mit->first);
		std::sort(mit->second.begin(), mit->second.end(), BookComparator());
	}
	for (BooksByTag::iterator mjt = myBooksByTag.begin(); mjt != myBooksByTag.end(); ++mjt) {
		myTags.push_back(mjt->first);
		std::sort(mjt->second.begin(), mjt->second.end(), BookComparator());
	}
}

void Library::collectDirNames(std::set<std::string> &nameSet) const {
	std::queue<std::string> nameQueue;

	std::string path = myPath;
	int pos = path.find(ZLibrary::PathDelimiter);
	while (pos != -1) {
		nameQueue.push(path.substr(0, pos));
		path.erase(0, pos + 1);
		pos = path.find(ZLibrary::PathDelimiter);
	}
	if (!path.empty()) {
		nameQueue.push(path);
	}

	std::set<std::string> resolvedNameSet;
	while (!nameQueue.empty()) {
		std::string name = nameQueue.front();
		nameQueue.pop();
		ZLFile f(name);
		const std::string resolvedName = f.resolvedPath();
		if (resolvedNameSet.find(resolvedName) == resolvedNameSet.end()) {
			if (myScanSubdirs) {
				shared_ptr<ZLDir> dir = f.directory();
				if (!dir.isNull()) {
					std::vector<std::string> subdirs;
					dir->collectSubDirs(subdirs, true);
					for (std::vector<std::string>::const_iterator it = subdirs.begin(); it != subdirs.end(); ++it) {
						nameQueue.push(dir->itemPath(*it));
					}
				}
			}
			resolvedNameSet.insert(resolvedName);
			nameSet.insert(name);
		}
	}
}

void Library::updateBook(shared_ptr<Book> book) {
	BooksDB::Instance().saveBook(book);
	myBuildMode = (BuildMode)(myBuildMode | BUILD_UPDATE_BOOKS_INFO);
}

void Library::addBook(shared_ptr<Book> book) {
	if (!book.isNull()) {
		BooksDB::Instance().saveBook(book);
		insertIntoBookSet(book);
		myBuildMode = (BuildMode)(myBuildMode | BUILD_UPDATE_BOOKS_INFO);
	}
}

void Library::removeBook(shared_ptr<Book> book) {
	if (!book.isNull()) {
		BookSet::iterator it = myBooks.find(book);
		if (it != myBooks.end()) {
			myBooks.erase(it);
			myBuildMode = (BuildMode)(myBuildMode | BUILD_UPDATE_BOOKS_INFO);
		}
		BooksDB::Instance().deleteFromBookList(*book);
		bool recentListChanged = false;
		for (BookList::iterator it = myRecentBooks.begin(); it != myRecentBooks.end();) {
			if ((*it)->file() == book->file()) {
				it = myRecentBooks.erase(it);
				recentListChanged = true;
			} else {
				++it;
			}
		}
		if (recentListChanged) {
			BooksDB::Instance().saveRecentBooks(myRecentBooks);
		}
	}
}

const AuthorList &Library::authors() const {
	synchronize();
	return myAuthors;
}

const TagList &Library::tags() const {
	synchronize();
	return myTags;
}

const BookList &Library::books(shared_ptr<Author> author) const {
	synchronize();
	return myBooksByAuthor[author];
}

const BookList &Library::books(shared_ptr<Tag> tag) const {
	synchronize();
	return myBooksByTag[tag];
}

void Library::collectSeriesTitles(shared_ptr<Author> author, std::set<std::string> &titles) const {
	const BookList &bookList = books(author);
	for (BookList::const_iterator it = bookList.begin(); it != bookList.end(); ++it) {
		const std::string &current = (*it)->seriesTitle();
		if (!current.empty()) {
			titles.insert(current);
		}
	}
}

void Library::removeTag(shared_ptr<Tag> tag, bool includeSubTags) {
	for (BookSet::const_iterator it = myBooks.begin(); it != myBooks.end(); ++it) {
		if ((*it)->removeTag(tag, includeSubTags)) {
			BooksDB::Instance().saveTags(*it);
		}
	}
	myBuildMode = (BuildMode)(myBuildMode | BUILD_UPDATE_BOOKS_INFO);
}

void Library::renameTag(shared_ptr<Tag> from, shared_ptr<Tag> to, bool includeSubTags) {
	if (to != from) {
		synchronize();
		for (BookSet::const_iterator it = myBooks.begin(); it != myBooks.end(); ++it) {
			BooksDBUtil::renameTag(*it, from, to, includeSubTags);
		}
	}
	myBuildMode = (BuildMode)(myBuildMode | BUILD_UPDATE_BOOKS_INFO);
}

void Library::cloneTag(shared_ptr<Tag> from, shared_ptr<Tag> to, bool includeSubTags) {
	if (to != from) {
		synchronize();
		for (BookSet::const_iterator it = myBooks.begin(); it != myBooks.end(); ++it) {
			BooksDBUtil::cloneTag(*it, from, to, includeSubTags);
		}
	}
	myBuildMode = (BuildMode)(myBuildMode | BUILD_UPDATE_BOOKS_INFO);
}

bool Library::hasBooks(shared_ptr<Tag> tag) const {
	synchronize();
	const BooksByTag::const_iterator it = myBooksByTag.find(tag);
	return it != myBooksByTag.end() && !it->second.empty();
}

bool Library::hasSubtags(shared_ptr<Tag> tag) const {
	const TagList &tagList = tags();
	const TagList::const_iterator it =
		std::upper_bound(tagList.begin(), tagList.end(), tag, TagComparator());
	return it != tagList.end() && tag->isAncestorOf(*it);
}

void Library::replaceAuthor(shared_ptr<Author> from, shared_ptr<Author> to) {
	if (to != from) {
		for (BookSet::const_iterator it = myBooks.begin(); it != myBooks.end(); ++it) {
			if ((*it)->replaceAuthor(from, to)) {
				BooksDB::Instance().saveAuthors(*it);
			}
		}
		myBuildMode = (BuildMode)(myBuildMode | BUILD_UPDATE_BOOKS_INFO);
	}
}

Library::RemoveType Library::canRemove(shared_ptr<Book> book) const {
	synchronize();
	return (RemoveType)(
		(myExternalBooks.find(book) != myExternalBooks.end() ?
			REMOVE_FROM_LIBRARY : REMOVE_DONT_REMOVE) |
		(BooksDBUtil::canRemoveFile(book->file().path()) ?
			REMOVE_FROM_DISK : REMOVE_DONT_REMOVE)
	);
}

void Library::insertIntoBookSet(shared_ptr<Book> book) const {
	if (!book.isNull()) {
		myBooks.insert(book);
	}
}

const BookList &Library::recentBooks() const {
	return myRecentBooks;
}

void Library::addBookToRecentList(shared_ptr<Book> book) {
	if (book.isNull()) {
		return;
	}
	for (BookList::iterator it = myRecentBooks.begin(); it != myRecentBooks.end(); ++it) {
		if ((*it)->file() == book->file()) {
			if (it == myRecentBooks.begin()) {
				return;
			}
			myRecentBooks.erase(it);
			break;
		}
	}
	myRecentBooks.insert(myRecentBooks.begin(), book);
	if (myRecentBooks.size() > MaxRecentListSize) {
		myRecentBooks.erase(myRecentBooks.begin() + MaxRecentListSize, myRecentBooks.end());
	}
	BooksDB::Instance().saveRecentBooks(myRecentBooks);
}
