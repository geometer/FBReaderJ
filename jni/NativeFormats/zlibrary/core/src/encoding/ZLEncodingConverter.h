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

#ifndef __ZLENCODINGCONVERTER_H__
#define __ZLENCODINGCONVERTER_H__

#include <string>
#include <vector>
#include <map>

#include <shared_ptr.h>

class ZLEncodingConverter {

public:
	static const std::string ASCII;
	static const std::string UTF8;
	static const std::string UTF16;
	static const std::string UTF16BE;

protected:
	ZLEncodingConverter();

public:
	virtual ~ZLEncodingConverter();
	virtual std::string name() const = 0;
	virtual void convert(std::string &dst, const char *srcStart, const char *srcEnd) = 0;
	void convert(std::string &dst, const std::string &src);
	virtual void reset() = 0;
	virtual bool fillTable(int *map) = 0;

private:
	ZLEncodingConverter(const ZLEncodingConverter&);
	ZLEncodingConverter &operator = (const ZLEncodingConverter&);
};

class ZLEncodingConverterProvider;

class ZLEncodingCollection {

public:
	static ZLEncodingCollection &Instance();
	static std::string encodingDescriptionPath();

private:
	static ZLEncodingCollection *ourInstance;

public:
	shared_ptr<ZLEncodingConverter> converter(const std::string &name) const;
	shared_ptr<ZLEncodingConverter> converter(int code) const;
	shared_ptr<ZLEncodingConverter> defaultConverter() const;
	void registerProvider(shared_ptr<ZLEncodingConverterProvider> provider);

private:
	std::vector<shared_ptr<ZLEncodingConverterProvider> > myProviders;

private:
	ZLEncodingCollection();
	~ZLEncodingCollection();
};

#endif /* __ZLENCODINGCONVERTER_H__ */
