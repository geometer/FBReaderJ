/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLDir.h>
#include <ZLUnicodeUtil.h>
#include <ZLEncodingConverter.h>

#include "ZLLanguageList.h"
#include "ZLLanguageDetector.h"
#include "ZLLanguageMatcher.h"
#include "ZLStatisticsGenerator.h"
#include "ZLStatistics.h"
#include "ZLCharSequence.h"

ZLLanguageDetector::LanguageInfo::LanguageInfo(const std::string &language, const std::string &encoding) : Language(language), Encoding(encoding) {
}

ZLLanguageDetector::ZLLanguageDetector() {
	const ZLFile patternsArchive(ZLLanguageList::patternsDirectoryPath());
	shared_ptr<ZLInputStream> lock = patternsArchive.inputStream();
	shared_ptr<ZLDir> dir = patternsArchive.directory(false);
	if (!dir.isNull()) {
		std::vector<std::string> fileNames;
		dir->collectFiles(fileNames, false);
		for (std::vector<std::string>::const_iterator it = fileNames.begin(); it != fileNames.end(); ++it) {
			const int index = it->find('_');
			if (index != -1) {
				const std::string language = it->substr(0, index);
				const std::string encoding = it->substr(index + 1);
				shared_ptr<ZLStatisticsBasedMatcher> matcher = new ZLStatisticsBasedMatcher(dir->itemPath(*it), new LanguageInfo(language, encoding));
				myMatchers.push_back(matcher);
			}
		}
	}
}

ZLLanguageDetector::~ZLLanguageDetector() {
}

static std::string naiveEncodingDetection(const unsigned char *buffer, std::size_t length) {
	if (buffer[0] == 0xFE && buffer[1] == 0xFF) {
		return ZLEncodingConverter::UTF16BE;
	}
	if (buffer[0] == 0xFF && buffer[1] == 0xFE) {
		return ZLEncodingConverter::UTF16;
	}

	bool ascii = true;
	const unsigned char *end = buffer + length;
	int utf8count = 0;
	for (const unsigned char *ptr = buffer; ptr < end; ++ptr) {
		if (utf8count > 0) {
			if ((*ptr & 0xc0) != 0x80) {
				return std::string();
			}
			--utf8count;
		} else if ((*ptr & 0x80) == 0) {
		} else if ((*ptr & 0xe0) == 0xc0) {
			ascii = false;
			utf8count = 1;
		} else if ((*ptr & 0xf0) == 0xe0) {
			ascii = false;
			utf8count = 2;
		} else if ((*ptr & 0xf8) == 0xf0) {
			ascii = false;
			utf8count = 3;
		} else {
			return std::string();
		}
	}
	return ascii ? ZLEncodingConverter::ASCII : ZLEncodingConverter::UTF8;
}

shared_ptr<ZLLanguageDetector::LanguageInfo> ZLLanguageDetector::findInfo(const char *buffer, std::size_t length, int matchingCriterion) {
	std::string naive;
	if ((unsigned char)buffer[0] == 0xFE &&
			(unsigned char)buffer[1] == 0xFF) {
		naive = ZLEncodingConverter::UTF16BE;
	} else if ((unsigned char)buffer[0] == 0xFF &&
			(unsigned char)buffer[1] == 0xFE) {
		naive = ZLEncodingConverter::UTF16;
	} else {
		naive = naiveEncodingDetection((const unsigned char*)buffer, length);
	}
	return findInfoForEncoding(naive, buffer, length, matchingCriterion);
}

shared_ptr<ZLLanguageDetector::LanguageInfo> ZLLanguageDetector::findInfoForEncoding(const std::string &encoding, const char *buffer, std::size_t length, int matchingCriterion) {
	shared_ptr<LanguageInfo> info;
	std::map<int,shared_ptr<ZLMapBasedStatistics> > statisticsMap;
	for (SBVector::const_iterator it = myMatchers.begin(); it != myMatchers.end(); ++it) {
		if (!encoding.empty() && (*it)->info()->Encoding != encoding) {
			continue;
		}

		const int charSequenceLength = (*it)->charSequenceLength();
		shared_ptr<ZLMapBasedStatistics> stat = statisticsMap[charSequenceLength];
		if (stat.isNull()) {
			stat = new ZLMapBasedStatistics();
			ZLStatisticsGenerator("\r\n ").generate(
				buffer, length, charSequenceLength, *stat
			);
			statisticsMap[charSequenceLength] = stat;
		}
		const int criterion = (*it)->criterion(*stat);
		if (criterion > matchingCriterion) {
			info = (*it)->info();
			matchingCriterion = criterion;
		}
	}
	return info;
}
