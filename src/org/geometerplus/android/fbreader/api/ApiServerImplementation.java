/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import java.util.*;

import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.text.view.*;

import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.fbreader.*;

import org.geometerplus.android.fbreader.*;

public class ApiServerImplementation extends ApiInterface.Stub implements Api, ApiMethods {
	public static void sendEvent(ContextWrapper context, String eventType) {
		context.sendBroadcast(
			new Intent(FBReaderIntents.Action.API_CALLBACK)
				.putExtra(ApiClientImplementation.EVENT_TYPE, eventType)
		);
	}

	private final Context myContext;
	private volatile FBReaderApp myReader;
	private final ZLKeyBindings myBindings = new ZLKeyBindings();

	ApiServerImplementation(Context context) {
		myContext = context;
	}

	private synchronized FBReaderApp getReader() {
		if (myReader == null) {
			myReader = (FBReaderApp)FBReaderApp.Instance();
		}
		return myReader;
	}

	private ApiObject.Error unsupportedMethodError(int method) {
		return new ApiObject.Error("Unsupported method code: " + method);
	}

	private ApiObject.Error exceptionInMethodError(int method, Throwable e) {
		return new ApiObject.Error("Exception in method " + method + ": " + e);
	}

	public ApiObject request(int method, ApiObject[] parameters) {
		try {
			switch (method) {
				case GET_FBREADER_VERSION:
					return ApiObject.envelope(getFBReaderVersion());
				case GET_OPTION_VALUE:
					return ApiObject.envelope(getOptionValue(
						((ApiObject.String)parameters[0]).Value,
						((ApiObject.String)parameters[1]).Value
					));
				case SET_OPTION_VALUE:
					setOptionValue(
						((ApiObject.String)parameters[0]).Value,
						((ApiObject.String)parameters[1]).Value,
						((ApiObject.String)parameters[2]).Value
					);
					return ApiObject.Void.Instance;
				case GET_BOOK_LANGUAGE:
					if (parameters.length == 0) {
						return ApiObject.envelope(getBookLanguage());
					} else {
						return ApiObject.envelope(getBookLanguage(((ApiObject.Long)parameters[0]).Value));
					}
				case GET_BOOK_TITLE:
					if (parameters.length == 0) {
						return ApiObject.envelope(getBookTitle());
					} else {
						return ApiObject.envelope(getBookTitle(((ApiObject.Long)parameters[0]).Value));
					}
				case GET_BOOK_FILE_PATH:
					if (parameters.length == 0) {
						return ApiObject.envelope(getBookFilePath());
					} else {
						return ApiObject.envelope(getBookFilePath(((ApiObject.Long)parameters[0]).Value));
					}
				case GET_BOOK_HASH:
					if (parameters.length == 0) {
						return ApiObject.envelope(getBookHash());
					} else {
						return ApiObject.envelope(getBookHash(((ApiObject.Long)parameters[0]).Value));
					}
				case GET_BOOK_UNIQUE_ID:
					if (parameters.length == 0) {
						return ApiObject.envelope(getBookUniqueId());
					} else {
						return ApiObject.envelope(getBookUniqueId(((ApiObject.Long)parameters[0]).Value));
					}
				case GET_BOOK_LAST_TURNING_TIME:
					if (parameters.length == 0) {
						return ApiObject.envelope(getBookLastTurningTime());
					} else {
						return ApiObject.envelope(getBookLastTurningTime(((ApiObject.Long)parameters[0]).Value));
					}
				case GET_BOOK_PROGRESS:
					return ApiObject.envelope(getBookProgress());
				case GET_PARAGRAPHS_NUMBER:
					return ApiObject.envelope(getParagraphsNumber());
				case GET_PARAGRAPH_ELEMENTS_COUNT:
					return ApiObject.envelope(getParagraphElementsCount(
						((ApiObject.Integer)parameters[0]).Value
					));
				case GET_PARAGRAPH_TEXT:
					return ApiObject.envelope(getParagraphText(
						((ApiObject.Integer)parameters[0]).Value
					));
				case GET_PAGE_START:
					return getPageStart();
				case GET_PAGE_END:
					return getPageEnd();
				case IS_PAGE_END_OF_SECTION:
					return ApiObject.envelope(isPageEndOfSection());
				case IS_PAGE_END_OF_TEXT:
					return ApiObject.envelope(isPageEndOfText());
				case SET_PAGE_START:
					setPageStart((TextPosition)parameters[0]);
					return ApiObject.Void.Instance;
				case HIGHLIGHT_AREA:
				{
					highlightArea((TextPosition)parameters[0], (TextPosition)parameters[1]);
					return ApiObject.Void.Instance;
				}
				case CLEAR_HIGHLIGHTING:
					clearHighlighting();
					return ApiObject.Void.Instance;
				case GET_BOTTOM_MARGIN:
					return ApiObject.envelope(getBottomMargin());
				case SET_BOTTOM_MARGIN:
					setBottomMargin(((ApiObject.Integer)parameters[0]).Value);
					return ApiObject.Void.Instance;
				case GET_TOP_MARGIN:
					return ApiObject.envelope(getTopMargin());
				case SET_TOP_MARGIN:
					setTopMargin(((ApiObject.Integer)parameters[0]).Value);
					return ApiObject.Void.Instance;
				case GET_LEFT_MARGIN:
					return ApiObject.envelope(getLeftMargin());
				case SET_LEFT_MARGIN:
					setLeftMargin(((ApiObject.Integer)parameters[0]).Value);
					return ApiObject.Void.Instance;
				case GET_RIGHT_MARGIN:
					return ApiObject.envelope(getRightMargin());
				case SET_RIGHT_MARGIN:
					setRightMargin(((ApiObject.Integer)parameters[0]).Value);
					return ApiObject.Void.Instance;
				case GET_KEY_ACTION:
					return ApiObject.envelope(getKeyAction(
						((ApiObject.Integer)parameters[0]).Value,
						((ApiObject.Boolean)parameters[1]).Value
					));
				case SET_KEY_ACTION:
					setKeyAction(
						((ApiObject.Integer)parameters[0]).Value,
						((ApiObject.Boolean)parameters[1]).Value,
						((ApiObject.String)parameters[2]).Value
					);
					return ApiObject.Void.Instance;
				case GET_ZONEMAP:
					return ApiObject.envelope(getZoneMap());
				case SET_ZONEMAP:
					setZoneMap(((ApiObject.String)parameters[0]).Value);
					return ApiObject.Void.Instance;
				case GET_ZONEMAP_HEIGHT:
					return ApiObject.envelope(getZoneMapHeight(((ApiObject.String)parameters[0]).Value));
				case GET_ZONEMAP_WIDTH:
					return ApiObject.envelope(getZoneMapWidth(((ApiObject.String)parameters[0]).Value));
				case GET_TAPZONE_ACTION:
					return ApiObject.envelope(getTapZoneAction(
						((ApiObject.String)parameters[0]).Value,
						((ApiObject.Integer)parameters[1]).Value,
						((ApiObject.Integer)parameters[2]).Value,
						((ApiObject.Boolean)parameters[3]).Value
					));
				case GET_TAP_ACTION_BY_COORDINATES:
					return ApiObject.envelope(getTapActionByCoordinates(
						((ApiObject.String)parameters[0]).Value,
						((ApiObject.Integer)parameters[1]).Value,
						((ApiObject.Integer)parameters[2]).Value,
						((ApiObject.Integer)parameters[3]).Value,
						((ApiObject.Integer)parameters[4]).Value,
						((ApiObject.String)parameters[5]).Value
					));
				case SET_TAPZONE_ACTION:
					setTapZoneAction(
						((ApiObject.String)parameters[0]).Value,
						((ApiObject.Integer)parameters[1]).Value,
						((ApiObject.Integer)parameters[2]).Value,
						((ApiObject.Boolean)parameters[3]).Value,
						((ApiObject.String)parameters[4]).Value
					);
					return ApiObject.Void.Instance;
				case CREATE_ZONEMAP:
					createZoneMap(
						((ApiObject.String)parameters[0]).Value,
						((ApiObject.Integer)parameters[1]).Value,
						((ApiObject.Integer)parameters[2]).Value
					);
					return ApiObject.Void.Instance;
				case IS_ZONEMAP_CUSTOM:
					return ApiObject.envelope(isZoneMapCustom(
						((ApiObject.String)parameters[0]).Value
					));
				case DELETE_ZONEMAP:
					deleteZoneMap(((ApiObject.String)parameters[0]).Value);
					return ApiObject.Void.Instance;
				case GET_RESOURCE_STRING:
				{
					final String[] stringParams = new String[parameters.length];
					for (int i = 0; i < parameters.length; ++i) {
						stringParams[i] = ((ApiObject.String)parameters[i]).Value;
					}
					return ApiObject.envelope(getResourceString(stringParams));
				}
				case GET_BITMAP:
					return ApiObject.envelope(getBitmap(((ApiObject.Integer)parameters[0]).Value));
				default:
					return unsupportedMethodError(method);
			}
		} catch (Throwable e) {
			return new ApiObject.Error("Exception in method " + method + ": " + e);
		}
	}

