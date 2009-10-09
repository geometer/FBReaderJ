package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.options.ZLColorOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLColorOptionTests2 extends ZLOptionTests {

	private void runTask2a(String configValue, ZLColor value2, ZLColor value3,
			int expectedValue) {
		getConfig().setValue("color_group_2", "name", configValue,
				"color_category");
		ZLColorOption option = new ZLColorOption("color_category",
				"color_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(option.getValue().getIntValue(), expectedValue);
	}

	private void runTask2b(String configValue, ZLColor value2, ZLColor value3,
			String expectedValue) {
		getConfig().setValue("color_group_2", "name", configValue,
				"color_category");
		ZLColorOption option = new ZLColorOption("color_category",
				"color_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(getConfig().getValue("color_group_2", "name", "def"),
				expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("color_group_2", "name");
	}

	public void test2a_1_01() {
		runTask2a(null, null, null, 0);
	}

	public void test2a_1_02() {
		runTask2a(null, null, new ZLColor(0), 0);
	}

	public void test2a_1_03() {
		runTask2a(null, null, new ZLColor(11599616), 11599616);
	}

	public void test2a_1_04() {
		runTask2a(null, new ZLColor(0), null, 0);
	}

	public void test2a_1_05() {
		runTask2a(null, new ZLColor(0), new ZLColor(0), 0);
	}

	public void test2a_1_06() {
		runTask2a(null, new ZLColor(0), new ZLColor(11599616), 11599616);
	}

	public void test2a_1_07() {
		runTask2a(null, new ZLColor(11599617), null, 11599617);
	}

	public void test2a_1_08() {
		runTask2a(null, new ZLColor(11599618), new ZLColor(0), 0);
	}

	public void test2a_1_09() {
		runTask2a(null, new ZLColor(11599619), new ZLColor(11599620), 11599620);
	}

	public void test2a_1_10() {
		runTask2a(null, new ZLColor(11599619), new ZLColor(11599619), 11599619);
	}

	public void test2a_2_01() {
		runTask2a("", null, null, 0);
	}

	public void test2a_2_02() {
		runTask2a("", null, new ZLColor(0), 0);
	}

	public void test2a_2_03() {
		runTask2a("", null, new ZLColor(11599616), 11599616);
	}

	public void test2a_2_04() {
		runTask2a("", new ZLColor(0), null, 0);
	}

	public void test2a_2_05() {
		runTask2a("", new ZLColor(0), new ZLColor(0), 0);
	}

	public void test2a_2_06() {
		runTask2a("", new ZLColor(0), new ZLColor(11599616), 11599616);
	}

	public void test2a_2_07() {
		runTask2a("", new ZLColor(11599617), null, 11599617);
	}

	public void test2a_2_08() {
		runTask2a("", new ZLColor(11599618), new ZLColor(0), 0);
	}

	public void test2a_2_09() {
		runTask2a("", new ZLColor(11599619), new ZLColor(11599620), 11599620);
	}

	public void test2a_2_10() {
		runTask2a("", new ZLColor(11599619), new ZLColor(11599619), 11599619);
	}

	public void test2a_3_01() {
		runTask2a("0", null, null, 0);
	}

	public void test2a_3_02() {
		runTask2a("0", null, new ZLColor(0), 0);
	}

	public void test2a_3_03() {
		runTask2a("0", null, new ZLColor(11599616), 11599616);
	}

	public void test2a_3_04() {
		runTask2a("0", new ZLColor(0), null, 0);
	}

	public void test2a_3_05() {
		runTask2a("0", new ZLColor(0), new ZLColor(0), 0);
	}

	public void test2a_3_06() {
		runTask2a("0", new ZLColor(0), new ZLColor(11599616), 11599616);
	}

	public void test2a_3_07() {
		runTask2a("0", new ZLColor(11599617), null, 0);
	}

	public void test2a_3_08() {
		runTask2a("0", new ZLColor(11599618), new ZLColor(0), 0);
	}

	public void test2a_3_09() {
		runTask2a("0", new ZLColor(11599619), new ZLColor(11599620), 11599620);
	}

	public void test2a_3_10() {
		runTask2a("0", new ZLColor(11599619), new ZLColor(11599619), 11599619);
	}

	public void test2a_4_01() {
		runTask2a("12323412", null, null, 12323412);
	}

	public void test2a_4_02() {
		runTask2a("12323412", null, new ZLColor(0), 0);
	}

	public void test2a_4_03() {
		runTask2a("12323412", null, new ZLColor(11599616), 11599616);
	}

	public void test2a_4_04() {
		runTask2a("12323412", new ZLColor(0), null, 12323412);
	}

	public void test2a_4_05() {
		runTask2a("12323412", new ZLColor(0), new ZLColor(0), 0);
	}

	public void test2a_4_06() {
		runTask2a("12323412", new ZLColor(0), new ZLColor(11599616), 11599616);
	}

	public void test2a_4_07() {
		runTask2a("12323412", new ZLColor(11599617), null, 12323412);
	}

	public void test2a_4_08() {
		runTask2a("12323412", new ZLColor(11599618), new ZLColor(0), 0);
	}

	public void test2a_4_09() {
		runTask2a("12323412", new ZLColor(11599619), new ZLColor(11599620),
				11599620);
	}

	public void test2a_4_10() {
		runTask2a("12323412", new ZLColor(11599619), new ZLColor(11599619),
				11599619);
	}

	public void test2a_5_01() {
		runTask2a("1232342351263423745612", null, null, 0);
	}

	public void test2a_5_02() {
		runTask2a("1232342351263423745612", null, new ZLColor(0), 0);
	}

	public void test2a_5_03() {
		runTask2a("1232342351263423745612", null, new ZLColor(11599616),
				11599616);
	}

	public void test2a_5_04() {
		runTask2a("1232342351263423745612", new ZLColor(0), null, 0);
	}

	public void test2a_5_05() {
		runTask2a("1232342351263423745612", new ZLColor(0), new ZLColor(0), 0);
	}

	public void test2a_5_06() {
		runTask2a("1232342351263423745612", new ZLColor(0), new ZLColor(
				11599616), 11599616);
	}

	public void test2a_5_07() {
		runTask2a("1232342351263423745612", new ZLColor(11599617), null,
				11599617);
	}

	public void test2a_5_08() {
		runTask2a("1232342351263423745612", new ZLColor(11599618), new ZLColor(
				0), 0);
	}

	public void test2a_5_09() {
		runTask2a("1232342351263423745612", new ZLColor(11599619), new ZLColor(
				11599620), 11599620);
	}

	public void test2a_5_10() {
		runTask2a("1232342351263423745612", new ZLColor(11599619), new ZLColor(
				11599619), 11599619);
	}

	public void test2a_6_01() {
		runTask2a("ерунда", null, null, 0);
	}

	public void test2a_6_02() {
		runTask2a("ерунда", null, new ZLColor(0), 0);
	}

	public void test2a_6_03() {
		runTask2a("ерунда", null, new ZLColor(11599616), 11599616);
	}

	public void test2a_6_04() {
		runTask2a("ерунда", new ZLColor(0), null, 0);
	}

	public void test2a_6_05() {
		runTask2a("ерунда", new ZLColor(0), new ZLColor(0), 0);
	}

	public void test2a_6_06() {
		runTask2a("ерунда", new ZLColor(0), new ZLColor(11599616), 11599616);
	}

	public void test2a_6_07() {
		runTask2a("ерунда", new ZLColor(11599617), null, 11599617);
	}

	public void test2a_6_08() {
		runTask2a("ерунда", new ZLColor(11599618), new ZLColor(0), 0);
	}

	public void test2a_6_09() {
		runTask2a("ерунда", new ZLColor(11599619), new ZLColor(11599620),
				11599620);
	}

	public void test2a_6_10() {
		runTask2a("ерунда", new ZLColor(11599619), new ZLColor(11599619),
				11599619);
	}

	public void test2b_1_01() {
		runTask2b(null, null, null, "def");
	}

	public void test2b_1_02() {
		runTask2b(null, null, new ZLColor(0), "def");
	}

	public void test2b_1_03() {
		runTask2b(null, null, new ZLColor(11599616), "11599616");
	}

	public void test2b_1_04() {
		runTask2b(null, new ZLColor(0), null, "def");
	}

	public void test2b_1_05() {
		runTask2b(null, new ZLColor(0), new ZLColor(0), "def");
	}

	public void test2b_1_06() {
		runTask2b(null, new ZLColor(0), new ZLColor(11599616), "11599616");
	}

	public void test2b_1_07() {
		runTask2b(null, new ZLColor(11599617), null, "def");
	}

	public void test2b_1_08() {
		runTask2b(null, new ZLColor(11599618), new ZLColor(0), "0");
	}

	public void test2b_1_09() {
		runTask2b(null, new ZLColor(11599619), new ZLColor(11599620),
				"11599620");
	}

	public void test2b_1_10() {
		runTask2b(null, new ZLColor(11599619), new ZLColor(11599619), "def");
	}

	public void test2b_2_01() {
		runTask2b("", null, null, "");
	}

	public void test2b_2_02() {
		runTask2b("", null, new ZLColor(0), "def");
	}

	public void test2b_2_03() {
		runTask2b("", null, new ZLColor(11599616), "11599616");
	}

	public void test2b_2_04() {
		runTask2b("", new ZLColor(0), null, "");
	}

	public void test2b_2_05() {
		runTask2b("", new ZLColor(0), new ZLColor(0), "def");
	}

	public void test2b_2_06() {
		runTask2b("", new ZLColor(0), new ZLColor(11599616), "11599616");
	}

	public void test2b_2_07() {
		runTask2b("", new ZLColor(11599617), null, "");
	}

	public void test2b_2_08() {
		runTask2b("", new ZLColor(11599618), new ZLColor(0), "0");
	}

	public void test2b_2_09() {
		runTask2b("", new ZLColor(11599619), new ZLColor(11599620), "11599620");
	}

	public void test2b_2_10() {
		runTask2b("", new ZLColor(11599619), new ZLColor(11599619), "def");
	}

	public void test2b_3_01() {
		runTask2b("0", null, null, "0");
	}

	public void test2b_3_02() {
		runTask2b("0", null, new ZLColor(0), "def");
	}

	public void test2b_3_03() {
		runTask2b("0", null, new ZLColor(11599616), "11599616");
	}

	public void test2b_3_04() {
		runTask2b("0", new ZLColor(0), null, "0");
	}

	public void test2b_3_05() {
		runTask2b("0", new ZLColor(0), new ZLColor(0), "def");
	}

	public void test2b_3_06() {
		runTask2b("0", new ZLColor(0), new ZLColor(11599616), "11599616");
	}

	public void test2b_3_07() {
		runTask2b("0", new ZLColor(11599617), null, "0");
	}

	public void test2b_3_08() {
		runTask2b("0", new ZLColor(11599618), new ZLColor(0), "0");
	}

	public void test2b_3_09() {
		runTask2b("0", new ZLColor(11599619), new ZLColor(11599620), "11599620");
	}

	public void test2b_3_10() {
		runTask2b("0", new ZLColor(11599619), new ZLColor(11599619), "def");
	}

	public void test2b_4_01() {
		runTask2b("12323412", null, null, "12323412");
	}

	public void test2b_4_02() {
		runTask2b("12323412", null, new ZLColor(0), "def");
	}

	public void test2b_4_03() {
		runTask2b("12323412", null, new ZLColor(11599616), "11599616");
	}

	public void test2b_4_04() {
		runTask2b("12323412", new ZLColor(0), null, "12323412");
	}

	public void test2b_4_05() {
		runTask2b("12323412", new ZLColor(0), new ZLColor(0), "def");
	}

	public void test2b_4_06() {
		runTask2b("12323412", new ZLColor(0), new ZLColor(11599616), "11599616");
	}

	public void test2b_4_07() {
		runTask2b("12323412", new ZLColor(11599617), null, "12323412");
	}

	public void test2b_4_08() {
		runTask2b("12323412", new ZLColor(11599618), new ZLColor(0), "0");
	}

	public void test2b_4_09() {
		runTask2b("12323412", new ZLColor(11599619), new ZLColor(11599620),
				"11599620");
	}

	public void test2b_4_10() {
		runTask2b("12323412", new ZLColor(11599619), new ZLColor(11599619),
				"def");
	}

	public void test2b_5_01() {
		runTask2b("1232342351263423745612", null, null,
				"1232342351263423745612");
	}

	public void test2b_5_02() {
		runTask2b("1232342351263423745612", null, new ZLColor(0), "def");
	}

	public void test2b_5_03() {
		runTask2b("1232342351263423745612", null, new ZLColor(11599616),
				"11599616");
	}

	public void test2b_5_04() {
		runTask2b("1232342351263423745612", new ZLColor(0), null,
				"1232342351263423745612");
	}

	public void test2b_5_05() {
		runTask2b("1232342351263423745612", new ZLColor(0), new ZLColor(0),
				"def");
	}

	public void test2b_5_06() {
		runTask2b("1232342351263423745612", new ZLColor(0), new ZLColor(
				11599616), "11599616");
	}

	public void test2b_5_07() {
		runTask2b("1232342351263423745612", new ZLColor(11599617), null,
				"1232342351263423745612");
	}

	public void test2b_5_08() {
		runTask2b("1232342351263423745612", new ZLColor(11599618), new ZLColor(
				0), "0");
	}

	public void test2b_5_09() {
		runTask2b("1232342351263423745612", new ZLColor(11599619), new ZLColor(
				11599620), "11599620");
	}

	public void test2b_5_10() {
		runTask2b("1232342351263423745612", new ZLColor(11599619), new ZLColor(
				11599619), "def");
	}

	public void test2b_6_01() {
		runTask2b("ерунда", null, null, "ерунда");
	}

	public void test2b_6_02() {
		runTask2b("ерунда", null, new ZLColor(0), "def");
	}

	public void test2b_6_03() {
		runTask2b("ерунда", null, new ZLColor(11599616), "11599616");
	}

	public void test2b_6_04() {
		runTask2b("ерунда", new ZLColor(0), null, "ерунда");
	}

	public void test2b_6_05() {
		runTask2b("ерунда", new ZLColor(0), new ZLColor(0), "def");
	}

	public void test2b_6_06() {
		runTask2b("ерунда", new ZLColor(0), new ZLColor(11599616), "11599616");
	}

	public void test2b_6_07() {
		runTask2b("ерунда", new ZLColor(11599617), null, "ерунда");
	}

	public void test2b_6_08() {
		runTask2b("ерунда", new ZLColor(11599618), new ZLColor(0), "0");
	}

	public void test2b_6_09() {
		runTask2b("ерунда", new ZLColor(11599619), new ZLColor(11599620),
				"11599620");
	}

	public void test2b_6_10() {
		runTask2b("ерунда", new ZLColor(11599619), new ZLColor(11599619), "def");
	}
}
