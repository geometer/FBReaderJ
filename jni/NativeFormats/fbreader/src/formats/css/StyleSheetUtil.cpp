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

#include <ZLStringUtil.h>

#include "StyleSheetUtil.h"

std::string StyleSheetUtil::strip(const std::string &data) {
	std::string res = data;
	ZLStringUtil::stripWhiteSpaces(res);
	if (res.size() > 1 && (res[0] == '"' || res[0] == '\'') && res[0] == res[res.size() - 1]) {
		return res.substr(1, res.size() - 2);
	} else {
		return res;
	}
}

std::vector<std::string> StyleSheetUtil::splitCommaSeparatedList(const std::string &data) {
	std::vector<std::string> split;

	enum {
		S_QUOTED,
		D_QUOTED,
		NORMAL
	} state = NORMAL;

	std::size_t start = 0;
	for (std::size_t i = 0; i < data.size(); ++i) {
		const char ch = data[i];
		switch (state) {
			case NORMAL:
				if (ch == ',') {
					if (i > start) {
						split.push_back(strip(data.substr(start, i - start)));
					}
					start = i + 1;
				}
				break;
			case S_QUOTED:
				if (ch == '\'') {
					state = NORMAL;
				}
				break;
			case D_QUOTED:
				if (ch == '"') {
					state = NORMAL;
				}
				break;
		}
	}
	if (data.size() > start) {
		split.push_back(strip(data.substr(start)));
	}

	return split;
}

ZLTextStyleEntry::DisplayCode StyleSheetUtil::displayCode(const std::string &data) {
	if (data.empty()) {
		return ZLTextStyleEntry::DC_NOT_DEFINED;
	}

	static const char* values[] = {
		"inline", "block", "flex", "inline-block", "inline-flex",
		"inline-table", "list-item", "run-in", "table", "table-caption",
		"table-column-group", "table-header-group", "table-footer-group",
		"table-row-group", "table-cell", "table-column", "table-row",
		"none", "initial", "inherit"
	};
	int index = sizeof(values) / sizeof(const char*) - 1;
	for (; index >= 0; --index) {
		if (data == values[index]) {
			return (ZLTextStyleEntry::DisplayCode)index;
		}
	}
	return ZLTextStyleEntry::DC_NOT_DEFINED;
}
