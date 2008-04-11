package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.dialogs.ZLDialogManager;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.*;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.impl.*;

import org.geometerplus.fbreader.collection.*;
import org.geometerplus.fbreader.description.*;

public class CollectionView extends FBView {
	private final BookCollection myCollection = new BookCollection();
	private boolean myTreeStateIsFrozen;
	private boolean myUpdateModel;
	private boolean myShowTags;
	private boolean myShowAllBooksList;

	private static final String LIBRARY = "Library";
	public final ZLBooleanOption ShowTagsOption =
		new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, LIBRARY, "ShowTags", true);
	public final ZLBooleanOption ShowAllBooksTagOption =
		new ZLBooleanOption(ZLOption.LOOK_AND_FEEL_CATEGORY, LIBRARY, "ShowAllBooksTag", true);
	
	public CollectionView(FBReader reader, ZLPaintContext context) {
		super(reader, context);
		myUpdateModel = true;
		setModel(new CollectionModel(this, myCollection));
		myShowTags = ShowTagsOption.getValue();
		myShowAllBooksList = ShowAllBooksTagOption.getValue();
	}
	
	public String getCaption() {
		return ZLResource.resource("library").getResource("caption").getValue();
	}

	public boolean onStylusPress(int x, int y) {
		if (super.onStylusPress(x, y)) {
			return true;
		}

		ZLTextElementArea imageArea = getElementByCoordinates(x, y);
		if ((imageArea != null) && (imageArea.Element instanceof ZLTextImageElement)) {
			ZLTextWordCursor cursor = new ZLTextWordCursor(getStartCursor());
			cursor.moveToParagraph(imageArea.ParagraphNumber);
			cursor.moveTo(imageArea.TextElementNumber, 0);
			final ZLTextElement element = cursor.getElement();
			if (!(element instanceof ZLTextImageElement)) {
				return false;
			}
			final ZLTextImageElement imageElement = (ZLTextImageElement)element;

			final BookDescription book = collectionModel().bookByParagraphNumber(imageArea.ParagraphNumber);
			if (book == null) {
				return false;
			}

			final String imageId = imageElement.Id;
			if (imageId == CollectionModel.BookInfoImageId) {
				Runnable action = new Runnable() {
					public void run() {
						myCollection.rebuild(false);
						myUpdateModel = true;
						selectBook(book);
						repaintView();
					}
				};
				new BookInfoDialog(myCollection, book.getFileName(), action).getDialog().run();
				return true;
			} else if (imageId == CollectionModel.RemoveBookImageId) {
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
						CollectionModel cModel = collectionModel();
          
						new BookList().removeFileName(book.getFileName());
						cModel.removeBook(book);
          
						if (cModel.getParagraphsNumber() == 0) {
							gotoParagraph(0, false);
						} else {
							int index = getStartCursor().getParagraphCursor().getIndex();
							if (index >= cModel.getParagraphsNumber()) {
								index = cModel.getParagraphsNumber() - 1;
							}
							while (!cModel.getTreeParagraph(index).getParent().isOpen()) {
								--index;
							}
							gotoParagraph(index, false);
						}
						rebuildPaintInfo(true);
						getApplication().refreshWindow();
					}
				};
				ZLDialogManager.getInstance().showQuestionBox(
					boxKey, message,
					ZLDialogManager.YES_BUTTON, okAction,
					ZLDialogManager.NO_BUTTON, null,
					null, null
				);
				return true;
			}
			return false;
		}

		int index = getParagraphIndexByCoordinate(y);
		if (index == -1) {
			return false;
		}

		final BookDescription book = collectionModel().bookByParagraphNumber(index);
		if (book != null) {
			getFBReader().openBook(book);
			getFBReader().showBookTextView();
			return true;
		}

		return false;
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
		myCollection.rebuild(true);
		myUpdateModel = true;
		myCollection.authors();
	}
	
	public void synchronizeModel() {
		if (myCollection.synchronize()) {
	   	System.out.println("synchronizeModel");
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
		int toSelect = collectionModel().paragraphNumberByBook(book);
		if (toSelect >= 0) {
			highlightParagraph(toSelect);
			gotoParagraph(toSelect, false);
			scrollPage(false, ZLTextView.ScrollingMode.SCROLL_PERCENTAGE, 40);
		}
	}

	public BookCollection getCollection() {
		return myCollection;
	}

	private CollectionModel collectionModel() {
		return (CollectionModel)getModel();
	}
}
