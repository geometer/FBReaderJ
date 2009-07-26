/* vim: set tabstop=4 shiftwidth=4: */

/*
 * Line breaking in a Unicode sequence.  Designed to be used in a
 * generic text renderer.
 *
 * Copyright (C) 2008-2009 Wu Yongwei <wuyongwei at gmail dot com>
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the author be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute
 * it freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must
 *    not claim that you wrote the original software.  If you use this
 *    software in a product, an acknowledgement in the product
 *    documentation would be appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must
 *    not be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source
 *    distribution.
 *
 * The main reference is Unicode Standard Annex 14 (UAX #14):
 *		<URL:http://www.unicode.org/reports/tr14/>
 *
 * When this library was designed, this annex was at Revision 19, for
 * Unicode 5.0.0:
 *		<URL:http://www.unicode.org/reports/tr14/tr14-19.html>
 *
 * This library has been updated according to Revision 22, for
 * Unicode 5.1.0:
 *		<URL:http://www.unicode.org/reports/tr14/tr14-22.html>
 *
 * The Unicode Terms of Use are available at
 *		<URL:http://www.unicode.org/copyright.html>
 */

/**
 * @file	linebreak.h
 *
 * Header file for the line breaking algorithm.
 *
 * @version	1.2, 2009/05/03
 * @author	Wu Yongwei
 */

#ifndef LINEBREAK_H
#define LINEBREAK_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifndef LINEBREAK_UTF_TYPES_DEFINED
#define LINEBREAK_UTF_TYPES_DEFINED
typedef unsigned char	utf8_t;		/**< Type for UTF-8 data points */
typedef unsigned short	utf16_t;	/**< Type for UTF-16 data points */
typedef unsigned int	utf32_t;	/**< Type for UTF-32 data points */
#endif

#define LINEBREAK_MUSTBREAK		0	/**< Break is mandatory */
#define LINEBREAK_ALLOWBREAK	1	/**< Break is allowed */
#define LINEBREAK_NOBREAK		2	/**< No break is possible */
#define LINEBREAK_INSIDEACHAR	3	/**< A UTF-8/16 sequence is unfinished */

/**
 * Initializes the second-level index to the line breaking properties.
 * If it is not called, the performance of #get_char_lb_class_lang (and
 * thus the main functionality) can be pretty bad, especially for big
 * code points like those of Chinese.
 */
void init_linebreak(void);

int is_breakable(utf32_t char1, utf32_t char2, const char* lang);

/**
 * Sets the line breaking information for a UTF-8 input string.
 *
 * @param[in]  s	input UTF-8 string
 * @param[in]  len	length of the input
 * @param[in]  lang	language of the input
 * @param[out] brks	pointer to the output breaking data, containing
 *					#LINEBREAK_MUSTBREAK, #LINEBREAK_ALLOWBREAK,
 *					#LINEBREAK_NOBREAK, or #LINEBREAK_INSIDEACHAR
 */
void set_linebreaks_utf8(
		const utf8_t *s, size_t len, const char* lang, char *brks);

/**
 * Sets the line breaking information for a UTF-16 input string.
 *
 * @param[in]  s	input UTF-16 string
 * @param[in]  len	length of the input
 * @param[in]  lang	language of the input
 * @param[out] brks	pointer to the output breaking data, containing
 *					#LINEBREAK_MUSTBREAK, #LINEBREAK_ALLOWBREAK,
 *					#LINEBREAK_NOBREAK, or #LINEBREAK_INSIDEACHAR
 */
void set_linebreaks_utf16(
		const utf16_t *s, size_t len, const char* lang, char *brks);

/**
 * Sets the line breaking information for a UTF-32 input string.
 *
 * @param[in]  s	input UTF-32 string
 * @param[in]  len	length of the input
 * @param[in]  lang	language of the input
 * @param[out] brks	pointer to the output breaking data, containing
 *					#LINEBREAK_MUSTBREAK, #LINEBREAK_ALLOWBREAK,
 *					#LINEBREAK_NOBREAK, or #LINEBREAK_INSIDEACHAR
 */
void set_linebreaks_utf32(
		const utf32_t *s, size_t len, const char* lang, char *brks);

/**
 * Replacement form for the deprecated function #is_breakable.  Please
 * use the macro for the present moment, before the function is renamed.
 * <p>This is a wrapper function to expose a simple interface.  Generally
 * speaking, it is better to use #set_linebreaks_utf32 instead, since
 * complicated cases involving combining marks, spaces, etc. cannot be
 * correctly processed.
 *
 * @param char1 the first Unicode character
 * @param char2 the second Unicode character
 * @param lang  language of the input
 * @return      one of #LINEBREAK_MUSTBREAK, #LINEBREAK_ALLOWBREAK,
 *				#LINEBREAK_NOBREAK, or #LINEBREAK_INSIDEACHAR
 */
#define is_line_breakable is_breakable

#ifdef __cplusplus
}
#endif

#endif /* LINEBREAK_H */
