package org.geometerplus.android.fbreader.library;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.widget.Toast;

public class FileUtil {
	
	public static void copyFile(String src, String targetDirectory) throws IOException {
		if (ZLFile.createFileByPath(targetDirectory).isDirectory()){
			ZLFile srcFile = ZLFile.createFileByPath(src);
			String srcName = srcFile.getShortName();
			
			for (ZLFile f : ZLFile.createFileByPath(targetDirectory).children()){
				if (f.getShortName().equals(srcName))
					throw new IOException ("file is already exists!");
			}
			
			String target = targetDirectory + "/" + srcFile.getShortName(); 
			//Log.v(FileManager.LOG, "src: " + src + " targer " + target);
			
			FileChannel ic = new FileInputStream(src).getChannel();
			FileChannel oc = new FileOutputStream(target).getChannel();
			ic.transferTo(0, ic.size(), oc);
			ic.close();
			oc.close();
		}else{
			// TODO
			throw new IOException("Is not a directory!");
		}
	}
	
	public static void moveFile(String src, String targetDirectory) throws IOException {
		copyFile(src, targetDirectory);
		ZLFile.createFileByPath(src).getPhysicalFile().delete();
	}
	

}
