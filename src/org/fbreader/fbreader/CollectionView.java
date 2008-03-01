package org.fbreader.fbreader;

import java.util.*;
import org.zlibrary.core.util.*;

import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.image.ZLImageMap;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.*;
import org.zlibrary.text.model.impl.*;
import org.zlibrary.text.view.*;
import org.zlibrary.text.view.impl.*;

import org.fbreader.bookmodel.FBTextKind;
import org.fbreader.collection.*;
import org.fbreader.description.*;

public class CollectionView extends FBView {
	static private final String LIBRARY = "Library";
	static private final String DELETE_IMAGE_ID = "delete";
	static private final String BOOK_INFO_IMAGE_ID = "bookInfo";
	static private final String AUTHOR_INFO_IMAGE_ID = "authorInfo";
	static private final String SERIES_ORDER_IMAGE_ID = "seriesOrder";

	
	private final BookCollection myCollection = new BookCollection();
	private boolean myTreeStateIsFrozen;
	private boolean myUpdateModel;

	public CollectionView(FBReader reader, ZLPaintContext context) {
		super(reader, context);
		myUpdateModel = true;
		setModel(new CollectionModel(myCollection));
	}
	
	public String getCaption() {
		return LIBRARY;
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

			/*if (imageElement.getId() == BOOK_INFO_IMAGE_ID) {
				if (new BookInfoDialog(myCollection, book.getFileName()).dialog().run()) {
					myCollection.rebuild(false);
					myUpdateModel = true;
					selectBook(book);
					repaintView();
				}
				return true;
			} else if (imageElement.id() == DELETE_IMAGE_ID) {
				//ZLResourceKey
				String boxKey = "removeBookBox";
				final String message;
				final String format = ZLDialogManager.getDialogMessage(boxKey);
				int index = format.indexOf("%s");
				if (index == -1) {
					message = format;
				} else {
				    message = ZLDialogManager.getDialogMessage(boxKey).substring(0, index) + book.getTitle() + format.substring(index + 2);
				}
				//final String message =
				//	ZLStringUtil.printf(ZLDialogManager.dialogMessage(boxKey), book.getTitle());
				if (ZLDialogManager.getInstance().getQuestionBox(boxKey, message,
					ZLDialogManager.YES_BUTTON, ZLDialogManager.NO_BUTTON) == 0) {
					//collectionModel().removeAllMarks();
					new BookList().removeFileName(book.getFileName());
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
				}
				return true;
			}*/
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
	}
	
	public void synchronizeModel() {
		if (myCollection.synchronize()) {
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
	
	
	private class CollectionModel extends ZLTextTreeModelImpl {

		
		private final BookCollection myCollection;
		private ZLImageMap myImageMap = new ZLImageMap();
		private final HashMap/*<ZLTextParagraph,BookDescription>*/ myParagraphToBook = new HashMap();
		private final HashMap/*<BookDescription,Integer>*/ myBookToParagraph = new HashMap();

		public CollectionModel(BookCollection collection) {
			super();
			myCollection = collection;
			//TODO
			
			String prefix = "";//ZLResource.ourApplicationImageDirectory() + ZLibrary.FileNameDelimiter;
			/*myImageMap.put(DELETE_IMAGE_ID, new ZLFileImage("image/png", prefix + "tree-remove.png", 0));
			myImageMap[BOOK_INFO_IMAGE_ID] = new ZLFileImage("image/png", prefix + "tree-bookinfo.png", 0);
			myImageMap[AUTHOR_INFO_IMAGE_ID] = new ZLFileImage("image/png", prefix + "tree-authorinfo.png", 0);
			myImageMap[SERIES_ORDER_IMAGE_ID] = new ZLFileImage("image/png", prefix + "tree-order.png", 0);
			*/
		}

		public BookDescription bookByParagraphNumber(int num) {
			if ((num < 0) || ((int)getParagraphsNumber() <= num)) {
				return null;
			}
			return (BookDescription)myParagraphToBook.get(getParagraph(num));
		}
		
		public int paragraphNumberByBook(BookDescription book) {
			Integer num = (Integer)myBookToParagraph.get(book);
			return (num != null) ? num.intValue() : 0;
		}

		public void update() {
			myParagraphToBook.clear();
			myBookToParagraph.clear();
			for (int i = getParagraphsNumber() - 1; i >= 0; --i) {
				removeParagraph(i);
			}
			build();
		}

		private void build() {
			final ArrayList/*<Author>*/ authors = myCollection.authors();
			String currentSequenceName = "";
			ZLTextTreeParagraph sequenceParagraph;
			for (int i = 0; i < authors.size(); i++) {
				Author it = (Author)authors.get(i);
				final ArrayList/*<BookDescription>*/ books = myCollection.books(it);
				if (!books.isEmpty()) {
					currentSequenceName = "";
					sequenceParagraph = null;
                    //todo 
					ZLTextTreeParagraph authorParagraph = createParagraph(null);
					insertText(FBTextKind.LIBRARY_AUTHOR_ENTRY, it.getDisplayName());
					//insertImage(AUTHOR_INFO_IMAGE_ID);
					for (int j = 0; j < books.size(); j++) {
						BookDescription jt = (BookDescription)books.get(j);
						final String sequenceName = jt.getSequenceName();
						if (sequenceName.length() == 0) {
							currentSequenceName = "";
							sequenceParagraph = null;
						} else if (sequenceName != currentSequenceName) {
							currentSequenceName = sequenceName;
							sequenceParagraph = createParagraph(authorParagraph);
							insertText(FBTextKind.LIBRARY_BOOK_ENTRY, sequenceName);
							//insertImage(SERIES_ORDER_IMAGE_ID);
						}
						ZLTextTreeParagraph bookParagraph = createParagraph(
							(sequenceParagraph == null) ? authorParagraph : sequenceParagraph
						);
						insertText(FBTextKind.LIBRARY_BOOK_ENTRY, jt.getTitle());
						insertImage(BOOK_INFO_IMAGE_ID);
						if (myCollection.isBookExternal(jt)) {
							insertImage(DELETE_IMAGE_ID);
						}
						myParagraphToBook.put(bookParagraph, jt);
						myBookToParagraph.put(jt, getParagraphsNumber() - 1);
					}
				}
			}
		}

		private void insertText(byte kind, String text) {
			addControl(kind, true);
			addText(text.toCharArray());
		}
		
		private void insertImage(String id) {
			addFixedHSpace((short)1);
			addImage(id, myImageMap, (short)0);
		}
	};
}
