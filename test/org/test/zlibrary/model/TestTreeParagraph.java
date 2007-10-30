package org.test.zlibrary.model;

import org.zlibrary.text.model.ZLTextTreeParagraph;
import org.zlibrary.text.model.impl.ZLModelFactory;

import junit.framework.TestCase;

public class TestTreeParagraph extends TestCase {
	private ZLModelFactory factory = new ZLModelFactory(); 
	
	public void testCreateParagraph() {
		 ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		 ZLTextTreeParagraph paragraph2 = factory.createTreeParagraph(paragraph);
		 assertTrue(paragraph.getChildren().contains(paragraph2));
		 assertTrue(paragraph.getDepth() == 0);
		 assertTrue(paragraph2.getDepth() == 1);
		 assertTrue(paragraph2.getParent() == paragraph);
	}
}
