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

#ifndef __ZLSTATISTICS_H__
#define __ZLSTATISTICS_H__

#include <vector>
#include <map>
#include <string>
#include <shared_ptr.h>

#include "ZLCharSequence.h"
#include "ZLStatisticsItem.h"

class ZLStatistics {

public:
	ZLStatistics();
	ZLStatistics(std::size_t charSequenceSize);
	ZLStatistics(std::size_t charSequenceSize, std::size_t volume, unsigned long long squaresVolume);
	virtual ~ZLStatistics();

	std::size_t getVolume() const;
	unsigned long long getSquaresVolume() const;
	std::size_t getCharSequenceSize() const;

public:
	virtual shared_ptr<ZLStatisticsItem> begin() const = 0;
	virtual shared_ptr<ZLStatisticsItem> end() const = 0;

protected:
	virtual void calculateVolumes() const = 0;

public:
	static int correlation(const ZLStatistics &candidate, const ZLStatistics &pattern);

protected:
	std::size_t myCharSequenceSize;
	mutable bool myVolumesAreUpToDate;
	mutable std::size_t myVolume;
	mutable unsigned long long mySquaresVolume;
};

class ZLMapBasedStatistics : public ZLStatistics {

private:
	typedef std::vector<std::pair<ZLCharSequence, std::size_t> > Vector;
	typedef std::map<ZLCharSequence, std::size_t>                Dictionary;

public:
	ZLMapBasedStatistics();
	ZLMapBasedStatistics(const Dictionary &dictionary);
	~ZLMapBasedStatistics();

	std::size_t getSize() const;

	ZLMapBasedStatistics top(std::size_t amount) const;

	void scaleToShort();
	void retain(const ZLMapBasedStatistics &other);

	bool empty() const;

	virtual shared_ptr<ZLStatisticsItem> begin() const;
	virtual shared_ptr<ZLStatisticsItem> end() const;

protected:
	void calculateVolumes() const;

private:
	struct LessFrequency {
		bool operator() (const std::pair<ZLCharSequence, std::size_t> a, const std::pair<ZLCharSequence, std::size_t> b) {
			return (a.second < b.second);
		}
	};

private:
	Dictionary myDictionary;
};

class ZLArrayBasedStatistics : public ZLStatistics {
public:
	ZLArrayBasedStatistics();
	ZLArrayBasedStatistics(std::size_t charSequenceSize, std::size_t size, std::size_t volume, unsigned long long squaresVolume);
	~ZLArrayBasedStatistics();

	ZLArrayBasedStatistics &operator = (const ZLArrayBasedStatistics &other);
	void insert(const ZLCharSequence &charSequence, std::size_t frequency);

	bool empty() const;

	virtual shared_ptr<ZLStatisticsItem> begin() const;
	virtual shared_ptr<ZLStatisticsItem> end() const;

protected:
	void calculateVolumes() const;

private:
	std::size_t myCapacity;
	std::size_t myBack;
	char* mySequences;
	unsigned short* myFrequencies;
};

inline std::size_t ZLStatistics::getCharSequenceSize() const {
	return myCharSequenceSize;
}

inline std::size_t ZLMapBasedStatistics::getSize() const {
	return myDictionary.size();
}

inline bool ZLMapBasedStatistics::empty() const {
	return myDictionary.empty();
}

inline bool ZLArrayBasedStatistics::empty() const {
	return (myBack == 0);
}

#endif //__ZLSTATISTICS_H__
