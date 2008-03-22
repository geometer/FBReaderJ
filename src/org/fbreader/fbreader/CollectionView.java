package org.fbreader.fbreader;

import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.*;
import org.zlibrary.text.view.ZLTextView;
import org.zlibrary.text.view.impl.*;

import org.fbreader.collection.*;
import org.fbreader.description.*;

public class CollectionView extends FBView {
	private final BookCollection myCollection = new BookCollection();
	private boolean myTreeStateIsFrozen;
	private boolean myUpdateModel;

	public CollectionView(FBReader reader, ZLPaintContext context) {
		super(reader, context);
		myUpdateModel = true;
		setModel(new CollectionModel(this, myCollection));
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
			ZLTextWordCursor cursor = getStartCursor();
			cursor.moveToParagraph(imageArea.ParagraphNumber);
			cursor.moveTo(imageArea.TextElementNumber, 0);
			final ZLTextElement element = cursor.getElement();
			if (!(element instanceof ZLTextImageElement)) {
				return false;
			}
			final ZLTextImageElement imageElement = (ZLTextImageElement)element;

			BookDescription book = collectionModel().bookByParagraphNumber(imageArea.ParagraphNumber);
			if (book == null) {
				return false;
			}

			final String imageId = imageElement.Id;
			if (imageId == CollectionModel.BookInfoImageId) {
				if (new BookInfoDialog(myCollection, book.getFileName()).getDialog().run()) {
					myCollection.rebuild(false);
					myUpdateModel = true;
					selectBook(book);
					repaintView();
				}
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
				if (ZLDialogManager.getInstance().showQuestionBox(boxKey, message,
					ZLDialogManager.YES_BUTTON, ZLDialogManager.NO_BUTTON, null) == 0) {
					CollectionModel cModel = collectionModel();
					//collectionModel().removeAllMarks();

					new BookList().removeFileName(book.getFileName());

					int index = cModel.paragraphNumberByBook(book);
					ZLTextTreeParagraph paragraph = cModel.getTreeParagraph(index);
					int count = 1;
					for (ZLTextTreeParagraph parent = paragraph.getParent(); (parent != null) && (parent.getChildren().size() == 1); parent = parent.getParent()) {
						++count;
					}
        
					if (count > index) {
						count = index;
					}
        
					for (; count > 0; --count) {
						cModel.removeParagraph(index--);
					}
  
					if (cModel.getParagraphsNumber() == 0) {
						//setStartCursor(null);
					} else {
						int pIndex = getStartCursor().getParagraphCursor().getIndex();
						if (pIndex >= cModel.getParagraphsNumber()) {
							pIndex = cModel.getParagraphsNumber() - 1;
						}
						while (!cModel.getTreeParagraph(index).getParent().isOpen()) {
							--pIndex;
						}
						gotoParagraph(pIndex);
					}
					rebuildPaintInfo(true);
					getApplication().refreshWindow();
					/*
					ZLTextTreeParagraph paragraph = (ZLTextTreeParagraph)collectionModel().getTreeParagraph(imageArea.ParagraphNumber);
					ZLTextTreeParagraph parent = paragraph.getParent();
					if (parent.getChildren().size() == 1) {
						collectionModel().removeParagraph(imageArea.ParagraphNumber);
						collectionModel().removeParagraph(imageArea.ParagraphNumber - 1);
					} else {
						collectionModel().removeParagraph(imageArea.ParagraphNumber);
					}
					if (collectionModel().getParagraphsNumber() == 0) {
						this.getStartCursor().setCursor((ZLTextParagraphCursor)null);
					}
					rebuildPaintInfo(true);
					repaintView();
					*/
				}
				return true;
			}
			return false;
		}

		int index = getParagraphIndexByCoordinate(y);
		if (index == -1) {
			return false;
		}

		BookDescription book = collectionModel().bookByParagraphNumber(index);
		if (book != null) {
			
			getFBReader().openBook(book);
			getFBReader().showBookTextView();
			return true;
		}

		return false;
	}

	public void paint() {
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
			gotoParagraph(toSelect);
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
