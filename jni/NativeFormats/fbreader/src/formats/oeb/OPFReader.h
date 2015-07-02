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

#ifndef __OPFREADER_H__
#define __OPFREADER_H__

#include <vector>

#include <ZLXMLReader.h>

class OPFReader : public ZLXMLReader {

protected:
	OPFReader();

public:
	bool processNamespaces() const;
	const std::vector<std::string> &externalDTDs() const;

protected:
	bool testOPFTag(const std::string &expected, const std::string &tag) const;
	bool testDCTag(const std::string &expected, const std::string &tag) const;

	bool isNSName(const std::string &fullName, const std::string &shortName, const std::string &fullNSId) const;
	bool isMetadataTag(const std::string &tagName);
};

#endif /* __OPFREADER_H__ */