	public List<ApiObject> requestList(int method, ApiObject[] parameters) {
		try {
			switch (method) {
				case LIST_OPTION_GROUPS:
					return ApiObject.envelopeStringList(getOptionGroups());
				case LIST_OPTION_NAMES:
					return ApiObject.envelopeStringList(getOptionNames(
						((ApiObject.String)parameters[0]).Value
					));
				case LIST_BOOK_TAGS:
					return ApiObject.envelopeStringList(getBookTags());
				case LIST_BOOK_AUTHORS:
					return ApiObject.envelopeStringList(getBookAuthors());
				case LIST_ACTIONS:
					return ApiObject.envelopeStringList(listActions());
				case LIST_ACTION_NAMES:
				{
					final ArrayList<String> actions = new ArrayList<String>(parameters.length);
					for (ApiObject o : parameters) {
						actions.add(((ApiObject.String)o).Value);
					}
					return ApiObject.envelopeStringList(listActionNames(actions));
				}
				case LIST_ZONEMAPS:
					return ApiObject.envelopeStringList(listZoneMaps());
				case GET_PARAGRAPH_WORDS:
					return ApiObject.envelopeStringList(getParagraphWords(
						((ApiObject.Integer)parameters[0]).Value
					));
				case GET_PARAGRAPH_WORD_INDICES:
					return ApiObject.envelopeIntegerList(getParagraphWordIndices(
						((ApiObject.Integer)parameters[0]).Value
					));
				case GET_MAIN_MENU_CONTENT:
					return ApiObject.envelopeSerializableList(getMainMenuContent());
				default:
					return Collections.<ApiObject>singletonList(unsupportedMethodError(method));
			}
		} catch (Throwable e) {
			return Collections.<ApiObject>singletonList(exceptionInMethodError(method, e));
		}
	}

