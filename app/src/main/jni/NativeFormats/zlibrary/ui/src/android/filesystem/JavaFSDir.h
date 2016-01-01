/*
 * Copyright (C) 2011-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#ifndef __JAVAFSDIR_H__
#define __JAVAFSDIR_H__

#include <jni.h>

#include "../../../../core/src/filesystem/ZLFSDir.h"

class JavaFSDir : public ZLFSDir {

public:
	JavaFSDir(const std::string &name);
	~JavaFSDir();

	//void collectSubDirs(std::vector<std::string> &names, bool includeSymlinks);
	void collectFiles(std::vector<std::string> &names, bool includeSymlinks);

private:
	void initJavaFile(JNIEnv *env);
	jobjectArray getFileChildren(JNIEnv *env); // returns array of ZLFile or NULL

	void collectChildren(std::vector<std::string> &names, bool filesNotDirs);

private:
	jobject myJavaFile;
};

#endif /* __JAVAFSDIR_H__ */
