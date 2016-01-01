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

#ifndef __ZLLOGGER_H__
#define __ZLLOGGER_H__

#include <string>
#include <set>

class ZLLogger {

public:
	static const std::string DEFAULT_CLASS;

	static ZLLogger &Instance();

private:
	static ZLLogger *ourInstance;

private:
	ZLLogger();

public:
	void registerClass(const std::string &className);
	void print(const std::string &className, const std::string &message) const;
	void println(const std::string &className, const std::string &message) const;

private:
	std::set<std::string> myRegisteredClasses;
};

#endif /* __ZLLOGGER_H__ */
