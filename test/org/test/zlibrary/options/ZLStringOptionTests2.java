package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLStringOptionTests2 extends ZLOptionTests {

	private void runTask2a(String configValue, String value2, String value3,
			String expectedValue) {
		getConfig().setValue("string_group_2", "name", configValue,
				"string_category");
		ZLStringOption option = new ZLStringOption("string_category",
				"string_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(option.getValue(), expectedValue);
	}

	private void runTask2b(String configValue, String value2, String value3,
			String expectedValue) {
		getConfig().setValue("string_group_2", "name", configValue,
				"string_category");
		ZLStringOption option = new ZLStringOption("string_category",
				"string_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(getConfig().getValue("string_group_2", "name", "def"),
				expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("string_group_2", "name");
	}

	public void test2a_1_01() {
		runTask2a(null, null, null, "");
	}

	public void test2a_1_02() {
		runTask2a(null, null, "", "");
	}

	public void test2a_1_03() {
		runTask2a(null, null, "str", "str");
	}

	public void test2a_1_04() {
		runTask2a(null, "", null, "");
	}

	public void test2a_1_05() {
		runTask2a(null, "", "", "");
	}

	public void test2a_1_06() {
		runTask2a(null, "", "str", "str");
	}

	public void test2a_1_07() {
		runTask2a(null, "str", null, "str");
	}

	public void test2a_1_08() {
		runTask2a(null, "str", "", "");
	}

	public void test2a_1_09() {
		runTask2a(null, "str", "str", "str");
	}

	public void test2a_2_01() {
		runTask2a("", null, null, "");
	}

	public void test2a_2_02() {
		runTask2a("", null, "", "");
	}

	public void test2a_2_03() {
		runTask2a("", null, "str", "str");
	}

	public void test2a_2_04() {
		runTask2a("", "", null, "");
	}

	public void test2a_2_05() {
		runTask2a("", "", "", "");
	}

	public void test2a_2_06() {
		runTask2a("", "", "str", "str");
	}

	public void test2a_2_07() {
		runTask2a("", "str", null, "");
	}

	public void test2a_2_08() {
		runTask2a("", "str", "", "");
	}

	public void test2a_2_09() {
		runTask2a("", "str", "str", "str");
	}

	public void test2a_3_01() {
		runTask2a("str", null, null, "str");
	}

	public void test2a_3_02() {
		runTask2a("str", null, "", "");
	}

	public void test2a_3_03() {
		runTask2a("str", null, "str", "str");
	}

	public void test2a_3_04() {
		runTask2a("str", "", null, "str");
	}

	public void test2a_3_05() {
		runTask2a("str", "", "", "");
	}

	public void test2a_3_06() {
		runTask2a("str", "", "str", "str");
	}

	public void test2a_3_07() {
		runTask2a("str", "str", null, "str");
	}

	public void test2a_3_08() {
		runTask2a("str", "str", "", "");
	}

	public void test2a_3_09() {
		runTask2a("str", "str", "str", "str");
	}

	public void test2b_1_01() {
		runTask2b(null, null, null, "def");
	}

	public void test2b_1_02() {
		runTask2b(null, null, "", "def");
	}

	public void test2b_1_03() {
		runTask2b(null, null, "str", "str");
	}

	public void test2b_1_04() {
		runTask2b(null, "", null, "def");
	}

	public void test2b_1_05() {
		runTask2b(null, "", "", "def");
	}

	public void test2b_1_06() {
		runTask2b(null, "", "str", "str");
	}

	public void test2b_1_07() {
		runTask2b(null, "str", null, "def");
	}

	public void test2b_1_08() {
		runTask2b(null, "str", "", "");
	}

	public void test2b_1_09() {
		runTask2b(null, "str", "str", "def");
	}

	public void test2b_2_01() {
		runTask2b("", null, null, "");
	}

	public void test2b_2_02() {
		runTask2b("", null, "", "def");
	}

	public void test2b_2_03() {
		runTask2b("", null, "str", "str");
	}

	public void test2b_2_04() {
		runTask2b("", "", null, "");
	}

	public void test2b_2_05() {
		runTask2b("", "", "", "def");
	}

	public void test2b_2_06() {
		runTask2b("", "", "str", "str");
	}

	public void test2b_2_07() {
		runTask2b("", "str", null, "");
	}

	public void test2b_2_08() {
		runTask2b("", "str", "", "");
	}

	public void test2b_2_09() {
		runTask2b("", "str", "str", "def");
	}

	public void test2b_3_01() {
		runTask2b("str", null, null, "str");
	}

	public void test2b_3_02() {
		runTask2b("str", null, "", "def");
	}

	public void test2b_3_03() {
		runTask2b("str", null, "str", "str");
	}

	public void test2b_3_04() {
		runTask2b("str", "", null, "str");
	}

	public void test2b_3_05() {
		runTask2b("str", "", "", "def");
	}

	public void test2b_3_06() {
		runTask2b("str", "", "str", "str");
	}

	public void test2b_3_07() {
		runTask2b("str", "str", null, "str");
	}

	public void test2b_3_08() {
		runTask2b("str", "str", "", "");
	}

	public void test2b_3_09() {
		runTask2b("str", "str", "str", "def");
	}
}
