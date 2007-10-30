package org.test.fbreader.formats.fb2;

import org.fbreader.formats.fb2.FB2Reader;
import org.fbreader.formats.fb2.FB2Tag;
import org.zlibrary.text.model.ZLTextModel;

import junit.framework.TestCase;

public class TestFB2Reader extends TestCase {
	public void testOneParagraph() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test1.fb2");
		ZLTextModel model = reader.read();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест 1[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testTwoParagraphs() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test2.fb2");
		ZLTextModel model = reader.read();
		assertEquals(model.dump(),
				"[PARAGRAPH]\n[TEXT]Тест 2[/TEXT][/PARAGRAPH]\n[PARAGRAPH]\n[TEXT]Два абзаца[/TEXT][/PARAGRAPH]\n");
	}
	
	public void testControlSup() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test3.fb2");
		ZLTextModel model = reader.read();
		int sup = FB2Tag.SUP.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест [/TEXT][CONTROL " + sup +
				"][TEXT]3[/TEXT][/CONTROL " + sup + "][/PARAGRAPH]\n");
	}
	
	public void testControlSub() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test4.fb2");
		ZLTextModel model = reader.read();
		int sub = FB2Tag.SUB.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест [/TEXT][CONTROL " + sub +
				"][TEXT]4[/TEXT][/CONTROL " + sub + "][/PARAGRAPH]\n");
	}
	
	public void testControlEmphasis() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test5.fb2");
		ZLTextModel model = reader.read();
		int emphasis = FB2Tag.EMPHASIS.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + emphasis +
				"][TEXT]Тест 5[/TEXT][/CONTROL " + emphasis + "][/PARAGRAPH]\n");
	}

	public void testControlStrong() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test6.fb2");
		ZLTextModel model = reader.read();
		int strong = FB2Tag.STRONG.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест [/TEXT][CONTROL " + 
				strong + "][TEXT]6[/TEXT][/CONTROL " + strong + "][/PARAGRAPH]\n");
	}
	
	public void testControlStrikeThrough() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test7.fb2");
		ZLTextModel model = reader.read();
		int strikethrough = FB2Tag.STRIKETHROUGH.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[CONTROL " + strikethrough +
				"][TEXT]Тест 7[/TEXT][/CONTROL " + strikethrough + "][/PARAGRAPH]\n");
	}
	
	public void testControlCode() {
		FB2Reader reader = new FB2Reader("FB2ReaderTests/test8.fb2");
		ZLTextModel model = reader.read();
		int code = FB2Tag.CODE.ordinal();
		assertEquals(model.dump(), "[PARAGRAPH]\n[TEXT]Тест 8. [/TEXT][CONTROL " + code +
				"][TEXT]Оформление кода[/TEXT][/CONTROL " + code + "][/PARAGRAPH]\n");
	}
}
