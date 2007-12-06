package org.test.zlibrary.options;

import junit.framework.*;

import org.zlibrary.core.options.ZLBoolean3Option;
import org.zlibrary.core.options.ZLBooleanOption;
import org.zlibrary.core.options.ZLColorOption;
import org.zlibrary.core.options.ZLDoubleOption;
import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.ZLIntegerRangeOption;
import org.zlibrary.core.options.ZLOption;
import org.zlibrary.core.options.ZLOptionType;
import org.zlibrary.core.options.ZLStringOption;
import org.zlibrary.core.options.util.*;

/**
 * тесты на модель опций.
 * 
 * @author Администратор тесты 00 - это тесты на соотвествующие методы класса
 *         ZLOption 
 *         тесты 01 - это тесты на геттеры значений 
 *         тесты 02 - это тесты на сеттеры при условии корректости геттеров 
 *         тесты 04 - это тесты на getType
 */
public class ModelTests extends TestCase {

	private ZLColorOption myColorOption;

	private ZLDoubleOption myDoubleOption;

	private ZLIntegerOption myIntegerOption;

	private ZLIntegerRangeOption myIntegerRangeOption;

	private ZLBoolean3Option myBoolean3Option;

	private ZLBooleanOption myBooleanOption;

	private ZLStringOption myStringOption;

	private final int myDefaultColor = 11599616;

	private final int myDefaultIntRange = 75;

	private final int myDefaultInt = 15;

	private final ZLBoolean3 myDefaultBoolean3 = ZLBoolean3.B3_UNDEFINED;

	private final boolean myDefaultBoolean = true;

	private final String myDefaultString = "Hello World";

	private final double myDefaultDouble2 = 1.03;

	private final int myDefaultColor2 = 1159126;

	private final int myDefaultIntRange2 = 78;

	private final int myDefaultInt2 = 10;

	private final ZLBoolean3 myDefaultBoolean32 = ZLBoolean3.B3_TRUE;

	private final boolean myDefaultBoolean2 = false;

	private final String myDefaultString2 = " World";

	private final double myDefaultDouble = 1.02;
	
	public void setUp() {
		myColorOption = new ZLColorOption("", "", "my Color", new ZLColor(
				(short) 176, (short) 255, (short) 0));
		myDoubleOption = new ZLDoubleOption("", "", "My Double",
				myDefaultDouble);
		myIntegerOption = new ZLIntegerOption("", "", "I", myDefaultInt);
		myIntegerRangeOption = new ZLIntegerRangeOption("", "", "IR", -90, 90,
				myDefaultIntRange);
		myBoolean3Option = new ZLBoolean3Option("", "", "my Boolean 3",
				myDefaultBoolean3);
		myBooleanOption = new ZLBooleanOption("", "", "my Boolean",
				myDefaultBoolean);
		myStringOption = new ZLStringOption("qw", "qwe", "my String",
				myDefaultString);
	}

	public void test00_equals() {
		ZLOption zlc = new ZLStringOption("qw", "qwe", "my String", "fire");
		assertTrue(zlc.equals(myStringOption));
	}

	public void test00_getName() {
		assertEquals(myStringOption.getName(), "my String");
	}

	public void test00_getGroup() {
		assertEquals(myStringOption.getGroup(), "qwe");
	}

	public void test00_getCategory() {
		assertEquals(myStringOption.getCategory(), "qw");
	}

	public void test01_color() {
		assertEquals(myColorOption.getValue(), myDefaultColor);
	}

	public void test01_double() {
		assertEquals(myDoubleOption.getValue(), myDefaultDouble);
	}

	public void test01_integer() {
		assertEquals(myIntegerOption.getValue(), myDefaultInt);
	}

	public void test01_integerRange() {
		assertEquals(myIntegerRangeOption.getValue(), myDefaultIntRange);
	}

	public void test01_boolean3() {
		assertEquals(myBoolean3Option.getValue(), myDefaultBoolean3);
	}

	public void test01_boolean() {
		assertEquals(myBooleanOption.getValue(), myDefaultBoolean);
	}

	public void test01_string() {
		assertEquals(myStringOption.getValue(), myDefaultString);
	}

	public void test02_double() {
		myDoubleOption.setValue(0.1);
		assertEquals(myDoubleOption.getValue(), 0.1);
	}

	public void test02_integer() {
		myIntegerOption.setValue(10000000);
		assertEquals(myIntegerOption.getValue(), 10000000);
	}

	public void test02_integerRangeRight() {
		myIntegerRangeOption.setValue(-1);
		assertEquals(myIntegerRangeOption.getValue(), -1);
	}

	public void test02_integerRangeWrong() {
		int expected = myIntegerRangeOption.getValue();
		myIntegerRangeOption.setValue(10000000);
		assertEquals(myIntegerRangeOption.getValue(), expected);
	}

	public void test02_boolean3() {
		myBoolean3Option.setValue(ZLBoolean3.B3_TRUE);
		assertEquals(myBoolean3Option.getValue(), ZLBoolean3.B3_TRUE);
	}

	public void test02_boolean() {
		myBooleanOption.setValue(false);
		assertEquals(myBooleanOption.getValue(), false);
	}

	public void test03_double() {
		myDoubleOption.setValue(123.12314);
		ZLDoubleOption newOption = new ZLDoubleOption("", "", "My Double",
				myDefaultDouble2);
		assertEquals(newOption.getValue(), 123.12314);
	}

	public void test03_integer() {
		myIntegerOption.setValue(123);
		ZLIntegerOption newOption = new ZLIntegerOption("", "", "I",
				myDefaultInt2);
		assertEquals(newOption.getValue(), 123);
	}

	public void test03_integerRange() {
		myIntegerRangeOption.setValue(-1);
		myIntegerRangeOption.setValue(123);
		ZLIntegerRangeOption newOption = new ZLIntegerRangeOption("", "", 
				"IR", 189, 12, myDefaultInt2);
		assertEquals(newOption.getValue(), -1);
	}

	public void test03_boolean3() {
		myBoolean3Option.setValue(ZLBoolean3.B3_TRUE);
		ZLBoolean3Option newOption = new ZLBoolean3Option("", "", 
				"My Boolean 3", myDefaultBoolean32);
		assertEquals(newOption.getValue(), ZLBoolean3.B3_TRUE);
	}

	public void test03_boolean() {
		myBooleanOption.setValue(false);
		ZLBooleanOption newOption = new ZLBooleanOption("", "", 
				"My Boolean", myDefaultBoolean2);
		assertEquals(newOption.getValue(), false);
	}

	public void test04_boolean3() {
		assertEquals(myBoolean3Option.getType(), ZLOptionType.TYPE_BOOLEAN3);
	}

	public void test04_boolean() {
		assertEquals(myBooleanOption.getType(), ZLOptionType.TYPE_BOOLEAN);
	}

	public void test04_string() {
		assertEquals(myStringOption.getType(), ZLOptionType.TYPE_STRING);
	}

}
