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

#ifndef __CSSSELECTOR_H__
#define __CSSSELECTOR_H__

#include <string>

#include <shared_ptr.h>

class CSSSelector {

public:
	enum Relation {
		Ancestor, // "X Y" selector, X is ancestor for Y
		Parent, // "X > Y" selector, X is parent for Y
		Previous, // "X + Y" selector, X is previous sibling for Y
		Predecessor, // "X ~ Y", X is a sibling for Y that was occured before Y
	};

	struct Component {
		Component(Relation delimiter, shared_ptr<CSSSelector> selector);

		const Relation Delimiter;
		const shared_ptr<CSSSelector> Selector;
	};

public:
	static shared_ptr<CSSSelector> parse(const std::string &data);

private:
	static void update(shared_ptr<CSSSelector> &selector, const char *&start, const char *end, char delimiter);

private:
	CSSSelector(const std::string &simple);

public:
	CSSSelector(const std::string &tag, const std::string &clazz);
	bool weakEquals(const CSSSelector &selector) const;
	bool operator < (const CSSSelector &selector) const;

public:
	std::string Tag;
	std::string Class;
	shared_ptr<Component> Next;
};

#endif /* __CSSSELECTOR_H__ */
