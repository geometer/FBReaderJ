package org.zlibrary.ui.swing.dialogs;

import javax.swing.*;

import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.core.resources.ZLResource;
import org.zlibrary.core.runnable.ZLRunnable;
import org.zlibrary.ui.swing.application.ZLSwingApplicationWindow;

public class ZLSwingDialogManager extends ZLDialogManager {
	private ZLSwingApplicationWindow myApplicationWindow;
	
	public ZLSwingDialogManager() {
	}
	
	public boolean runSelectionDialog(String key, ZLTreeHandler handler) {
		return new ZLSwingSelectionDialog(myApplicationWindow.getFrame(), getDialogTitle(key), handler).run();
	}

	public void showErrorBox(String key, String message) {
		JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
	}

	public void showInformationBox(String key, String message) {
		JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
	}

	public int showQuestionBox(String key, String message, String button0, String button1, String button2) {
		Object [] options = new Object [3];
		int size = 0;
		if (button0 != null) {
			options[size++] = getButtonText(button0).replace("&", "");
		}
		if (button1 != null) {
			options[size++] = getButtonText(button1).replace("&", "");
		}
		if (button2 != null) {
			options[size++] = getButtonText(button2).replace("&", "");
		}
		int optionType;
		Object [] opt = new Object[size];
		System.arraycopy(options, 0, opt, 0, size);
		switch (size) {
		case 3:
			optionType = JOptionPane.YES_NO_CANCEL_OPTION;
			break;
		case 2:
			optionType = JOptionPane.YES_NO_OPTION;
			break;
		case 1:
			optionType = JOptionPane.YES_OPTION;
			break;
		default:
			return 0;
		}
		
		return JOptionPane.showOptionDialog(myApplicationWindow.getFrame(), message,
			    getDialogTitle(key), optionType, JOptionPane.QUESTION_MESSAGE,
			    null, opt, opt[0]);
	}

	public ZLSwingApplicationWindow createApplicationWindow(ZLApplication application) {
		myApplicationWindow = new ZLSwingApplicationWindow(application);
		return myApplicationWindow;
	}

	static JButton createButton(String key) {
		String text = getButtonText(key).replace("&", "");
		return new JButton(text);
	}

	public ZLDialog createDialog(String key, ZLRunnable applyAction, boolean showApplyButton) {
		return new ZLSwingDialog(myApplicationWindow.getFrame(), getResource().getResource(key));
	}

	public ZLOptionsDialog createOptionsDialog(String key, ZLRunnable applyAction, boolean showApplyButton) {
		return new ZLSwingOptionsDialog(myApplicationWindow.getFrame(), getResource().getResource(key),
				applyAction, showApplyButton);
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
