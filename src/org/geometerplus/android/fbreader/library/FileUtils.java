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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Activity;
import android.widget.Button;
import android.widget.ImageButton;

public class FileUtils {
	
	public static Button getOkBtn(Activity parent, int id){
		return getBtn(parent, id, "ok");
	}

	public static Button getCancelBtn(Activity parent, int id){
		return getBtn(parent, id, "cancel");
	}
	
	public static Button getBtn(Activity parent, int id, String keyName){
		Button btn = (Button)parent.findViewById(id);
		btn.setText(ZLResource.resource("dialog").getResource("button").getResource(keyName).getValue());
		return btn;
	}

	public static ImageButton getImgBtn(Activity parent, int id, int resId){
		ImageButton imgBtn = (ImageButton) parent.findViewById(id);
		imgBtn.setImageResource(resId);
		return imgBtn;
	}
	
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
}
