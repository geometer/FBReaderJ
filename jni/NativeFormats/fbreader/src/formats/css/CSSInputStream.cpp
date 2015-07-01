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

#include <cstring>

#include <ZLLogger.h>

#include "CSSInputStream.h"

CSSInputStream::Buffer::Buffer(std::size_t capacity) : Capacity(capacity - 1) {
	Content = new char[capacity];
	Length = 0;
	Offset = 0;
}

CSSInputStream::Buffer::~Buffer() {
	delete[] Content;
}

CSSInputStream::CSSInputStream(shared_ptr<ZLInputStream> base) : myBaseStream(base), myBuffer(8192), myBufferNoComments(8192) {
	//ZLLogger::Instance().registerClass("CSSInputStream");
}

CSSInputStream::~CSSInputStream() {
	close();
}

bool CSSInputStream::open() {
	myState = PLAIN_TEXT;
	return myBaseStream->open();
}

std::size_t CSSInputStream::read(char *buffer, std::size_t maxSize) {
	std::size_t ready = 0;
	while (ready < maxSize) {
		fillBufferNoComments();
		if (myBufferNoComments.isEmpty()) {
			break;
		}
		std::size_t len = std::min(
			maxSize - ready,
			myBufferNoComments.Length - myBufferNoComments.Offset
		);
		if (buffer != 0) {
			std::memcpy(buffer + ready, myBufferNoComments.Content + myBufferNoComments.Offset, len);
		}
		myBufferNoComments.Offset += len;
		ready += len;
	}
	//ZLLogger::Instance().println("CSSInputStream", std::string(buffer, ready));
	return ready;
}

void CSSInputStream::close() {
	return myBaseStream->close();
}

void CSSInputStream::seek(int offset, bool absoluteOffset) {
	// TODO: implement
}

std::size_t CSSInputStream::offset() const {
	// TODO: implement
	return 0;
}

std::size_t CSSInputStream::sizeOfOpened() {
	// TODO: not a correct computation
	return myBaseStream->sizeOfOpened();
}

void CSSInputStream::fillBufferNoComments() {
	if (!myBufferNoComments.isEmpty()) {
		return;
	}
	myBufferNoComments.Length = 0;
	myBufferNoComments.Offset = 0;
	while (!myBufferNoComments.isFull()) {
		if (myBuffer.isEmpty()) {
			myBuffer.Offset = 0;
			myBuffer.Length = myBaseStream->read(myBuffer.Content, myBuffer.Capacity);
		}
		if (myBuffer.isEmpty()) {
			break;
		}
		while (!myBuffer.isEmpty() && !myBufferNoComments.isFull()) {
			const char ch = myBuffer.Content[myBuffer.Offset++];
			switch (myState) {
				case PLAIN_TEXT:
					switch (ch) {
						case '\'':
							myBufferNoComments.Content[myBufferNoComments.Length++] = ch;
							myState = S_QUOTED_TEXT;
							break;
						case '"':
							myBufferNoComments.Content[myBufferNoComments.Length++] = ch;
							myState = D_QUOTED_TEXT;
							break;
						case '/':
							myState = COMMENT_START_SLASH;
							break;
						default:
							myBufferNoComments.Content[myBufferNoComments.Length++] = ch;
							break;
					}
					break;
				case S_QUOTED_TEXT:
					if (ch == '\'') {
						myState = PLAIN_TEXT;
					}
					myBufferNoComments.Content[myBufferNoComments.Length++] = ch;
					break;
				case D_QUOTED_TEXT:
					if (ch == '"') {
						myState = PLAIN_TEXT;
					}
					myBufferNoComments.Content[myBufferNoComments.Length++] = ch;
					break;
				case COMMENT_START_SLASH:
					switch (ch) {
						case '/':
							myBufferNoComments.Content[myBufferNoComments.Length++] = '/';
							break;
						case '*':
							myState = COMMENT;
							break;
						default:
							myState = PLAIN_TEXT;
							myBufferNoComments.Content[myBufferNoComments.Length++] = '/';
							myBufferNoComments.Content[myBufferNoComments.Length++] = ch;
							break;
					}
					break;
				case COMMENT:
					if (ch == '*') {
						myState = COMMENT_END_ASTERISK;
					}
					break;
				case COMMENT_END_ASTERISK:
					switch (ch) {
						case '/':
							myState = PLAIN_TEXT;
							break;
						case '*':
							break;
						default:
							myState = COMMENT;
							break;
					}
					break;
			}
		}
	}
}
