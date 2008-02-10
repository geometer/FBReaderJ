package org.test.zlibrary.description;

import junit.framework.TestCase;

import org.fbreader.description.Author;
import org.fbreader.description.BookDescription;
import org.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;

public class TestDescriptionBook extends TestCase {
	private final String filename = "testfb2book.fb2";
	private final String filenameZip = "testbookZip.zip";	
	
	public void setUp() {
		new ZLSwingLibrary().init();
		new ZLOwnXMLProcessorFactory();
	}


	private String myDirectory = "test\\data\\fb2\\filesystem";

	public void testAuthor() {
		BookDescription bd = BookDescription.getDescription(myDirectory+"\\"+filename);
		assertTrue(bd != null);
		Author author = bd.getAuthor();
		assertTrue(author != null);
		assertEquals(author.getDisplayName(), "Unknown Author");
		//System.out.println(author.getSortKey());
		//System.out.println(author.isSingle());
		assertEquals(author.isSingle(), true);	
	}
	
	public void testLanguageEncoding() {
		BookDescription bd = BookDescription.getDescription(myDirectory+"\\"+filename);
		assertTrue(bd != null);
        //System.out.println(bd.getEncoding());	
        //System.out.println(bd.getFileName());	
        //System.out.println(bd.getLanguage());	
        //System.out.println(bd.getNumberInSequence());	
        //System.out.println(bd.getSequenceName());	
	    System.out.println(bd.getTitle());	
	    

		
	}
	
	public void test() {
		BookDescription bd = BookDescription.getDescription(myDirectory+"\\"+filename);
		assertTrue(bd != null);
        
		//System.out.println(bd.getEncoding());	
        //System.out.println(bd.getFileName());	
        //System.out.println(bd.getLanguage());	
        //System.out.println(bd.getNumberInSequence());	
        //System.out.println(bd.getSequenceName());	
	}

}
