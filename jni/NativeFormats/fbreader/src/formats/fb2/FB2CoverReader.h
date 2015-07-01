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

#ifndef __FB2COVERREADER_H__
#define __FB2COVERREADER_H__

#include <ZLFile.h>
#include <ZLImage.h>

#include "FB2Reader.h"

class FB2CoverReader : public FB2Reader {

public:
	FB2CoverReader(const ZLFile &file);
	shared_ptr<const ZLImage> readCover();

private:
	bool processNamespaces() const;
	void startElementHandler(int tag, const char **attributes);
	void endElementHandler(int tag);
	void characterDataHandler(const char *text, std::size_t len);

private:
	const ZLFile myFile;
	bool myReadCoverPage;
	bool myLookForImage;
	std::string myImageId;
	int myImageStart;
	shared_ptr<const ZLImage> myImage;
};

#endif /* __FB2COVERREADER_H__ */
