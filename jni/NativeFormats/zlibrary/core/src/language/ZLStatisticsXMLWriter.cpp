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

#include <string>
#include <ZLStringUtil.h>

#include "ZLCharSequence.h"
#include "ZLStatistics.h"
#include "ZLStatisticsItem.h"
#include "ZLStatisticsXMLWriter.h"
#include <shared_ptr.h>

void ZLStatisticsXMLWriter::writeStatistics(const ZLMapBasedStatistics &statistics) {
	addTag("statistics", false);
	std::string charSequenceSizeString;
	std::string volumeString;
	std::string squaresVolumeString;
	std::string sizeString;
	ZLStringUtil::appendNumber(charSequenceSizeString, statistics.getCharSequenceSize());
	ZLStringUtil::appendNumber(sizeString, statistics.getSize());
	ZLStringUtil::appendNumber(volumeString, statistics.getVolume());
	ZLStatisticsXMLWriter::appendLongNumber(squaresVolumeString, statistics.getSquaresVolume());
	addAttribute("charSequenceSize", charSequenceSizeString);
	addAttribute("size", sizeString);
	addAttribute("volume", volumeString);
	addAttribute("squaresVolume", squaresVolumeString);
	//ZLStatisticsItem *ptr = statistics.begin();
	//const ZLStatisticsItem *end = statistics.end();
	shared_ptr<ZLStatisticsItem> ptr = statistics.begin();
	const shared_ptr<ZLStatisticsItem> end = statistics.end();
	while (*ptr != *end) {
		writeSequence(ptr->sequence().toHexSequence(), ptr->frequency());
		ptr->next();
	}
	//delete ptr;
	//delete end;
	closeTag();
}

void ZLStatisticsXMLWriter::writeSequence(const std::string &key, size_t frequency) {
	addTag("item", true);
	addAttribute("sequence", key);
	std::string frequencyString;
	ZLStringUtil::appendNumber(frequencyString, frequency);
	addAttribute("frequency", frequencyString);
}

void ZLStatisticsXMLWriter::appendLongNumber(std::string &str, unsigned long long n) {
	int len;
	if (n > 0) {
		len = 0;
		for (unsigned long long copy = n; copy > 0; copy /= 10) {
			len++;
		}
	} else {
		len = 1;
	}

	str.append(len, '\0');
	char *ptr = (char*)str.data() + str.length() - 1;
	for (int i = 0; i < len; ++i) {
		*ptr-- = '0' + n % 10;
		n /= 10;
	}
}
