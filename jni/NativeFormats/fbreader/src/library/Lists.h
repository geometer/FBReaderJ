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

#ifndef __LISTS_H__
#define __LISTS_H__

#include <vector>
#include <set>

#include <shared_ptr.h>

class Book;
class Author;
class Tag;
class UID;
class BookByFileNameComparator;

typedef std::vector<shared_ptr<Book> > BookList;
typedef std::set<shared_ptr<Book>,BookByFileNameComparator> BookSet;
typedef std::vector<shared_ptr<Author> > AuthorList;
typedef std::vector<shared_ptr<Tag> > TagList;
typedef std::set<shared_ptr<Tag> > TagSet;
typedef std::vector<shared_ptr<UID> > UIDList;

#endif /* __LISTS_H__ */
