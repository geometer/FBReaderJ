package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLIntegerOptionTests2 extends ZLOptionTests {

	private void runTask2a(String configValue, int value2, int value3,
			int expectedValue) {
		getConfig().setValue("integer_group_2", "name", configValue,
				"integer_category");
		ZLIntegerOption option = new ZLIntegerOption("integer_category",
				"integer_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(option.getValue(), expectedValue);
	}

	private void runTask2b(String configValue, int value2, int value3,
			String expectedValue) {
		getConfig().setValue("integer_group_2", "name", configValue,
				"integer_category");
		ZLIntegerOption option = new ZLIntegerOption("integer_category",
				"integer_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(getConfig().getValue("integer_group_2", "name", "def"),
				expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("integer_group_2", "name");
	}

	public void test2a_1_01() {
		runTask2a(null, 0, 0, 0);
	}

	public void test2a_1_02() {
		runTask2a(null, 1, 0, 0);
	}

	public void test2a_1_03() {
		runTask2a(null, 0, 1, 1);
	}

	public void test2a_1_04() {
		runTask2a(null, 1, 1, 1);
	}

	public void test2a_2_01() {
		runTask2a("", 0, 0, 0);
	}

	public void test2a_2_02() {
		runTask2a("", 1, 0, 0);
	}

	public void test2a_2_03() {
		runTask2a("", 0, 1, 1);
	}

	public void test2a_2_04() {
		runTask2a("", 1, 1, 1);
	}

	public void test2a_3_01() {
		runTask2a("0", 0, 0, 0);
	}

	public void test2a_3_02() {
		runTask2a("0", 1, 0, 0);
	}

	public void test2a_3_03() {
		runTask2a("0", 0, 1, 1);
	}

	public void test2a_3_04() {
		runTask2a("0", 1, 1, 1);
	}

	public void test2a_4_01() {
		runTask2a("1", 0, 0, 0);
	}

	public void test2a_4_02() {
		runTask2a("1", 1, 0, 0);
	}

	public void test2a_4_03() {
		runTask2a("1", 0, 1, 1);
	}

	public void test2a_4_04() {
		runTask2a("1", 1, 1, 1);
	}

	public void test2a_5_01() {
		runTask2a("ерунда", 0, 0, 0);
	}

	public void test2a_5_02() {
		runTask2a("ерунда", 1, 0, 0);
	}

	public void test2a_5_03() {
		runTask2a("ерунда", 0, 1, 1);
	}

	public void test2a_5_04() {
		runTask2a("ерунда", 1, 1, 1);
	}

	public void test2b_1_01() {
		runTask2b(null, 0, 0, "def");
	}

	public void test2b_1_02() {
		runTask2b(null, 1, 0, "0");
	}

	public void test2b_1_03() {
		runTask2b(null, 0, 1, "1");
	}

	public void test2b_1_04() {
		runTask2b(null, 1, 1, "def");
	}

	public void test2b_2_01() {
		runTask2b("", 0, 0, "def");
	}

	public void test2b_2_02() {
		runTask2b("", 1, 0, "0");
	}

	public void test2b_2_03() {
		runTask2b("", 0, 1, "1");
	}

	public void test2b_2_04() {
		runTask2b("", 1, 1, "def");
	}

	public void test2b_3_01() {
		runTask2b("0", 0, 0, "def");
	}

	public void test2b_3_02() {
		runTask2b("0", 1, 0, "0");
	}

	public void test2b_3_03() {
		runTask2b("0", 0, 1, "1");
	}

	public void test2b_3_04() {
		runTask2b("0", 1, 1, "def");
	}

	public void test2b_4_01() {
		runTask2b("1", 0, 0, "def");
	}

	public void test2b_4_02() {
		runTask2b("1", 1, 0, "0");
	}

	public void test2b_4_03() {
		runTask2b("1", 0, 1, "1");
	}

	public void test2b_4_04() {
		runTask2b("1", 1, 1, "def");
	}

	public void test2b_5_01() {
		runTask2b("ерунда", 0, 0, "def");
	}

	public void test2b_5_02() {
		runTask2b("ерунда", 1, 0, "0");
	}

	public void test2b_5_03() {
		runTask2b("ерунда", 0, 1, "1");
	}

	public void test2b_5_04() {
		runTask2b("ерунда", 1, 1, "def");
	}
}
