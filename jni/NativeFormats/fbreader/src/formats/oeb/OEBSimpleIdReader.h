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

#ifndef __OEBSIMPLEIDREADER_H__
#define __OEBSIMPLEIDREADER_H__

#include "OPFReader.h"

class OEBSimpleIdReader : public OPFReader {

public:
	std::string readId(const ZLFile &file);

	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);
	void characterDataHandler(const char *text, std::size_t len);

private:
	enum {
		READ_NONE,
		READ_METADATA,
		READ_IDENTIFIER,
	} myReadState;

	std::string myBuffer;
	std::string myPublicationId;
};

#endif /* __OEBSIMPLEIDREADER_H__ */
