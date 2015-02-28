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

#ifndef __ZLLANGUAGEMATCHER_H__
#define __ZLLANGUAGEMATCHER_H__

#include "ZLLanguageDetector.h"

#include "ZLStatistics.h"

class ZLLanguageMatcher {

public:
	ZLLanguageMatcher(shared_ptr<ZLLanguageDetector::LanguageInfo> info);
	virtual ~ZLLanguageMatcher();

	shared_ptr<ZLLanguageDetector::LanguageInfo> info() const;

private:
	shared_ptr<ZLLanguageDetector::LanguageInfo> myInfo;
};

class ZLStatisticsBasedMatcher : public ZLLanguageMatcher {

public:
	ZLStatisticsBasedMatcher(const std::string &fileName, shared_ptr<ZLLanguageDetector::LanguageInfo> info);
	~ZLStatisticsBasedMatcher(); // надо ли его объявлять, если он ничего не делает??

	int charSequenceLength() const;
	int criterion(const ZLStatistics &otherStatistics) const;

private:
	shared_ptr<ZLArrayBasedStatistics> myStatisticsPtr;
};

#endif /* __ZLLANGUAGEMATCHER_H__ */
