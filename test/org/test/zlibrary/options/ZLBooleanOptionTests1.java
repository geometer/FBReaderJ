package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLBooleanOptionTests1 extends ZLOptionTests {

	private void runTask1(String configValue, boolean defaultValue,
			boolean expectedValue) {
		getConfig().setValue("boolean_group", "name", configValue, "category");
		ZLBooleanOption option = new ZLBooleanOption("category",
				"boolean_group", "name", defaultValue);
		assertEquals(option.getValue(), expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("boolean_group", "name");
	}

	public void test1_01() {
		runTask1(null, true, true);
	}

	public void test1_02() {
		runTask1(null, false, false);
	}

	public void test1_03() {
		runTask1("true", true, true);
	}

	public void test1_04() {
		runTask1("true", false, true);
	}

	public void test1_05() {
		runTask1("false", true, false);
	}

	public void test1_06() {
		runTask1("false", false, false);
	}

	public void test1_07() {
		runTask1("", true, true);
	}

	public void test1_08() {
		runTask1("", false, false);
	}

	public void test1_09() {
		runTask1("ерунда", true, true);
	}

	public void test1_10() {
		runTask1("ерунда", false, false);
	}
}
