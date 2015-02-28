/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#ifndef __ZLLANGUAGELIST_H__
#define __ZLLANGUAGELIST_H__

#include <vector>

#include <ZLDir.h>

class ZLLanguageList {

public:
	static std::string patternsDirectoryPath();
	static const std::vector<std::string> &languageCodes();
	//static std::string languageName(const std::string &code);

private:
	static std::vector<std::string> ourLanguageCodes;

private:
	ZLLanguageList();
};

#endif /* __ZLLANGUAGELIST_H__ */
