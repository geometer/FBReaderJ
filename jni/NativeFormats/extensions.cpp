/*
 * Copyright (C) 2011 Geometer Plus <contact@geometerplus.com>
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

#include <AndroidLog.h>

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLXMLReader.h>


void extension1() {
	AndroidLog log;

	log.w("FBREADER", "extension 1 start");

	ZLFile file("/");
	//ZLFile file("/mnt/sdcard/Books/a.txt");

	log.wf("FBREADER", "file: %s", file.path().c_str());
	log.wf("FBREADER", "exists: \"%s\"", file.exists() ? "true" : "false");
	log.wf("FBREADER", "size: %d", file.size());

	shared_ptr<ZLInputStream> input = file.inputStream();
	if (input.isNull() || !input->open()) {
		log.w("FBREADER", "unable to open file");
	} else {
		log.wf("FBREADER", "size of opened: %lu", input->sizeOfOpened());
		log.w("FBREADER", "contents:");
		std::string line;

		const size_t size = 256;
		char *buffer = new char[size];
		size_t length;

		do {
			length = input->read(buffer, size);
			if (length > 0) {
				line.append(buffer, length);
			}

			size_t index = 0;
			while (true) {
				const size_t next = line.find('\n', index);
				if (next == std::string::npos) {
					break;
				}
				log.w("FBREADER", line.substr(index, next - index));
				index = next + 1;
			}
			line.erase(0, index);
		} while (length == size);

		if (line.length() > 0) {
			log.w("FBREADER", line);
			log.w("FBREADER", "/*no end of line*/");
		}

		log.w("FBREADER", "contents: EOF");

		delete[] buffer;
		input->close();
	}

	log.w("FBREADER", "extension 1 end");
}


class EnRuXMLReader : public ZLXMLReader {
public:
	void startElementHandler(const char *tag, const char **attributes);

	std::string EnText;
	std::string RuText;
};

static const std::string ROOT = "root";

void EnRuXMLReader::startElementHandler(const char *tag, const char **attributes) {
	if (ROOT == tag) {
		const char *en = attributeValue(attributes, "en");
		const char *ru = attributeValue(attributes, "ru");
		if (en != 0) {
			EnText = en;
		}
		if (ru != 0) {
			RuText = ru;
		}
	}
}


void extension2() {
	/*ZLFile utf8File("/mnt/sdcard/Books/a-utf8.xml");
	ZLFile win1251File("/mnt/sdcard/Books/a-win1251.xml");

	EnRuXMLReader utf8Reader;
	EnRuXMLReader win1251Reader;

	bool utf8res = utf8Reader.readDocument(utf8File);
	bool win1251res = win1251Reader.readDocument(win1251File);

	AndroidLog log;

	log.wf("FBREADER", "utf8res: %s", utf8res ? "true" : "false");
	log.wf("FBREADER", "win1251res: %s", win1251res ? "true" : "false");
	log.wf("FBREADER", "utf8   :EN = %s", utf8Reader.EnText.c_str());
	log.wf("FBREADER", "win1251:EN = %s", win1251Reader.EnText.c_str());
	log.wf("FBREADER", "utf8   :RU = %s", utf8Reader.RuText.c_str());
	log.wf("FBREADER", "win1251:RU = %s", win1251Reader.RuText.c_str());
	log.wf("FBREADER", "EN == EN: %s", (utf8Reader.EnText == win1251Reader.EnText) ? "true" : "false");
	log.wf("FBREADER", "RU == RU: %s", (utf8Reader.RuText == win1251Reader.RuText) ? "true" : "false");*/
}
