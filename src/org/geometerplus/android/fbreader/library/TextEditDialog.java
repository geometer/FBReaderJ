package org.geometerplus.android.fbreader.library;

import org.geometerplus.android.fbreader.library.FileManager.FileItem;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
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
	private FileItem myItem;
	private Context myContext;
	private static ZLResource myResource = ZLResource.resource("libraryView");
	
	RenameDialog(Context context, FileItem item) {
		super(context,
				myResource.getResource("renameFile").getValue(),
				myResource.getResource("renameBtn").getValue(),
				myResource.getResource("cancelBtn").getValue()
				);
		myContext = context;
		myItem = item;
		setText(myItem.getFile().getShortName());
	}

	protected void cancelAction(){
		cancel();
	}
	
	public void okAction()  {
		String newName = getText();
		ZLFile file = myItem.getFile();
		if (file.getShortName().equals(newName)){
			dismiss();
		}
		
		if (correctName(file, newName)){
			file.getPhysicalFile().rename(newName);
			myItem.update();
			((ListActivity)myContext).getListView().invalidateViews();
			dismiss();
		}else{
			Toast.makeText(myContext, 
					myResource.getResource("fileExists").getValue(),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private boolean correctName(ZLFile file, String newName){
		for(ZLFile f : file.getParent().children()){
			if (f.getShortName().equals(newName))
				return false;
		}
		return true;
	}
}

