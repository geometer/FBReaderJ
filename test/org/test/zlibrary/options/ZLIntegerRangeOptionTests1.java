package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLIntegerRangeOptionTests1 extends ZLOptionTests {

	private void runTask1(String configValue, int defaultValue,
			int expectedValue) {
		getConfig().setValue("integer_range_group", "name", configValue,
				"category");
		ZLIntegerRangeOption option = new ZLIntegerRangeOption("category",
				"integer_range_group", "name", -100, 100, defaultValue);
		assertEquals(option.getValue(), expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("integer_range_group", "name");
	}

	public void test1_01() {
		runTask1("", 0, 0);
	}

	public void test1_02() {
		runTask1("", 12, 12);
	}

	public void test1_03a() {
		runTask1("", 200, 100);
	}

	public void test1_03b() {
		runTask1("", -200, -100);
	}

	public void test1_04() {
		runTask1(null, 0, 0);
	}

	public void test1_05() {
		runTask1(null, 12, 12);
	}

	public void test1_06a() {
		runTask1(null, 200, 100);
	}

	public void test1_06b() {
		runTask1(null, -200, -100);
	}

	public void test1_07() {
		runTask1("ерунда", 0, 0);
	}

	public void test1_08() {
		runTask1("ерунда", 12, 12);
	}

	public void test1_09a() {
		runTask1("ерунда", 200, 100);
	}

	public void test1_09b() {
		runTask1("ерунда", -200, -100);
	}

	public void test1_10() {
		runTask1("1", 0, 1);
	}

	public void test1_11() {
		runTask1("1", 12, 1);
	}

	public void test1_12a() {
		runTask1("1", 200, 1);
	}

	public void test1_12b() {
		runTask1("1", -200, 1);
	}

	public void test1_13() {
		runTask1("200", 0, 100);
	}

	public void test1_14() {
		runTask1("200", 12, 100);
	}

	public void test1_15a() {
		runTask1("200", 200, 100);
	}

	public void test1_15b() {
		runTask1("200", -200, 100);
	}

	public void test1_16() {
		runTask1("-200", 0, -100);
	}

	public void test1_17() {
		runTask1("-200", 12, -100);
	}

	public void test1_18a() {
		runTask1("-200", 200, -100);
	}

	public void test1_18b() {
		runTask1("-200", -200, -100);
	}
}
