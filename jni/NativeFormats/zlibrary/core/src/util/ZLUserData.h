/*
 * Copyright (C) 2008-2012 Geometer Plus <contact@geometerplus.com>
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

#ifndef __ZLUSERDATA_H__
#define __ZLUSERDATA_H__

#include <map>
#include <string>

#include <shared_ptr.h>

class ZLUserData {

public:
	virtual ~ZLUserData();
};

class ZLUserDataHolder {

protected:
	virtual ~ZLUserDataHolder();

public:
	void addUserData(const std::string &key, shared_ptr<ZLUserData> data);
	void removeUserData(const std::string &key);
	shared_ptr<ZLUserData> getUserData(const std::string &key) const;

private:
	std::map<std::string,shared_ptr<ZLUserData> > myDataMap;
};

#endif /* __ZLUSERDATA_H__ */
