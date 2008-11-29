/*
 * Copyright (C) 2007-2008 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.fbreader;

import java.util.*;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.impl.*;

import org.geometerplus.fbreader.collection.*;
import org.geometerplus.fbreader.description.*;

public class CollectionView extends FBView {
	final static String SpecialTagAllBooks = ",AllBooks,";
	final static String SpecialTagNoTagsBooks = ",NoTags,";

	public final ZLBooleanOption ShowTagsOption =
		new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Library", "ShowTags", true);
	public final ZLBooleanOption ShowAllBooksTagOption =
		new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, "Library", "ShowAllBooksTag", true);
	
	public final BookCollection Collection = new BookCollection();
	private boolean myTreeStateIsFrozen;
	private boolean myUpdateModel;
	private boolean myShowTags;
	private boolean myShowAllBooksList;

	public CollectionView(FBReader reader, ZLPaintContext context) {
		super(reader, context);
		myUpdateModel = true;
		setModel(new CollectionModel(this, Collection));
		myShowTags = ShowTagsOption.getValue();
		myShowAllBooksList = ShowAllBooksTagOption.getValue();
	}
	
	public String getCaption() {
		return ZLResource.resource("library").getResource("caption").getValue();
	}

	public boolean onStylusRelease(int x, int y) {
		if (super.onStylusRelease(x, y)) {
			return false;
		}

		ZLTextElementArea imageArea = getElementByCoordinates(x, y);
		if ((imageArea != null) && (imageArea.Element instanceof ZLTextImageElement)) {
			ZLTextWordCursor cursor = new ZLTextWordCursor(StartCursor);
			cursor.moveToParagraph(imageArea.ParagraphIndex);
			cursor.moveTo(imageArea.TextElementIndex, 0);
			final ZLTextElement element = cursor.getElement();
			if (!(element instanceof ZLTextImageElement)) {
				return false;
			}
			final ZLTextImageElement imageElement = (ZLTextImageElement)element;

			final String imageId = imageElement.Id;
			if (imageId == CollectionModel.BookInfoImageId) {
				editBookInfo(imageArea.ParagraphIndex);
				return true;
			} else if (imageId == CollectionModel.RemoveBookImageId) {
				removeBook(imageArea.ParagraphIndex);
				return true;
			} else if (imageId == CollectionModel.RemoveTagImageId) {
				removeTag(imageArea.ParagraphIndex);
				return true;
			} else if (imageId == CollectionModel.TagInfoImageId) {
				editTagInfo(imageArea.ParagraphIndex);
				return true;
			}
			return false;
		}

		int index = getParagraphIndexByCoordinate(y);
		if (index == -1) {
			return false;
		}

		final BookDescription book = getCollectionModel().getBookByParagraphIndex(index);
		if (book != null) {
			final FBReader fbreader = (FBReader)Application;
			fbreader.openBook(book);
			fbreader.showBookTextView();
			return true;
		}

		return false;
	}

	private void editBookInfo(int paragraphIndex) {
		final BookDescription book = getCollectionModel().getBookByParagraphIndex(paragraphIndex);
		if (book == null) {
			return;
		}

		Runnable action = new Runnable() {
			public void run() {
				Collection.rebuild(false);
				myUpdateModel = true;
				selectBook(book);
				Application.refreshWindow();
			}
		};
		new BookInfoDialog(Collection, book.FileName, action).getDialog().run();
	}

	private void removeBook(int paragraphIndex) {
		final CollectionModel cModel = getCollectionModel();
		final BookDescription book = cModel.getBookByParagraphIndex(paragraphIndex);

		if (book == null) {
			return;
		}

		String boxKey = "removeBookBox";
		final String message;
		{
			final String format = ZLDialogManager.getDialogMessage(boxKey);
			int index = format.indexOf("%s");
			if (index == -1) {
				message = format;
			} else {
				message = format.substring(0, index) + book.getTitle() + format.substring(index + 2);
			}
		}

		Runnable okAction = new Runnable() {
			public void run() {
				cModel.removeAllMarks();
				new BookList().removeFileName(book.FileName);
				cModel.removeBook(book);
      
				if (cModel.getParagraphsNumber() == 0) {
					gotoParagraph(0, false);
				} else {
					int index = StartCursor.getParagraphCursor().Index;
					if (index >= cModel.getParagraphsNumber()) {
						index = cModel.getParagraphsNumber() - 1;
					}
					while (!cModel.getTreeParagraph(index).getParent().isOpen()) {
						--index;
					}
					gotoParagraph(index, false);
				}
				rebuildPaintInfo(true);
				Application.refreshWindow();
			}
		};
		ZLDialogManager.getInstance().showQuestionBox(
			boxKey, message,
			ZLDialogManager.YES_BUTTON, okAction,
			ZLDialogManager.NO_BUTTON, null,
			null, null
		);
	}

	private void removeTag(int paragraphIndex) {
		final String tag = getCollectionModel().getTagByParagraphIndex(paragraphIndex);
		if (tag == null) {
			return;
		}

		final String boxKey = "removeTagBox";
		final String message;
		{
			final String format = ZLDialogManager.getDialogMessage(boxKey);
			int index = format.indexOf("%s");
			if (index == -1) {
				message = format;
			} else {
				message = format.substring(0, index) + tag + format.substring(index + 2);
			}
		}

		final Runnable removeThisTagOnlyAction = new Runnable() {
			public void run() {
				getCollectionModel().removeAllMarks();
				Collection.removeTag(tag, false);
				updateModel();
				Application.refreshWindow();
			}
		};
		final Runnable removeSubTree = new Runnable() {
			public void run() {
				getCollectionModel().removeAllMarks();
				Collection.removeTag(tag, true);
				updateModel();
				Application.refreshWindow();
			}
		};
		if (Collection.hasSubtags(tag)) {
			if (Collection.hasBooks(tag)) {
				ZLDialogManager.getInstance().showQuestionBox(boxKey, message,
					"thisOnly", removeThisTagOnlyAction,
					"withSubtags", removeSubTree,
					ZLDialogManager.CANCEL_BUTTON, null
				);
			} else {
				ZLDialogManager.getInstance().showQuestionBox(boxKey, message,
					"withSubtags", removeSubTree,
					ZLDialogManager.CANCEL_BUTTON, null,
					null, null
				);
			}
		} else {
			ZLDialogManager.getInstance().showQuestionBox(boxKey, message,
				ZLDialogManager.YES_BUTTON, removeSubTree,
				ZLDialogManager.CANCEL_BUTTON, null,
				null, null
			);
		}
	}

	private void editTagInfo(int paragraphIndex) {
		final String tag = getCollectionModel().getTagByParagraphIndex(paragraphIndex);
		if (tag == null) {
			return;
		}

		final boolean tagIsSpecial = (tag == SpecialTagAllBooks) || (tag == SpecialTagNoTagsBooks);
		final String tagDisplayName = tagIsSpecial ? "" : tag;
		final boolean editNotClone = tag != SpecialTagAllBooks;
		final boolean includeSubtags = !tagIsSpecial && Collection.hasSubtags(tag);
		final boolean hasBooks = Collection.hasBooks(tag);

		final TreeSet tagSet = new TreeSet();
		final ArrayList books = Collection.books();
		final int len = books.size();
		for (int i = 0; i < len; ++i) {
			tagSet.addAll(((BookDescription)books.get(i)).getTags());
		}
		final TreeSet fullTagSet = new TreeSet(tagSet);
		for (Iterator it = tagSet.iterator(); it.hasNext(); ) {
			final String value = (String)it.next();
			for (int index = 0;;) {
				index = value.indexOf('/', index);
				if (index == -1) {
					break;
				}
				fullTagSet.add(value.substring(0, index++));
			}
		}
		final ArrayList names = new ArrayList();
		if (!fullTagSet.contains(tagDisplayName)) {
			names.add(tagDisplayName);
		}
		names.addAll(fullTagSet);

		new EditTagDialog(names, tag, tagIsSpecial, includeSubtags, hasBooks, editNotClone).run();
	}

	public void paint() {
		if ((myShowTags != ShowTagsOption.getValue()) ||
				(myShowAllBooksList != ShowAllBooksTagOption.getValue())) {
			myShowTags = ShowTagsOption.getValue();
			myShowAllBooksList = ShowAllBooksTagOption.getValue();
			myUpdateModel = true;
		}
		if (myUpdateModel) {			
			ZLTextModel oldModel = getModel();
			setModel(null);
			((CollectionModel)oldModel).update();
			setModel(oldModel);
			myUpdateModel = false;
		}
		super.paint();
	}
	
	public void updateModel() {
		Collection.rebuild(true);
		myUpdateModel = true;
		Collection.authors();
		clearCaches();
	}
	
	public void synchronizeModel() {
		if (Collection.synchronize()) {
			updateModel();
		}
	}

	public void selectBook(BookDescription book) {
		if (myUpdateModel) {
			ZLTextModel oldModel = getModel();
			setModel(null);
			((CollectionModel)oldModel).update();
			setModel(oldModel);
			myUpdateModel = false;
		}
		final ArrayList toSelect = getCollectionModel().paragraphIndicesByBook(book);
		if ((toSelect != null) && !toSelect.isEmpty()) {
			final int len = toSelect.size();
			int index = 0;
			for (int i = 0; i < len; ++i) {
				index = ((Integer)toSelect.get(i)).intValue();
				highlightParagraph(index);
			}
			gotoParagraph(index, false);
			scrollPage(false, ZLTextView.ScrollingMode.SCROLL_PERCENTAGE, 40);
		}
	}

	private CollectionModel getCollectionModel() {
		return (CollectionModel)getModel();
	}

	final private class EditTagDialog implements Runnable {
		private final static String EDIT_OR_CLONE_KEY = "editOrClone";

		private final String myTag;
		private final boolean myIncludeSubtags;
		private final EditOrCloneEntry myEditOrCloneEntry;
		private final TagNameEntry myTagNameEntry;
		private final IncludeSubtagsEntry myIncludeSubtagsEntry;

		public EditTagDialog(ArrayList names, String tag, boolean tagIsSpecial, boolean includeSubtags, boolean hasBooks, boolean editNotClone) {
			myTag = tag;
			myIncludeSubtags = includeSubtags;

			final ZLDialog dialog = ZLDialogManager.getInstance().createDialog("editTagInfoDialog");

			myEditOrCloneEntry = new EditOrCloneEntry(dialog.getResource(EDIT_OR_CLONE_KEY), editNotClone);
			myEditOrCloneEntry.setActive(!tagIsSpecial);
	  
			myTagNameEntry = new TagNameEntry(names, tag);

			myIncludeSubtagsEntry = new IncludeSubtagsEntry(includeSubtags);
			if (includeSubtags && !hasBooks) {
				myIncludeSubtagsEntry.setActive(false);
			}
		}

		public void run() {
			final ZLDialog dialog = ZLDialogManager.getInstance().createDialog("editTagInfoDialog");
	  
			dialog.addOption(EDIT_OR_CLONE_KEY, myEditOrCloneEntry);
			dialog.addOption("name", myTagNameEntry);
			if (myIncludeSubtags) {
				dialog.addOption("includeSubtags", myIncludeSubtagsEntry);
			}

			final Runnable acceptAction = new Runnable() {
				public void run() {
					dialog.acceptValues();
					final String tagValue = myTagNameEntry.initialValue().trim();
					if (tagValue.length() == 0) {
						ZLDialogManager.getInstance().showErrorBox("tagMustBeNonEmpty", EditTagDialog.this);
						return;
					}
					if (tagValue.indexOf(',') != -1) {
						ZLDialogManager.getInstance().showErrorBox("tagMustNotContainComma", EditTagDialog.this);
						return;
					}
					if (tagValue.equals(myTag)) {
						return;
					}
					getCollectionModel().removeAllMarks();
					if (myTag == SpecialTagAllBooks) {
						Collection.addTagToAllBooks(tagValue);
					} else if (myTag == SpecialTagNoTagsBooks) {
						Collection.addTagToBooksWithNoTags(tagValue);
					} else if (myEditOrCloneEntry.getEditNotClone()) {
						Collection.renameTag(myTag, tagValue, myIncludeSubtagsEntry.initialState());
					} else {
						Collection.cloneTag(myTag, tagValue, myIncludeSubtagsEntry.initialState());
					}
					updateModel();
					Application.refreshWindow();
				}
			};

			dialog.addButton(ZLDialogManager.OK_BUTTON, acceptAction);
			dialog.addButton(ZLDialogManager.CANCEL_BUTTON, null);

			dialog.run();
		}
	}
}

final class EditOrCloneEntry extends ZLChoiceOptionEntry {
	private final ZLResource myResource;
	private boolean myEditNotClone;

	public EditOrCloneEntry(ZLResource resource, boolean editNotClone) {
		myResource = resource;
		myEditNotClone = editNotClone;
	}

	public String getText(int index) {
		return (index == 0) ? myResource.getResource("edit").getValue() : myResource.getResource("clone").getValue();
	}

	public int choiceNumber() {
		return 2;
	}

	public int initialCheckedIndex() {
		return myEditNotClone ? 0 : 1;
	}

	public void onAccept(int index) {
		myEditNotClone = (index == 0);
	}

	public boolean getEditNotClone() {
		return myEditNotClone;
	}
}

final class TagNameEntry extends ZLComboOptionEntry {
	private final ArrayList myValuesList;
	private String myValue;
	private boolean myAddedManualValue;

	public TagNameEntry(ArrayList valuesList, String initialValue) {
		super(true);
		myValuesList = valuesList;
		myValue = initialValue;
	}

	public String initialValue() {
		return myValue;
	}

	public ArrayList getValues() {
		return myValuesList;
	}

	public void onAccept(String value) {
		myValue = value;
		if (!myValuesList.contains(value)) {
			if (myAddedManualValue) {
				myValuesList.set(0, value);
			} else {
				myAddedManualValue = true;
				myValuesList.add(0, value);
			}
		}
	}
}

final class IncludeSubtagsEntry extends ZLBooleanOptionEntry {
	private boolean myValue;

	public IncludeSubtagsEntry(boolean initialValue) {
		myValue = initialValue;
	}

	public boolean initialState() {
		return myValue;
	}

	public void onAccept(boolean state) {
		myValue = state;
	}
}
