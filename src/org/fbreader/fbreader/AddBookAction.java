package org.fbreader.fbreader;

import org.zlibrary.core.dialogs.ZLDialogManager;

class AddBookAction extends FBAction {
	AddBookAction(FBReader fbreader) {
		super(fbreader);
	}

	public boolean isVisible() {
		return fbreader().getMode() != FBReader.ViewMode.FOOTNOTE;
	}

	public void run() {
		ZLDialogManager.getInstance().runSelectionDialog("addFileDialog", null);
		/*
		FBFileHandler handler;
		if (ZLDialogManager::instance().selectionDialog(ZLResourceKey("addFileDialog"), handler)) {
			BookDescriptionPtr description = handler.description();
			if (!description.isNull() && fbreader().runBookInfoDialog(description->fileName())) {
				BookList().addFileName(description->fileName());
				fbreader().setMode(FBReader::BOOK_TEXT_MODE);
			}
		}
		*/
	}
}
