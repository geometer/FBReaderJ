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

#ifndef __ZLSTATISTICSGENERATOR_H__
#define __ZLSTATISTICSGENERATOR_H__

#include <string>

class ZLMapBasedStatistics;

class ZLStatisticsGenerator {

public:
	ZLStatisticsGenerator(const std::string &breakSymbols);
	~ZLStatisticsGenerator();

	void generate(const std::string &inputFileName, std::size_t charSequenceSizpe, ZLMapBasedStatistics &statistics);
	void generate(const char* buffer, std::size_t length, std::size_t charSequenceSize, ZLMapBasedStatistics &statistics);

private:
	int read(const std::string &inputFileName);

private:
	char *myBreakSymbolsTable;

	char *myStart;
	char *myEnd;

	static std::size_t ourBufferSize;
};

#endif //__ZLSTATISTICSGENERATOR_H__