	private Map<ApiObject,ApiObject> errorMap(ApiObject.Error error) {
		return Collections.<ApiObject,ApiObject>singletonMap(error, error);
	}

	public Map<ApiObject,ApiObject> requestMap(int method, ApiObject[] parameters) {
		try {
			switch (method) {
				default:
					return errorMap(unsupportedMethodError(method));
			}
		} catch (Throwable e) {
			return errorMap(exceptionInMethodError(method, e));
		}
	}

	// information about fbreader
	public String getFBReaderVersion() {
		return ZLibrary.Instance().getVersionName();
	}

	// preferences information
	public List<String> getOptionGroups() {
		return Config.Instance().listGroups();
	}

	public List<String> getOptionNames(String group) {
		return Config.Instance().listNames(group);
	}

	public String getOptionValue(String group, String name) {
		return new ZLStringOption(group, name, null).getValue();
	}

	public void setOptionValue(String group, String name, String value) {
		new ZLStringOption(group, name, null).setValue(value);
	}

	public String getBookLanguage() {
		final Book book = getReader().getCurrentBook();
		return book != null ? book.getLanguage() : null;
	}

	public String getBookTitle() {
		final Book book = getReader().getCurrentBook();
		return book != null ? book.getTitle() : null;
	}

	public List<String> getBookTags() {
		// TODO: implement
		return Collections.emptyList();
	}

	public float getBookProgress() {
		final Book book = getReader().getCurrentBook();
		if (book == null) {
			return -1.0f;
		}
		final RationalNumber progress = book.getProgress();
		return progress != null ? progress.toFloat() : -1.0f;
	}

	public List<String> getBookAuthors() {
		final Book book = getReader().getCurrentBook();
		if (book == null) {
			return null;
		}
		final List<Author> authors = book.authors();
		final List<String> authorNames = new ArrayList<String>(authors.size());
		for (Author a : authors) {
			authorNames.add(a.DisplayName);
		}
		return authorNames;
	}

	public String getBookFilePath() {
		final Book book = getReader().getCurrentBook();
		return book != null ? book.getPath() : null;
	}

