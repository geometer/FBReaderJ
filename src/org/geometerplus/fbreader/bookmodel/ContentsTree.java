/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

import java.util.*;
import org.geometerplus.zlibrary.core.util.*;

import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.core.tree.ZLTextTree;

public class ContentsTree extends ZLTextTree {
	private final HashMap<ZLTextTree,Reference> myReferenceByTree = new HashMap<ZLTextTree,Reference>();
	
	public Reference getReference(ZLTextTree tree) {
		return myReferenceByTree.get(tree);
	}
	
	public void setReference(ZLTextTree tree, ZLTextModel model, int reference) {
		myReferenceByTree.put(tree, new Reference(reference, model));
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
