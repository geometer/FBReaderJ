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

#include <ZLFile.h>
#include <ZLInputStream.h>
#include <ZLEncodingConverter.h>
#include <ZLStringUtil.h>
#include <ZLLanguageUtil.h>
#include <ZLImage.h>
#include <ZLFileImage.h>

#include "PdbPlugin.h"
#include "PalmDocStream.h"
#include "MobipocketHtmlBookReader.h"

#include "../css/StyleSheetParser.h"
#include "../txt/PlainTextFormat.h"
#include "../../library/Book.h"
#include "../../bookmodel/BookModel.h"

const std::string MobipocketPlugin::supportedFileType() const {
	return "mobi";
}
//bool MobipocketPlugin::acceptsFile(const ZLFile &file) const {
//	return PdbPlugin::fileType(file) == "BOOKMOBI";
//}

void MobipocketPlugin::readDocumentInternal(const ZLFile &file, BookModel &model, const PlainTextFormat &format, const std::string &encoding, ZLInputStream &stream) const {
	MobipocketHtmlBookReader reader(file, model, format, encoding);

	shared_ptr<StyleSheetParser> cssParser = reader.createCSSParser();
	cssParser->parseStream(new PalmDocCssStream(file));

	reader.readDocument(stream);
}

bool MobipocketPlugin::readModel(BookModel &model) const {
	const Book &book = *model.book();
	const ZLFile &file = book.file();

	shared_ptr<ZLInputStream> stream = createStream(file);

	PlainTextFormat format(file);
	readDocumentInternal(file, model, format, book.encoding(), *stream);
	return true;
}

bool MobipocketPlugin::readMetainfo(Book &book) const {
	shared_ptr<ZLInputStream> stream = book.file().inputStream();
	if (stream.isNull() || ! stream->open()) {
		return false;
	}
	PdbHeader header;
	if (!header.read(stream)) {
		return false;
	}
	stream->seek(header.Offsets[0] + 16, true);
	char test[5];
	test[4] = '\0';
	stream->read(test, 4);
	static const std::string MOBI = "MOBI";
	if (MOBI != test) {
		return PalmDocLikePlugin::readMetainfo(book);
	}

	const unsigned long length = PdbUtil::readUnsignedLongBE(*stream);

	stream->seek(4, false);

	const unsigned long encodingCode = PdbUtil::readUnsignedLongBE(*stream);
	if (book.encoding().empty()) {
		shared_ptr<ZLEncodingConverter> converter =
			ZLEncodingCollection::Instance().converter(encodingCode);
		if (!converter.isNull()) {
			book.setEncoding(converter->name());
		}
	}

	stream->seek(52, false);

	const unsigned long fullNameOffset = PdbUtil::readUnsignedLongBE(*stream);
	const unsigned long fullNameLength = PdbUtil::readUnsignedLongBE(*stream);

	const unsigned long languageCode = PdbUtil::readUnsignedLongBE(*stream);
	const std::string lang =
		ZLLanguageUtil::languageByIntCode(languageCode & 0xFF, (languageCode >> 8) & 0xFF);
	if (lang != "") {
		book.setLanguage(lang);
	}

	stream->seek(32, false);

	const unsigned long exthFlags = PdbUtil::readUnsignedLongBE(*stream);
	if (exthFlags & 0x40) {
		stream->seek(header.Offsets[0] + 16 + length, true);

		stream->read(test, 4);
		static const std::string EXTH = "EXTH";
		if (EXTH == test) {
			stream->seek(4, false);
			const unsigned long recordsNum = PdbUtil::readUnsignedLongBE(*stream);
			for (unsigned long i = 0; i < recordsNum; ++i) {
				const unsigned long type = PdbUtil::readUnsignedLongBE(*stream);
				const unsigned long size = PdbUtil::readUnsignedLongBE(*stream);
				if (size > 8) {
					std::string value(size - 8, '\0');
					stream->read((char*)value.data(), size - 8);
					switch (type) {
						case 100: // author
						{
							int index = value.find(',');
							if (index != -1) {
								std::string part0 = value.substr(0, index);
								std::string part1 = value.substr(index + 1);
								ZLStringUtil::stripWhiteSpaces(part0);
								ZLStringUtil::stripWhiteSpaces(part1);
								value = part1 + ' ' + part0;
							} else {
								ZLStringUtil::stripWhiteSpaces(value);
							}
							book.addAuthor(value);
							break;
						}
						case 105: // subject
							book.addTag(value);
							break;
					}
				}
			}
		}
	}

	stream->seek(header.Offsets[0] + fullNameOffset, true);
	std::string title(fullNameLength, '\0');
	stream->read((char*)title.data(), fullNameLength);
	book.setTitle(title);

	stream->close();
	return PalmDocLikePlugin::readMetainfo(book);
}

