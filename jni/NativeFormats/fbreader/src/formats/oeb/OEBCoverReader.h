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

#ifndef __OEBCOVERREADER_H__
#define __OEBCOVERREADER_H__

#include <vector>

#include <shared_ptr.h>
#include <ZLXMLReader.h>

class ZLImage;

class OEBCoverReader : public ZLXMLReader {

public:
	OEBCoverReader();
	shared_ptr<const ZLImage> readCover(const ZLFile &file);

private:
	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);
	bool processNamespaces() const;

	void createImage(const char *href);

private:
	shared_ptr<const ZLImage> myImage;
	std::string myPathPrefix;
	std::string myCoverXHTML;
	std::string myCoverId;
	enum {
		READ_NOTHING,
		READ_METADATA,
		READ_MANIFEST,
		READ_GUIDE
	} myReadState;
};

#endif /* __OEBCOVERREADER_H__ */
