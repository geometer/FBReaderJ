package org.test.fbreader.formats.fb2;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.bookmodel.ContentsModel;
import org.fbreader.formats.fb2.FB2Reader;
import org.zlibrary.core.xml.ZLXMLProcessorFactory;
import org.zlibrary.core.xml.sax.ZLSaxXMLProcessorFactory;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.ZLTextTreeParagraph;
import org.zlibrary.text.model.entry.ZLTextEntry;

import junit.framework.TestCase;

import java.io.*;

public class TestFB2Reader extends TestCase {
	
	private ZLXMLProcessorFactory myFactory = new ZLSaxXMLProcessorFactory();
	
	private String myDirectory = "test/data/fb2/";
	
	private boolean compareFiles(String f1, String f2) {
		int i1;
		int i2;
		InputStreamReader fis1 = null;
		InputStreamReader fis2 = null;
		try {
			fis1 = new InputStreamReader(new FileInputStream(f1), "utf8");
			fis2 = new InputStreamReader(new FileInputStream(f2), "utf8");
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		try{
			try{			
				i1 = fis1.read();
				i2 = fis2.read();
				while ((i1 == i2) && (i1 != -1)){
					i1 = fis1.read();
					i2 = fis2.read();
				}
			} finally {
				fis1.close();
				fis2.close();
			}		
		} catch (IOException e) {
			return false;
		}
		return i1 == i2;
	}
	
	private void writeDumpToFile(ZLTextModel model, String outputFile) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(
					new FileOutputStream(outputFile), "utf8");
			osw.write(model.dump());
			osw.close();
		} catch (Exception e) {
			fail();
		}		
	}
	
	private void writeTextModelDumpToFile(String inputFile, String outputFile) {
		ZLTextModel model = (new FB2Reader()).readBook(inputFile).getBookModel();
		writeDumpToFile(model, outputFile);
	}
	
	private void writeTreeModelDumpToFile(String inputFile, String outputFile) {
		ZLTextModel model = (new FB2Reader()).readBook(inputFile).getContentsModel();
		writeDumpToFile(model, outputFile);
	}
	
	private void doTreeModelTest(String test) {
		String test_result = myDirectory + test + "_act.txt";
		writeTreeModelDumpToFile(myDirectory + test + ".fb2", test_result);
		assertTrue(compareFiles(myDirectory + test + "_exp.txt", test_result));
		(new File(test_result)).delete();
	}
	
	private void doTest(String test) {
		String test_result = myDirectory + test + "_act.txt";
		writeTextModelDumpToFile(myDirectory + test + ".fb2", test_result);
		assertTrue(compareFiles(myDirectory + test + "_exp.txt", test_result));
		(new File(test_result)).delete();
	}
	
	public void testOneParagraph() {
		doTest("test1");
	}
	
	public void testTwoParagraphs() {
		doTest("test2");
	}
	
	public void testControlSup() {
		doTest("test3");
	}
	
	public void testControlSub() {
		doTest("test4");
	}
	
	public void testControlEmphasis() {
		doTest("test5");
	}

	public void testControlStrong() {
		doTest("test6");
	}
	
	public void testControlStrikeThrough() {
		doTest("test7");
	}
	
	public void testControlCode() {
		doTest("test8");
	}
	
	public void testControlVerse() {
		doTest("verse");
	}
	
	public void testControlDate() {
		doTest("date");
	}
	
	public void testControlSubtitle() {
		doTest("subtitle");
	}
	
	public void testControlTextAuthor() {
		doTest("text_author");
	}
	
	public void testEmptyLine() {
		doTest("empty_line");
	}
	
	public void testEmptyLineParagraphKind() {
		FB2Reader reader = new FB2Reader();
		ZLTextModel model = reader.readBook(myDirectory + "empty_line.fb2").getBookModel();
		assertEquals(model.getParagraph(0).getKind(),
				ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
	}
	
	public void testControlCite() {
		doTest("cite");
	}
	
	public void testControlEpigraph() {
		doTest("epigraph");
	}
	
	public void testNotesSimple() {
		BookModel model = (new FB2Reader()).readBook(myDirectory + "simple_notes.fb2");
		assertTrue(model.getFootnotes().containsKey("1"));
		assertNotNull(model.getFootnotes().get("1"));
	}
	
	public void testOneNote() {
		FB2Reader reader = new FB2Reader();
		ZLTextModel model = reader.readBook(myDirectory + "one_note.fb2").getFootnoteModel("1");
		String output = myDirectory + "one_note_act.txt";
		writeDumpToFile(model, output);
		assertTrue(compareFiles(myDirectory + "one_note_exp.txt", output));
		(new File(output)).delete();
	}
	
	public void testStanza() {
		doTest("stanza");
	}
	
	public void testStanzaParagraphKinds() {
		FB2Reader reader = new FB2Reader();
		ZLTextModel model = reader.readBook(myDirectory + "stanza.fb2").getBookModel();
		assertEquals(model.getParagraph(0).getKind(), ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
		assertEquals(model.getParagraph(2).getKind(), ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
		assertEquals(model.getParagraph(1).getKind(), ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	}
	
	public void testAnnotation() {
		doTest("annotation");
	}
	
	public void testAnnotationBeforeBodyParagraph() {
		FB2Reader reader = new FB2Reader();
		ZLTextModel model = reader.readBook(myDirectory + "annotation_before.fb2").getBookModel();
		assertEquals(model.getParagraph(1).getKind(),
				ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
	}
	
	public void testAnnotationBeforeBody() {
		doTest("annotation_before");
	}
	
	public void testEndOfSection() {
		doTest("section");
	}
	
	public void testEndOfSectionParagraph() {		
		FB2Reader reader = new FB2Reader();
		ZLTextModel model = reader.readBook(myDirectory + "section.fb2").getBookModel();
		assertEquals(model.getParagraph(1).getKind(),
				ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
	}
	
	public void testTitle() {
		doTest("title");
	}
	
	public void testPoemTitle() {
		doTest("poem_title");
	}
	
	public void testSectionTitle() {
		doTest("section_title");
	}
	
	public void testFootnote() {
		doTest("footnote");
	}
	
	public void testFootnote1() {
		BookModel model = (new FB2Reader()).readBook(myDirectory + "footnote1.fb2");
		assertEquals("footnote", 
				((ZLTextEntry)model.getParagraphByLink("note1").getEntries().get(1)).getData());
	}
	
	public void testExternalHyperlink() {
		doTest("ext_hyperlink");
	}
	
	public void testTreeParagraph() {
		doTreeModelTest("tree1");
	}
	
	public void testTreeParagraphRef() {
		FB2Reader reader = new FB2Reader();
		ContentsModel model = reader.readBook(myDirectory + "tree1.fb2").getContentsModel();
		assertTrue(model.getReference((ZLTextTreeParagraph) model.getParagraph(0)) == 0);
	}
	
	public void testTreeParagraphTitle() {
		doTreeModelTest("tree2");
	}
	
	public void test2TreeParagraphs() {
		doTreeModelTest("tree2sections");
	}
	
	// big tests
	public void testPoem() {
		doTest("poem");
	}
	
	public void testText1() {
		doTest("karenina");
	}
	
	public void testTextWithNotes() {
		doTest("whiteguard");
	}
	
/*	public void testPnvs() {
		FB2Reader reader = new FB2Reader("M:/Books/pnvs.fb2");
		reader.read();
	}
*/	
}
	
