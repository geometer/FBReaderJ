/*
 * Copyright (C) 2010-2011 Geometer Plus <contact@geometerplus.com>
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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

class ToastMaker{
	private static ZLResource myResource = ZLResource.resource("libraryView");

	public static void MakeToast(Context context, String messageKey){
		Toast.makeText(context,	myResource.getResource(messageKey).getValue(),
				Toast.LENGTH_SHORT).show();
	}
}

public class TextEditDialog extends Dialog{
	private Context myContext;
	private EditText myEditText;
	
	public TextEditDialog(Context context, String title, String okName, String cancelName) {
		super(context);
		myContext = context;
		setTitle(title);
		
	   	LinearLayout linearLayout = new LinearLayout(myContext);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        myEditText = new EditText(myContext);
        linearLayout.addView(myEditText);
        
        LinearLayout btnLayout = new LinearLayout(myContext);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setLayoutParams(new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT,
    			LayoutParams.FILL_PARENT, 1f));
        btnLayout.setGravity(Gravity.FILL_HORIZONTAL);
        
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

    	LinearLayout.LayoutParams llppp = new LinearLayout.LayoutParams(
    			LayoutParams.FILL_PARENT,
    			LayoutParams.FILL_PARENT, 0.5f);
        btnLayout.addView(ok, llppp);
        btnLayout.addView(cancel, llppp);
        linearLayout.addView(btnLayout);
        setContentView(linearLayout);
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


/*
class RenameDialog extends TextEditDialog{
	private ZLFile myFile;
	private Context myContext;
	
	private static ZLResource myResource = ZLResource.resource("libraryView");
	
	RenameDialog(Context context, ZLFile file) {
		super(context,
				myResource.getResource("rename").getValue(),
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
		} else if (!FileUtil.contain(newName, FileUtil.getParent(myFile))){
			if(myFile.getPhysicalFile().rename(newName)){
				((Activity) myContext).startActivityForResult(
						new Intent(myContext, FileManager.class)
							.putExtra(FileManager.FILE_MANAGER_PATH, FileUtil.getParent(myFile).getPath())
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
}
*/

class MkDirDialog extends TextEditDialog{
	private Context myContext;
	private String myPath;
	
	private static ZLResource myResource = ZLResource.resource("libraryView");
	
	MkDirDialog(Context context, String curPath) {
		super(context,
				myResource.getResource("newDirectory").getValue(),
				ZLResource.resource("dialog").getResource("button").getResource("ok").getValue(),
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
		ZLFile file = ZLFile.createFileByPath(myPath);
		if (newName == ""){
			dismiss();
			return;
		}else if (!file.isDirectory()){
			ToastMaker.MakeToast(myContext, "messDirectoryIntoArchive");
			dismiss();
			return;
		}
			
		if (!FileUtil.contain(newName, file)){
			ZLFile.createFileByPath(myPath + "/" + newName).mkdir();
			FileUtil.refreshActivity((Activity)myContext, myPath);
			dismiss();
		}else{
			ToastMaker.MakeToast(myContext, "messFileExists");
		}
	}
}

class RadioButtonDialog{
	protected Context myContext;
	private String myTitle;
	private String[] myItems;
	private int mySelectedItem;
	
	public RadioButtonDialog(Context context, String title, String[] items, int selectedItem){
		myContext = context;
		myTitle = title;
		myItems = items;
		mySelectedItem = selectedItem;
	}

	protected void itemSelected(DialogInterface dialog, int item){
		dialog.dismiss();
	}
	
	public void show(){
		AlertDialog.Builder builder = new AlertDialog.Builder(myContext);
		builder.setTitle(myTitle);
		builder.setSingleChoiceItems(myItems, mySelectedItem, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	itemSelected(dialog, item);
		    }
		});
		builder.create().show();
	}
}

class SortingDialog extends RadioButtonDialog{
	private static String myTitle = ZLResource.resource("libraryView").getResource("sortingBox").getResource("title").getValue();
	private static String[] myItems = SortType.toStringArray();
	private String myPath;

