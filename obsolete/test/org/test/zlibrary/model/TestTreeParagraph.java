package org.test.zlibrary.model;

import java.util.List;

import org.geometerplus.zlibrary.text.model.ZLTextTreeParagraph;
import org.geometerplus.zlibrary.text.model.impl.ZLModelFactory;

import junit.framework.TestCase;

public class TestTreeParagraph extends TestCase {
	private ZLModelFactory factory = new ZLModelFactory(); 
	
	public void testCreateParagraph() {
		 ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		 ZLTextTreeParagraph paragraph2 = factory.createTreeParagraph(paragraph);
		 //assertTrue(paragraph.children().contains(paragraph2));
		 assertTrue(paragraph.getDepth() == 0);
		 assertTrue(paragraph2.getDepth() == 1);
		 assertTrue(paragraph2.getParent() == paragraph);
	}

	public void testIsOpen() {
		ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		assertTrue(!paragraph.isOpen());
		paragraph.open(true);
		assertTrue(paragraph.isOpen());
		paragraph.open(false);
		assertTrue(!paragraph.isOpen());		
	}
	
	public void testOpenTree() {
		ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		ZLTextTreeParagraph paragraph2 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph4 = factory.createTreeParagraph(paragraph2);
	    paragraph4.openTree();
	    assertTrue(paragraph2.isOpen());
	    assertTrue(paragraph.isOpen());
	}
	
	public void testGetDepth() {
		ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		ZLTextTreeParagraph paragraph2 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph3 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph4 = factory.createTreeParagraph(paragraph2);
	    assertTrue(paragraph.getDepth() == 0);
	    assertTrue(paragraph2.getDepth() == 1);
	    assertTrue(paragraph3.getDepth() == 1);
	    assertTrue(paragraph4.getDepth() == 2);
	}
	
	public void testGetParent() {
		ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		ZLTextTreeParagraph paragraph2 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph3 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph4 = factory.createTreeParagraph(paragraph2);
		assertTrue(paragraph.getParent() == null);
		assertTrue(paragraph2.getParent() == paragraph);
		assertTrue(paragraph3.getParent() == paragraph);
		assertTrue(paragraph4.getParent() == paragraph2);	
	}
	
	public void testGetChildren() {
		ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		ZLTextTreeParagraph paragraph2 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph3 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph4 = factory.createTreeParagraph(paragraph2);
		assertTrue(paragraph.hasChildren());
		assertTrue(paragraph2.getParent() == paragraph);
		assertTrue(paragraph3.getParent() == paragraph);
		assertTrue(paragraph4.getParent() == paragraph2);	
		assertTrue(paragraph2.hasChildren());
		assertFalse(paragraph3.hasChildren());		
		assertFalse(paragraph2.isLastChild());		
		assertTrue(paragraph3.isLastChild());		
		assertTrue(paragraph4.isLastChild());		
	}
	/*
	 *	
	void removeFromParent();
	 * 
	 * */

	public void testGetFullSize() {
		ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		ZLTextTreeParagraph paragraph2 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph3 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph4 = factory.createTreeParagraph(paragraph2);

		assertTrue(paragraph.getFullSize() == 4);
		assertTrue(paragraph2.getFullSize() == 2);
		assertTrue(paragraph3.getFullSize() == 1);
		assertTrue(paragraph4.getFullSize() == 1);
	}
	
	public void testRemoveFromParent() {
		ZLTextTreeParagraph paragraph = factory.createTreeParagraph();
		ZLTextTreeParagraph paragraph2 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph3 = factory.createTreeParagraph(paragraph);
		ZLTextTreeParagraph paragraph4 = factory.createTreeParagraph(paragraph2);
		paragraph4.removeFromParent();
		assertFalse(paragraph3.hasChildren());
		paragraph2.removeFromParent();
		assertTrue(paragraph.hasChildren());
	}
}
