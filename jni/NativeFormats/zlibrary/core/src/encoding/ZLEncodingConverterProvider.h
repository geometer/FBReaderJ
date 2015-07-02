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

#ifndef __ZLENCODINGCONVERTERPROVIDER_H__
#define __ZLENCODINGCONVERTERPROVIDER_H__

#include <string>

#include <shared_ptr.h>

class ZLEncodingConverter;

class ZLEncodingConverterProvider {

protected:
	ZLEncodingConverterProvider();

public:
	virtual ~ZLEncodingConverterProvider();
	virtual bool providesConverter(const std::string &encoding) = 0;
	virtual shared_ptr<ZLEncodingConverter> createConverter(const std::string &encoding) = 0;

private:
	ZLEncodingConverterProvider(const ZLEncodingConverterProvider&);
	const ZLEncodingConverterProvider &operator = (const ZLEncodingConverterProvider&);
};

#endif /* __ZLENCODINGCONVERTERPROVIDER_H__ */
