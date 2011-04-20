/*
 * Copyright (C) 2004-2011 Geometer Plus <contact@geometerplus.com>
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

#ifndef __ZLTEXTFONTMODIFIER_H__
#define __ZLTEXTFONTMODIFIER_H__

enum ZLTextFontModifier {
	FONT_MODIFIER_DEFAULT = 0,
	FONT_MODIFIER_BOLD = 1 << 0,
	FONT_MODIFIER_ITALIC = 1 << 1,
	FONT_MODIFIER_SMALLCAPS = 1 << 2,
};

#endif /* __ZLTEXTFONTMODIFIER_H__ */
