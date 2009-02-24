/*
 * Copyright (C) 2007-2009 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.ui.swing.application.ZLSwingApplicationWindow;

public class ZLSwingDialogManager extends ZLDialogManager {
	private ZLSwingApplicationWindow myApplicationWindow;
	
	public ZLSwingDialogManager() {
	}
	
	public void runSelectionDialog(String key, ZLTreeHandler handler, Runnable actionOnAccept) {
		new ZLSwingSelectionDialog(myApplicationWindow.getFrame(), getDialogTitle(key), handler, actionOnAccept).run();
	}

	public void showErrorBox(String key, String message, Runnable action) {
		JOptionPane.showMessageDialog(myApplicationWindow.getFrame(), message, getDialogTitle(key), 1, null);
		if (action != null) {
			action.run();
		}
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

	public ZLOptionsDialog createOptionsDialog(String key, Runnable exitAction, Runnable applyAction, boolean showApplyButton) {
		return new ZLSwingOptionsDialog(myApplicationWindow.getFrame(), getResource().getResource(key), exitAction, applyAction, showApplyButton);
	}

	public void wait(String key, Runnable runnable) {
		Thread t = new Thread(runnable);		
		t.start();
		try {
			t.join(500);
		} catch (InterruptedException e) {}
		if (t.isAlive()) {
			JDialog dialog = new JDialog();
			dialog.setUndecorated(true);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(new JLabel(getWaitMessageText(key)), BorderLayout.CENTER);
			panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
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
	
	bool isClipboardSupported(ClipboardType type) const;
	void setClipboardText(const std::string &text, ClipboardType type) const;

 */
