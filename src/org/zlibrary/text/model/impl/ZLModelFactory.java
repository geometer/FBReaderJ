package org.zlibrary.text.model.impl;

import org.zlibrary.text.model.ZLTextPlainModel;
import org.zlibrary.text.model.ZLTextTreeModel;

public class ZLModelFactory {
	public ZLTextPlainModel createPlainModel() {
		return new ZLTextPlainModelImpl();
	} 
	
	public ZLTextTreeModel createZLTextTreeModel() {
		return new ZLTextTreeModelImpl();
	}
}
