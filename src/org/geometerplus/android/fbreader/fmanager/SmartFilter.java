package org.geometerplus.android.fbreader.fmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.util.Log;

public class SmartFilter implements Runnable {

	// **************************************************************************//
	// Enums //
	// **************************************************************************//

	// **************************************************************************//
	// Members //
	// **************************************************************************//
	private Activity myParent;
	private Runnable myAction;
	private List<String> myOrders;

	private File myFile;
	private String myNewTypes;

	private String myCurPath = "";
	private List<String> myCurFiles = new ArrayList<String>();
	private String myCurTypes = "";
	
	// **************************************************************************//
	// Constructors //
	// **************************************************************************//
	public SmartFilter(Activity parent, List<String> orders, Runnable action) {
		myParent = parent;
		myOrders = orders;
		myAction = action;
	}
	
	// **************************************************************************//
	// Getters //
	// **************************************************************************//

	// **************************************************************************//
	// Setters //
	// **************************************************************************//

	// **************************************************************************//
	// Publics //
	// **************************************************************************//
	public void setPreferences(File file, String types) {
		myFile = file;
		myNewTypes = types;
		((ReturnRes) myAction).refresh();
	}

	public void run() {
		getOrders();
	}
	
	
	// **************************************************************************//
	// Abstracts //
	// **************************************************************************//

	// **************************************************************************//
	// Protected //
	// **************************************************************************//

	// **************************************************************************//
	// Privates //
	// **************************************************************************//
	private void getOrders() {
		try {
			if (myCurPath.equals(myFile.getPath())
					&& compareTypes(myCurTypes, myNewTypes))
				getOrderInCurrentDir();
			else
				getOrderInNewDir();
		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.getMessage());
		}
	}

	private void getOrderInCurrentDir() {
		String delTypes = "";
		for (String curType : myCurTypes.split("[\\s]+")) {
			if (myNewTypes.indexOf(curType.trim()) < 0)
				delTypes += curType + " ";
		}

		List<String> newListFiles = new ArrayList<String>();
		for (String file : myCurFiles) {
			if (!Thread.currentThread().isInterrupted()) {

				// В реальных условиях здесь
				// код будет красивее
				// timeKiller() - работаем c файлом
				// FIXME delete later!!!
//				timeKiller(); // TODO DELETE LATER

				String fileName = null;
				if (!condition(file, delTypes))
					fileName = file;
				if (!Thread.currentThread().isInterrupted() && fileName != null) {
					myOrders.add(fileName);
					newListFiles.add(fileName);
				}
				myParent.runOnUiThread(myAction);
			}
			myCurFiles = newListFiles;
			myParent.runOnUiThread(myAction);
		}
	}

	private void getOrderInNewDir() {
		myCurPath = myFile.getPath();
		for (File file : myFile.listFiles()) {
			if (!Thread.currentThread().isInterrupted()) {

				// В реальных условиях здесь
				// код будет красивее
				// timeKiller() - работаем c файлом
				// FIXME delete later!!!
//				timeKiller(); // TODO DELETE LATER

				String fileName = null;
				if (file.isDirectory())
					fileName = file.getName();
				else if (condition(file, myNewTypes))
					fileName = file.getName();
				if (!Thread.currentThread().isInterrupted() && fileName != null) {
					myOrders.add(fileName);
					myCurFiles.add(fileName);
				}

				myParent.runOnUiThread(myAction);
			}
		}
		myParent.runOnUiThread(myAction);
	}

	private boolean condition(File file, String types) {
		return condition(file.getName(), types);
	}

	private boolean condition(String val, String types) {
		if (types.equals(""))
			return true;
		for (String type : types.split("[\\s]+")) {
			if (val.endsWith(type))
				return true;
		}
		return false;
	}

	private boolean compareTypes(String curTypes, String newTypes) {
		if (newTypes.equals(""))
			return false;
		if (newTypes.length() > curTypes.length())
			return false;
		for (String newType : newTypes.split("[\\s]+")) {
			if (curTypes.indexOf(newType.trim()) < 0)
				return false;
		}
		return true;
	}

	// TODO delete later
	private void timeKiller() {
		for (int i = 0; i < 200; i++) {
			System.out.println(i);
		}
	}
	
	// **************************************************************************//
	// Public Statics //
	// **************************************************************************//

	// **************************************************************************//
	// Private Statics //
	// **************************************************************************//

	// **************************************************************************//
	// Internal Classes //
	// **************************************************************************//

}
