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

#ifndef __FONTMANAGER_H__
#define __FONTMANAGER_H__

#include <string>
#include <map>
#include <vector>

#include <shared_ptr.h>

#include <FontMap.h>

class FontManager {

public:
	std::string put(const std::string &family, shared_ptr<FontEntry> entry);
	int familyListIndex(const std::vector<std::string> &familyList);

	const std::map<std::string,shared_ptr<FontEntry> > &entries() const;
	const std::vector<std::vector<std::string> > &familyLists() const;

private:
	std::map<std::string,shared_ptr<FontEntry> > myEntries;
	std::vector<std::vector<std::string> > myFamilyLists;
};

#endif /* __FONTMANAGER_H__ */
