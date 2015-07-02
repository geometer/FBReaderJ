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

#ifndef __NCXREADER_H__
#define __NCXREADER_H__

#include <map>
#include <vector>

#include <ZLXMLReader.h>

#include "../../bookmodel/BookReader.h"

class NCXReader : public ZLXMLReader {

public:
	struct NavPoint {
		NavPoint();
		NavPoint(int order, std::size_t level);

		int Order;
		std::size_t Level;
		std::string Text;
		std::string ContentHRef;
	};

public:
	NCXReader(BookReader &modelReader);
	const std::map<int,NavPoint> &navigationMap() const;

private:
	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);
	void characterDataHandler(const char *text, std::size_t len);
	const std::vector<std::string> &externalDTDs() const;

private:
	BookReader &myModelReader;
	std::map<int,NavPoint> myNavigationMap;
	std::vector<NavPoint> myPointStack;

	enum {
		READ_NONE,
		READ_MAP,
		READ_POINT,
		READ_LABEL,
		READ_TEXT
	} myReadState;

	int myPlayIndex;
};

#endif /* __NCXREADER_H__ */
