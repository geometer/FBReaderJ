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

#include <algorithm>
#include <map>

#include "ZLCharSequence.h"
#include "ZLStatistics.h"
#include "ZLStatisticsItem.h"

ZLStatistics::ZLStatistics() : myCharSequenceSize(0), myVolumesAreUpToDate(true),
		myVolume(0), mySquaresVolume(0) {
}

ZLStatistics::ZLStatistics(std::size_t charSequenceSize) :
		myCharSequenceSize(charSequenceSize), myVolumesAreUpToDate(true),
		myVolume(0), mySquaresVolume(0) {
}

ZLStatistics::ZLStatistics(std::size_t charSequenceSize, std::size_t volume, unsigned long long squaresVolume) :
		myCharSequenceSize(charSequenceSize), myVolumesAreUpToDate(true),
		myVolume(volume), mySquaresVolume(squaresVolume) {
}

ZLStatistics::~ZLStatistics() {
}

std::size_t ZLStatistics::getVolume() const {
	if (!myVolumesAreUpToDate) {
		calculateVolumes();
	}
	return myVolume;
}

unsigned long long ZLStatistics::getSquaresVolume() const {
	if (!myVolumesAreUpToDate) {
		calculateVolumes();
	}
	return mySquaresVolume;
}

static int log10(long long number) {
	int count = 0;
	while (number != 0) {
		number /= 10;
		++count;
	}
	return count;
}

//static int power10(unsigned int number) {
//	int power = 1;
//	while (number-- > 0) {
//		power *= 10;
//	}
//	return power;
//}

int ZLStatistics::correlation(const ZLStatistics& candidate, const ZLStatistics& pattern) {
	if (&candidate == &pattern) {
		return 1000000;
	}
	const unsigned long long candidateSum = candidate.getVolume();
	const unsigned long long patternSum = pattern.getVolume();
	const unsigned long long candidateSum2 = candidate.getSquaresVolume();
	const unsigned long long patternSum2 = pattern.getSquaresVolume();

	shared_ptr<ZLStatisticsItem> ptrA = candidate.begin();
	shared_ptr<ZLStatisticsItem> ptrB = pattern.begin();
	const shared_ptr<ZLStatisticsItem> endA = candidate.end();
	const shared_ptr<ZLStatisticsItem> endB = pattern.end();

	std::size_t count = 0;
	long long correlationSum = 0;
	while ((*ptrA != *endA) && (*ptrB != *endB)) {
		++count;
		const int comparison = ptrA->sequence().compareTo(ptrB->sequence());
		if (comparison < 0) {
			ptrA->next();
		} else if (comparison > 0) {
			ptrB->next();
		} else {
			correlationSum += ptrA->frequency() * ptrB->frequency();
			ptrA->next();
			ptrB->next();
		}
	}
	while (*ptrA != *endA) {
		++count;
		ptrA->next();
	}
	while (*ptrB != *endB) {
		++count;
		ptrB->next();
	}

	const long long patternDispersion = patternSum2 * count - patternSum * patternSum;
	const long long candidateDispersion = candidateSum2 * count - candidateSum * candidateSum;
	const long long numerator = correlationSum * count - candidateSum * patternSum ;

	if ((patternDispersion == 0) || (candidateDispersion == 0)) {
		return 0;
	}

	int orderDiff = ::log10(patternDispersion) - ::log10(candidateDispersion);
	int patternMult = 1000;
	if (orderDiff >= 5) {
		//patternMult = ::power10(6);
		patternMult = 1000000;
	} else if (orderDiff >= 3) {
		//patternMult = ::power10(5);
		patternMult = 100000;
	} else if (orderDiff >= 1) {
		//patternMult = ::power10(4);
		patternMult = 10000;
	} else if (orderDiff <= -1) {
		//patternMult = ::power10(2);
		patternMult = 100;
	} else if (orderDiff <= -3) {
		//patternMult = ::power10(1);
		patternMult = 10;
	} else if (orderDiff <= -5) {
		//patternMult = ::power10(0);
		patternMult = 1;
	}
	int candidateMult = 1000000 / patternMult;

	const long long quotient1 = (patternMult * numerator / patternDispersion);
	const long long quotient2 = (candidateMult * numerator / candidateDispersion);
	const int sign = (numerator >= 0) ? 1 : -1;

	return sign * quotient1 * quotient2;
}


ZLMapBasedStatistics::ZLMapBasedStatistics() : ZLStatistics() {
}

ZLMapBasedStatistics::ZLMapBasedStatistics(const Dictionary &dictionary) {
	if (!dictionary.empty()) {
		myCharSequenceSize = dictionary.begin()->first.getSize();
		myVolumesAreUpToDate = false;
		myDictionary = dictionary;
	} else {
		myCharSequenceSize = 0;
		myVolumesAreUpToDate = true;
		myVolume = 0;
		mySquaresVolume = 0;
	}
}

ZLMapBasedStatistics::~ZLMapBasedStatistics() {
}

void ZLMapBasedStatistics::calculateVolumes() const {
	myVolume = 0;
	mySquaresVolume = 0;
	for (Dictionary::const_iterator it = myDictionary.begin(); it != myDictionary.end(); ++it) {
		const std::size_t frequency = it->second;
		myVolume += frequency;
		mySquaresVolume += frequency * frequency;
	}
	myVolumesAreUpToDate = true;
}

