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

#include <set>
#include <algorithm>

#include <AndroidUtil.h>
#include <JniEnvelope.h>

#include <ZLUnicodeUtil.h>

#include "Tag.h"

TagList Tag::ourRootTags;
std::map <int,shared_ptr<Tag> > Tag::ourTagsById;

const std::string Tag::DELIMITER = "/";

shared_ptr<Tag> Tag::getTag(const std::string &name, shared_ptr<Tag> parent, int tagId) {
	if (name.empty()) {
		return 0;
	}
	TagList &tags = parent.isNull() ? ourRootTags : parent->myChildren;
	for (TagList::const_iterator it = tags.begin(); it != tags.end(); ++it) {
		if ((*it)->name() == name) {
			return *it;
		}
	}
	shared_ptr<Tag> t = new Tag(name, parent, tagId);
	tags.push_back(t);
	if (tagId > 0) {
		ourTagsById[tagId] = t;
	}
	return t;
}

shared_ptr<Tag> Tag::getTagByFullName(const std::string &fullName) {
	std::string tag = fullName;
	ZLUnicodeUtil::utf8Trim(tag);
	std::size_t index = tag.rfind(DELIMITER);
	if (index == std::string::npos) {
		return getTag(tag);
	} else {
		std::string lastName = tag.substr(index + 1);
		ZLUnicodeUtil::utf8Trim(lastName);
		return getTag(lastName, getTagByFullName(tag.substr(0, index)));
	}
}

shared_ptr<Tag> Tag::getTagById(int tagId) {
	std::map<int,shared_ptr<Tag> >::const_iterator it = ourTagsById.find(tagId);
	return it != ourTagsById.end() ? it->second : 0;
}

shared_ptr<Tag> Tag::cloneSubTag(shared_ptr<Tag> tag, shared_ptr<Tag> oldparent, shared_ptr<Tag> newparent) {
	std::vector<std::string> levels;

	while (tag != oldparent) {
		levels.push_back(tag->name());
		tag = tag->parent();
		if (tag.isNull()) {
			return 0;
		}
	}

	if (levels.empty()) {
		return 0;
	}

	shared_ptr<Tag> res = newparent;
	while (!levels.empty()) {
		res = getTag(levels.back(), res);
		levels.pop_back();
	}
	return res;
}

void Tag::collectAncestors(shared_ptr<Tag> tag, TagList &parents) {
	for (; !tag.isNull(); tag = tag->parent()) {
		parents.push_back(tag);
	}
	std::reverse(parents.begin(), parents.end());
}

void Tag::collectTagNames(std::vector<std::string> &tags) {
	std::set<std::string> tagsSet;
	TagList stack(ourRootTags);
	while (!stack.empty()) {
		shared_ptr<Tag> tag = stack.back();
		stack.pop_back();
		tagsSet.insert(tag->fullName());
		stack.insert(stack.end(), tag->myChildren.begin(), tag->myChildren.end());
	}
	tags.insert(tags.end(), tagsSet.begin(), tagsSet.end());
}

Tag::Tag(const std::string &name, shared_ptr<Tag> parent, int tagId) : myName(name), myParent(parent), myLevel(parent.isNull() ? 0 : parent->level() + 1), myTagId(tagId), myJavaTag(0) {
}

Tag::~Tag() {
	JNIEnv *env = AndroidUtil::getEnv();
	env->DeleteGlobalRef(myJavaTag);
}

const std::string &Tag::fullName() const {
	if (myParent.isNull()) {
		return myName;
	}
	if (myFullName.empty()) {
		myFullName = myParent->fullName() + DELIMITER + myName;
	}
	return myFullName;
}

jobject Tag::javaTag(JNIEnv *env) const {
	if (myJavaTag != 0) {
		return myJavaTag;
	}
	jobject parentTag = 0;
	if (!myParent.isNull()) {
		parentTag = myParent->javaTag(env);
	}

	jobject javaName = env->NewStringUTF(myName.c_str());
	jobject tag = AndroidUtil::StaticMethod_Tag_getTag->call(parentTag, javaName);
	myJavaTag = env->NewGlobalRef(tag);
	env->DeleteLocalRef(tag);
	env->DeleteLocalRef(javaName);
	return myJavaTag;
}


bool Tag::isAncestorOf(shared_ptr<Tag> tag) const {
	if (tag->level() <= level()) {
		return false;
	}
	while (tag->level() > level()) {
		tag = tag->parent();
	}
	return &*tag == this;
}

void Tag::setTagId(shared_ptr<Tag> tag, int tagId) {
	if (tag.isNull() || tag->myTagId != 0) {
		return;
	}
	tag->myTagId = tagId;
	ourTagsById[tagId] = tag;
}
