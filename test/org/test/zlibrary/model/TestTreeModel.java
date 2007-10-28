package org.test.zlibrary.model;

import org.zlibrary.model.ZLTextTreeModel;
import org.zlibrary.model.impl.ZLModelFactory;

import junit.framework.TestCase;

public class TestTreeModel extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
	
    public void testCreateParagraph() {
    	ZLTextTreeModel model = factory.createZLTextTreeModel();
    	model.createParagraph(factory.createTreeParagraph());
    }
}