	public String getBookHash() {
		final Book book = getReader().getCurrentBook();
		if (book == null) {
			return null;
		}
		final UID uid = BookUtil.createUid(book, "SHA-256");
		return uid != null ? uid.Id : null;
	}

	public String getBookUniqueId() {
		// TODO: implement
		return null;
	}

	public Date getBookLastTurningTime() {
		// TODO: implement
		return null;
	}

	public String getBookLanguage(long id) {
		// TODO: implement
		return null;
	}

	public String getBookTitle(long id) {
		// TODO: implement
		return null;
	}

	public List<String> getBookTags(long id) {
		// TODO: implement
		return Collections.emptyList();
	}

	public String getBookFilePath(long id) {
		// TODO: implement
		return null;
	}

	public String getBookHash(long id) {
		// TODO: implement
		return null;
	}

	public String getBookUniqueId(long id) {
		// TODO: implement
		return null;
	}

	public Date getBookLastTurningTime(long id) {
		// TODO: implement
		return null;
	}

	// page information
	public TextPosition getPageStart() {
		return getTextPosition(getReader().getTextView().getStartCursor());
	}

	public TextPosition getPageEnd() {
		return getTextPosition(getReader().getTextView().getEndCursor());
	}

	public boolean isPageEndOfSection() {
		final ZLTextWordCursor cursor = getReader().getTextView().getEndCursor();
		return cursor.isEndOfParagraph() && cursor.getParagraphCursor().isEndOfSection();
	}

	public boolean isPageEndOfText() {
		final ZLTextWordCursor cursor = getReader().getTextView().getEndCursor();
		return cursor.isEndOfParagraph() && cursor.getParagraphCursor().isLast();
	}

	private TextPosition getTextPosition(ZLTextWordCursor cursor) {
		return new TextPosition(
			cursor.getParagraphIndex(),
			cursor.getElementIndex(),
			cursor.getCharIndex()
		);
	}

	private ZLTextFixedPosition getZLTextPosition(TextPosition position) {
		return new ZLTextFixedPosition(
			position.ParagraphIndex,
			position.ElementIndex,
			position.CharIndex
		);
	}

	// manage view
	public void setPageStart(TextPosition position) {
		getReader().getTextView().gotoPosition(position.ParagraphIndex, position.ElementIndex, position.CharIndex);
		getReader().getViewWidget().repaint();
		getReader().storePosition();
	}

	public void highlightArea(TextPosition start, TextPosition end) {
		getReader().getTextView().highlight(
			getZLTextPosition(start),
			getZLTextPosition(end)
		);
	}

	public void clearHighlighting() {
		getReader().getTextView().clearHighlighting();
	}

	public int getBottomMargin() {
		return getReader().ViewOptions.BottomMargin.getValue();
	}

	public void setBottomMargin(int value) {
		getReader().ViewOptions.BottomMargin.setValue(value);
	}

	public int getTopMargin() {
		return getReader().ViewOptions.TopMargin.getValue();
	}

	public void setTopMargin(int value) {
		getReader().ViewOptions.TopMargin.setValue(value);
	}

	public int getLeftMargin() {
		return getReader().ViewOptions.LeftMargin.getValue();
	}

	public void setLeftMargin(int value) {
		getReader().ViewOptions.LeftMargin.setValue(value);
	}

	public int getRightMargin() {
		return getReader().ViewOptions.RightMargin.getValue();
	}

	public void setRightMargin(int value) {
		getReader().ViewOptions.RightMargin.setValue(value);
	}

	public int getParagraphsNumber() {
		return getReader().Model.getTextModel().getParagraphsNumber();
	}

	public int getParagraphElementsCount(int paragraphIndex) {
		final ZLTextWordCursor cursor = new ZLTextWordCursor(getReader().getTextView().getStartCursor());
		cursor.moveToParagraph(paragraphIndex);
		cursor.moveToParagraphEnd();
		return cursor.getElementIndex();
	}

