/*
 * Copyright (C) 2008-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include "ZLLanguageUtil.h"

std::string ZLLanguageUtil::languageByIntCode(unsigned char languageCode, unsigned char subLanguageCode) {
	switch (languageCode) {
		default:   return "";
		case 0x01: return "ar";  // Arabic
		case 0x02: return "bg";  // Bulgarian
		case 0x03: return "ca";  // Catalan
		case 0x04: return "zh";  // Chinese
		case 0x05: return "cs";  // Czech
		case 0x06: return "da";  // Danish
		case 0x07: return "de";  // German
		case 0x08: return "el";  // Greek
		case 0x09: return "en";  // English
		case 0x0A: return "es";  // Spanish
		case 0x0B: return "fi";  // Finnish
		case 0x0C: return "fr";  // French
		case 0x0D: return "he";  // Hebrew
		case 0x0E: return "hu";  // Hungarian
		case 0x0F: return "is";  // Icelandic
		case 0x10: return "it";  // Italian
		case 0x11: return "ja";  // Japanese
		case 0x12: return "ko";  // Korean
		case 0x13: return "nl";  // Dutch
		case 0x14: return "no";  // Norwegian
		case 0x15: return "pl";  // Polish
		case 0x16: return "pt";  // Portuguese
		case 0x17: return "rm";  // Romansh
		case 0x18: return "ro";  // Romanian
		case 0x19: return "ru";  // Russian
		case 0x1A:
			switch (subLanguageCode) {
				default:   return "sr";  // Serbian
				case 0x04:
				case 0x10: return "hr";  // Croatian
				case 0x14:
				case 0x20:
				case 0x78: return "bs";  // Bosnian
                        }
		case 0x1B: return "sk";  // Slovak
		case 0x1C: return "sq";  // Albanian
		case 0x1D: return "sv";  // Swedish
		case 0x1E: return "th";  // Thai
		case 0x1F: return "tr";  // Turkish
		case 0x20: return "ur";  // Urdu
		case 0x21: return "id";  // Indonesian
		case 0x22: return "uk";  // Ukrainian
		case 0x23: return "be";  // Belarusian
		case 0x24: return "sl";  // Slovenian
		case 0x25: return "et";  // Estonian
		case 0x26: return "lv";  // Latvian
		case 0x27: return "lt";  // Lithuanian
		case 0x28: return "tg";  // Tajik
		case 0x29: return "fa";  // Persian (Farsi)
		case 0x2A: return "vi";  // Vietnamese
		case 0x2B: return "hy";  // Armenian
		case 0x2C: return "az";  // Azeri
		case 0x2D: return "eu";  // Basque
		case 0x2E: return (subLanguageCode == 0x08)
			? "dsb"          // Lower Sorbian
			: "wen";         // Upper Sorbian
		case 0x2F: return "mk";  // Makedonian
		case 0x32: return "tn";  // Setswana/Tswana
		case 0x34: return "xh";  // Xhosa/isiXhosa
		case 0x35: return "zu";  // Zulu/isiZulu
		case 0x36: return "af";  // Afrikaans
		case 0x37: return "ka";  // Georgian
		case 0x38: return "fo";  // Faeroese
		case 0x39: return "hi";  // Hindi
		case 0x3A: return "mt";  // Maltese
		case 0x3B: return "se";  // Sami
		case 0x3C: return "ga";  // Irish
		case 0x3E: return "ms";  // Malay
		case 0x3F: return "kk";  // Kazak
		case 0x40: return "ky";  // Kyrgyz
		case 0x41: return "sw";  // Swahili
		case 0x42: return "tk";  // Turkmen
		case 0x43: return "uz";  // Uzbek
		case 0x44: return "tt";  // Tatar
		case 0x45: return "bn";  // Bengali
		case 0x46: return "pa";  // Punjabi
		case 0x47: return "gu";  // Gujaratu
		case 0x48: return "or";  // Oriya
		case 0x49: return "ta";  // Tamil
		case 0x4A: return "te";  // Telugi
		case 0x4B: return "kn";  // Kannada
		case 0x4C: return "ml";  // Malayalam
		case 0x4D: return "as";  // Assamese
		case 0x4E: return "mr";  // Marathi
		case 0x4F: return "sa";  // Sanskrit
		case 0x50: return "mn";  // Mongolian
		case 0x51: return "bo";  // Tibetian
		case 0x52: return "cy";  // Welsh
		case 0x53: return "kh";  // Khmer
		case 0x54: return "lo";  // Lao
		case 0x56: return "gl";  // Galician
		case 0x57: return "kok"; // Konkani
		case 0x58: return "mni"; // Manipuri
		case 0x59: return "sd";  // Sindhi
		case 0x5A: return "syr"; // Syriac
		case 0x5B: return "si";  // Sinhala
		case 0x5D: return "iu";  // Inuktitut
		case 0x5E: return "am";  // Amharic
		case 0x5F: return "tzm"; // Tamazight
		case 0x60: return "ks";  // Kashmiri
		case 0x61: return "ne";  // Nepali
		case 0x62: return "fy";  // Frisian
		case 0x63: return "ps";  // Pashto
		case 0x64: return "fil"; // Filipino
		case 0x65: return "dv";  // Divehi
		case 0x68: return "ha";  // Hausa
		case 0x6A: return "yo";  // Yoruba
		case 0x6B: return "quz"; // Quechua
		case 0x6C: return "ns";  // Northern Sotho
		case 0x6D: return "ba";  // Bashkir
		case 0x6E: return "lb";  // Luxemburgish
		case 0x6F: return "kl";  // Greenlandic
		case 0x70: return "ig";  // Igbo
		case 0x73: return "ti";  // Tigrinya
		case 0x78: return "yi";  // Yi
		case 0x7A: return "arn"; // Mapudungun
		case 0x7C: return "moh"; // Mohawk
		case 0x7E: return "be";  // Breton
		case 0x80: return "ug";  // Uighur
		case 0x81: return "mi";  // Maori
		case 0x82: return "oc";  // Occitan
		case 0x83: return "co";  // Corsican
		case 0x84: return "gsw"; // Alsatian
		case 0x85: return "sah"; // Yakut
		case 0x86: return "qut"; // K'iche
		case 0x87: return "rw";  // Kinyarwanda
		case 0x88: return "wo";  // Wolof
		case 0x8C: return "prs"; // Dari
		case 0x8D: return "mg";  // Malagasy
	}
}

bool ZLLanguageUtil::isRTLLanguage(const std::string &languageCode) {
	return
		(languageCode == "ar") ||
		(languageCode == "he");
}
