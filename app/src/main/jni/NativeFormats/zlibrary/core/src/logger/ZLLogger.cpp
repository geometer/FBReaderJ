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

#include <android/log.h>

#include "ZLLogger.h"

const std::string ZLLogger::DEFAULT_CLASS;

ZLLogger *ZLLogger::ourInstance = 0;

ZLLogger &ZLLogger::Instance() {
	if (ourInstance == 0) {
		ourInstance = new ZLLogger();
	}
	return *ourInstance;
}

ZLLogger::ZLLogger() {
}

void ZLLogger::registerClass(const std::string &className) {
	myRegisteredClasses.insert(className);
}

void ZLLogger::print(const std::string &className, const std::string &message) const {
	std::string m = message;
	for (std::size_t index = m.find('%'); index != std::string::npos; index = m.find('%', index + 2)) {
		m.replace(index, 1, "%%");
	}
	if (className == DEFAULT_CLASS) {
		__android_log_print(ANDROID_LOG_WARN, "ZLLogger", "%s", m.c_str());
	} else {
		if (myRegisteredClasses.find(className) != myRegisteredClasses.end()) {
			__android_log_print(ANDROID_LOG_WARN, className.c_str(), "%s", m.c_str());
		}
	}
}

void ZLLogger::println(const std::string &className, const std::string &message) const {
	print(className, message);
}
