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

import java.util.HashMap;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.geometerplus.zlibrary.core.dialogs.*;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.ui.swing.util.ZLSwingIconUtil;

class ZLSwingSelectionDialog extends ZLSelectionDialog {
	private static final String OPTION_GROUP_NAME = "OpenFileDialog";
	private static final HashMap ourIcons = new HashMap();
	private static final String ourIconDirectory = "icons/filetree/";

	private final JDialog myDialog;
	private final JTextField myStateLine = new JTextField();
	private final JList myList = new JList();
	private OKAction myOKAction;
	
	private final ZLIntegerRangeOption myWidthOption;
	private	final ZLIntegerRangeOption myHeightOption;
	
	private boolean myReturnValue = false;
	private final Runnable myActionOnAccept;
	
	ZLSwingSelectionDialog(JFrame frame, String caption, ZLTreeHandler myHandler, Runnable actionOnAccept) {
		super(myHandler);
		myWidthOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTION_GROUP_NAME, "Width", 10, 2000, 400);
		myHeightOption = new ZLIntegerRangeOption(ZLOption.LOOK_AND_FEEL_CATEGORY, OPTION_GROUP_NAME, "Height", 10, 2000, 300);
		myActionOnAccept = actionOnAccept;
		myDialog = new JDialog(frame);
		myDialog.setTitle(caption);
		update();
	}

	@Override
	protected void exitDialog() {
		myWidthOption.setValue(myDialog.getWidth());
		myHeightOption.setValue(myDialog.getHeight());
		myDialog.setVisible(false);
	}

	public void run() {
		myDialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				myWidthOption.setValue(myDialog.getWidth());
				myHeightOption.setValue(myDialog.getHeight());
			}
		});
		myDialog.setLayout(new BorderLayout());
		myStateLine.setEditable(!handler().isOpenHandler());
		myStateLine.setEnabled(!handler().isOpenHandler());
		myDialog.add(myStateLine, BorderLayout.NORTH);
	
		myList.setCellRenderer(new CellRenderer());
		JScrollPane scrollPane = new JScrollPane(myList);
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());		
		myDialog.add(scrollPane, BorderLayout.CENTER);
		
		myList.addListSelectionListener(new SelectionListener());
		myList.addKeyListener(new MyKeyAdapter());
		myList.addMouseListener(new MyMouseListener());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button1 = new JButton();
		myOKAction = new OKAction(ZLSwingDialogManager.createButtonText(ZLSwingDialogManager.OK_BUTTON));
		button1.setAction(myOKAction);
		JButton button2 = new JButton();
		CancelAction cancelAction = new CancelAction(ZLSwingDialogManager.createButtonText(ZLSwingDialogManager.CANCEL_BUTTON));
		button2.setAction(cancelAction);
		if (button1.getPreferredSize().width < button2.getPreferredSize().width) {
			button1.setPreferredSize(button2.getPreferredSize());
		} else {
			button2.setPreferredSize(button1.getPreferredSize());
		}
		buttonPanel.add(button1);
		buttonPanel.add(button2);
		myDialog.add(buttonPanel, BorderLayout.SOUTH);
		
		KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		myDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
		myDialog.getRootPane().getActionMap().put("ESCAPE", cancelAction);
		
		myDialog.pack();
		myList.requestFocusInWindow();	
		myDialog.setSize(myWidthOption.getValue(), myHeightOption.getValue());
		myDialog.setLocationRelativeTo(myDialog.getParent());
		myDialog.setModal(true);
		myDialog.setVisible(true);
		
		if (myReturnValue) {
			myActionOnAccept.run();
		}
	}

	@Override
	protected void selectItem(int index) {
		myList.setSelectedIndex(index);
		myList.ensureIndexIsVisible(index);
	}

	@Override
	protected void updateList() {
		myList.setListData(handler().subnodes().toArray());
	}

	@Override
	protected void updateStateLine() {
		myStateLine.setText(handler().stateDisplayName());
	}
	
	private static ImageIcon getIcon(ZLTreeNode node) {
		final String pixmapName = node.PixmapName;
		ImageIcon icon = (ImageIcon)ourIcons.get(pixmapName);
		if (icon == null) {
			icon = ZLSwingIconUtil.getIcon(ourIconDirectory + pixmapName + ".png");
			ourIcons.put(pixmapName, icon);
		}
		return icon;
	}
	
	private void changeFolder(int index) {
		ZLTreeNode node = (ZLTreeNode)handler().subnodes().get(index);
		myReturnValue = !node.IsFolder;
		runNode(node);
	}
	
	private class MyMouseListener extends MouseInputAdapter {
		public void mouseClicked(MouseEvent e) {
			if (!((System.getProperty("os.name").startsWith("Windows")) && (e.getClickCount() == 1))) {
				changeFolder(myList.locationToIndex(e.getPoint()));
			}		
		}	
	}
	
	private class MyKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				changeFolder(myList.getSelectedIndex());
			}	
		}
	}
	
	private class CancelAction extends AbstractAction {
		public CancelAction(String text) {
			putValue(NAME, text);
		}
		
		public void actionPerformed(ActionEvent e) {
			exitDialog();
		}		
	}
	
	private class OKAction extends AbstractAction {
		public OKAction(String text) {
			putValue(NAME, text);
			setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent e) {
			myReturnValue = true;
			runNode((ZLTreeNode)handler().subnodes().get(myList.getSelectedIndex())); 
		}
	}
	
	private class SelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			int index = myList.getSelectedIndex();
			myOKAction.setEnabled(index != -1 && !((ZLTreeNode)handler().subnodes().get(index)).IsFolder);
		}		
	}
	
	private static class CellRenderer extends JLabel implements ListCellRenderer {
		
		public Component getListCellRendererComponent(
			JList list,
			Object value,            // value to display
			int index,               // cell index
			boolean isSelected,      // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
		{
			final ZLTreeNode node = (ZLTreeNode)value;
			setText(node.DisplayName);
			setIcon(ZLSwingSelectionDialog.getIcon(node));
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);			
			return this;
		}
	}
	
}
