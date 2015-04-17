/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader.sync;

import java.util.*;

import org.geometerplus.fbreader.book.Bookmark;

class BookmarkSync {
	static abstract class Request extends HashMap<String,Object> {
		Request(String action) {
			put("action", action);
		}
	}

	static abstract class ChangeRequest extends Request {
		ChangeRequest(String action, Bookmark bookmark, String bookHash) {
			super(action);
			final Map<String,Object> bmk = new HashMap<String,Object>();
			bmk.put("book_hash", bookHash);
			bmk.put("uid", bookmark.Uid);
			bmk.put("version_uid", bookmark.getVersionUid());
			bmk.put("style_id", bookmark.getStyleId());
			bmk.put("text", bookmark.getText());
			bmk.put("model_id", bookmark.ModelId);
			bmk.put("para_start", bookmark.getParagraphIndex());
			bmk.put("elmt_start", bookmark.getElementIndex());
			bmk.put("char_start", bookmark.getCharIndex());
			bmk.put("para_end", bookmark.getEnd().getParagraphIndex());
			bmk.put("elmt_end", bookmark.getEnd().getElementIndex());
			bmk.put("char_end", bookmark.getEnd().getCharIndex());
			bmk.put("creation_timestamp", bookmark.getDate(Bookmark.DateType.Creation).getTime());
			final Date accessDate = bookmark.getDate(Bookmark.DateType.Access);
			if (accessDate != null) {
				bmk.put("access_timestamp", accessDate.getTime());
			}
			final Date modificationDate = bookmark.getDate(Bookmark.DateType.Modification);
			if (modificationDate != null) {
				bmk.put("modification_timestamp", modificationDate.getTime());
			}

			put("bookmark", bmk);
		}
	}

	static class AddRequest extends ChangeRequest {
		AddRequest(Bookmark bookmark, String bookHash) {
			super("add", bookmark, bookHash);
		}
	}

	static class UpdateRequest extends ChangeRequest {
		UpdateRequest(Bookmark bookmark, String bookHash) {
			super("update", bookmark, bookHash);
		}
	}

	static class DeleteRequest extends Request {
		DeleteRequest(String uid) {
			super("delete");
			put("uid", uid);
		}
	}
}
