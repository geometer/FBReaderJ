package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLBooleanOptionTests2 extends ZLOptionTests {

	private void runTask2a(String configValue, boolean value2, boolean value3,
			boolean expectedValue) {
		getConfig().setValue("boolean_group_2", "name", configValue,
				"boolean_category");
		ZLBooleanOption option = new ZLBooleanOption("boolean_category",
				"boolean_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(option.getValue(), expectedValue);
	}

	private void runTask2b(String configValue, boolean value2, boolean value3,
			String expectedValue) {
		getConfig().setValue("boolean_group_2", "name", configValue,
				"boolean_category");
		ZLBooleanOption option = new ZLBooleanOption("boolean_category",
				"boolean_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(getConfig().getValue("boolean_group_2", "name", "def"),
				expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("boolean_group_2", "name");
	}

	public void test2a_1_01() {
		runTask2a(null, true, true, true);
	}

	public void test2a_1_02() {
		runTask2a(null, true, false, false);
	}

	public void test2a_1_03() {
		runTask2a(null, false, true, true);
	}

	public void test2a_1_04() {
		runTask2a(null, false, false, false);
	}

	public void test2a_2_01() {
		runTask2a("", true, true, true);
	}

	public void test2a_2_02() {
		runTask2a("", true, false, false);
	}

	public void test2a_2_03() {
		runTask2a("", false, true, true);
	}

	public void test2a_2_04() {
		runTask2a("", false, false, false);
	}

	public void test2a_3_01() {
		runTask2a("true", true, true, true);
	}

	public void test2a_3_02() {
		runTask2a("true", true, false, false);
	}

	public void test2a_3_03() {
		runTask2a("true", false, true, true);
	}

	public void test2a_3_04() {
		runTask2a("true", false, false, false);
	}

	public void test2a_4_01() {
		runTask2a("ерунда", true, true, true);
	}

	public void test2a_4_02() {
		runTask2a("ерунда", true, false, false);
	}

	public void test2a_4_03() {
		runTask2a("ерунда", false, true, true);
	}

	public void test2a_4_04() {
		runTask2a("ерунда", false, false, false);
	}

	public void test2a_5_01() {
		runTask2a("false", true, true, true);
	}

	public void test2a_5_02() {
		runTask2a("false", true, false, false);
	}

	public void test2a_5_03() {
		runTask2a("false", false, true, true);
	}

	public void test2a_5_04() {
		runTask2a("false", false, false, false);
	}

	public void test2b_1_01() {
		runTask2b(null, true, true, "def");
	}

	public void test2b_1_02() {
		runTask2b(null, true, false, "false");
	}

	public void test2b_1_03() {
		runTask2b(null, false, true, "true");
	}

	public void test2b_1_04() {
		runTask2b(null, false, false, "def");
	}

	public void test2b_2_01() {
		runTask2b("", true, true, "def");
	}

	public void test2b_2_02() {
		runTask2b("", true, false, "false");
	}

	public void test2b_2_03() {
		runTask2b("", false, true, "true");
	}

	public void test2b_2_04() {
		runTask2b("", false, false, "def");
	}

	public void test2b_3_01() {
		runTask2b("true", true, true, "def");
	}

	public void test2b_3_02() {
		runTask2b("true", true, false, "false");
	}

	public void test2b_3_03() {
		runTask2b("true", false, true, "true");
	}

	public void test2b_3_04() {
		runTask2b("true", false, false, "def");
	}

	public void test2b_4_01() {
		runTask2b("ерунда", true, true, "def");
	}

	public void test2b_4_02() {
		runTask2b("ерунда", true, false, "false");
	}

	public void test2b_4_03() {
		runTask2b("ерунда", false, true, "true");
	}

	public void test2b_4_04() {
		runTask2b("ерунда", false, false, "def");
	}

	public void test2b_5_01() {
		runTask2b("false", true, true, "def");
	}

	public void test2b_5_02() {
		runTask2b("false", true, false, "false");
	}

	public void test2b_5_03() {
		runTask2b("false", false, true, "true");
	}

	public void test2b_5_04() {
		runTask2b("false", false, false, "def");
	}
}
