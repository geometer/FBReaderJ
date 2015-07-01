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

#ifndef __ZLDIR_H__
#define __ZLDIR_H__

#include <string>
#include <vector>

#include <shared_ptr.h>

class ZLDir {

public:
	static shared_ptr<ZLDir> root();

protected:
	ZLDir(const std::string &path);

public:
	virtual ~ZLDir();
	const std::string &path() const;
	std::string name() const;
	std::string parentPath() const;
	std::string itemPath(const std::string &name) const;
	bool isRoot() const;

	//virtual void collectSubDirs(std::vector<std::string> &names, bool includeSymlinks) = 0;
	virtual void collectFiles(std::vector<std::string> &names, bool includeSymlinks) = 0;

protected:
	virtual std::string delimiter() const = 0;

private:
	std::string myPath;

private:
	ZLDir(const ZLDir&);
	const ZLDir &operator = (const ZLDir&);
};

#endif /* __ZLDIR_H__ */
