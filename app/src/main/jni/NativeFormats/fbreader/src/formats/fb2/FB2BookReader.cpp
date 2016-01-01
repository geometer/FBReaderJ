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

#include <cstdlib>
#include <cstring>

#include <ZLInputStream.h>
#include <ZLStringUtil.h>
#include <ZLFileImage.h>

#include <ZLTextParagraph.h>

#include "FB2BookReader.h"
#include "../../library/Book.h"
#include "../../bookmodel/BookModel.h"

FB2BookReader::FB2BookReader(BookModel &model) : myModelReader(model) {
	myInsideCoverpage = false;
	myParagraphsBeforeBodyNumber = (std::size_t)-1;
	myInsidePoem = false;
	mySectionDepth = 0;
	myBodyCounter = 0;
	myReadMainText = false;
	myFootnoteTagDepth = 0;
	myCurrentImageStart = -1;
	mySectionStarted = false;
	myInsideTitle = false;
	myListDepth = 0;
}

void FB2BookReader::characterDataHandler(const char *text, std::size_t len) {
	if (len > 0 && (!myCurrentImageId.empty() || myModelReader.paragraphIsOpen())) {
		std::string str(text, len);
		if (!myCurrentImageId.empty()) {
			if (myCurrentImageStart == -1) {
				myCurrentImageStart = getCurrentPosition();
			}
		} else {
			myModelReader.addData(str);
			if (myInsideTitle) {
				myModelReader.addContentsData(str);
			}
		}
	}
}

bool FB2BookReader::processNamespaces() const {
	return true;
}

void FB2BookReader::startElementHandler(int tag, const char **xmlattributes) {
	if (!myReadMainText && myFootnoteTagDepth > 0) {
		++myFootnoteTagDepth;
	}

	const char *id = attributeValue(xmlattributes, "id");
	if (id != 0 && tag != _BINARY) {
		if (!myReadMainText && myFootnoteTagDepth == 0) {
			myModelReader.setFootnoteTextModel(id);
			myFootnoteTagDepth = 1;
		}
		myModelReader.addHyperlinkLabel(id);
	}
	switch (tag) {
		case _P:
			if (mySectionStarted) {
				mySectionStarted = false;
			} else if (myInsideTitle) {
				static const std::string SPACE = " ";
				myModelReader.addContentsData(SPACE);
			}
			myModelReader.beginParagraph();
			break;
		case _UL:
		case _OL:
			++myListDepth;
			break;
		case _LI:
		{
			if (mySectionStarted) {
				mySectionStarted = false;
			}
			myModelReader.beginParagraph();
			static const std::string BULLET = "\xE2\x80\xA2";
			if (myListDepth > 1) {
				myModelReader.addFixedHSpace(3 * (myListDepth - 1));
			}
			myModelReader.addData(BULLET);
			myModelReader.addFixedHSpace(1);
			break;
		}
		case _V:
			myModelReader.pushKind(VERSE);
			myModelReader.beginParagraph();
			break;
		case _SUBTITLE:
			myModelReader.pushKind(SUBTITLE);
			myModelReader.beginParagraph();
			break;
		case _TEXT_AUTHOR:
			myModelReader.pushKind(AUTHOR);
			myModelReader.beginParagraph();
			break;
		case _DATE:
			myModelReader.pushKind(DATEKIND);
			myModelReader.beginParagraph();
			break;
		case _CITE:
			myModelReader.pushKind(CITE);
			break;
		case _SECTION:
			if (myReadMainText) {
				myModelReader.insertEndOfSectionParagraph();
				++mySectionDepth;
				myModelReader.beginContentsParagraph();
				mySectionStarted = true;
			}
			break;
		case _TITLE:
			if (myInsidePoem) {
				myModelReader.pushKind(POEM_TITLE);
			} else if (mySectionDepth == 0) {
				myModelReader.insertEndOfSectionParagraph();
				myModelReader.pushKind(TITLE);
			} else {
				myModelReader.pushKind(SECTION_TITLE);
				myModelReader.enterTitle();
				myInsideTitle = true;
			}
			break;
		case _POEM:
			myInsidePoem = true;
			break;
		case _STANZA:
			myModelReader.pushKind(STANZA);
			myModelReader.beginParagraph(ZLTextParagraph::BEFORE_SKIP_PARAGRAPH);
			myModelReader.endParagraph();
			break;
		case _EPIGRAPH:
			myModelReader.pushKind(EPIGRAPH);
			break;
		case _ANNOTATION:
			if (myBodyCounter == 0) {
				myModelReader.setMainTextModel();
			}
			myModelReader.pushKind(ANNOTATION);
			break;
		case _COVERPAGE:
			if (myBodyCounter == 0) {
				myInsideCoverpage = true;
				myModelReader.setMainTextModel();
			}
			break;
		case _SUB:
			myModelReader.addControl(SUB, true);
			break;
		case _SUP:
			myModelReader.addControl(SUP, true);
			break;
		case _CODE:
			myModelReader.addControl(CODE, true);
			break;
		case _STRIKETHROUGH:
			myModelReader.addControl(STRIKETHROUGH, true);
			break;
		case _STRONG:
			myModelReader.addControl(STRONG, true);
			break;
		case _EMPHASIS:
			myModelReader.addControl(EMPHASIS, true);
			break;
		case _A:
		{
			const char *ref = attributeValue(xmlattributes, myHrefPredicate);
			if (ref == 0) {
				ref = attributeValue(xmlattributes, myBrokenHrefPredicate);
			}
			if (ref != 0) {
				if (ref[0] == '#') {
					const char *type = attributeValue(xmlattributes, "type");
					static const std::string NOTE = "note";
					if (type != 0 && NOTE == type) {
						myHyperlinkType = FOOTNOTE;
					} else {
						myHyperlinkType = INTERNAL_HYPERLINK;
					}
					++ref;
				} else {
					myHyperlinkType = EXTERNAL_HYPERLINK;
				}
				myModelReader.addHyperlinkControl(myHyperlinkType, ref);
			} else {
				myHyperlinkType = FOOTNOTE;
				myModelReader.addControl(myHyperlinkType, true);
			}
			break;
		}
		case _IMAGE:
		{
			const char *ref = attributeValue(xmlattributes, myHrefPredicate);
			if (ref == 0) {
				ref = attributeValue(xmlattributes, myBrokenHrefPredicate);
			}
			const char *vOffset = attributeValue(xmlattributes, "voffset");
			char offset = vOffset != 0 ? std::atoi(vOffset) : 0;
			if (ref != 0 && *ref == '#') {
				++ref;
				const bool isCoverImage =
					myParagraphsBeforeBodyNumber ==
					myModelReader.model().bookTextModel()->paragraphsNumber();
				if (myCoverImageReference != ref || !isCoverImage) {
					myModelReader.addImageReference(ref, offset, myInsideCoverpage || isCoverImage);
				}
				if (myInsideCoverpage) {
					myCoverImageReference = ref;
				}
			}
			break;
		}
		case _BINARY:
		{
			static const std::string STRANGE_MIME_TYPE = "text/xml";
			const char *contentType = attributeValue(xmlattributes, "content-type");
			if (contentType != 0 && id != 0 && STRANGE_MIME_TYPE != contentType) {
				myCurrentImageId.assign(id);
			}
			break;
		}
		case _EMPTY_LINE:
			myModelReader.beginParagraph(ZLTextParagraph::EMPTY_LINE_PARAGRAPH);
			myModelReader.endParagraph();
			break;
		case _BODY:
			++myBodyCounter;
			myParagraphsBeforeBodyNumber = myModelReader.model().bookTextModel()->paragraphsNumber();
			if ((myBodyCounter == 1) || (attributeValue(xmlattributes, "name") == 0)) {
				myModelReader.setMainTextModel();
				myReadMainText = true;
			}
			myModelReader.pushKind(REGULAR);
			break;
		default:
			break;
	}
}

