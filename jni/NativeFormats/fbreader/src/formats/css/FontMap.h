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

#ifndef __FONTMAP_H__
#define __FONTMAP_H__

#include <string>
#include <map>

#include <shared_ptr.h>

class FontEntry {

public:
	void addFile(const std::string &weight, const std::string &style, const std::string &filePath);

	bool operator == (const FontEntry &other) const;
	bool operator != (const FontEntry &other) const;

public:
	shared_ptr<std::string> Normal;
	shared_ptr<std::string> Bold;
	shared_ptr<std::string> Italic;
	shared_ptr<std::string> BoldItalic;
};

class FontMap {

public:
	bool operator == (const FontMap &other) const;
	bool operator != (const FontMap &other) const;

	void appendFontFace(const std::string &family, const std::string &weight, const std::string &style, const std::string &path);

private:
	std::map<std::string,FontEntry> myMap;
};

#endif /* __FONTMAP_H__ */
