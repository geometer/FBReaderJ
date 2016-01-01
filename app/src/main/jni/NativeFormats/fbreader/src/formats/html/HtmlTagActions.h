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

#ifndef __HTMLTAGACTIONS_H__
#define __HTMLTAGACTIONS_H__

#include <set>

#include "HtmlBookReader.h"

class HtmlTagAction {

protected:
	HtmlTagAction(HtmlBookReader &reader);

public:
	virtual ~HtmlTagAction();
	virtual void run(const HtmlReader::HtmlTag &tag) = 0;
	virtual void reset();

protected:
	BookReader &bookReader();

protected:
	HtmlBookReader &myReader;
};

class DummyHtmlTagAction : public HtmlTagAction {

public:
	DummyHtmlTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class HtmlControlTagAction : public HtmlTagAction {

public:
	HtmlControlTagAction(HtmlBookReader &reader, FBTextKind kind);
	void run(const HtmlReader::HtmlTag &tag);

private:
	FBTextKind myKind;
};

class HtmlHeaderTagAction : public HtmlTagAction {

public:
	HtmlHeaderTagAction(HtmlBookReader &reader, FBTextKind kind);
	void run(const HtmlReader::HtmlTag &tag);

private:
	FBTextKind myKind;
};

class HtmlIgnoreTagAction : public HtmlTagAction {

public:
	HtmlIgnoreTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);

private:
	std::set<std::string> myTagNames;
};

class HtmlHrefTagAction : public HtmlTagAction {

public:
	HtmlHrefTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
	void reset();

protected:
	FBTextKind hyperlinkType() const;
	void setHyperlinkType(FBTextKind hyperlinkType);

private:
	FBTextKind myHyperlinkType;
};

class HtmlImageTagAction : public HtmlTagAction {

public:
	HtmlImageTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class HtmlBreakTagAction : public HtmlTagAction {

public:
	enum BreakType {
		BREAK_AT_START = 1,
		BREAK_AT_END = 2,
		BREAK_AT_START_AND_AT_END = BREAK_AT_START | BREAK_AT_END
	};
	HtmlBreakTagAction(HtmlBookReader &reader, BreakType breakType);
	void run(const HtmlReader::HtmlTag &tag);

private:
	BreakType myBreakType;
};

class HtmlPreTagAction : public HtmlTagAction {

public:
	HtmlPreTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class HtmlListTagAction : public HtmlTagAction {

public:
	HtmlListTagAction(HtmlBookReader &reader, int startIndex);
	void run(const HtmlReader::HtmlTag &tag);

private:
	int myStartIndex;
};

class HtmlListItemTagAction : public HtmlTagAction {

public:
	HtmlListItemTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class HtmlTableTagAction : public HtmlTagAction {

public:
	HtmlTableTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

class HtmlStyleTagAction : public HtmlTagAction {

public:
	HtmlStyleTagAction(HtmlBookReader &reader);
	void run(const HtmlReader::HtmlTag &tag);
};

inline BookReader &HtmlTagAction::bookReader() { return myReader.myBookReader; }

#endif /* __HTMLTAGACTIONS_H__ */
