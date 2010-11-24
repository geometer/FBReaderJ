package org.geometerplus.android.fbreader.fmanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

	// **************************************************************************//
	// Enums //
	// **************************************************************************//

	// **************************************************************************//
	// Members //
	// **************************************************************************//

	// **************************************************************************//
	// Constructors //
	// **************************************************************************//

	// **************************************************************************//
	// Getters //
	// **************************************************************************//

	// **************************************************************************//
	// Setters //
	// **************************************************************************//

	// **************************************************************************//
	// Publics //
	// **************************************************************************//

	// **************************************************************************//
	// Abstracts //
	// **************************************************************************//

	// **************************************************************************//
	// Protected //
	// **************************************************************************//

	// **************************************************************************//
	// Privates //
	// **************************************************************************//

	// **************************************************************************//
	// Public Statics //
	// **************************************************************************//

	// unstable - not testing
	public static void copyRecursive(String src, String path)
			throws IOException {
		// TODO
		File file = new File(src);
		if (file.isDirectory()) {
			path += src;
			for (String f : file.list()) {
				copyRecursive(f, path);
				copyFile(f, path + f);
			}
		}
	}

	// stable
	public static void copyFile(String src, String target) throws IOException {
		FileChannel ic = new FileInputStream(src).getChannel();
		FileChannel oc = new FileOutputStream(target).getChannel();
		ic.transferTo(0, ic.size(), oc);
		ic.close();
		oc.close();
	}

	public static List<String> getFiltredList(File[] files, String types){
		List<String> resultList = new ArrayList<String>();
		for(File file : files){
			if (file.isDirectory())
				resultList.add(file.getName());
			else if (condition(file, types))
				resultList.add(file.getName());
		}
		return resultList;
	}

	// **************************************************************************//
	// Private Statics //
	// **************************************************************************//
	private static boolean condition(File file, String types) {
		return condition(file.getName(), types);
	}
	
	private static boolean condition(String val, String types) {
		for (String type : types.split("[\\s]+")) {
			if (val.endsWith(type))
				return true;
		}
		return false;
	}
	
	// **************************************************************************//
	// Internal Classes //
	// **************************************************************************//

}
