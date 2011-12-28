/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
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

#ifndef __ZLTEXTMARK_H__
#define __ZLTEXTMARK_H__

struct ZLTextMark {
	int ParagraphIndex;
	int Offset, Length;

	ZLTextMark();
	ZLTextMark(int paragraphIndex, int offset, int length);
	ZLTextMark(const ZLTextMark &mark);
	const ZLTextMark &operator = (const ZLTextMark &mark);
	~ZLTextMark();

	bool operator < (const ZLTextMark &mark) const;
	bool operator > (const ZLTextMark &mark) const;
	bool operator <= (const ZLTextMark &mark) const;
	bool operator >= (const ZLTextMark &mark) const;
};

inline ZLTextMark::ZLTextMark() : ParagraphIndex(-1), Offset(-1), Length(-1) {}
inline ZLTextMark::ZLTextMark(int paragraphIndex, int offset, int length) : ParagraphIndex(paragraphIndex), Offset(offset), Length(length) {}
inline ZLTextMark::ZLTextMark(const ZLTextMark &mark) : ParagraphIndex(mark.ParagraphIndex), Offset(mark.Offset), Length(mark.Length) {}
inline const ZLTextMark &ZLTextMark::operator = (const ZLTextMark &mark) {
	ParagraphIndex = mark.ParagraphIndex;
	Offset = mark.Offset;
	Length = mark.Length;
	return *this;
}
inline ZLTextMark::~ZLTextMark() {}

inline bool ZLTextMark::operator < (const ZLTextMark &mark) const {
	return (ParagraphIndex < mark.ParagraphIndex) ||
				 ((ParagraphIndex == mark.ParagraphIndex) && (Offset < mark.Offset));
}
inline bool ZLTextMark::operator > (const ZLTextMark &mark) const {
	return mark < *this;
}
inline bool ZLTextMark::operator <= (const ZLTextMark &mark) const {
	return !(*this > mark);
}
inline bool ZLTextMark::operator >= (const ZLTextMark &mark) const {
	return !(*this < mark);
}

#endif /* __ZLTEXTMARK_H__ */
