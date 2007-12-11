package org.zlibrary.ui.swing.dialogs;

import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.dialogs.ZLDialogManager;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.resources.ZLResourceKey;
import org.zlibrary.ui.swing.application.ZLSwingApplicationWindow;

import javax.swing.JOptionPane;

public class ZLSwingDialogManager extends ZLDialogManager {
	private ZLSwingApplicationWindow myApplicationWindow;
	
	private ZLSwingDialogManager() {
	}
	
	public static void createInstance() {
		ourInstance = new ZLSwingDialogManager();
	}
	
	@Override
	public void errorBox(ZLResourceKey key, String message) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(null, message, dialogTitle(key), 1, null);
	}

	@Override
	public void informationBox(ZLResourceKey key, String message) {
		// TODO Auto-generated method stub
		JOptionPane.showMessageDialog(null, message, dialogTitle(key), 1, null);
	}

	@Override
	public void createApplicationWindow(ZLApplication application) {
		myApplicationWindow = new ZLSwingApplicationWindow(application);
	}

}

/*
 * public:
	void createApplicationWindow(ZLApplication *application) const;

	shared_ptr<ZLDialog> createDialog(const ZLResourceKey &key) const;
	shared_ptr<ZLOptionsDialog> createOptionsDialog(const ZLResourceKey &id, shared_ptr<ZLRunnable> applyAction, bool showApplyButton) const;
	void informationBox(const ZLResourceKey &key, const std::string &message) const;
	void errorBox(const ZLResourceKey &key, const std::string &message) const;
	int questionBox(const ZLResourceKey &key, const std::string &message, const ZLResourceKey &button0, const ZLResourceKey &button1, const ZLResourceKey &button2) const;
	bool selectionDialog(const ZLResourceKey &key, ZLTreeHandler &handler) const;
	void wait(const ZLResourceKey &key, ZLRunnable &runnable) const;

	bool isClipboardSupported(ClipboardType type) const;
	void setClipboardText(const std::string &text, ClipboardType type) const;

 */
