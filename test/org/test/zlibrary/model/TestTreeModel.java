package org.test.zlibrary.model;

import org.zlibrary.text.model.ZLTextTreeModel;
import org.zlibrary.text.model.ZLTextTreeParagraph;
import org.zlibrary.text.model.impl.ZLModelFactory;

import junit.framework.TestCase;

public class TestTreeModel extends TestCase {
    private ZLModelFactory factory = new ZLModelFactory(); 
	
    public void testCreateParagraph() {
    	ZLTextTreeModel model = factory.createZLTextTreeModel();
    	ZLTextTreeParagraph parant = factory.createTreeParagraph();
    	ZLTextTreeParagraph paragraph = model.createParagraph(parant);
        assertTrue(paragraph.getParent() == parant);
        assertTrue(model.getParagraphsNumber() == 1);
    }
    
		/*
    public void testRemoveParagraph() {
    	ZLTextTreeModel model = factory.createZLTextTreeModel();
    	ZLTextTreeParagraph parant = factory.createTreeParagraph();
    	ZLTextTreeParagraph paragraph = model.createParagraph(parant);
    	model.removeParagraphInternal(0);
    	assertTrue(model.getParagraphsNumber() == 0);
    }
		*/
    
}
