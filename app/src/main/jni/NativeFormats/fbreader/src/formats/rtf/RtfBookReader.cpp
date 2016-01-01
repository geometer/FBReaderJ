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

#include <cctype>

#include <ZLStringUtil.h>
#include <ZLFileImage.h>
#include <ZLTextStyleEntry.h>

#include "RtfBookReader.h"
#include "../../bookmodel/BookModel.h"

RtfBookReader::RtfBookReader(BookModel &model, const std::string &encoding) : RtfReader(encoding), myBookReader(model) {
}

static const std::size_t maxBufferSize = 1024;

void RtfBookReader::addCharData(const char *data, std::size_t len, bool convert) {
	if (myCurrentState.ReadText) {
		if (convert || myConverter.isNull()) {
			myOutputBuffer.append(data, len);
			if (myOutputBuffer.size() >= maxBufferSize) {
				flushBuffer();
			}
		} else {
			flushBuffer();
			std::string newString(data, len);
			characterDataHandler(newString);
		}
	}
}

void RtfBookReader::flushBuffer() {
	if (!myOutputBuffer.empty()) {
		if (myCurrentState.ReadText) {
			if (!myConverter.isNull()) {
				static std::string newString;
					myConverter->convert(newString, myOutputBuffer.data(), myOutputBuffer.data() + myOutputBuffer.length());
				characterDataHandler(newString);
				newString.erase();
			} else {
				characterDataHandler(myOutputBuffer);
			}
		}
		myOutputBuffer.erase();
	}
}

void RtfBookReader::switchDestination(DestinationType destination, bool on) {
	switch (destination) {
		case DESTINATION_NONE:
			break;
		case DESTINATION_SKIP:
		case DESTINATION_INFO:
		case DESTINATION_TITLE:
		case DESTINATION_AUTHOR:
		case DESTINATION_STYLESHEET:
			myCurrentState.ReadText = !on;
			break;
		case DESTINATION_PICTURE:
			if (on) {
				flushBuffer();
				if (myBookReader.paragraphIsOpen()) {
					myBookReader.endParagraph();
				}
			}
			myCurrentState.ReadText = !on;
			break;
		case DESTINATION_FOOTNOTE:
			flushBuffer();
			if (on) {
				std::string id;
				ZLStringUtil::appendNumber(id, myFootnoteIndex++);

				myStateStack.push(myCurrentState);
				myCurrentState.Id = id;
				myCurrentState.ReadText = true;

				myBookReader.addHyperlinkControl(FOOTNOTE, id);
				myBookReader.addData(id);
				myBookReader.addControl(FOOTNOTE, false);

				myBookReader.setFootnoteTextModel(id);
				myBookReader.addHyperlinkLabel(id);
				myBookReader.pushKind(REGULAR);
				myBookReader.beginParagraph();
			} else {
				myBookReader.endParagraph();
				myBookReader.popKind();

				if (!myStateStack.empty()) {
					myCurrentState = myStateStack.top();
					myStateStack.pop();
				}

				if (myStateStack.empty()) {
					myBookReader.setMainTextModel();
				} else {
					myBookReader.setFootnoteTextModel(myCurrentState.Id);
				}
			}
			break;
	}
}

void RtfBookReader::insertImage(const std::string &mimeType, const std::string &fileName, std::size_t startOffset, std::size_t size) {
	std::string id;
	ZLStringUtil::appendNumber(id, myImageIndex++);
	myBookReader.addImageReference(id, 0, false);
	const ZLFile file(fileName, mimeType);
	myBookReader.addImage(id, new ZLFileImage(file, "hex", startOffset, size));
}

bool RtfBookReader::characterDataHandler(std::string &str) {
	if (myCurrentState.ReadText) {
		if (!myBookReader.paragraphIsOpen()) {
			myBookReader.beginParagraph();
		}
		myBookReader.addData(str);
	}
	return true;
}

bool RtfBookReader::readDocument(const ZLFile &file) {
	myImageIndex = 0;
	myFootnoteIndex = 1;

	myCurrentState.ReadText = true;

	myBookReader.setMainTextModel();
	myBookReader.pushKind(REGULAR);
	myBookReader.beginParagraph();

	bool code = RtfReader::readDocument(file);

	flushBuffer();
	myBookReader.endParagraph();
	while (!myStateStack.empty()) {
		myStateStack.pop();
	}

	return code;
}

void RtfBookReader::setFontProperty(FontProperty property) {
	if (!myCurrentState.ReadText) {
		//DPRINT("change style not in text.\n");
		return;
	}
	flushBuffer();

	switch (property) {
		case FONT_BOLD:
			if (myState.Bold) {
				myBookReader.pushKind(STRONG);
			} else {
				myBookReader.popKind();
			}
			myBookReader.addControl(STRONG, myState.Bold);
			break;
		case FONT_ITALIC:
			if (myState.Italic) {
				if (!myState.Bold) {
					//DPRINT("add style emphasis.\n");
					myBookReader.pushKind(EMPHASIS);
					myBookReader.addControl(EMPHASIS, true);
				} else {
					//DPRINT("add style emphasis and strong.\n");
					myBookReader.popKind();
					myBookReader.addControl(STRONG, false);

					myBookReader.pushKind(EMPHASIS);
					myBookReader.addControl(EMPHASIS, true);
					myBookReader.pushKind(STRONG);
					myBookReader.addControl(STRONG, true);
				}
			} else {
				if (!myState.Bold) {
					//DPRINT("remove style emphasis.\n");
					myBookReader.addControl(EMPHASIS, false);
					myBookReader.popKind();
				} else {
					//DPRINT("remove style strong n emphasis, add strong.\n");
					myBookReader.addControl(STRONG, false);
					myBookReader.popKind();
					myBookReader.addControl(EMPHASIS, false);
					myBookReader.popKind();

					myBookReader.pushKind(STRONG);
					myBookReader.addControl(STRONG, true);
				}
			}
			break;
		case FONT_UNDERLINED:
			break;
	}
}

void RtfBookReader::newParagraph() {
	flushBuffer();
	myBookReader.endParagraph();
	myBookReader.beginParagraph();
	if (myState.Alignment != ALIGN_UNDEFINED) {
		setAlignment();
	}
}

void RtfBookReader::setEncoding(int) {
}

void RtfBookReader::setAlignment() {
	ZLTextStyleEntry entry(ZLTextStyleEntry::STYLE_OTHER_ENTRY);
	entry.setAlignmentType(myState.Alignment);
	// TODO: replace 0 with depth?
	myBookReader.addStyleEntry(entry, 0);
	// TODO: call addStyleCloseEntry somewhere (?)
}
