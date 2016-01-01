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

#include <ZLLogger.h>

#include "OleMainStream.h"
#include "OleUtil.h"
#include "OleStreamReader.h"

OleStreamReader::OleStreamReader() : myNextPieceNumber(0) {
}

bool OleStreamReader::readDocument(shared_ptr<ZLInputStream> inputStream, bool doReadFormattingData) {
	static const std::string WORD_DOCUMENT = "WordDocument";

	shared_ptr<OleStorage> storage = new OleStorage;

	if (!storage->init(inputStream, inputStream->sizeOfOpened())) {
		ZLLogger::Instance().println("DocPlugin", "Broken OLE file");
		return false;
	}

	OleEntry wordDocumentEntry;
	if (!storage->getEntryByName(WORD_DOCUMENT, wordDocumentEntry)) {
		return false;
	}

	OleMainStream oleStream(storage, wordDocumentEntry, inputStream);
	if (!oleStream.open(doReadFormattingData)) {
		ZLLogger::Instance().println("DocPlugin", "Cannot open OleMainStream");
		return false;
	}
	return readStream(oleStream);
}

bool OleStreamReader::readNextPiece(OleMainStream &stream) {
	const OleMainStream::Pieces &pieces = stream.getPieces();
	if (myNextPieceNumber >= pieces.size()) {
		return false;
	}
	const OleMainStream::Piece &piece = pieces.at(myNextPieceNumber);

	if (piece.Type == OleMainStream::Piece::PIECE_FOOTNOTE) {
		footnotesStartHandler();
	} else if (piece.Type == OleMainStream::Piece::PIECE_OTHER) {
		return false;
	}

	if (!stream.seek(piece.Offset, true)) {
		//TODO maybe in that case we should take next piece?
		return false;
	}
	char *textBuffer = new char[piece.Length];
	std::size_t readBytes = stream.read(textBuffer, piece.Length);
	if (readBytes != (std::size_t)piece.Length) {
		ZLLogger::Instance().println("DocPlugin", "not all bytes have been read from piece");
	}

	if (!piece.IsANSI) {
		for (std::size_t i = 0; i < readBytes; i += 2) {
			ucs2SymbolHandler(OleUtil::getU2Bytes(textBuffer, i));
		}
	} else {
		ansiDataHandler(textBuffer, readBytes);
	}
	++myNextPieceNumber;
	delete[] textBuffer;

	return true;
}
