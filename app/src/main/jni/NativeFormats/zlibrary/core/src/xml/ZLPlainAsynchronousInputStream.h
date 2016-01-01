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

#ifndef __ZLPLAINASYNCHRONOUSINPUTSTREAM_H__
#define __ZLPLAINASYNCHRONOUSINPUTSTREAM_H__

#include <ZLAsynchronousInputStream.h>


class ZLPlainAsynchronousInputStream : public ZLAsynchronousInputStream {

public:
	ZLPlainAsynchronousInputStream(const char *encoding = 0);

private:
	bool processInputInternal(Handler &handler);

private:
	// disable copying
	ZLPlainAsynchronousInputStream(const ZLPlainAsynchronousInputStream &);
	const ZLPlainAsynchronousInputStream &operator = (const ZLPlainAsynchronousInputStream &);
};

#endif /* __ZLPLAINASYNCHRONOUSINPUTSTREAM_H__ */
