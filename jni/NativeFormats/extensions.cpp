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

#include <jni.h>

// This code is temporary: it makes eclipse not complain
#ifndef _JNI_H
#define JNIIMPORT
#define JNIEXPORT
#define JNICALL
#endif /* _JNI_H */


#include <AndroidLog.h>

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLXMLReader.h>


#include <ZLTextModel.h>

#include "fbreader/src/formats/FormatPlugin.h"
#include "fbreader/src/library/Book.h"
#include "fbreader/src/bookmodel/BookModel.h"


void extension1();
void extension2();

extern "C"
JNIEXPORT void JNICALL Java_org_geometerplus_fbreader_formats_PluginCollection_runTests(JNIEnv* env, jobject thiz) {
	//extension1();
	//extension2();
}


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


class TestXMLReader : public ZLXMLReader {
public:
	void startElementHandler(const char *tag, const char **attributes);
};

void TestXMLReader::startElementHandler(const char *tag, const char **attributes) {
}


void extension2() {
	AndroidLog log;
	log.w("FBREADER", "extension 2 start");

	const char fileName[] = "/mnt/sdcard/Books/David Drake - An_Oblique_Approach.fb2";
	//const char fileName[] = "/mnt/sdcard/Books/data.fbreader.org/catalogs/prochtenie/light/P1.epub";

	ZLFile file(fileName);
	log.wf("FBREADER", "extension 2: file exists: %s", (file.exists() ? "true" : "false"));

	if (file.exists()) {
		shared_ptr<Book> book = Book::loadFromFile(file);
		log.wf("FBREADER", "extension 2: book loaded: %s", (!book.isNull() ? "true" : "false"));

		if (!book.isNull()) {
			shared_ptr<BookModel> model = new BookModel(book);
			shared_ptr<ZLTextModel> textModel = model->bookTextModel();
			textModel->flush();

			log.wf("FBREADER", "extension 2: model paragraphs: %d", textModel->paragraphsNumber());

			textModel.reset();
			model.reset();
		}
	}

	log.w("FBREADER", "extension 2 end");
}
