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

#include <ZLFile.h>
#include <ZLibrary.h>
#include <ZLStringUtil.h>
#include <ZLUnicodeUtil.h>
#include <ZLXMLReader.h>

#include "ZLEncodingConverter.h"
#include "DummyEncodingConverter.h"
//#include "MyEncodingConverter.h"
#include "EncodingCollectionReader.h"

ZLEncodingCollection *ZLEncodingCollection::ourInstance = 0;

ZLEncodingCollection &ZLEncodingCollection::Instance() {
	if (ourInstance == 0) {
		ourInstance = new ZLEncodingCollection();
	}
	return *ourInstance;
}

std::string ZLEncodingCollection::encodingDescriptionPath() {
	return ZLibrary::ZLibraryDirectory() + ZLibrary::FileNameDelimiter + "encodings";
}

ZLEncodingCollection::ZLEncodingCollection() {
	registerProvider(new DummyEncodingConverterProvider());
	//registerProvider(new MyEncodingConverterProvider());
}

void ZLEncodingCollection::registerProvider(shared_ptr<ZLEncodingConverterProvider> provider) {
	myProviders.push_back(provider);
}

void ZLEncodingCollection::init() {
	if (mySets.empty()) {
		ZLEncodingCollectionReader(*this).readDocument(ZLFile(
			encodingDescriptionPath() + ZLibrary::FileNameDelimiter + "Encodings.xml"
		));
	}
}

ZLEncodingCollection::~ZLEncodingCollection() {
}

const std::vector<shared_ptr<ZLEncodingConverterProvider> > &ZLEncodingCollection::providers() const {
	return myProviders;
}

const std::vector<shared_ptr<ZLEncodingSet> > &ZLEncodingCollection::sets() {
	init();
	return mySets;
}

ZLEncodingConverterInfoPtr ZLEncodingCollection::info(const std::string &name) {
	init();
	std::string lowerCaseName = ZLUnicodeUtil::toLower(name);
	if (lowerCaseName == "iso-8859-1") {
		lowerCaseName = "windows-1252";
	}
	return myInfosByName[lowerCaseName];
}

shared_ptr<ZLEncodingConverter> ZLEncodingCollection::defaultConverter() {
	return DummyEncodingConverterProvider().createConverter();
}

ZLEncodingConverterInfoPtr ZLEncodingCollection::info(int code) {
	std::string name;
	ZLStringUtil::appendNumber(name, code);
	return info(name);
}
