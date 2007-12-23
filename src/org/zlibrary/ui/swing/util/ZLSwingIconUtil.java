package org.zlibrary.ui.swing.util;

import java.net.URL;
import javax.swing.ImageIcon;

public abstract class ZLSwingIconUtil {
	public static ImageIcon getIcon(String fileName) {
		URL iconURL = ZLSwingIconUtil.class.getClassLoader().getResource(fileName);
		return (iconURL != null) ? new ImageIcon(iconURL) : new ImageIcon(fileName);
	}
}
