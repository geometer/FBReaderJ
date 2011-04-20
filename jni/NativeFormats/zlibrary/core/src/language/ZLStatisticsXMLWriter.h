/*
 * Copyright (C) 2009-2010 Geometer Plus <contact@geometerplus.com>
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

#ifndef __STATISTICSXMLWRITER_H__
#define __STATISTICSXMLWRITER_H__

#include <string>

#include <ZLOutputStream.h>
#include <ZLXMLWriter.h>

class ZLMapBasedStatistics;

class ZLStatisticsXMLWriter: public ZLXMLWriter {

public:
	ZLStatisticsXMLWriter(ZLOutputStream &stream) : ZLXMLWriter(stream) {}

	void writeStatistics(const ZLMapBasedStatistics &statistics);

private:
	void writeSequence(const std::string &key, size_t frequency);

private:
	static void appendLongNumber(std::string &str, unsigned long long n); 
};

#endif /*__STATISTICSXMLWRITER_H__*/
