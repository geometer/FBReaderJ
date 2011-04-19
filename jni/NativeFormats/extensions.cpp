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

	if (true) {
		ZLFile nonexistent("/mnt/sdcard/Books/b.txt");
		log.w("FBREADER", "nonexistent created");
		bool flag = nonexistent.exists();
		log.wf("FBREADER", "Does nonexistent file exist?: \"%s\"", flag ? "true" : "false");
	}

	//ZLFile file("/mnt/sdcard/Books/a.txt");
	ZLFile file("data/b.xml");

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



class Ext2XMLReader : public ZLXMLReader {
public:
	Ext2XMLReader();
	void startElementHandler(const char *tag, const char **attributes);
	void endElementHandler(const char *tag);
	void characterDataHandler(const char *text, size_t len);

private:
	void printLine();

private:
	std::string myBuffer;

	enum {
		READ_NOTHING,
		READ_ROOT,
		READ_LINE,
	} myState;
};

Ext2XMLReader::Ext2XMLReader() {
	myState = READ_NOTHING;
}

static const std::string ROOT = "root";
static const std::string LINE = "line";

void Ext2XMLReader::startElementHandler(const char *tag, const char **attributes) {
	if (ROOT == tag && myState == READ_NOTHING) {
		myState = READ_ROOT;
	} else if (LINE == tag && myState == READ_ROOT) {
		myBuffer.erase();
		const char *name = attributeValue(attributes, "name");
		myBuffer.append("<line");
		if (name != 0) {
			myBuffer.append(" name=\"");
			myBuffer.append(name);
			myBuffer.append("\"");
		}
		myBuffer.append(">");
		myState = READ_LINE;
	}
}

void Ext2XMLReader::endElementHandler(const char *tag) {
	if (ROOT == tag && myState == READ_ROOT) {
		myState = READ_NOTHING;
	} else if (LINE == tag && myState == READ_LINE) {
		myBuffer.append("</line>");
		myState = READ_ROOT;
		printLine();
	}
}

void Ext2XMLReader::characterDataHandler(const char *text, size_t len) {
	if (myState == READ_LINE) {
		myBuffer.append(text, len);
	}
}

void Ext2XMLReader::printLine() {
	AndroidLog log;
	log.w("FBREADER", myBuffer);
	myBuffer.erase();
}


void extension2() {
	Ext2XMLReader reader;
	//ZLFile file("/mnt/sdcard/Books/a.xml");
	ZLFile file("data/b.xml");
	reader.readDocument(file);
}
