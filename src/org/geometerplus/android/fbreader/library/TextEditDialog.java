/*
 * Copyright (C) 2010 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class TextEditDialog extends Dialog{
	private Context myContext;
	private EditText myEditText;
	
	public TextEditDialog(Context context, String title, String okName, String cancelName) {
		super(context);
		myContext = context;
		setTitle(title);
		
	   	LinearLayout ll = new LinearLayout(myContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        myEditText = new EditText(myContext);
        ll.addView(myEditText);

        LinearLayout btnLayout = new LinearLayout(myContext);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        btnLayout.setGravity(Gravity.CENTER);
        
        Button ok = new Button(myContext);
        ok.setText(okName);
        ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				okAction();
			}
		});
        
        Button cancel = new Button(myContext);
        cancel.setText(cancelName);
        cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				cancelAction();
			}
		});

        btnLayout.addView(ok);
        btnLayout.addView(cancel);
        ll.addView(btnLayout);
        setContentView(ll);
	}

	public void setText(String text){
		myEditText.setText(text);
	}
	
	public String getText(){
		return myEditText.getText().toString().trim();
	}
	
	protected void cancelAction(){
		cancel();
	}
	
	protected void okAction()  {
		dismiss();
	}
	
}

class ToastMaker{
	private static ZLResource myResource = ZLResource.resource("libraryView");

	public static void MakeToast(Context context, String messageKey){
		Toast.makeText(context,	myResource.getResource(messageKey).getValue(),
				Toast.LENGTH_SHORT).show();
	}
}


class RenameDialog extends TextEditDialog{
	private ZLFile myFile;
	private Context myContext;
	
	private static ZLResource myResource = ZLResource.resource("libraryView");
	
	RenameDialog(Context context, ZLFile file) {
		super(context,
				myResource.getResource("renameFile").getValue(),
				ZLResource.resource("dialog").getResource("button").getResource("rename").getValue(),
				ZLResource.resource("dialog").getResource("button").getResource("cancel").getValue()
				);
		myContext = context;
		myFile = file;
		
		if(myFile.isDirectory()){
			setText(myFile.getShortName());
		}else{
			String extension = myFile.getExtension(); 
			String name = myFile.getShortName();
			name = name.substring(0, name.indexOf(extension) - 1);
			setText(name);
		}
	}

	protected void cancelAction(){
		cancel();
	}
	
	public void okAction()  {
		String newName = getText();
		if (newName == ""){
			dismiss();
			return;
		}
		if (!myFile.isDirectory())
			newName += "." + myFile.getExtension();
		if (newName.startsWith(".")){
			ToastMaker.MakeToast(myContext, "messFileIncorrect");
		} else if (consistInParent(myFile, newName)){
			if(myFile.getPhysicalFile().rename(newName)){
				((Activity) myContext).startActivityForResult(
						new Intent(myContext, FileManager.class)
							.putExtra(FileManager.FILE_MANAGER_PATH, myFile.getParent().getPath())
							.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
						FileManager.CHILD_LIST_REQUEST
				);
				dismiss();
			} else {
				ToastMaker.MakeToast(myContext, "messRenameFailed");
			}
		}else{
			ToastMaker.MakeToast(myContext, "messFileExists");
		}
	}
	
	private boolean consistInParent(ZLFile file, String newName){
		ZLFile parent;
		if (file.isDirectory()){					// FIXME  ZLFile.createFileByPath("/sdcard/Books").getParent()  --> exception. see TODO.fileManager
			String path = file.getPath();
			path = path.substring(0, path.lastIndexOf("/"));
			parent = ZLFile.createFileByPath(path);
			Log.v(FileManager.LOG, "parent: " + path);
		}else{
			parent = file.getParent();
		}
		for(ZLFile f : parent.children()){
			if (f.getShortName().equals(newName))
				return false;
		}
		return true;
	}
}


class MkDirDialog extends TextEditDialog{
	private Context myContext;
	private String myPath;
	private String myInsertPath;
	
	private static ZLResource myResource = ZLResource.resource("libraryView");
	
	MkDirDialog(Context context, String curPath, String insertPath) {
		super(context,
				myResource.getResource("newDirectory").getValue(),
				ZLResource.resource("dialog").getResource("button").getResource("create").getValue(),
				ZLResource.resource("dialog").getResource("button").getResource("cancel").getValue()
				);
		myContext = context;
		myPath = curPath;
		myInsertPath = insertPath;
		setText(myResource.getResource("newDirectory").getValue());
	}

	protected void cancelAction(){
		cancel();
	}
	
	public void okAction()  {
		String newName = getText();
		ZLFile file = ZLFile.createFileByPath(myPath);
		if (newName == ""){
			dismiss();
			return;
		}else if (!file.isDirectory()){
			
			Toast.makeText(myContext, 
					myResource.getResource("messNotDir").getValue(),
					Toast.LENGTH_SHORT).show();
			dismiss();
			return;
		}
			
		if (consistInParent(file, newName)){
			ZLFile.createFileByPath(myPath + "/" + newName).mkdir();
			((Activity) myContext).startActivityForResult(
					new Intent(myContext, FileManager.class)
						.putExtra(FileManager.FILE_MANAGER_PATH, myPath)
						.putExtra(FileManager.FILE_MANAGER_INSERT_MODE, myInsertPath)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
					FileManager.CHILD_LIST_REQUEST
			);
			dismiss();
		}else{
			ToastMaker.MakeToast(myContext, "messFileExists");
		}
	}
	
	private boolean consistInParent(ZLFile file, String newDir){
		for(ZLFile f : file.children()){
			if (f.getShortName().equals(newDir))
				return false;
		}
		return true;
	}
}