ZLMapBasedStatistics ZLMapBasedStatistics::top(std::size_t amount) const {
	if (myDictionary.empty()) {
		return ZLMapBasedStatistics();
	}
	if (amount >= myDictionary.size()) {
		return *this;
	}
	Dictionary dictionary;
	Vector tempVector;
	tempVector.resize(myDictionary.size());
	std::copy(myDictionary.begin(), myDictionary.end(), tempVector.begin());
	std::sort(tempVector.rbegin(), tempVector.rend(), LessFrequency());
	Vector::const_iterator it = tempVector.begin();
	while (amount != 0) {
		dictionary[it->first] = it->second;
		++it;
		--amount;
	}
	return ZLMapBasedStatistics(dictionary);
}

void ZLMapBasedStatistics::retain(const ZLMapBasedStatistics &other) {
	if (this == &other) {
		return;
	}
	if (myCharSequenceSize == other.myCharSequenceSize) {
		myVolume = 0;
		mySquaresVolume = 0;

		Dictionary::iterator itA = myDictionary.begin();
		Dictionary::const_iterator itB = other.myDictionary.begin();
		const Dictionary::iterator endA = myDictionary.end();
		const Dictionary::const_iterator endB = other.myDictionary.end();

		while ((itA != endA) && (itB != endB)) {
			const int comparison = itA->first.compareTo(itB->first);
			if (comparison < 0) {
				myDictionary.erase(itA++);
			} else if (comparison > 0) {
				++itB;
			} else {
				itA->second += itB->second;
				myVolume += itA->second;
				mySquaresVolume += itA->second * itA->second;
				++itA;
				++itB;
			}
		}
		if (itA != endA) {
			myDictionary.erase(itA, endA);
		}
		myVolumesAreUpToDate = true;
	} else {
		*this = ZLMapBasedStatistics();
	}
}

void ZLMapBasedStatistics::scaleToShort() {
	const std::size_t maxFrequency = std::max_element(myDictionary.begin(), myDictionary.end(), LessFrequency())->second;
	const std::size_t maxShort = 65535;
	if (maxFrequency > maxShort) {
		const std::size_t devider = maxFrequency / maxShort + 1;
		Dictionary::iterator it = myDictionary.begin();
		const Dictionary::iterator end = myDictionary.end();
		while (it != end) {
			if (it->second < devider) {
				myDictionary.erase(it++);
			} else {
				it->second /= devider;
				++it;
			}
		}
	}
}

shared_ptr<ZLStatisticsItem> ZLMapBasedStatistics::begin() const {
	return new ZLMapBasedStatisticsItem(myDictionary.begin(), 0);
}

shared_ptr<ZLStatisticsItem> ZLMapBasedStatistics::end() const {
	return new ZLMapBasedStatisticsItem(myDictionary.end(), myDictionary.size());
}

ZLArrayBasedStatistics::ZLArrayBasedStatistics() : ZLStatistics(),
		myCapacity(0), myBack(0), mySequences(0), myFrequencies(0) {
}

ZLArrayBasedStatistics::ZLArrayBasedStatistics(std::size_t charSequenceSize, std::size_t size, std::size_t volume, unsigned long long squaresVolume) :
		ZLStatistics(charSequenceSize, volume, squaresVolume), myCapacity(size) {
	myBack = 0;
	mySequences = new char[myCharSequenceSize * size];
	myFrequencies = new unsigned short[size];
}

ZLArrayBasedStatistics::~ZLArrayBasedStatistics() {
	if (mySequences != 0) {
		delete[] mySequences;
		delete[] myFrequencies;
	}
}

void ZLArrayBasedStatistics::insert(const ZLCharSequence &charSequence, std::size_t frequency) {
	if (myBack == myCapacity) {
		return;
	}
	for (std::size_t i = 0; i < myCharSequenceSize; ++i) {
		mySequences[myBack * myCharSequenceSize + i] = charSequence[i];
	}
	myFrequencies[myBack] = (unsigned short) frequency;
	++myBack;
	//myVolumesAreUpToDate = false;
}

void ZLArrayBasedStatistics::calculateVolumes() const {
	myVolume = 0;
	mySquaresVolume = 0;
	for (std::size_t i = 0; i != myBack; ++i) {
		const std::size_t frequency = myFrequencies[i];
		myVolume += frequency;
		mySquaresVolume += frequency * frequency;
	}
	myVolumesAreUpToDate = true;
}

shared_ptr<ZLStatisticsItem> ZLArrayBasedStatistics::begin() const {
	return new ZLArrayBasedStatisticsItem(myCharSequenceSize, mySequences, myFrequencies, 0);
}

shared_ptr<ZLStatisticsItem> ZLArrayBasedStatistics::end() const {
	return new ZLArrayBasedStatisticsItem(myCharSequenceSize, mySequences + myBack * myCharSequenceSize, myFrequencies + myBack, myBack);
}

ZLArrayBasedStatistics& ZLArrayBasedStatistics::operator= (const ZLArrayBasedStatistics &other) {
	if (this == &other) {
		return *this;
	}
	myCharSequenceSize = other.myCharSequenceSize;
	myVolumesAreUpToDate = false;
	if (mySequences != 0) {
		delete[] mySequences;
		delete[] myFrequencies;
	}
	myCapacity = other.myCapacity;
	myBack = 0;
	if (other.mySequences != 0) {
		mySequences = new char[myCapacity * other.myCharSequenceSize];
		myFrequencies = new unsigned short[myCapacity];
		while (myBack < other.myBack) {
			mySequences[myBack] = other.mySequences[myBack];
			myFrequencies[myBack] = other.myFrequencies[myBack];
			++myBack;
		}
	} else {
		mySequences = 0;
		myFrequencies = 0;
	}
	return *this;
}
