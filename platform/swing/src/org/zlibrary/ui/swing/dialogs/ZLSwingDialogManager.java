package org.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import org.zlibrary.core.dialogs.*;
import org.zlibrary.core.application.ZLApplication;
import org.zlibrary.ui.swing.application.ZLSwingApplicationWindow;

public class ZLSwingDialogManager extends ZLDialogManager {
	private ZLSwingApplicationWindow myApplicationWindow;
	
	public ZLSwingDialogManager() {
	}
	
	public void runSelectionDialog(String key, ZLTreeHandler handler, Runnable actionOnAccept) {
		new ZLSwingSelectionDialog(myApplicationWindow.getFrame(), getDialogTitle(key), handler, actionOnAccept).run();
	}

	public void showErrorBox(String key, String message) {
		JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
	}

	public void showInformationBox(String key, String message) {
		JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
	}

	public void showQuestionBox(String key, String message, String button0, Runnable action0, String button1, Runnable action1, String button2, Runnable action2) {
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
			return;
		}
		
		Runnable action = null;
		switch (JOptionPane.showOptionDialog(myApplicationWindow.getFrame(), message,
			    getDialogTitle(key), optionType, JOptionPane.QUESTION_MESSAGE,
			    null, opt, opt[0])) {
			case 0:
				action = action0;
				break;
			case 1:
				action = action1;
				break;
			case 2:
				action = action2;
				break;
		}
		if (action != null) {
			action.run();
		}
	}

	public ZLSwingApplicationWindow createApplicationWindow(ZLApplication application) {
		myApplicationWindow = new ZLSwingApplicationWindow(application);
		return myApplicationWindow;
	}

	static String createButtonText(String key) {
		return getButtonText(key).replace("&", "");
	}

	public ZLDialog createDialog(String key) {
		return new ZLSwingDialog(myApplicationWindow.getFrame(), getResource().getResource(key));
	}

	public ZLOptionsDialog createOptionsDialog(String key, Runnable applyAction, boolean showApplyButton) {
		return new ZLSwingOptionsDialog(myApplicationWindow.getFrame(), getResource().getResource(key),
				applyAction, showApplyButton);
	}

	public void wait(String key, Runnable runnable) {
		Thread t = new Thread(runnable);		
		t.start();
		try {
			t.join(500);
		} catch (InterruptedException e) {}
		if (t.isAlive()) {
			JDialog dialog = new JDialog();
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel(getWaitMessageText(key)), BorderLayout.CENTER);
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			dialog.add(panel);
			if (myApplicationWindow != null) {
				dialog.setLocationRelativeTo(myApplicationWindow.getFrame());
			} else {
				dialog.setLocation(300, 300);
			}
			dialog.pack();
			dialog.setModal(true);
			dialog.addWindowListener(new MyWindowListener(dialog, t));		
			dialog.setVisible(true);
		}
	}
	
	private static class MyWindowListener extends WindowAdapter {
		private final JDialog myDialog;
		private final Thread myThread;
		
		public MyWindowListener(JDialog dialog, Thread thread) {
			myDialog = dialog;
			myThread = thread;
		}

		public void windowOpened(WindowEvent e) {
			try {
				myThread.join();
			} catch (InterruptedException ex) {}
			myDialog.dispose();
		}		
	}
}

/*
 * public:
	void createApplicationWindow(ZLApplication *application) const;

	shared_ptr<ZLDialog> createDialog(const ZLResourceKey &key) const;
	shared_ptr<ZLOptionsDialog> createOptionsDialog(const ZLResourceKey &id, shared_ptr<Runnable> applyAction, bool showApplyButton) const;
	void informationBox(const ZLResourceKey &key, const std::string &message) const;
	void errorBox(const ZLResourceKey &key, const std::string &message) const;
	int questionBox(const ZLResourceKey &key, const std::string &message, const ZLResourceKey &button0, const ZLResourceKey &button1, const ZLResourceKey &button2) const;
	bool selectionDialog(const ZLResourceKey &key, ZLTreeHandler &handler) const;
	void wait(const ZLResourceKey &key, Runnable &runnable) const;

	bool isClipboardSupported(ClipboardType type) const;
	void setClipboardText(const std::string &text, ClipboardType type) const;

 */