	public SortingDialog(Context content, String path) {
		super(content, myTitle, myItems, SortTypeConf.getValue());
		myPath = path;
	}

	@Override
	protected void itemSelected(DialogInterface dialog, int item){
		super.itemSelected(dialog, item);
		if (SortTypeConf.getValue() != item){
			SortTypeConf.setValue(item);
			LibraryCommon.SortTypeInstance = SortType.values()[item];
			
			if (LibraryCommon.ViewTypeInstance == ViewType.SIMPLE){
				FileManager.launchActivity(myContext, myPath);
			} else if (LibraryCommon.ViewTypeInstance == ViewType.SKETCH) {
				SketchGalleryActivity.launchActivity(myContext, myPath);
			}
		}
	}
}

abstract class AbstractViewChangeDialog extends RadioButtonDialog{
	private static String myTitle = ZLResource.resource("libraryView").getResource("viewBox").getResource("title").getValue();
	private static String[] myItems = ViewType.toStringArray();
	
	public AbstractViewChangeDialog(Context content) {
		super(content, myTitle, myItems, ViewTypeConf.getValue());
		myContext = content;
	}	
}

class ViewChangeDialog extends AbstractViewChangeDialog{
	private String myPath;
	
	public ViewChangeDialog(Context content, String path) {
		super(content);
		myPath = path;
	}

	@Override
	protected void itemSelected(DialogInterface dialog, int item){
		super.itemSelected(dialog, item);
		if (ViewTypeConf.getValue() != item){
			ViewTypeConf.setValue(item);
			LibraryCommon.ViewTypeInstance = ViewType.values()[item];
			if (LibraryCommon.ViewTypeInstance == ViewType.SIMPLE){
				FileManager.launchActivity(myContext, myPath);
			} else if (LibraryCommon.ViewTypeInstance == ViewType.SKETCH){
				SketchGalleryActivity.launchActivity(myContext, myPath);
			}
			((Activity)myContext).finish();			// TODO ??? Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
	}
}

class LibraryTreeChanger extends AbstractViewChangeDialog{
	private String mySelectedBook;
	private String myTreePathString;

	public LibraryTreeChanger(Context content, String selectedBook, String treePathString) {
		super(content);
		mySelectedBook = selectedBook;
		myTreePathString = treePathString;
	}

	@Override
	protected void itemSelected(DialogInterface dialog, int item){
		super.itemSelected(dialog, item);
		if (ViewTypeConf.getValue() != item){
			ViewTypeConf.setValue(item);
			LibraryCommon.ViewTypeInstance = ViewType.values()[item];		// TODO think about
			if (LibraryCommon.ViewTypeInstance == ViewType.SIMPLE){
				LibraryTreeActivity.launchActivity((Activity) myContext, mySelectedBook, myTreePathString);
			} else if (LibraryCommon.ViewTypeInstance == ViewType.SKETCH){
				GalleryLibraryTreeActivity.launchActivity((Activity) myContext, mySelectedBook, myTreePathString);	
			}
			((Activity)myContext).finish();			// TODO ??? Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
	}
}


class LibraryTopLevelViewChanger extends AbstractViewChangeDialog{
	private String mySelectedBookPath;

	public LibraryTopLevelViewChanger(Context content, String selectedBookPath) {
		super(content);
		mySelectedBookPath = selectedBookPath;
	}

	@Override
	protected void itemSelected(DialogInterface dialog, int item){
		super.itemSelected(dialog, item);
		if (ViewTypeConf.getValue() != item){
			ViewTypeConf.setValue(item);
			LibraryCommon.ViewTypeInstance = ViewType.values()[item];
			if (LibraryCommon.ViewTypeInstance == ViewType.SIMPLE){
				LibraryTopLevelActivity.launchActivity((Activity) myContext, mySelectedBookPath);
			} else if (LibraryCommon.ViewTypeInstance == ViewType.SKETCH){
				GalleryLibraryTopLevelActivity.launchActivity((Activity) myContext, mySelectedBookPath);
			}
			((Activity)myContext).finish();			// TODO ??? Intent.FLAG_ACTIVITY_CLEAR_TOP
		}
	}
}

