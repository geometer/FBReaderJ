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

#ifndef __TAG_H__
#define __TAG_H__

#include <jni.h>

#include <string>
#include <map>

#include <shared_ptr.h>

#include "Lists.h"

class Tag {

private:
	static TagList ourRootTags;
	static std::map<int,shared_ptr<Tag> > ourTagsById;

public:
	static shared_ptr<Tag> getTag(const std::string &name, shared_ptr<Tag> parent = 0, int tagId = 0);
	static shared_ptr<Tag> getTagByFullName(const std::string &fullName);
	static shared_ptr<Tag> getTagById(int tagId);

	static void setTagId(shared_ptr<Tag>, int tagId);

	static shared_ptr<Tag> cloneSubTag(shared_ptr<Tag> tag, shared_ptr<Tag> oldparent, shared_ptr<Tag> newparent);

	static void collectAncestors(shared_ptr<Tag> tag, TagList &parents);

	static void collectTagNames(std::vector<std::string> &tags);

private:
	static const std::string DELIMITER;

private:
	Tag(const std::string &name, shared_ptr<Tag> parent, int tagId);

public:
	~Tag();

public:
	const std::string &fullName() const;
	const std::string &name() const;

	shared_ptr<Tag> parent() const;

	jobject javaTag(JNIEnv *env) const;

public:
	bool isAncestorOf(shared_ptr<Tag> tag) const;

	int tagId() const;
	std::size_t level() const;

private:
	const std::string myName;
	mutable std::string myFullName;

	shared_ptr<Tag> myParent;
	TagList myChildren;
	const std::size_t myLevel;

	int myTagId;

	mutable jobject myJavaTag;

private: // disable copying
	Tag(const Tag &);
	const Tag &operator = (const Tag &);
};

class TagComparator {

public:
	bool operator () (
		shared_ptr<Tag> tag0,
		shared_ptr<Tag> tag1
	) const;
};

inline const std::string &Tag::name() const { return myName; }

inline shared_ptr<Tag> Tag::parent() const { return myParent; }

inline int Tag::tagId() const { return myTagId; }
inline std::size_t Tag::level() const { return myLevel; }

#endif /* __TAG_H__ */
