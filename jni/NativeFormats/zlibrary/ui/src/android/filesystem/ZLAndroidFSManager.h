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

#ifndef __ZLANDROIDFSMANAGER_H__
#define __ZLANDROIDFSMANAGER_H__

#include "../../../../core/src/unix/filesystem/ZLUnixFSManager.h"

class ZLAndroidFSManager : public ZLUnixFSManager {

public:
	static void createInstance();

private:
	ZLAndroidFSManager();

protected:
	std::string convertFilenameToUtf8(const std::string &name) const;
	std::string mimeType(const std::string &path) const;

private:
	static bool useNativeImplementation(const std::string &path);

protected: // Overridden methods
	void normalizeRealPath(std::string &path) const;

	std::string resolveSymlink(const std::string &path) const;
	ZLFSDir *createNewDirectory(const std::string &path) const;
	ZLFSDir *createPlainDirectory(const std::string &path) const;
	ZLInputStream *createPlainInputStream(const std::string &path) const;
	//ZLOutputStream *createOutputStream(const std::string &path) const;
	bool removeFile(const std::string &path) const;

	ZLFileInfo fileInfo(const std::string &path) const;

	bool canRemoveFile(const std::string &path) const;
};

inline ZLAndroidFSManager::ZLAndroidFSManager() {}
inline void ZLAndroidFSManager::createInstance() { ourInstance = new ZLAndroidFSManager(); }

inline bool ZLAndroidFSManager::useNativeImplementation(const std::string &path) {
	return path.length() > 0 && path[0] == '/';
}

#endif /* __ZLANDROIDFSMANAGER_H__ */
