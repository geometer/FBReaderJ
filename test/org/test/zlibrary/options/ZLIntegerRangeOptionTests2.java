package org.test.zlibrary.options;

import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.config.ZLConfig;

public class ZLIntegerRangeOptionTests2 extends ZLOptionTests {

	private void runTask2a(String configValue, int value2, int value3,
			int expectedValue) {
		getConfig().setValue("integer_range_group_2", "name", configValue,
				"integer_range_category");
		ZLIntegerRangeOption option = new ZLIntegerRangeOption(
				"integer_range_category", "integer_range_group_2", "name",
				-100, 100, value2);
		option.setValue(value3);
		assertEquals(option.getValue(), expectedValue);
	}

	private void runTask2b(String configValue, int value2, int value3,
			String expectedValue) {
		getConfig().setValue("integer_range_group_2", "name", configValue,
				"integer_range_category");
		ZLIntegerRangeOption option = new ZLIntegerRangeOption(
				"integer_range_category", "integer_range_group_2", "name",
				-100, 100, value2);
		option.setValue(value3);
		assertEquals(getConfig().getValue("integer_range_group_2", "name", "def"),
				expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("integer_range_group_2", "name");
	}

	public void test2a_1_01() {
		runTask2a(null, 0, 0, 0);
	}

	public void test2a_1_02() {
		runTask2a(null, 0, 1, 1);
	}

	public void test2a_1_03() {
		runTask2a(null, 0, 200, 100);
	}
	
	public void test2a_1_04() {
		runTask2a(null, 0, -200, -100);
	}

	public void test2a_1_05() {
		runTask2a(null, 1, 0, 0);
	}

	public void test2a_1_06() {
		runTask2a(null, 1, 1, 1);
	}

	public void test2a_1_07() {
		runTask2a(null, 1, 200, 100);
	}

	public void test2a_1_08() {
		runTask2a(null, 1, -200, -100);
	}
	
	public void test2a_1_09() {
		runTask2a(null, 200, 0, 0);
	}

	public void test2a_1_10() {
		runTask2a(null, 200, 1, 1);
	}

	public void test2a_1_11() {
		runTask2a(null, 200, 200, 100);
	}

	public void test2a_1_12() {
		runTask2a(null, 200, -200, -100);
	}
	
	public void test2a_1_13() {
		runTask2a(null, -200, 0, 0);
	}

	public void test2a_1_14() {
		runTask2a(null, -200, 1, 1);
	}

	public void test2a_1_15() {
		runTask2a(null, -200, 200, 100);
	}

	public void test2a_1_16() {
		runTask2a(null, -200, -200, -100);
	}
	
	public void test2a_2_01() {
		runTask2a("", 0, 0, 0);
	}
	
	public void test2a_2_02() {
		runTask2a("", 0, 1, 1);
	}

	public void test2a_2_03() {
		runTask2a("", 0, 200, 100);
	}
	
	public void test2a_2_04() {
		runTask2a("", 0, -200, -100);
	}

	public void test2a_2_05() {
		runTask2a("", 1, 0, 0);
	}

	public void test2a_2_06() {
		runTask2a("", 1, 1, 1);
	}

	public void test2a_2_07() {
		runTask2a("", 1, 200, 100);
	}

	public void test2a_2_08() {
		runTask2a("", 1, -200, -100);
	}
	
	public void test2a_2_09() {
		runTask2a("", 200, 0, 0);
	}

	public void test2a_2_10() {
		runTask2a("", 200, 1, 1);
	}

	public void test2a_2_11() {
		runTask2a("", 200, 200, 100);
	}

	public void test2a_2_12() {
		runTask2a("", 200, -200, -100);
	}
	
	public void test2a_2_13() {
		runTask2a("", -200, 0, 0);
	}

	public void test2a_2_14() {
		runTask2a("", -200, 1, 1);
	}

	public void test2a_2_15() {
		runTask2a("", -200, 200, 100);
	}

	public void test2a_2_16() {
		runTask2a("", -200, -200, -100);
	}
	
	public void test2a_3_01() {
		runTask2a("0", 0, 0, 0);
	}

	public void test2a_3_02() {
		runTask2a("0", 0, 1, 1);
	}

	public void test2a_3_03() {
		runTask2a("0", 0, 200, 100);
	}
	
	public void test2a_3_04() {
		runTask2a("0", 0, -200, -100);
	}

	public void test2a_3_05() {
		runTask2a("0", 1, 0, 0);
	}

	public void test2a_3_06() {
		runTask2a("0", 1, 1, 1);
	}

	public void test2a_3_07() {
		runTask2a("0", 1, 200, 100);
	}

	public void test2a_3_08() {
		runTask2a("0", 1, -200, -100);
	}
	
	public void test2a_3_09() {
		runTask2a("0", 200, 0, 0);
	}

	public void test2a_3_10() {
		runTask2a("0", 200, 1, 1);
	}

	public void test2a_3_11() {
		runTask2a("0", 200, 200, 100);
	}

	public void test2a_3_12() {
		runTask2a("0", 200, -200, -100);
	}
	
	public void test2a_3_13() {
		runTask2a("0", -200, 0, 0);
	}

	public void test2a_3_14() {
		runTask2a("0", -200, 1, 1);
	}

	public void test2a_3_15() {
		runTask2a("0", -200, 200, 100);
	}

	public void test2a_3_16() {
		runTask2a("0", -200, -200, -100);
	}
	
	public void test2a_4_01() {
		runTask2a("1", 0, 0, 0);
	}

	public void test2a_4_02() {
		runTask2a("1", 0, 1, 1);
	}

	public void test2a_4_03() {
		runTask2a("1", 0, 200, 100);
	}
	
	public void test2a_4_04() {
		runTask2a("1", 0, -200, -100);
	}

	public void test2a_4_05() {
		runTask2a("1", 1, 0, 0);
	}

	public void test2a_4_06() {
		runTask2a("1", 1, 1, 1);
	}

	public void test2a_4_07() {
		runTask2a("1", 1, 200, 100);
	}

	public void test2a_4_08() {
		runTask2a("1", 1, -200, -100);
	}
	
	public void test2a_4_09() {
		runTask2a("1", 200, 0, 0);
	}

	public void test2a_4_10() {
		runTask2a("1", 200, 1, 1);
	}

	public void test2a_4_11() {
		runTask2a("1", 200, 200, 100);
	}

	public void test2a_4_12() {
		runTask2a("1", 200, -200, -100);
	}
	
	public void test2a_4_13() {
		runTask2a("1", -200, 0, 0);
	}

	public void test2a_4_14() {
		runTask2a("1", -200, 1, 1);
	}

	public void test2a_4_15() {
		runTask2a("1", -200, 200, 100);
	}

	public void test2a_4_16() {
		runTask2a("1", -200, -200, -100);
	}
	
	public void test2a_5_01() {
		runTask2a("200", 0, 0, 0);
	}

	public void test2a_5_02() {
		runTask2a("200", 0, 1, 1);
	}

	public void test2a_5_03a() {
		runTask2a("200", 0, 200, 100);
	}
	
	public void test2a_5_04() {
		runTask2a("200", 0, -200, -100);
	}

	public void test2a_5_05() {
		runTask2a("200", 1, 0, 0);
	}

	public void test2a_5_06() {
		runTask2a("200", 1, 1, 1);
	}

	public void test2a_5_07() {
		runTask2a("200", 1, 200, 100);
	}

	public void test2a_5_08() {
		runTask2a("200", 1, -200, -100);
	}
	
	public void test2a_5_09() {
		runTask2a("200", 200, 0, 0);
	}

	public void test2a_5_10() {
		runTask2a("200", 200, 1, 1);
	}

	public void test2a_5_11() {
		runTask2a("200", 200, 200, 100);
	}

	public void test2a_5_12() {
		runTask2a("200", 200, -200, -100);
	}
	
	public void test2a_5_13() {
		runTask2a("200", -200, 0, 0);
	}

	public void test2a_5_14() {
		runTask2a("200", -200, 1, 1);
	}

	public void test2a_5_15() {
		runTask2a("200", -200, 200, 100);
	}

	public void test2a_5_16() {
		runTask2a("200", -200, -200, -100);
	}
	
	public void test2a_6_01() {
		runTask2a("-200", 0, 0, 0);
	}

	public void test2a_6_02() {
		runTask2a("-200", 0, 1, 1);
	}

	public void test2a_6_03a() {
		runTask2a("-200", 0, 200, 100);
	}
	
	public void test2a_6_04() {
		runTask2a("-200", 0, -200, -100);
	}

	public void test2a_6_05() {
		runTask2a("-200", 1, 0, 0);
	}

	public void test2a_6_06() {
		runTask2a("-200", 1, 1, 1);
	}

	public void test2a_6_07() {
		runTask2a("-200", 1, 200, 100);
	}

	public void test2a_6_08() {
		runTask2a("-200", 1, -200, -100);
	}
	
	public void test2a_6_09() {
		runTask2a("-200", 200, 0, 0);
	}

	public void test2a_6_10() {
		runTask2a("-200", 200, 1, 1);
	}

	public void test2a_6_11() {
		runTask2a("-200", 200, 200, 100);
	}

	public void test2a_6_12() {
		runTask2a("-200", 200, -200, -100);
	}
	
	public void test2a_6_13() {
		runTask2a("-200", -200, 0, 0);
	}

	public void test2a_6_14() {
		runTask2a("-200", -200, 1, 1);
	}

	public void test2a_6_15() {
		runTask2a("-200", -200, 200, 100);
	}

	public void test2a_6_16() {
		runTask2a("-200", -200, -200, -100);
	}
	
	public void test2a_7_01() {
		runTask2a("ерунда", 0, 0, 0);
	}

	public void test2a_7_02() {
		runTask2a("ерунда", 0, 1, 1);
	}

	public void test2a_7_03a() {
		runTask2a("ерунда", 0, 200, 100);
	}
	
	public void test2a_7_04() {
		runTask2a("ерунда", 0, -200, -100);
	}

	public void test2a_7_05() {
		runTask2a("ерунда", 1, 0, 0);
	}

	public void test2a_7_06() {
		runTask2a("ерунда", 1, 1, 1);
	}

	public void test2a_7_07() {
		runTask2a("ерунда", 1, 200, 100);
	}

	public void test2a_7_08() {
		runTask2a("ерунда", 1, -200, -100);
	}
	
	public void test2a_7_09() {
		runTask2a("ерунда", 200, 0, 0);
	}

	public void test2a_7_10() {
		runTask2a("ерунда", 200, 1, 1);
	}

	public void test2a_7_11() {
		runTask2a("ерунда", 200, 200, 100);
	}

	public void test2a_7_12() {
		runTask2a("ерунда", 200, -200, -100);
	}
	
	public void test2a_7_13() {
		runTask2a("ерунда", -200, 0, 0);
	}

	public void test2a_7_14() {
		runTask2a("ерунда", -200, 1, 1);
	}

	public void test2a_7_15() {
		runTask2a("ерунда", -200, 200, 100);
	}

	public void test2a_7_16() {
		runTask2a("ерунда", -200, -200, -100);
	}
	
	public void test2b_1_01() {
		runTask2b(null, 0, 0, "def");
	}

	public void test2b_1_02() {
		runTask2b(null, 0, 1, "1");
	}

	public void test2b_1_03() {
		runTask2b(null, 0, 200, "100");
	}
	
	public void test2b_1_04() {
		runTask2b(null, 0, -200, "-100");
	}

	public void test2b_1_05() {
		runTask2b(null, 1, 0, "0");
	}

	public void test2b_1_06() {
		runTask2b(null, 1, 1, "def");
	}

	public void test2b_1_07() {
		runTask2b(null, 1, 200, "100");
	}

	public void test2b_1_08() {
		runTask2b(null, 1, -200, "-100");
	}
	
	public void test2b_1_09() {
		runTask2b(null, 200, 0, "0");
	}

	public void test2b_1_10() {
		runTask2b(null, 200, 1, "1");
	}

	public void test2b_1_11() {
		runTask2b(null, 200, 200, "def");
	}

	public void test2b_1_12() {
		runTask2b(null, 200, -200, "-100");
	}
	
	public void test2b_1_13() {
		runTask2b(null, -200, 0, "0");
	}

	public void test2b_1_14() {
		runTask2b(null, -200, 1, "1");
	}

	public void test2b_1_15() {
		runTask2b(null, -200, 200, "100");
	}

	public void test2b_1_16() {
		runTask2b(null, -200, -200, "def");
	}
	
	public void test2b_2_01() {
		runTask2b("", 0, 0, "def");
	}
	
	public void test2b_2_02() {
		runTask2b("", 0, 1, "1");
	}

	public void test2b_2_03() {
		runTask2b("", 0, 200, "100");
	}
	
	public void test2b_2_04() {
		runTask2b("", 0, -200, "-100");
	}

	public void test2b_2_05() {
		runTask2b("", 1, 0, "0");
	}

	public void test2b_2_06() {
		runTask2b("", 1, 1, "def");
	}

	public void test2b_2_07() {
		runTask2b("", 1, 200, "100");
	}

	public void test2b_2_08() {
		runTask2b("", 1, -200, "-100");
	}
	
	public void test2b_2_09() {
		runTask2b("", 200, 0, "0");
	}

	public void test2b_2_10() {
		runTask2b("", 200, 1, "1");
	}

	public void test2b_2_11() {
		runTask2b("", 200, 200, "def");
	}

	public void test2b_2_12() {
		runTask2b("", 200, -200, "-100");
	}
	
	public void test2b_2_13() {
		runTask2b("", -200, 0, "0");
	}

	public void test2b_2_14() {
		runTask2b("", -200, 1, "1");
	}

	public void test2b_2_15() {
		runTask2b("", -200, 200, "100");
	}

	public void test2b_2_16() {
		runTask2b("", -200, -200, "def");
	}
	
	public void test2b_3_01() {
		runTask2b("0", 0, 0, "def");
	}

	public void test2b_3_02() {
		runTask2b("0", 0, 1, "1");
	}

	public void test2b_3_03() {
		runTask2b("0", 0, 200, "100");
	}
	
	public void test2b_3_04() {
		runTask2b("0", 0, -200, "-100");
	}

	public void test2b_3_05() {
		runTask2b("0", 1, 0, "0");
	}

	public void test2b_3_06() {
		runTask2b("0", 1, 1, "def");
	}

	public void test2b_3_07() {
		runTask2b("0", 1, 200, "100");
	}

	public void test2b_3_08() {
		runTask2b("0", 1, -200, "-100");
	}
	
	public void test2b_3_09() {
		runTask2b("0", 200, 0, "0");
	}

	public void test2b_3_10() {
		runTask2b("0", 200, 1, "1");
	}

	public void test2b_3_11() {
		runTask2b("0", 200, 200, "def");
	}

	public void test2b_3_12() {
		runTask2b("0", 200, -200, "-100");
	}
	
	public void test2b_3_13() {
		runTask2b("0", -200, 0, "0");
	}

	public void test2b_3_14() {
		runTask2b("0", -200, 1, "1");
	}

	public void test2b_3_15() {
		runTask2b("0", -200, 200, "100");
	}

	public void test2b_3_16() {
		runTask2b("0", -200, -200, "def");
	}
	
	public void test2b_4_01() {
		runTask2b("1", 0, 0, "def");
	}

	public void test2b_4_02() {
		runTask2b("1", 0, 1, "1");
	}

	public void test2b_4_03() {
		runTask2b("1", 0, 200, "100");
	}
	
	public void test2b_4_04() {
		runTask2b("1", 0, -200, "-100");
	}

	public void test2b_4_05() {
		runTask2b("1", 1, 0, "0");
	}

	public void test2b_4_06() {
		runTask2b("1", 1, 1, "def");
	}

	public void test2b_4_07() {
		runTask2b("1", 1, 200, "100");
	}

	public void test2b_4_08() {
		runTask2b("1", 1, -200, "-100");
	}
	
	public void test2b_4_09() {
		runTask2b("1", 200, 0, "0");
	}

	public void test2b_4_10() {
		runTask2b("1", 200, 1, "1");
	}

	public void test2b_4_11() {
		runTask2b("1", 200, 200, "def");
	}

	public void test2b_4_12() {
		runTask2b("1", 200, -200, "-100");
	}
	
	public void test2b_4_13() {
		runTask2b("1", -200, 0, "0");
	}

	public void test2b_4_14() {
		runTask2b("1", -200, 1, "1");
	}

	public void test2b_4_15() {
		runTask2b("1", -200, 200, "100");
	}

	public void test2b_4_16() {
		runTask2b("1", -200, -200, "def");
	}
	
	public void test2b_5_01() {
		runTask2b("200", 0, 0, "def");
	}

	public void test2b_5_02() {
		runTask2b("200", 0, 1, "1");
	}

	public void test2b_5_03a() {
		runTask2b("200", 0, 200, "100");
	}
	
	public void test2b_5_04() {
		runTask2b("200", 0, -200, "-100");
	}

	public void test2b_5_05() {
		runTask2b("200", 1, 0, "0");
	}

	public void test2b_5_06() {
		runTask2b("200", 1, 1, "def");
	}

	public void test2b_5_07() {
		runTask2b("200", 1, 200, "100");
	}

	public void test2b_5_08() {
		runTask2b("200", 1, -200, "-100");
	}
	
	public void test2b_5_09() {
		runTask2b("200", 200, 0, "0");
	}

	public void test2b_5_10() {
		runTask2b("200", 200, 1, "1");
	}

	public void test2b_5_11() {
		runTask2b("200", 200, 200, "def");
	}

	public void test2b_5_12() {
		runTask2b("200", 200, -200, "-100");
	}
	
	public void test2b_5_13() {
		runTask2b("200", -200, 0, "0");
	}

	public void test2b_5_14() {
		runTask2b("200", -200, 1, "1");
	}

	public void test2b_5_15() {
		runTask2b("200", -200, 200, "100");
	}

	public void test2b_5_16() {
		runTask2b("200", -200, -200, "def");
	}
	
	public void test2b_6_01() {
		runTask2b("-200", 0, 0, "def");
	}

	public void test2b_6_02() {
		runTask2b("-200", 0, 1, "1");
	}

	public void test2b_6_03a() {
		runTask2b("-200", 0, 200, "100");
	}
	
	public void test2b_6_04() {
		runTask2b("-200", 0, -200, "-100");
	}

	public void test2b_6_05() {
		runTask2b("-200", 1, 0, "0");
	}

	public void test2b_6_06() {
		runTask2b("-200", 1, 1, "def");
	}

	public void test2b_6_07() {
		runTask2b("-200", 1, 200, "100");
	}

	public void test2b_6_08() {
		runTask2b("-200", 1, -200, "-100");
	}
	
	public void test2b_6_09() {
		runTask2b("-200", 200, 0, "0");
	}

	public void test2b_6_10() {
		runTask2b("-200", 200, 1, "1");
	}

	public void test2b_6_11() {
		runTask2b("-200", 200, 200, "def");
	}

	public void test2b_6_12() {
		runTask2b("-200", 200, -200, "-100");
	}
	
	public void test2b_6_13() {
		runTask2b("-200", -200, 0, "0");
	}

	public void test2b_6_14() {
		runTask2b("-200", -200, 1, "1");
	}

	public void test2b_6_15() {
		runTask2b("-200", -200, 200, "100");
	}

	public void test2b_6_16() {
		runTask2b("-200", -200, -200, "def");
	}
	
	public void test2b_7_01() {
		runTask2b("ерунда", 0, 0, "def");
	}

	public void test2b_7_02() {
		runTask2b("ерунда", 0, 1, "1");
	}

	public void test2b_7_03a() {
		runTask2b("ерунда", 0, 200, "100");
	}
	
	public void test2b_7_04() {
		runTask2b("ерунда", 0, -200, "-100");
	}

	public void test2b_7_05() {
		runTask2b("ерунда", 1, 0, "0");
	}

	public void test2b_7_06() {
		runTask2b("ерунда", 1, 1, "def");
	}

	public void test2b_7_07() {
		runTask2b("ерунда", 1, 200, "100");
	}

	public void test2b_7_08() {
		runTask2b("ерунда", 1, -200, "-100");
	}
	
	public void test2b_7_09() {
		runTask2b("ерунда", 200, 0, "0");
	}

	public void test2b_7_10() {
		runTask2b("ерунда", 200, 1, "1");
	}

	public void test2b_7_11() {
		runTask2b("ерунда", 200, 200, "def");
	}

	public void test2b_7_12() {
		runTask2b("ерунда", 200, -200, "-100");
	}
	
	public void test2b_7_13() {
		runTask2b("ерунда", -200, 0, "0");
	}

	public void test2b_7_14() {
		runTask2b("ерунда", -200, 1, "1");
	}

	public void test2b_7_15() {
		runTask2b("ерунда", -200, 200, "100");
	}

	public void test2b_7_16() {
		runTask2b("ерунда", -200, -200, "def");
	}
	
}
