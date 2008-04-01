package org.zlibrary.ui.swing.dialogs;

import org.zlibrary.core.dialogs.ZLOptionEntry;
import org.zlibrary.core.dialogs.ZLOptionView;

public abstract class ZLSwingOptionView extends ZLOptionView {
	protected final ZLSwingDialogContent myTab;

	public ZLSwingOptionView(String name, ZLOptionEntry option, ZLSwingDialogContent tab) {
		super(name, option);
		myTab = tab;
	}
}
