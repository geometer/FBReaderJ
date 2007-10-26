package org.test.zlibrary.options;

import junit.framework.TestCase;
import org.zlibrary.options.util.*;

/**
 * тесты на Util часть, где лежат всякие вспомогательные сущности
 * тесты 01 - это тесты на соотвествующие методы ZLColor
 * @author Администратор
 *
 */
public class UtilTests extends TestCase {
	private ZLColor myColor;
	
	public void setUp(){
		myColor = new ZLColor (176, 255, 0);
	}
	
	public void test01_toString(){
		assertEquals(myColor.toString(), "176, 255, 0");
	}
	
	public void test01_equals(){
		ZLColor zlc = new ZLColor (176, 255, 0);
		assertTrue(zlc.equals(myColor));
	}
	
	public void test01_setColor(){
		myColor.setColor(0, 1, 0);
		assertEquals(myColor.toString(), "0, 1, 0");
	}
	
	public void test01_getIntValue(){
		assertEquals(myColor.getIntValue(), 176255000L);
	}
}
