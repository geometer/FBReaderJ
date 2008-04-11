package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLStringOptionTests1 extends ZLOptionTests {

	private void runTask1(String configValue, String defaultValue,
			String expectedValue) {
		getConfig().setValue("string_group", "name", configValue, "category");
		ZLStringOption option = new ZLStringOption("category", "string_group",
				"name", defaultValue);
		assertEquals(option.getValue(), expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("string_group", "name");
	}

	public void test1_01() {
		runTask1(null, "", "");
	}

	public void test1_02() {
		runTask1(null, "str", "str");
	}

	public void test1_03() {
		runTask1(null, null, "");
	}

	public void test1_04() {
		runTask1("", "", "");
	}

	public void test1_05() {
		runTask1("", "str", "");
	}

	public void test1_06() {
		runTask1("", null, "");
	}

	public void test1_07() {
		runTask1("str", "", "str");
	}

	public void test1_08() {
		runTask1("str", "str", "str");
	}

	public void test1_09() {
		runTask1("str", null, "str");
	}
}
