package org.geometerplus.android.fbreader;
import org.geometerplus.fbreader.fbreader.FBReader;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.fbreader.Paths;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.os.Environment;


public class OpenFileActivity extends ListActivity {
	private enum DISPLAYMODE{ ABSOLUTE, RELATIVE; }

	private final DISPLAYMODE displayMode = DISPLAYMODE.RELATIVE;
	private ArrayList<String> directoryEntries = new ArrayList<String>();
	private File currentDirectory = new File("/");
	private  ZLStringOption LastPath= new ZLStringOption("Files", "LastPath",Paths.BooksDirectoryOption.getValue());


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (LastPath.getValue().indexOf(Environment.getExternalStorageDirectory().getName())== -1)
			LastPath.setValue(Paths.BooksDirectoryOption.getValue());
		browseToRoot();
	}

	/**
	 * This function browses to the
	 * root-directory of the file-system.
	 */
	private void browseToRoot() {
		 browseTo(new File(LastPath.getValue()));
}

	/**
	 * This function browses up one level
	 * according to the field: currentDirectory
	 */
	private void upOneLevel(){
		 if(this.currentDirectory.getParent() != null)
			  this.browseTo(this.currentDirectory.getParentFile());
	}

	private void browseTo(final File aDirectory){
		 if (aDirectory.isDirectory()){
			this.currentDirectory = aDirectory;
			LastPath.setValue(aDirectory.toString());
			fill(aDirectory.listFiles());
		}else{
			final FBReader fbreader = (FBReader)ZLApplication.Instance();
			fbreader.openFile(new ZLPhysicalFile(new File(aDirectory.getPath())));
			fbreader.repaintView();
			fbreader.showBookTextView();
			finish();
		 }
	}
	private boolean CheckEnds(String fileName){
		return
		fileName.endsWith(".fb2.zip") ||
		fileName.endsWith(".fb2") ||
		fileName.endsWith(".epub") ||
		fileName.endsWith(".mobi") ||
		fileName.endsWith(".prc");

	}
	private void fill(File[] files) {
		this.directoryEntries.clear();

		if(this.currentDirectory.getParent() != null)
			this.directoryEntries.add("..");

		switch(this.displayMode){
			case ABSOLUTE:
				for (File file : files){
					this.directoryEntries.add(file.getPath());
				}
				break;
			  case RELATIVE: // On relative Mode, we have to add the current-path to the beginning
				for (File file : files){
					String fileName = file.getName();
					if(file.isDirectory()){
						this.directoryEntries.add("/"+fileName);
					}
					else if( CheckEnds(fileName)){
						this.directoryEntries.add(fileName);
					}
				 }
				break;
		 }
		 Collections.sort(this.directoryEntries);

		 ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
				R.layout.row, this.directoryEntries);

		 this.setListAdapter(directoryList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String selectedFileString = this.directoryEntries.get(position);
		if (selectedFileString.equals(".")) {
			// Refresh
			this.browseTo(this.currentDirectory);
		 } else if(selectedFileString.equals("..")){
			this.upOneLevel();
		 } else {
			File clickedFile = null;
			switch(this.displayMode){
				case RELATIVE:
					String nabspath=this.currentDirectory.getAbsolutePath();
					if(selectedFileString.charAt(0)!='/')
						nabspath=nabspath+"/";
					clickedFile = new File(nabspath + selectedFileString);
					break;
				case ABSOLUTE:
					clickedFile = new File(selectedFileString);
					break;
			}
			if(clickedFile != null)
				this.browseTo(clickedFile);
		 }
	}


}