void FB2BookReader::endElementHandler(int tag) {
	if (!myReadMainText && myFootnoteTagDepth > 0) {
		--myFootnoteTagDepth;
	}

	switch (tag) {
		case _P:
		case _LI:
			myModelReader.endParagraph();
			break;
		case _UL:
		case _OL:
			--myListDepth;
			break;
		case _V:
		case _SUBTITLE:
		case _TEXT_AUTHOR:
		case _DATE:
			myModelReader.popKind();
			myModelReader.endParagraph();
			break;
		case _CITE:
			myModelReader.popKind();
			break;
		case _SECTION:
			if (myReadMainText) {
				myModelReader.endContentsParagraph();
				--mySectionDepth;
				mySectionStarted = false;
			} else {
				myModelReader.unsetTextModel();
			}
			break;
		case _TITLE:
			myModelReader.exitTitle();
			myModelReader.popKind();
			myInsideTitle = false;
			break;
		case _POEM:
			myInsidePoem = false;
			break;
		case _STANZA:
			myModelReader.beginParagraph(ZLTextParagraph::AFTER_SKIP_PARAGRAPH);
			myModelReader.endParagraph();
			myModelReader.popKind();
			break;
		case _EPIGRAPH:
			myModelReader.popKind();
			break;
		case _ANNOTATION:
			myModelReader.popKind();
			if (myBodyCounter == 0) {
				myModelReader.insertEndOfSectionParagraph();
				myModelReader.unsetTextModel();
			}
			break;
		case _COVERPAGE:
			if (myBodyCounter == 0) {
				myInsideCoverpage = false;
				myModelReader.insertEndOfSectionParagraph();
				myModelReader.unsetTextModel();
			}
			break;
		case _SUB:
			myModelReader.addControl(SUB, false);
			break;
		case _SUP:
			myModelReader.addControl(SUP, false);
			break;
		case _CODE:
			myModelReader.addControl(CODE, false);
			break;
		case _STRIKETHROUGH:
			myModelReader.addControl(STRIKETHROUGH, false);
			break;
		case _STRONG:
			myModelReader.addControl(STRONG, false);
			break;
		case _EMPHASIS:
			myModelReader.addControl(EMPHASIS, false);
			break;
		case _A:
			myModelReader.addControl(myHyperlinkType, false);
			break;
		case _BINARY:
			if (!myCurrentImageId.empty() && myCurrentImageStart != -1) {
				myModelReader.addImage(myCurrentImageId, new ZLFileImage(
					myModelReader.model().book()->file(),
					"base64",
					myCurrentImageStart,
					getCurrentPosition() - myCurrentImageStart
				));
			}
			myCurrentImageId.clear();
			myCurrentImageStart = -1;
			break;
		case _BODY:
			myModelReader.popKind();
			myModelReader.unsetTextModel();
			myReadMainText = false;
			break;
		default:
			break;
	}
}

bool FB2BookReader::readBook() {
	return readDocument(myModelReader.model().book()->file());
}
