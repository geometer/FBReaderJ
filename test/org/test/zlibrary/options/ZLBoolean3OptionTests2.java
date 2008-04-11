package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.options.ZLBoolean3Option;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLBoolean3OptionTests2 extends ZLOptionTests {

	private void runTask2a(String configValue, int value2, int value3, int expectedValue) {
		getConfig().setValue("boolean3_group_2", "name", configValue,
				"boolean3_category");
		ZLBoolean3Option option = new ZLBoolean3Option("boolean3_category",
				"boolean3_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(option.getValue(), expectedValue);
	}

	private void runTask2b(String configValue, int value2, int value3, String expectedValue) {
		getConfig().setValue("boolean3_group_2", "name", configValue,
				"boolean3_category");
		ZLBoolean3Option option = new ZLBoolean3Option("boolean3_category",
				"boolean3_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(getConfig().getValue("boolean3_group_2", "name", "def"),
				expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("boolean3_group_2", "name");
	}

	public void test2a_1_05() {
		runTask2a(null, ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_1_06() {
		runTask2a(null, ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_1_08() {
		runTask2a(null, ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_1_09() {
		runTask2a(null, ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_2_05() {
		runTask2a("", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_2_06() {
		runTask2a("", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_2_08() {
		runTask2a("", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_2_09() {
		runTask2a("", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_3_05() {
		runTask2a("true", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_3_06() {
		runTask2a("true", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_3_08() {
		runTask2a("true", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_3_09() {
		runTask2a("true", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_4_05() {
		runTask2a("undefined", ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_4_06() {
		runTask2a("undefined", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_4_08() {
		runTask2a("undefined", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_4_09() {
		runTask2a("undefined", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_5_05() {
		runTask2a("ерунда", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_5_06() {
		runTask2a("ерунда", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2a_5_08() {
		runTask2a("ерунда", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED);
	}

	public void test2a_5_09() {
		runTask2a("ерунда", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE,
				ZLBoolean3.B3_TRUE);
	}

	public void test2b_1_05() {
		runTask2b(null, ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED, "def");
	}

	public void test2b_1_06() {
		runTask2b(null, ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE, "true");
	}

	public void test2b_1_08() {
		runTask2b(null, ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				"undefined");
	}

	public void test2b_1_09() {
		runTask2b(null, ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE, "def");
	}

	public void test2b_2_05() {
		runTask2b("", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED, "def");
	}

	public void test2b_2_06() {
		runTask2b("", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE, "true");
	}

	public void test2b_2_08() {
		runTask2b("", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED, "undefined");
	}

	public void test2b_2_09() {
		runTask2b("", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE, "def");
	}

	public void test2b_3_05() {
		runTask2b("true", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED,
				"def");
	}

	public void test2b_3_06() {
		runTask2b("true", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE, "true");
	}

	public void test2b_3_08() {
		runTask2b("true", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				"undefined");
	}

	public void test2b_3_09() {
		runTask2b("true", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE, "def");
	}

	public void test2b_4_05() {
		runTask2b("undefined", ZLBoolean3.B3_UNDEFINED,
				ZLBoolean3.B3_UNDEFINED, "def");
	}

	public void test2b_4_06() {
		runTask2b("undefined", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE,
				"true");
	}

	public void test2b_4_08() {
		runTask2b("undefined", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				"undefined");
	}

	public void test2b_4_09() {
		runTask2b("undefined", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE, "def");
	}

	public void test2b_5_05() {
		runTask2b("ерунда", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED,
				"def");
	}

	public void test2b_5_06() {
		runTask2b("ерунда", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE, "true");
	}

	public void test2b_5_08() {
		runTask2b("ерунда", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED,
				"undefined");
	}

	public void test2b_5_09() {
		runTask2b("ерунда", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE, "def");
	}
}