	public String getParagraphText(int paragraphIndex) {
		final StringBuffer sb = new StringBuffer();
		final ZLTextWordCursor cursor = new ZLTextWordCursor(getReader().getTextView().getStartCursor());
		cursor.moveToParagraph(paragraphIndex);
		cursor.moveToParagraphStart();
		while (!cursor.isEndOfParagraph()) {
			ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				sb.append(element.toString() + " ");
			}
			cursor.nextWord();
		}
		return sb.toString();
	}

	public List<String> getParagraphWords(int paragraphIndex) {
		final ArrayList<String> words = new ArrayList<String>();
		final ZLTextWordCursor cursor = new ZLTextWordCursor(getReader().getTextView().getStartCursor());
		cursor.moveToParagraph(paragraphIndex);
		cursor.moveToParagraphStart();
		while (!cursor.isEndOfParagraph()) {
			ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				words.add(element.toString());
			}
			cursor.nextWord();
		}
		return words;
	}

	public ArrayList<Integer> getParagraphWordIndices(int paragraphIndex) {
		final ArrayList<Integer> indices = new ArrayList<Integer>();
		final ZLTextWordCursor cursor = new ZLTextWordCursor(getReader().getTextView().getStartCursor());
		cursor.moveToParagraph(paragraphIndex);
		cursor.moveToParagraphStart();
		while (!cursor.isEndOfParagraph()) {
			ZLTextElement element = cursor.getElement();
			if (element instanceof ZLTextWord) {
				indices.add(cursor.getElementIndex());
			}
			cursor.nextWord();
		}
		return indices;
	}

	// action control
	public List<String> listActions() {
		// TODO: implement
		return Collections.emptyList();
	}

	public List<String> listActionNames(List<String> actions) {
		// TODO: implement
		return Collections.emptyList();
	}

	public String getKeyAction(int key, boolean longPress) {
		return myBindings.getBinding(key, longPress);
	}

	public void setKeyAction(int key, boolean longPress, String action) {
		// TODO: implement
	}

	public List<String> listZoneMaps() {
		return TapZoneMap.zoneMapNames();
	}

	public String getZoneMap() {
		return getReader().PageTurningOptions.TapZoneMap.getValue();
	}

	public void setZoneMap(String name) {
		getReader().PageTurningOptions.TapZoneMap.setValue(name);
	}

	public int getZoneMapHeight(String name) {
		return TapZoneMap.zoneMap(name).getHeight();
	}

	public int getZoneMapWidth(String name) {
		return TapZoneMap.zoneMap(name).getWidth();
	}

	public void createZoneMap(String name, int width, int height) {
		TapZoneMap.createZoneMap(name, width, height);
	}

	public boolean isZoneMapCustom(String name) throws ApiException {
		return TapZoneMap.zoneMap(name).isCustom();
	}

	public void deleteZoneMap(String name) throws ApiException {
		TapZoneMap.deleteZoneMap(name);
	}

	public String getTapZoneAction(String name, int h, int v, boolean singleTap) {
		return TapZoneMap.zoneMap(name).getActionByZone(
			h, v, singleTap ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.doubleTap
		);
	}

	public String getTapActionByCoordinates(String name, int x, int y, int width, int height, String tap) {
		TapZoneMap.Tap id;
		try {
			id = TapZoneMap.Tap.valueOf(tap);
		} catch (Exception e) {
			id = TapZoneMap.Tap.singleTap;
		}
		return TapZoneMap.zoneMap(name).getActionByCoordinates(x, y, width, height, id);
	}

	public void setTapZoneAction(String name, int h, int v, boolean singleTap, String action) {
		TapZoneMap.zoneMap(name).setActionForZone(h, v, singleTap, action);
	}

	private void setMenuTitles(List<MenuNode> nodes, ZLResource menuResource) {
		for (MenuNode n : nodes) {
			n.OptionalTitle = menuResource.getResource(n.Code).getValue();
			if (n instanceof MenuNode.Submenu) {
				setMenuTitles(((MenuNode.Submenu)n).Children, menuResource);
			}
		}
	}

	public List<MenuNode> getMainMenuContent() {
		final List<MenuNode> nodes = MenuData.topLevelNodes();
		final List<MenuNode> copies = new ArrayList<MenuNode>(nodes.size());
		for (MenuNode n : nodes) {
			copies.add(n.clone());
		}
		setMenuTitles(copies, ZLResource.resource("menu"));
		return copies;
	}

	public String getResourceString(String ... keys) {
		ZLResource resource = ZLResource.resource(keys[0]);
		for (int i = 1; i < keys.length; ++i) {
			resource = resource.getResource(keys[i]);
		}
		return resource.getValue();
	}

	public Bitmap getBitmap(int resourceId) {
		return BitmapFactory.decodeResource(myContext.getResources(), resourceId);
	}
}
