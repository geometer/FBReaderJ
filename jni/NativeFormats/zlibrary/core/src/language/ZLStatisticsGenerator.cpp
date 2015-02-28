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

#include <cstring>
#include <string>
#include <map>

#include <ZLFile.h>
#include <ZLInputStream.h>

#include "ZLCharSequence.h"
#include "ZLStatistics.h"
#include "ZLStatisticsGenerator.h"

std::size_t ZLStatisticsGenerator::ourBufferSize = 102400;

ZLStatisticsGenerator::ZLStatisticsGenerator(const std::string &breakSymbols) {
	myBreakSymbolsTable = new char[256];
	memset(myBreakSymbolsTable, 0, 256);
	for (int i = breakSymbols.size() - 1; i >= 0; --i) {
		myBreakSymbolsTable[(unsigned char)breakSymbols[i]] = 1;
	}
	myStart = new char[ourBufferSize];
	myEnd = myStart;
}

ZLStatisticsGenerator::~ZLStatisticsGenerator() {
	delete[] myStart;
	delete[] myBreakSymbolsTable;
}

int ZLStatisticsGenerator::read(const std::string &inputFileName) {
	shared_ptr<ZLInputStream> stream = ZLFile(inputFileName).inputStream();
	if (stream.isNull() || !stream->open()) {
		return 1;
	}
	myEnd = myStart + stream->read(myStart, ourBufferSize);
	std::string out = inputFileName;
	stream->close();
	return 0;
}

void ZLStatisticsGenerator::generate(const std::string &inputFileName, std::size_t charSequenceSize, ZLMapBasedStatistics &statistics) {
	if (read(inputFileName) == 1) {
		return;
	}
	if ((std::size_t)(myEnd - myStart) < (charSequenceSize-1)) {
		return;
	}
	generate(myStart, myEnd - myStart, charSequenceSize, statistics);
}

void ZLStatisticsGenerator::generate(const char* buffer, std::size_t length, std::size_t charSequenceSize, ZLMapBasedStatistics &statistics) {
	const char *start = buffer;
	const char *end = buffer + length;
	std::map<ZLCharSequence, std::size_t> dictionary;
	std::size_t locker = charSequenceSize;
	for (const char *ptr = start; ptr < end;) {
		if (myBreakSymbolsTable[(unsigned char)*(ptr)] == 1) {
			locker = charSequenceSize;
		} else if (locker != 0) {
			--locker;
		}
		if (locker == 0) {
			const char* sequenceStart = ptr - charSequenceSize + 1;
			++dictionary[ZLCharSequence(sequenceStart, charSequenceSize)];
		}
		++ptr;
	}
	statistics = ZLMapBasedStatistics(dictionary);
}
