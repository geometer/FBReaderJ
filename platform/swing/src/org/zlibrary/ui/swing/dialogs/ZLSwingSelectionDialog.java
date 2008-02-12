package org.zlibrary.ui.swing.dialogs;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import org.zlibrary.core.dialogs.ZLSelectionDialog;
import org.zlibrary.core.dialogs.ZLTreeHandler;
import org.zlibrary.core.dialogs.ZLTreeNode;
import org.zlibrary.core.dialogs.ZLTreeOpenHandler;
import org.zlibrary.ui.swing.util.ZLSwingIconUtil;

class ZLSwingSelectionDialog extends ZLSelectionDialog{
	private JDialog myJDialog;
	private final String myCaption;
	private JTextField myStateLine = new JTextField();
	private JList myList = new JList();
	private OKAction myOKAction;
	
	protected ZLSwingSelectionDialog(JFrame frame, String caption, ZLTreeHandler myHandler) {
		super(myHandler);
		myJDialog = new JDialog(frame);
		myCaption = caption;
		update();
	}

	@Override
	protected void exitDialog() {
		// TODO Auto-generated method stub
		myJDialog.dispose();
	}

	@Override
	public boolean run() {
		myJDialog.setLayout(new BorderLayout());
		myJDialog.setTitle(myCaption);
		myStateLine.setEditable(false);
		myJDialog.add(myStateLine, BorderLayout.NORTH);
	
		myList.setCellRenderer(new CellRenderer());
		JScrollPane scrollPane = new JScrollPane(myList);
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		myJDialog.add(scrollPane, BorderLayout.CENTER);

		myList.addListSelectionListener(new SelectionListener());
		myList.addKeyListener(new MyKeyAdapter());
		myList.addMouseListener(new MyMouseListener());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button1 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.OK_BUTTON);
		buttonPanel.add(button1);
		myOKAction = new OKAction(button1.getText());
		button1.setAction(myOKAction);
		JButton button2 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.CANCEL_BUTTON);
		buttonPanel.add(button2);
		button2.setAction(new CancelAction (button2.getText()));
		myJDialog.add(buttonPanel, BorderLayout.SOUTH);
		myJDialog.pack();
		myJDialog.setSize(600, 400);
		myJDialog.setLocationRelativeTo(myJDialog.getParent());
		myJDialog.setVisible(true);
		
		
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void selectItem(int index) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateList() {
		myList.setListData(handler().subnodes().toArray());
	}

	@Override
	protected void updateStateLine() {
		// TODO Auto-generated method stub
		myStateLine.setText(handler().stateDisplayName());
	}
	
	private void accept(ZLTreeNode node) {
		if (handler().isOpenHandler()) {
			((ZLTreeOpenHandler) handler()).accept(node);
		}
	}
	
	private void changeFolder(int index) {
		ZLTreeNode node = (ZLTreeNode) handler().subnodes().get(index);
        if (node.isFolder()) {
       	 handler().changeFolder(node);
            update();
        } else {
        	accept(node);
        }
	}
	
	private class MyMouseListener extends MouseInputAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.getClickCount() == 2) {
	             changeFolder(myList.locationToIndex(e.getPoint()));
	        }
		}
		
	}
	
	private class MyKeyAdapter extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			// TODO Auto-generated method stub
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
//			 TODO Auto-generated method stub
		}
		
	}
	
	private class SelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			int index = myList.getSelectedIndex();
			myOKAction.setEnabled(index != -1 && !((ZLTreeNode) handler().subnodes().get(index)).isFolder());
		}		
	}
	
	
	private static class CellRenderer extends JLabel implements ListCellRenderer {
		final static ImageIcon icon = ZLSwingIconUtil.getIcon("icons/filetree/unknown.png");
		final static ImageIcon upicon = ZLSwingIconUtil.getIcon("icons/filetree/upfolder.png");
		final static ImageIcon folderIcon = ZLSwingIconUtil.getIcon("icons/filetree/folder.png");

		public Component getListCellRendererComponent(
			JList list,
			Object value,            // value to display
			int index,               // cell index
			boolean isSelected,      // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
		{
			String s = ((ZLTreeNode) value).displayName();
			setText(s);
			setIcon(s.equals("..") ? upicon : (((ZLTreeNode) value).isFolder() ? folderIcon : icon));
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	private JDialog myJDialog;

	ZLSwingSelectionDialog(JFrame frame) {
		myJDialog = new JDialog(frame, true);
	}

	private static class CellRenderer extends JLabel implements ListCellRenderer {
		final static ImageIcon icon = ZLSwingIconUtil.getIcon("icons/filetree/unknown.png");
		final static ImageIcon upicon = ZLSwingIconUtil.getIcon("icons/filetree/upfolder.png");

		public Component getListCellRendererComponent(
			JList list,
			Object value,            // value to display
			int index,               // cell index
			boolean isSelected,      // is the cell selected
			boolean cellHasFocus)    // the list and the cell have the focus
		{
			String s = value.toString();
			setText(s);
			setIcon(s.equals("..") ? upicon : icon);
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

	void run() {
		myJDialog.setLayout(new BorderLayout());

		JTextField textLine = new JTextField();
		textLine.setText("directory name");
		textLine.setEditable(false);
		myJDialog.add(textLine, BorderLayout.NORTH);

		String[] books = new String[] { "..", "Book 0", "Book 1", "Book 2", "Book 3", "Book 4", "Book 5" };
		JList list = new JList(books);
		list.setCellRenderer(new CellRenderer());
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		myJDialog.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton button1 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.OK_BUTTON);
		buttonPanel.add(button1);
		JButton button2 = ZLSwingDialogManager.createButton(ZLSwingDialogManager.CANCEL_BUTTON);
		buttonPanel.add(button2);
		myJDialog.add(buttonPanel, BorderLayout.SOUTH);
		myJDialog.pack();
		myJDialog.setSize(600, 400);
		myJDialog.setLocationRelativeTo(myJDialog.getParent());
		myJDialog.setVisible(true);
	}
	*/
}