shared_ptr<const ZLImage> MobipocketPlugin::coverImage(const ZLFile &file) const {
	shared_ptr<ZLInputStream> stream = file.inputStream();
	if (stream.isNull() || ! stream->open()) {
		return 0;
	}
	PdbHeader header;
	if (!header.read(stream)) {
		return 0;
	}
	stream->seek(header.Offsets[0] + 16, true);
	char test[5];
	test[4] = '\0';
	stream->read(test, 4);
	static const std::string MOBI = "MOBI";
	if (MOBI != test) {
		return 0;
	}

	const unsigned long length = PdbUtil::readUnsignedLongBE(*stream);

	stream->seek(104, false);

	const unsigned long exthFlags = PdbUtil::readUnsignedLongBE(*stream);
	unsigned long coverIndex = (unsigned long)-1;
	unsigned long thumbIndex = (unsigned long)-1;
	if (exthFlags & 0x40) {
		stream->seek(header.Offsets[0] + 16 + length, true);

		stream->read(test, 4);
		static const std::string EXTH = "EXTH";
		if (EXTH != test) {
			return 0;
		}
		stream->seek(4, false);
		const unsigned long recordsNum = 	PdbUtil::readUnsignedLongBE(*stream);
		for (unsigned long i = 0; i < recordsNum; ++i) {
			const unsigned long type = PdbUtil::readUnsignedLongBE(*stream);
			const unsigned long size = PdbUtil::readUnsignedLongBE(*stream);
			switch (type) {
				case 201: // coveroffset
					if (size == 12) {
						coverIndex = PdbUtil::readUnsignedLongBE(*stream);
					} else {
						stream->seek(size - 8, false);
					}
					break;
				case 202: // thumboffset
					if (size == 12) {
						thumbIndex = PdbUtil::readUnsignedLongBE(*stream);
					} else {
						stream->seek(size - 8, false);
					}
					break;
				default:
					stream->seek(size - 8, false);
					break;
			}
		}
	}
	stream->close();

	if (coverIndex == (unsigned long)-1) {
		if (thumbIndex == (unsigned long)-1) {
			return 0;
		}
		coverIndex = thumbIndex;
	}

	PalmDocContentStream pbStream(file);
	if (!pbStream.open()) {
		return 0;
	}
	std::pair<int,int> imageLocation = pbStream.imageLocation(pbStream.header(), coverIndex);
	if (imageLocation.first > 0 && imageLocation.second > 0) {
		return new ZLFileImage(
			file,
			"",
			imageLocation.first,
			imageLocation.second
		);
	}
	return 0;
}

bool MobipocketPlugin::readLanguageAndEncoding(Book &book) const {
	shared_ptr<ZLInputStream> stream = book.file().inputStream();
	if (stream.isNull() || ! stream->open()) {
		return false;
	}
	PdbHeader header;
	if (!header.read(stream)) {
		return false;
	}
	stream->seek(header.Offsets[0] + 16, true);
	if (PdbUtil::readUnsignedLongBE(*stream) != 0x4D4F4249) /* "MOBI" */ {
		return false;
	}
	stream->seek(8, false);
	const unsigned long encodingCode = PdbUtil::readUnsignedLongBE(*stream);
	shared_ptr<ZLEncodingConverter> converter =
		ZLEncodingCollection::Instance().converter(encodingCode);
	book.setEncoding(converter.isNull() ? "utf-8" : converter->name());
	stream->seek(60, false);
	const unsigned long languageCode = PdbUtil::readUnsignedLongBE(*stream);
	const std::string lang =
		ZLLanguageUtil::languageByIntCode(languageCode & 0xFF, (languageCode >> 8) & 0xFF);
	if (lang != "") {
		book.setLanguage(lang);
	}
	return true;
}

std::string MobipocketPlugin::readAnnotation(const ZLFile &file) const {
	shared_ptr<ZLInputStream> stream = file.inputStream();
	if (stream.isNull() || ! stream->open()) {
		return "";
	}
	PdbHeader header;
	if (!header.read(stream)) {
		return "";
	}
	stream->seek(header.Offsets[0] + 16, true);
	char test[5];
	test[4] = '\0';
	stream->read(test, 4);
	static const std::string MOBI = "MOBI";
	if (MOBI != test) {
		return "";
	}

	std::string annotation;
	const unsigned long length = PdbUtil::readUnsignedLongBE(*stream);

	stream->seek(104, false);

	const unsigned long exthFlags = PdbUtil::readUnsignedLongBE(*stream);
	if (exthFlags & 0x40) {
		stream->seek(header.Offsets[0] + 16 + length, true);

		stream->read(test, 4);
		static const std::string EXTH = "EXTH";
		if (EXTH != test) {
			return 0;
		}
		stream->seek(4, false);
		const unsigned long recordsNum = 	PdbUtil::readUnsignedLongBE(*stream);
		for (unsigned long i = 0; i < recordsNum; ++i) {
			const unsigned long type = PdbUtil::readUnsignedLongBE(*stream);
			const unsigned long size = PdbUtil::readUnsignedLongBE(*stream);
			switch (type) {
				case 103: // description
					if (size > 8) {
						std::string value(size - 8, '\0');
						stream->read((char*)value.data(), size - 8);
						annotation = value;
					} else {
						stream->seek(size - 8, false);
					}
					break;
				default:
					stream->seek(size - 8, false);
					break;
			}
		}
	}
	stream->close();
	return annotation;
}

bool MobipocketPlugin::readUids(Book &/*book*/) const {
	return true;
}

std::vector<shared_ptr<FileEncryptionInfo> > MobipocketPlugin::readEncryptionInfos(Book &book) const {
	std::vector<shared_ptr<FileEncryptionInfo> > infos;

	PalmDocContentStream pbStream(book.file());
	if (pbStream.open()) {
		pbStream.close();
	} else if (pbStream.errorCode() == PalmDocContentStream::ERROR_ENCRYPTION) {
		infos.push_back(new FileEncryptionInfo("", EncryptionMethod::KINDLE, "", ""));
	} else {
		infos.push_back(new FileEncryptionInfo("", EncryptionMethod::UNSUPPORTED, "", ""));
	}
	return infos;
}
