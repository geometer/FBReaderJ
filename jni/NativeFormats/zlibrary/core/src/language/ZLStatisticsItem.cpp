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

#include "ZLStatisticsItem.h"

ZLMapBasedStatisticsItem::ZLMapBasedStatisticsItem(const std::map<ZLCharSequence, std::size_t>::const_iterator it, std::size_t index) : 	ZLStatisticsItem(index),
																																myIterator(it) {
}

ZLCharSequence ZLMapBasedStatisticsItem::sequence() const {
	return myIterator->first;
}

std::size_t ZLMapBasedStatisticsItem::frequency() const {
	return myIterator->second;
}

void ZLMapBasedStatisticsItem::next() {
	++myIndex;
	++myIterator;
}

ZLArrayBasedStatisticsItem::ZLArrayBasedStatisticsItem(std::size_t sequenceLength, char* sequencePtr, unsigned short* frequencyPtr, std::size_t index) :
	ZLStatisticsItem(index),
	mySequencePtr(sequencePtr),
	myFrequencyPtr(frequencyPtr),
	mySequenceLength(sequenceLength) {
}

ZLCharSequence ZLArrayBasedStatisticsItem::sequence() const {
	return ZLCharSequence(mySequencePtr, mySequenceLength);
}

std::size_t ZLArrayBasedStatisticsItem::frequency() const {
	return (std::size_t) *myFrequencyPtr;
}

void ZLArrayBasedStatisticsItem::next() {
	++myIndex;
	mySequencePtr += mySequenceLength;
	++myFrequencyPtr;
}
