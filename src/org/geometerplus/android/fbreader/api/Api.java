/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.api;

import java.util.List;

public interface Api {
	void connect();
	void disconnect();

	// fbreader information
	String getFBReaderVersion() throws ApiException;

	// book information
	String getBookLanguage() throws ApiException;
	String getBookTitle() throws ApiException;
	//List<String> getBookAuthors() throws ApiException;
	List<String> getBookTags() throws ApiException;
	String getBookFileName() throws ApiException;

	// text information
	int getParagraphsNumber() throws ApiException;
	int getElementsNumber(int paragraphIndex) throws ApiException;
	String getParagraphText(int paragraphIndex) throws ApiException;

	// page information
	TextPosition getPageStart() throws ApiException;
	TextPosition getPageEnd() throws ApiException;
	boolean isPageEndOfSection() throws ApiException;
	boolean isPageEndOfText() throws ApiException;

	// manage view
	void setPageStart(TextPosition position) throws ApiException;
	void highlightArea(TextPosition start, TextPosition end) throws ApiException;
	void clearHighlighting() throws ApiException;
}
