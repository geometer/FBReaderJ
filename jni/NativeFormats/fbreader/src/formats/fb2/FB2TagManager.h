/*
 * Copyright (C) 2008-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#ifndef __FB2TAGMANAGER_H__
#define __FB2TAGMANAGER_H__

#include <string>
#include <map>
#include <vector>

class FB2TagManager {

private:
	static FB2TagManager *ourInstance;

public:
	static const FB2TagManager &Instance();

private:
	FB2TagManager();

public:
	const std::vector<std::string> &humanReadableTags(const std::string &id) const;

private:
	std::map<std::string,std::vector<std::string> > myTagMap;
};

#endif /* __FB2TAGMANAGER_H__ */
