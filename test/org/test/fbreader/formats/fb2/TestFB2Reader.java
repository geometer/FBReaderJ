package org.test.fbreader.formats.fb2;

import org.fbreader.formats.fb2.FB2Reader;
import org.zlibrary.model.ZLTextModel;

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

}
