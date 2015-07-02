/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
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
#include "Utf8EncodingConverter.h"
#include "Utf16EncodingConverters.h"
#include "JavaEncodingConverter.h"

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
	registerProvider(new Utf8EncodingConverterProvider());
	registerProvider(new Utf16EncodingConverterProvider());
	registerProvider(new JavaEncodingConverterProvider());
}

void ZLEncodingCollection::registerProvider(shared_ptr<ZLEncodingConverterProvider> provider) {
	myProviders.push_back(provider);
}

ZLEncodingCollection::~ZLEncodingCollection() {
}

shared_ptr<ZLEncodingConverter> ZLEncodingCollection::converter(const std::string &name) const {
	for (std::vector<shared_ptr<ZLEncodingConverterProvider> >::const_iterator it = myProviders.begin(); it != myProviders.end(); ++it) {
		if ((*it)->providesConverter(name)) {
			return (*it)->createConverter(name);
		}
	}
	return 0;
}

shared_ptr<ZLEncodingConverter> ZLEncodingCollection::converter(int code) const {
	std::string name;
	ZLStringUtil::appendNumber(name, code);
	return converter(name);
}

shared_ptr<ZLEncodingConverter> ZLEncodingCollection::defaultConverter() const {
	return converter(ZLEncodingConverter::UTF8);
}
