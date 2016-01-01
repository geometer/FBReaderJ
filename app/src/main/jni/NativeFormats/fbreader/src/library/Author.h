/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#ifndef __AUTHOR_H__
#define __AUTHOR_H__

#include <string>
#include <map>
#include <set>

#include <shared_ptr.h>

#include "Lists.h"

class Author;

class AuthorComparator {

public:
	bool operator () (
		const shared_ptr<Author> author0,
		const shared_ptr<Author> author1
	) const;
};

class Author {

private:
	static std::set<shared_ptr<Author>,AuthorComparator> ourAuthorSet;

public:
	static shared_ptr<Author> getAuthor(const std::string &name, const std::string &sortKey = "");

private:
	Author(const std::string &name, const std::string &sortkey);

public:
	const std::string &name() const;
	const std::string &sortKey() const;

private:
	const std::string myName;
	const std::string mySortKey;

private: // disable copying:
	Author(const Author &);
	const Author &operator = (const Author &);
};

inline Author::Author(const std::string &name, const std::string &sortkey) : myName(name), mySortKey(sortkey) {}

inline const std::string &Author::name() const { return myName; }
inline const std::string &Author::sortKey() const { return mySortKey; }

#endif /* __AUTHOR_H__ */
