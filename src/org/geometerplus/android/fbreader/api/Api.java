/*
 * This code is in the public domain.
 */

package org.geometerplus.android.fbreader.api;

import java.util.List;
import java.util.Date;

import android.graphics.Bitmap;

public interface Api {
	// information about fbreader
	String getFBReaderVersion() throws ApiException;

	// preferences information
	List<String> getOptionGroups() throws ApiException;
	List<String> getOptionNames(String group) throws ApiException;
	String getOptionValue(String group, String name) throws ApiException;
	void setOptionValue(String group, String name, String value) throws ApiException;

	// book information for current book
	String getBookLanguage() throws ApiException;
	String getBookTitle() throws ApiException;
	List<String> getBookAuthors() throws ApiException;
	List<String> getBookTags() throws ApiException;
	String getBookFilePath() throws ApiException;
	String getBookHash() throws ApiException;
	String getBookUniqueId() throws ApiException;
	Date getBookLastTurningTime() throws ApiException;

	// book information for book defined by id
	String getBookLanguage(long id) throws ApiException;
	String getBookTitle(long id) throws ApiException;
	//List<String> getBookAuthors(long id) throws ApiException;
	List<String> getBookTags(long id) throws ApiException;
	String getBookFilePath(long id) throws ApiException;
	String getBookHash(long id) throws ApiException;
	String getBookUniqueId(long id) throws ApiException;
	Date getBookLastTurningTime(long id) throws ApiException;
	float getBookProgress() throws ApiException;

	//long findBookIdByUniqueId(String uniqueId) throws ApiException;
	//long findBookIdByFilePath(String uniqueId) throws ApiException;

	// text information
	int getParagraphsNumber() throws ApiException;
	int getParagraphElementsCount(int paragraphIndex) throws ApiException;
	String getParagraphText(int paragraphIndex) throws ApiException;
	List<String> getParagraphWords(int paragraphIndex) throws ApiException;
	List<Integer> getParagraphWordIndices(int paragraphIndex) throws ApiException;

	// page information
	TextPosition getPageStart() throws ApiException;
	TextPosition getPageEnd() throws ApiException;
	boolean isPageEndOfSection() throws ApiException;
	boolean isPageEndOfText() throws ApiException;

	// manage view
	void setPageStart(TextPosition position) throws ApiException;
	void highlightArea(TextPosition start, TextPosition end) throws ApiException;
	void clearHighlighting() throws ApiException;
	int getBottomMargin() throws ApiException;
	void setBottomMargin(int value) throws ApiException;
	int getTopMargin() throws ApiException;
	void setTopMargin(int value) throws ApiException;
	int getLeftMargin() throws ApiException;
	void setLeftMargin(int value) throws ApiException;
	int getRightMargin() throws ApiException;
	void setRightMargin(int value) throws ApiException;

	// action control
	List<String> listActions() throws ApiException;
	List<String> listActionNames(List<String> actions) throws ApiException;

	String getKeyAction(int key, boolean longPress) throws ApiException;
	void setKeyAction(int key, boolean longPress, String action) throws ApiException;

	List<String> listZoneMaps() throws ApiException;
	String getZoneMap() throws ApiException;
	void setZoneMap(String name) throws ApiException;
	int getZoneMapHeight(String name) throws ApiException;
	int getZoneMapWidth(String name) throws ApiException;
	void createZoneMap(String name, int width, int height) throws ApiException;
	boolean isZoneMapCustom(String name) throws ApiException;
	void deleteZoneMap(String name) throws ApiException;

	String getTapZoneAction(String name, int h, int v, boolean singleTap) throws ApiException;
	void setTapZoneAction(String name, int h, int v, boolean singleTap, String action) throws ApiException;
	String getTapActionByCoordinates(String name, int x, int y, int width, int height, String tap) throws ApiException;

	List<MenuNode> getMainMenuContent() throws ApiException;
	String getResourceString(String ... keys) throws ApiException;
	Bitmap getBitmap(int resourceId) throws ApiException;
}
