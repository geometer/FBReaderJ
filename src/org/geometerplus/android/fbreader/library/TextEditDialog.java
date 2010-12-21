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

import org.geometerplus.android.fbreader.library.FileManager.FileItem;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
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
		return myEditText.getText().toString();
	}
	
	protected void cancelAction(){
		cancel();
	}
	
	protected void okAction()  {
		dismiss();
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
		setText(myFile.getShortName());
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
		
		if (correctName(myFile, newName)){
			myFile.getPhysicalFile().rename(newName);
			((Activity) myContext).startActivityForResult(
					new Intent(myContext, FileManager.class)
						.putExtra(FileManager.FILE_MANAGER_PATH, myFile.getParent().getPath())
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
					FileManager.CHILD_LIST_REQUEST
			);
			dismiss();
		}else{
			Toast.makeText(myContext, 
					myResource.getResource("fileExists").getValue(),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private boolean correctName(ZLFile file, String newName){
		if (file.isDirectory())
			return true;			// ZLFile.createFileByPath("/sdcard/Books").getParent()  --> exception. see TODO.fileManager
		for(ZLFile f : file.getParent().children()){
			if (f.getShortName().equals(newName))
				return false;
		}
		return true;
	}
}


class MkDirDialog extends TextEditDialog{
	private Context myContext;
	private String myPath;
	
	private static ZLResource myResource = ZLResource.resource("libraryView");
	
	MkDirDialog(Context context, String curPath) {
		super(context,
				myResource.getResource("newDirectory").getValue(),
				ZLResource.resource("dialog").getResource("button").getResource("create").getValue(),
				ZLResource.resource("dialog").getResource("button").getResource("cancel").getValue()
				);
		myContext = context;
		myPath = curPath;
		setText(myResource.getResource("newDirectory").getValue());
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
		
		if (correctName(ZLFile.createFileByPath(myPath), newName)){
			ZLFile.createFileByPath(myPath + "/" + newName).mkdir();
			((Activity) myContext).startActivityForResult(
					new Intent(myContext, FileManager.class)
						.putExtra(FileManager.FILE_MANAGER_PATH, myPath)
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
					FileManager.CHILD_LIST_REQUEST
			);
			dismiss();
		}else{
			Toast.makeText(myContext, 
					myResource.getResource("fileExists").getValue(),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private boolean correctName(ZLFile file, String newDir){
		for(ZLFile f : file.children()){
			if (f.getShortName().equals(newDir))
				return false;
		}
		return true;
	}
	
}


