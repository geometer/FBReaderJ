/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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

#include "ZLEncodingConverter.h"
#include "ZLEncodingConverterProvider.h"

ZLEncodingConverterProvider::ZLEncodingConverterProvider() {
}

ZLEncodingConverterProvider::~ZLEncodingConverterProvider() {
}

bool ZLEncodingConverterInfo::canCreateConverter() const {
	ZLEncodingCollection &collection = ZLEncodingCollection::Instance();
	const std::vector<shared_ptr<ZLEncodingConverterProvider> > &providers = collection.providers();
	for (std::vector<shared_ptr<ZLEncodingConverterProvider> >::const_iterator it = providers.begin(); it != providers.end(); ++it) {
		for (std::vector<std::string>::const_iterator jt = myAliases.begin(); jt != myAliases.end(); ++jt) {
			if ((*it)->providesConverter(*jt)) {
				return true;
			}
		}
	}

	return false;
}

shared_ptr<ZLEncodingConverter> ZLEncodingConverterInfo::createConverter() const {
	ZLEncodingCollection &collection = ZLEncodingCollection::Instance();
	const std::vector<shared_ptr<ZLEncodingConverterProvider> > &providers = collection.providers();
	for (std::vector<shared_ptr<ZLEncodingConverterProvider> >::const_iterator it = providers.begin(); it != providers.end(); ++it) {
		for (std::vector<std::string>::const_iterator jt = myAliases.begin(); jt != myAliases.end(); ++jt) {
			if ((*it)->providesConverter(*jt)) {
				return (*it)->createConverter(*jt);
			}
		}
	}

	return ZLEncodingCollection::Instance().defaultConverter();
}

ZLEncodingConverter::ZLEncodingConverter() {
}

ZLEncodingConverter::~ZLEncodingConverter() {
}

void ZLEncodingConverter::convert(std::string &dst, const std::string &src) {
	convert(dst, src.data(), src.data() + src.length());
}

ZLEncodingConverterInfo::ZLEncodingConverterInfo(const std::string &name, const std::string &region) : myName(name), myVisibleName(region + " (" + name + ")") {
	addAlias(myName);
}

const std::string &ZLEncodingConverterInfo::name() const {
	return myName;
}

const std::string &ZLEncodingConverterInfo::visibleName() const {
	return myVisibleName;
}

void ZLEncodingConverterInfo::addAlias(const std::string &alias) {
	myAliases.push_back(alias);
}
