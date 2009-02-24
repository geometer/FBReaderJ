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

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.resources.ZLResource;

public class ZLSwingOptionsDialog extends ZLOptionsDialog {
	private final boolean myShowApplyButton;
	private final JDialog myDialog;
	private final JTabbedPane myTabbedPane = new JTabbedPane();
	private String mySelectedTabKey;
	private final HashMap<String, JPanel> myPanelToKeyMap = new HashMap<String, JPanel>();
	
	private final ZLIntegerRangeOption myWidthOption;
	private	final ZLIntegerRangeOption myHeightOption;
	
	protected ZLSwingOptionsDialog(JFrame frame, ZLResource resource, Runnable exitAction, Runnable applyAction, boolean showApplyButton) {
		super(resource, exitAction, applyAction);
		myDialog = new JDialog(frame);
		myDialog.setTitle(getCaption());
		myShowApplyButton = showApplyButton;
		final String optionGroupName = resource.Name;
		myWidthOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, optionGroupName, "Width", 10, 2000, 485);
		myHeightOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, optionGroupName, "Height", 10, 2000, 332);
	}

	@Override
	public ZLDialogContent createTab(String key) {
		ZLSwingDialogContent tab = new ZLSwingDialogContent(getTabResource(key));
		JPanel contentPanel = tab.getContentPanel();
		JPanel panel = new JPanel(new BorderLayout()); 
		panel.add(contentPanel, BorderLayout.PAGE_START);
		myPanelToKeyMap.put(tab.getKey(), panel);
		myTabbedPane.addTab(tab.getDisplayName(), panel);
		myTabs.add(tab);
		return tab;
	}

	@Override
	protected String getSelectedTabKey() {
		return mySelectedTabKey;
	}

	@Override
	protected void runInternal() {
		myDialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				myWidthOption.setValue(myDialog.getWidth());
				myHeightOption.setValue(myDialog.getHeight());
			}
		});
		myDialog.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton okButton = new JButton(new OptionsDialogAction(ZLSwingDialogManager.OK_BUTTON, true, true));
		Action cancelAction = new OptionsDialogAction(ZLSwingDialogManager.CANCEL_BUTTON, false, true);
		JButton cancelButton = new JButton(cancelAction);
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		Dimension preferredSize = okButton.getPreferredSize();
		Dimension size = cancelButton.getPreferredSize();
		if (preferredSize.width < size.width) {
			preferredSize = size;
		}
		if (myShowApplyButton) {
			JButton applyButton = new JButton(new OptionsDialogAction(ZLSwingDialogManager.APPLY_BUTTON, true, false));
			buttonPanel.add(applyButton);
			size = applyButton.getPreferredSize();
			if (preferredSize.width < size.width) {
				preferredSize = size;
			}
			applyButton.setPreferredSize(preferredSize);
		}
		okButton.setPreferredSize(preferredSize);
		cancelButton.setPreferredSize(preferredSize);
		myDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		myTabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));				
		myDialog.getContentPane().add(myTabbedPane, BorderLayout.CENTER);
		myTabbedPane.addChangeListener(new MyChangeListener());
		if (myPanelToKeyMap.get(mySelectedTabKey) != null) {
			myTabbedPane.setSelectedComponent(myPanelToKeyMap.get(mySelectedTabKey));
		}	
		
		okButton.requestFocusInWindow();
		
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		myDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		myDialog.getRootPane().getActionMap().put("ESCAPE", cancelAction);
			
		myDialog.pack();
		
		myDialog.setSize(myWidthOption.getValue(), myHeightOption.getValue());
		myDialog.setLocationRelativeTo(myDialog.getParent());
		myDialog.setModal(true);
		myDialog.setVisible(true);
	}

	protected void selectTab(String key) {
		mySelectedTabKey = key;
	}

	private void exitDialog() {
		myWidthOption.setValue(myDialog.getWidth());
		myHeightOption.setValue(myDialog.getHeight());
		myDialog.dispose();
		if (myExitAction != null) {
			myExitAction.run();
		}
	}
	
	private class MyChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			Component component = myTabbedPane.getSelectedComponent();
			for (Map.Entry entry : myPanelToKeyMap.entrySet()) {
				if (entry.getValue().equals(component)) {
					mySelectedTabKey = (String) entry.getKey();
					break;
				}
			}
		}		
	}
	
	private class OptionsDialogAction extends AbstractAction {
		private final boolean myDoAccept;
		private final boolean myDoExit;
	
		public OptionsDialogAction(String name, boolean doAccept, boolean doExit) {
			putValue(NAME, ZLSwingDialogManager.createButtonText(name));
			myDoAccept = doAccept;
			myDoExit = doExit;
		}
		
		public void actionPerformed(ActionEvent e) {
			if (myDoAccept) {
				accept();
			}
			if (myDoExit) {
				exitDialog();
			}
		}
	}
}
