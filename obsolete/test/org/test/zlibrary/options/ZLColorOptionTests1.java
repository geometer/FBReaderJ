package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.options.ZLColorOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLColorOptionTests1 extends ZLOptionTests {

	private void runTask1(String configValue, ZLColor defaultValue,
			int expectedValue) {
		getConfig().setValue("color_group", "name", configValue, "category");
		ZLColorOption option = new ZLColorOption("category", "color_group",
				"name", defaultValue);
		assertEquals(option.getValue().getIntValue(), expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("color_group", "name");
	}

	public void test1_01() {
		runTask1(null, null, 0);
	}

	public void test1_02() {
		runTask1(null, new ZLColor(0), 0);
	}

	public void test1_03() {
		runTask1(null, new ZLColor(11599616), 11599616);
	}

	public void test1_04() {
		runTask1("", null, 0);
	}

	public void test1_05() {
		runTask1("", new ZLColor(0), 0);
	}

	public void test1_06() {
		runTask1("", new ZLColor(11599616), 11599616);
	}

	public void test1_07() {
		runTask1("10000000", null, 10000000);
	}

	public void test1_08() {
		runTask1("10000000", new ZLColor(0), 10000000);
	}

	public void test1_09() {
		runTask1("10000000", new ZLColor(11599616), 10000000);
	}

	public void test1_10() {
		runTask1("ерунда", null, 0);
	}

	public void test1_11() {
		runTask1("ерунда", new ZLColor(0), 0);
	}

	public void test1_12() {
		runTask1("ерунда", new ZLColor(13222226), 13222226);
	}

	public void test1_13() {
		runTask1("0", null, 0);
	}

	public void test1_14() {
		runTask1("0", new ZLColor(0), 0);
	}

	public void test1_15() {
		runTask1("0", new ZLColor(12225226), 0);
	}
}
