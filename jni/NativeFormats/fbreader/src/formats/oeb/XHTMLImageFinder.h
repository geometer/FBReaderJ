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

#ifndef __XHTMLIMAGEFINDER_H__
#define __XHTMLIMAGEFINDER_H__

#include <shared_ptr.h>
#include <ZLXMLReader.h>

class ZLFile;
class ZLImage;

class XHTMLImageFinder : public ZLXMLReader {

public:
	shared_ptr<const ZLImage> readImage(const ZLFile &file);

private:
	bool processNamespaces() const;
	void startElementHandler(const char *tag, const char **attributes);

private:
	std::string myPathPrefix;
	shared_ptr<const ZLImage> myImage;
};

#endif /* __XHTMLIMAGEFINDER_H__ */
