package org.fbreader.fbreader;

import java.util.ArrayList;
import java.util.HashMap;

import org.fbreader.bookmodel.FBTextKind;
import org.fbreader.collection.BookCollection;
import org.fbreader.description.Author;
import org.fbreader.description.BookDescription;
import org.zlibrary.core.image.ZLImageMap;
import org.zlibrary.core.view.ZLPaintContext;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextTreeParagraph;
import org.zlibrary.text.model.impl.ZLModelFactory;
import org.zlibrary.text.model.impl.ZLTextTreeModelImpl;
import org.zlibrary.text.view.ZLTextView;

public class CollectionView extends FBView {
	static private final String LIBRARY = "Library";
	static private final String DELETE_IMAGE_ID = "delete";
	static private final String BOOK_INFO_IMAGE_ID = "bookInfo";
	static private final String AUTHOR_INFO_IMAGE_ID = "authorInfo";
	static private final String SERIES_ORDER_IMAGE_ID = "seriesOrder";

	
	private BookCollection myCollection;
	private boolean myTreeStateIsFrozen;
	private boolean myUpdateModel;

	public CollectionView(FBReader reader, ZLPaintContext context) {
		super(reader, context);
		myUpdateModel = true;
		setModel(new CollectionModel(myCollection));
		//setModel(new CollectionModel(myCollection), LIBRARY);
	}
	
	public String getCaption() {
		return LIBRARY;
	}

	/*public boolean onStylusPress(int x, int y) {
		ZLTextElementArea imageArea = elementByCoordinates(x, y);
		if ((imageArea != null) && (imageArea.Kind == ZLTextElement.IMAGE_ELEMENT)) {
			ZLTextWordCursor cursor = startCursor();
			cursor.moveToParagraph(imageArea.ParagraphNumber);
			cursor.moveTo(imageArea.TextElementNumber, 0);
			final ZLTextElement element = cursor.getElement();
			if (element.getKind() != ZLTextElement.IMAGE_ELEMENT) {
				return false;
			}
			final ZLTextImageElement imageElement = (ZLTextImageElement)element;

			BookDescription book = collectionModel().bookByParagraphNumber(imageArea.ParagraphNumber);
			if (book == null) {
				return false;
			}

			if (imageElement.getId() == BOOK_INFO_IMAGE_ID) {
				if (new BookInfoDialog(myCollection, book.getFileName()).dialog().run()) {
					myCollection.rebuild(false);
					myUpdateModel = true;
					selectBook(book);
					repaintView();
				}
				return true;
			} else if (imageElement.id() == DELETE_IMAGE_ID) {
				ZLResourceKey boxKey = new ZLResourceKey("removeBookBox");
				final String message =
					ZLStringUtil.printf(ZLDialogManager.dialogMessage(boxKey), book.getTitle());
				if (ZLDialogManager.instance().questionBox(boxKey, message,
					ZLDialogManager.YES_BUTTON, ZLDialogManager.NO_BUTTON) == 0) {
					collectionModel().removeAllMarks();
					new BookList().removeFileName(book.getFileName());
					ZLTextTreeParagraph paragraph = (ZLTextTreeParagraph)collectionModel()[imageArea.ParagraphNumber];
					ZLTextTreeParagraph parent = paragraph.getParent();
					if (parent.children().size() == 1) {
						collectionModel().removeParagraph(imageArea.ParagraphNumber);
						collectionModel().removeParagraph(imageArea.ParagraphNumber - 1);
					} else {
						collectionModel().removeParagraph(imageArea.ParagraphNumber);
					}
					if (collectionModel().getParagraphsNumber() == 0) {
						setStartCursor(0);
					}
					rebuildPaintInfo(true);
					repaintView();
				}
				return true;
			}
			return false;
		}

		int index = paragraphIndexByCoordinate(y);
		if (index == -1) {
			return false;
		}

		BookDescription book = collectionModel().bookByParagraphNumber(index);
		if (book != null) {
			fbreader().openBook(book);
			fbreader().showBookTextView();
			return true;
		}

		return false;
	}*/

	public void paint() {
		if (myUpdateModel) {
			ZLTextModel oldModel = getModel();
			setModel(null);
			//setModel(0, LIBRARY);
			((CollectionModel)oldModel).update();
			setModel(oldModel);
			//setModel(oldModel, LIBRARY);
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
			//setModel(0, LIBRARY);
			((CollectionModel)oldModel).update();
			setModel(oldModel);
			//setModel(oldModel, LIBRARY);
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

		
		private BookCollection myCollection = new BookCollection();
		private ZLImageMap myImageMap;//= new ImageMap();
		private HashMap/*<ZLTextParagraph,BookDescription>*/ myParagraphToBook;
		private HashMap/*<BookDescription,Integer>*/ myBookToParagraph;

		public CollectionModel(BookCollection collection) {
			super();
			myCollection = collection;
			//TODO
			String prefix = "";//ZLResource.ourApplicationImageDirectory() + ZLibrary.FileNameDelimiter;
			//myImageMap.put(DELETE_IMAGE_ID, new ZLFileImage("image/png", prefix + "tree-remove.png", 0));
			//myImageMap[BOOK_INFO_IMAGE_ID] = new ZLFileImage("image/png", prefix + "tree-bookinfo.png", 0);
			//myImageMap[AUTHOR_INFO_IMAGE_ID] = new ZLFileImage("image/png", prefix + "tree-authorinfo.png", 0);
			//myImageMap[SERIES_ORDER_IMAGE_ID] = new ZLFileImage("image/png", prefix + "tree-order.png", 0);

		}

		public BookDescription bookByParagraphNumber(int num) {
			if ((num < 0) || ((int)getParagraphsNumber() <= num)) {
				return null;
			}
			return (BookDescription)myParagraphToBook.get(getParagraph(num));
		}
		
		public int paragraphNumberByBook(BookDescription book) {
			Integer num = (Integer)myBookToParagraph.get(book);
			return (num != null) ? num : 0;
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
					ZLTextTreeParagraph authorParagraph = ZLModelFactory.createTreeParagraph(null);
					insertText(FBTextKind.LIBRARY_AUTHOR_ENTRY, it.getDisplayName());
					insertImage(AUTHOR_INFO_IMAGE_ID);
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
							insertImage(SERIES_ORDER_IMAGE_ID);
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
			//addFixedHSpace(1);
			addImage(id, myImageMap, (short)0);
		}
	};
}
