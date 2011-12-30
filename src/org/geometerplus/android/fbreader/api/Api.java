/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

import java.util.List;

public interface Api {
	// information about fbreader
	String getFBReaderVersion() throws ApiException;

	// preferences information
	List<String> getOptionGroups() throws ApiException;
	List<String> getOptionNames(String group) throws ApiException;
	String getOptionValue(String group, String name) throws ApiException;
	void setOptionValue(String group, String name, String value) throws ApiException;

	// book information
	String getBookLanguage() throws ApiException;
	String getBookTitle() throws ApiException;
	//List<String> getBookAuthors() throws ApiException;
	List<String> getBookTags() throws ApiException;
	String getBookFileName() throws ApiException;
	String getBookHash() throws ApiException;

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
