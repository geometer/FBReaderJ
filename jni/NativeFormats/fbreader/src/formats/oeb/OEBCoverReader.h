/*
 * Copyright (C) 2009-2011 Geometer Plus <contact@geometerplus.com>
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

#ifndef __OEBCOVERREADER_H__
#define __OEBCOVERREADER_H__

#include <vector>

#include <shared_ptr.h>
#include <ZLXMLReader.h>

class ZLImage;

class OEBCoverReader : public ZLXMLReader {

public:
	OEBCoverReader();
	shared_ptr<ZLImage> readCover(const ZLFile &file);

	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);

private:
	shared_ptr<ZLImage> myImage;
	std::string myPathPrefix;
	std::string myCoverXHTML;
	bool myReadGuide;

friend class XHTMLImageFinder;
};

#endif /* __OEBCOVERREADER_H__ */
