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

#ifndef __ZLSTATISTICSXMLREADER_H__
#define __ZLSTATISTICSXMLREADER_H__

#include <shared_ptr.h>
#include <ZLXMLReader.h>

#include "ZLStatistics.h"

class ZLCharSequence;

class ZLStatisticsXMLReader : public ZLXMLReader {

public:
	shared_ptr<ZLArrayBasedStatistics> readStatistics(const std::string &fileName);

	void startElementHandler(const char *tag, const char **attributes);

private:
	shared_ptr<ZLArrayBasedStatistics> myStatisticsPtr;

private:
	static const std::string ITEM_TAG;
	static const std::string STATISTICS_TAG;
};

#endif /*__ZLSTATISTICSXMLREADER_H__*/
