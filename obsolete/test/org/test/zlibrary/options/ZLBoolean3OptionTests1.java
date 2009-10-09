package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.core.options.ZLBoolean3Option;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLBoolean3OptionTests1 extends ZLOptionTests {

	private void runTask1(String configValue, int defaultValue, int expectedValue) {
		getConfig().setValue("boolean3_group", "name", configValue, "category");
		ZLBoolean3Option option = new ZLBoolean3Option("category",
				"boolean3_group", "name", defaultValue);
		assertEquals(option.getValue(), expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("boolean3_group", "name");
	}

	public void test1_02() {
		runTask1("", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED);
	}

	public void test1_03() {
		runTask1("", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}

	public void test1_05() {
		runTask1(null, ZLBoolean3.B3_FALSE, ZLBoolean3.B3_FALSE);
	}

	public void test1_06() {
		runTask1(null, ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}

	public void test1_08() {
		runTask1("true", ZLBoolean3.B3_FALSE, ZLBoolean3.B3_TRUE);
	}

	public void test1_09() {
		runTask1("true", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_TRUE);
	}

	public void test1_11() {
		runTask1("ерунда", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED);
	}

	public void test1_12() {
		runTask1("ерунда", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}

	public void test1_14() {
		runTask1("undefined", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_UNDEFINED);
	}

	public void test1_15() {
		runTask1("undefined", ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
}
