package org.test.fbreader.formats.fb2;

import org.fbreader.bookmodel.BookModel;
import org.fbreader.formats.fb2.FB2Reader;
import org.fbreader.formats.fb2.FB2Tag;
import org.zlibrary.text.model.ZLTextModel;
import org.zlibrary.text.model.ZLTextParagraph;
import org.zlibrary.text.model.entry.ZLTextEntry;

import junit.framework.TestCase;

public class TestFB2Reader extends TestCase {
	public void testOneParagraph() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test1.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест 1[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testTwoParagraphs() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test2.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(),
				"[PARAGRAPH]\n[TEXT]Тест 2[/TEXT][/PARAGRAPH]\n[PARAGRAPH]\n[TEXT]Два абзаца[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testControlSup() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test3.fb2");
		ZLTextModel model = reader.read().getBookModel();
		int sup = FB2Tag.SUP.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест [/TEXT][CONTROL " + sup +
				"][TEXT]3[/TEXT][/CONTROL " + sup + "][/PARAGRAPH]\n");
	}
	
	public void testControlSub() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test4.fb2");
		ZLTextModel model = reader.read().getBookModel();
		int sub = FB2Tag.SUB.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест [/TEXT][CONTROL " + sub +
				"][TEXT]4[/TEXT][/CONTROL " + sub + "][/PARAGRAPH]\n");
	}
	
	public void testControlEmphasis() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test5.fb2");
		ZLTextModel model = reader.read().getBookModel();
		int emphasis = FB2Tag.EMPHASIS.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + emphasis +
				"][TEXT]Тест 5[/TEXT][/CONTROL " + emphasis + "][/PARAGRAPH]\n");
	}

	public void testControlStrong() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test6.fb2");
		ZLTextModel model = reader.read().getBookModel();
		int strong = FB2Tag.STRONG.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест [/TEXT][CONTROL " + 
				strong + "][TEXT]6[/TEXT][/CONTROL " + strong + "][/PARAGRAPH]\n");
	}
	
	public void testControlStrikeThrough() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test7.fb2");
		ZLTextModel model = reader.read().getBookModel();
		int strikethrough = FB2Tag.STRIKETHROUGH.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + strikethrough +
				"][TEXT]Тест 7[/TEXT][/CONTROL " + strikethrough + "][/PARAGRAPH]\n");
	}
	
	public void testControlCode() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test8.fb2");
		ZLTextModel model = reader.read().getBookModel();
		int code = FB2Tag.CODE.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест 8. [/TEXT][CONTROL " + code +
				"][TEXT]Оформление кода[/TEXT][/CONTROL " + code + "][/PARAGRAPH]\n");
	}
	
	public void testControlVerse() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/verse.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + FB2Tag.V.ordinal() +
				"][TEXT]Verse[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testControlDate() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/date.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + FB2Tag.DATE.ordinal() +
				"][TEXT]Date[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testControlSubtitle() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/subtitle.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + FB2Tag.SUBTITLE.ordinal() +
				"][TEXT]Subtitle[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testControlTextAuthor() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/text_author.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + FB2Tag.TEXT_AUTHOR.ordinal() +
				"][TEXT]Text author[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testEmptyLine() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/empty_line.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[/PARAGRAPH]\n");
		assertEquals(model.getParagraph(0).getKind(),
				ZLTextParagraph.Kind.EMPTY_LINE_PARAGRAPH);
	}
	
	public void testControlCite() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/cite.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + FB2Tag.CITE.ordinal() +
				"][TEXT]Cite[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testControlEpigraph() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/epigraph.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + FB2Tag.EPIGRAPH.ordinal() +
				"][TEXT]Epigraph[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testNotesSimple() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/simple_notes.fb2");
		assertTrue(reader.read().getFootnotes().containsKey("1"));
		assertNotNull(reader.read().getFootnotes().get("1"));
	}
	
	public void testOneNote() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/one_note.fb2");
		ZLTextModel model = reader.read().getFootnoteModel("1");
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Note[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testStanza() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/stanza.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.getParagraph(0).getKind(), ZLTextParagraph.Kind.BEFORE_SKIP_PARAGRAPH);
		assertEquals(model.getParagraph(2).getKind(), ZLTextParagraph.Kind.AFTER_SKIP_PARAGRAPH);
		assertEquals(model.getParagraph(1).getKind(), ZLTextParagraph.Kind.TEXT_PARAGRAPH);
		int stanza = FB2Tag.STANZA.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + stanza +
				"][/PARAGRAPH]\n[PARAGRAPH]\n[CONTROL " + stanza + "][CONTROL " +
				FB2Tag.V.ordinal() + "][TEXT]Stanza[/TEXT][/PARAGRAPH]\n[PARAGRAPH]\n[CONTROL " + 
				stanza + "][/PARAGRAPH]\n");
	}
	
	public void testAnnotation() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/annotation.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + 
				FB2Tag.ANNOTATION.ordinal() + "][TEXT]annotation[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testAnnotationBeforeBody() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/annotation_before.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.getParagraph(1).getKind(),
				ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + 
				FB2Tag.ANNOTATION.ordinal() +
				"][TEXT]annotation[/TEXT][/PARAGRAPH]\n[PARAGRAPH]\n[/PARAGRAPH]\n");
	}
	
	public void testEndOfSection() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/section.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.getParagraph(1).getKind(),
				ZLTextParagraph.Kind.END_OF_SECTION_PARAGRAPH);
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]section[/TEXT][/PARAGRAPH]\n[PARAGRAPH]\n[/PARAGRAPH]\n");
	}
	
	public void testTitle() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/title.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + 
				FB2Tag.TITLE.ordinal() + "][TEXT]Title[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testPoemTitle() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/poem_title.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + 
				FB2Tag.POEM.ordinal() + "][TEXT]Title[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testSectionTitle() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/section_title.fb2");
		ZLTextModel model = reader.read().getBookModel();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + 
				FB2Tag.SECTION.ordinal() + "][TEXT]Title[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testFootnote() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/footnote.fb2");
		ZLTextModel model = reader.read().getBookModel();
		int footnote = FB2Tag.FOOTNOTE.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + footnote +
				"][TEXT][1][/TEXT][/CONTROL " + footnote + "][/PARAGRAPH]\n");
	}
	
	public void testFootnote1() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/footnote1.fb2");
		BookModel model = reader.read();
		assertEquals("footnote", 
				((ZLTextEntry)model.getParagraphByLink("note1").getEntries().get(0)).getData());
	}
	
	public void testExternalHyperlink() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/ext_hyperlink.fb2");
		ZLTextModel model = reader.read().getBookModel();
		int hyperlink = FB2Tag.A.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + hyperlink +
				"][TEXT][1][/TEXT][/CONTROL " + hyperlink + "][/PARAGRAPH]\n");
	}
}
