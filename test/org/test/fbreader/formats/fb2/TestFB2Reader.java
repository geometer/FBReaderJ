package org.test.fbreader.formats.fb2;

import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.ContentsModel;
import org.geometerplus.fbreader.formats.fb2.FB2Reader;
import org.geometerplus.zlibrary.core.image.*;

import org.geometerplus.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.model.ZLTextParagraph;
import org.geometerplus.zlibrary.text.model.ZLTextTreeParagraph;
import org.geometerplus.zlibrary.text.model.impl.ZLImageEntry;
import org.geometerplus.zlibrary.ui.swing.library.ZLSwingLibrary;
import org.geometerplus.zlibrary.ui.swing.view.ZLSwingPaintContext;

import org.test.zlibrary.model.ModelDumper;

import junit.framework.TestCase;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class TestFB2Reader extends TestCase {
	
	public void setUp() {
		new ZLSwingLibrary();
		new ZLOwnXMLProcessorFactory();
	}
	
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
			return false;
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
			osw.write(ModelDumper.dump(model));
			osw.close();
		} catch (Exception e) {
			fail();
		}		
	}
	
	private void writeTextModelDumpToFile(String inputFile, String outputFile) {
		BookModel bookModel = new BookModel(inputFile);
		new FB2Reader(bookModel).read();
		ZLTextModel model = bookModel.getBookTextModel();
		writeDumpToFile(model, outputFile);
	}
	
	private void writeTreeModelDumpToFile(String inputFile, String outputFile) {
		BookModel bookModel = new BookModel(inputFile);
		new FB2Reader(bookModel).read();
		ZLTextModel model = bookModel.getContentsModel();
		writeDumpToFile(model, outputFile);
	}
	
	private void doTreeModelTest(String test) {
		String test_result = myDirectory + test + "_act.txt";
		writeTreeModelDumpToFile(myDirectory + test + ".fb2", test_result);
		assertTrue("File " + test, compareFiles(myDirectory + test + "_exp.txt", test_result));
		new File(test_result).delete();
	}
	
	private void doTest(String test) {
		String test_result = myDirectory + test + "_act.txt";
		writeTextModelDumpToFile(myDirectory + test + ".fb2", test_result);
		assertTrue(compareFiles(myDirectory + test + "_exp.txt", test_result));
		new File(test_result).delete();
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
		BookModel bookModel = new BookModel(myDirectory + "empty_line.fb2");
		new FB2Reader(bookModel).read();
		ZLTextModel model = bookModel.getBookTextModel();
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
		BookModel model = new BookModel(myDirectory + "simple_notes.fb2");
		new FB2Reader(model).read();
		assertNotNull(model.getFootnoteModel("1"));
	}
	
	public void testOneNote() {
		BookModel bookModel = new BookModel(myDirectory + "one_note.fb2");
		new FB2Reader(bookModel).read();
		ZLTextModel model = bookModel.getFootnoteModel("1");
		String output = myDirectory + "one_note_act.txt";
		writeDumpToFile(model, output);
		assertTrue(compareFiles(myDirectory + "one_note_exp.txt", output));
		new File(output).delete();
	}
	
	public void testStanza() {
		doTest("stanza");
	}
	
	public void testStanzaParagraphKinds() {
		BookModel bookModel = new BookModel(myDirectory + "stanza.fb2");
		new FB2Reader(bookModel).read();
		ZLTextModel model = bookModel.getBookTextModel();
		assertEquals(model.getParagraph(0).getKind(), ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
		assertEquals(model.getParagraph(2).getKind(), ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
		assertEquals(model.getParagraph(1).getKind(), ZLTextParagraph.Kind.TEXT_PARAGRAPH);
	}
	
	public void testAnnotation() {
		doTest("annotation");
	}
	
	public void testAnnotationBeforeBodyParagraph() {
		BookModel bookModel = new BookModel(myDirectory + "annotation_before.fb2");
		new FB2Reader(bookModel).read();
		ZLTextModel model = bookModel.getBookTextModel();
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
		BookModel bookModel = new BookModel(myDirectory + "section.fb2");
		new FB2Reader(bookModel).read();
		ZLTextModel model = bookModel.getBookTextModel();
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
		BookModel model = new BookModel(myDirectory + "footnote1.fb2");
		new FB2Reader(model).read();
		ZLTextParagraph paragraph = model.getParagraphByLink("note1");
		ZLTextParagraph.EntryIterator it = paragraph.iterator();
		it.next();
		it.next();
		assertEquals(it.getType(), ZLTextParagraph.Entry.TEXT);
		assertEquals("footnote", new String(it.getTextData(), it.getTextOffset(), it.getTextLength()));
	}
	
	public void testExternalHyperlink() {
		doTest("ext_hyperlink");
	}
	
	public void testTreeParagraph() {
		doTreeModelTest("tree1");
	}
	
	public void testTreeParagraphRef() {
		BookModel bookModel = new BookModel(myDirectory + "tree1.fb2");
		new FB2Reader(bookModel).read();
		ContentsModel model = bookModel.getContentsModel();
		assertTrue(model.getReference((ZLTextTreeParagraph)model.getParagraph(0)) == 0);
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
//		for (Object s : System.getProperties().keySet())
//		System.out.println(s);
	}
	
/*	public void testPnvs() {
		FB2Reader reader = new FB2Reader("M:/Books/pnvs.fb2");
		reader.read();
	}
*/	
	
	public void testImage() {
		BookModel model = new BookModel(myDirectory + "image.fb2");
		new FB2Reader(model).read();
		byte [] image = (model.getImageMap().getImage("cover.jpg")).byteData();
		try {
			BufferedImage img = ImageIO.read(new ByteArrayInputStream(image));
			ImageIO.write(img, "jpg", new File(myDirectory + "img.jpg"));
		} catch (IOException e) {
			fail();
		} 
	}
	
	public void testImageKind() {
		doTest("image");
	}
	
	public void testImageModel() {
		BookModel bModel = new BookModel(myDirectory + "image.fb2");
		new FB2Reader(bModel).read();
		ZLTextModel model = bModel.getBookTextModel();
		ZLTextParagraph paragraph = model.getParagraph(0);
		ZLTextParagraph.EntryIterator it = paragraph.iterator();
		it.next();
		it.next();
		ZLImageEntry entry = it.getImageEntry();
		assertEquals(entry.VOffset, 0);
		assertEquals(entry.getImage(), bModel.getImageMap().getImage("cover.jpg"));
	}
	
	public void testImageSize() {
		BookModel model = new BookModel(myDirectory + "image.fb2");
		new FB2Reader(model).read();
		ZLImage image = model.getImageMap().getImage("cover.jpg");
		ZLImageData imageData = ZLImageManager.getInstance().getImageData(image);
		ZLSwingPaintContext paint = new ZLSwingPaintContext();
		assertTrue(paint.imageHeight(imageData) == 277);
		assertTrue(paint.imageWidth(imageData) == 200);
	}
	
}
	
