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

#include <cstdlib>
#include <string>
#include <map>

#include <ZLFile.h>
#include <ZLInputStream.h>

#include "ZLCharSequence.h"
#include "ZLStatistics.h"
#include "ZLStatisticsXMLReader.h"

const std::string ZLStatisticsXMLReader::ITEM_TAG = "item";
const std::string ZLStatisticsXMLReader::STATISTICS_TAG = "statistics";

void ZLStatisticsXMLReader::startElementHandler(const char *tag, const char **attributes) {
	if (STATISTICS_TAG == tag) {
		std::size_t volume = atoi(attributeValue(attributes, "volume"));
		unsigned long long squaresVolume = atoll(attributeValue(attributes, "squaresVolume"));
		//std::cerr << "XMLReader: frequencies sum & ^2: " << volume << ":" << squaresVolume << "\n";
		myStatisticsPtr = new ZLArrayBasedStatistics( atoi(attributeValue(attributes, "charSequenceSize")), atoi(attributeValue(attributes, "size")), volume, squaresVolume);
	} else if (ITEM_TAG == tag) {
		const char *sequence = attributeValue(attributes, "sequence");
		const char *frequency = attributeValue(attributes, "frequency");
		if ((sequence != 0) && (frequency != 0)) {
			std::string hexString(sequence);
			myStatisticsPtr->insert(ZLCharSequence(hexString), atoi(frequency));
		}
	}
}

static std::map<std::string, shared_ptr<ZLArrayBasedStatistics> > statisticsMap;

shared_ptr<ZLArrayBasedStatistics> ZLStatisticsXMLReader::readStatistics(const std::string &fileName) {
	std::map<std::string, shared_ptr<ZLArrayBasedStatistics> >::iterator it = statisticsMap.find(fileName);
	if (it != statisticsMap.end()) {
		return it->second;
	}

	shared_ptr<ZLInputStream> statisticsStream = ZLFile(fileName).inputStream();
	if (statisticsStream.isNull() || !statisticsStream->open()) {
 		return 0;
 	}
	readDocument(statisticsStream);
	statisticsStream->close();

	statisticsMap.insert(std::make_pair(fileName, myStatisticsPtr));

	return myStatisticsPtr;
}
