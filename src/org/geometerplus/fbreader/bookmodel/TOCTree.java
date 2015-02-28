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

package org.geometerplus.fbreader.bookmodel;

import org.geometerplus.zlibrary.core.tree.ZLTree;

import org.geometerplus.zlibrary.text.model.ZLTextModel;

public class TOCTree extends ZLTree<TOCTree> {
	private String myText;
	private Reference myReference;

	protected TOCTree() {
		super();
	}

	public TOCTree(TOCTree parent) {
		super(parent);
	}

	public final String getText() {
		return myText;
	}

	// faster replacement for
	// return text.trim().replaceAll("[\t ]+", " ");
	private static String trim(String text) {
		final char[] data = text.toCharArray();
		int count = 0;
		int shift = 0;
		boolean changed = false;
		char space = ' ';
		for (int i = 0; i < data.length; ++i) {
			final char ch = data[i];
			if (ch == ' ' || ch == '\t') {
				++count;
				space = ch;
			} else {
				if (count > 0) {
					if (count == i) {
						shift += count;
						changed = true;
					} else {
						shift += count - 1;
						if (shift > 0 || space == '\t') {
							data[i - shift - 1] = ' ';
							changed = true;
						}
					}
					count = 0;
				}
				if (shift > 0) {
					data[i - shift] = data[i];
				}
			}
		}
		if (count > 0) {
			changed = true;
			shift += count;
		}
		return changed ? new String(data, 0, data.length - shift) : text;
	}

	public final void setText(String text) {
		myText = text != null ? trim(text) : null;
	}

	public Reference getReference() {
		return myReference;
	}

	public void setReference(ZLTextModel model, int reference) {
		myReference = new Reference(reference, model);
	}

	public static class Reference {
		public final int ParagraphIndex;
		public final ZLTextModel Model;

		public Reference(final int paragraphIndex, final ZLTextModel model) {
			ParagraphIndex = paragraphIndex;
			Model = model;
		}
	}
}
