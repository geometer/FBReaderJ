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


#ifndef __ZLSTATISTICSITEM_H__
#define __ZLSTATISTICSITEM_H__

#include <map>

#include "ZLCharSequence.h"

struct ZLStatisticsItem {

public:
	ZLStatisticsItem(std::size_t index);
	virtual ~ZLStatisticsItem();

	virtual ZLCharSequence sequence() const = 0;
	virtual std::size_t frequency() const = 0;
	virtual void next() = 0;

	bool operator == (const ZLStatisticsItem &otherItem) const;
	bool operator != (const ZLStatisticsItem &otherItem) const;

	std::size_t index() const;

protected:
	std::size_t myIndex;
};

struct ZLMapBasedStatisticsItem : public ZLStatisticsItem {
	ZLMapBasedStatisticsItem(const std::map<ZLCharSequence, std::size_t>::const_iterator it, std::size_t index);

	ZLCharSequence sequence() const;
	std::size_t frequency() const;
	void next();

private:
	std::map<ZLCharSequence, std::size_t>::const_iterator myIterator;
};

struct ZLArrayBasedStatisticsItem : public ZLStatisticsItem {
	ZLArrayBasedStatisticsItem(std::size_t sequenceLength, char* sequencePtr, unsigned short* frequencyPtr, std::size_t index);

	ZLCharSequence sequence() const;
	std::size_t frequency() const;
	void next();

private:
	char const *mySequencePtr;
	unsigned short const *myFrequencyPtr;
	const std::size_t mySequenceLength;
};

inline ZLStatisticsItem::ZLStatisticsItem(std::size_t index) : myIndex(index) {
}

inline ZLStatisticsItem::~ZLStatisticsItem() {
}

inline std::size_t ZLStatisticsItem::index() const {
	return myIndex;
}

inline bool ZLStatisticsItem::operator == (const ZLStatisticsItem& otherItem) const {
	return (this->index() == otherItem.index());
}

inline bool ZLStatisticsItem::operator != (const ZLStatisticsItem& otherItem) const {
	return (this->index() != otherItem.index());
}

#endif /*__ZLSTATISTICSITEM_H__*/
