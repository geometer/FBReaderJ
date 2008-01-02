package org.test.zlibrary.options;

import org.zlibrary.core.options.ZLDoubleOption;
import org.zlibrary.core.config.ZLConfig;

public class ZLDoubleOptionTests2b extends ZLOptionTests {

	private void runTask2b(String configValue, double value2, double value3,
			String expectedValue) {
		getConfig().setValue("double_group_2b", "name", configValue, "category");
		ZLDoubleOption option = new ZLDoubleOption("category",
				"double_group_2b", "name", value2);
		option.setValue(value3);
		assertEquals(getConfig().getValue("double_group_2b", "name", "def"),
				expectedValue);
	}

	public void tearDown() {
		getConfig().unsetValue("double_group_2b", "name");
	}

	public void test2b_1_01() {
		runTask2b(null, 0, 0, "def");
	}

	public void test2b_1_02() {
		runTask2b(null, 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_1_03() {
		runTask2b(null, 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_1_04() {
		runTask2b(null, 0, Double.NaN, "NaN");
	}

	public void test2b_1_05() {
		runTask2b(null, 0, 1.0, "1.0");
	}

	public void test2b_1_06() {
		runTask2b(null, 0, +0.0, "def");
	}

	public void test2b_1_07() {
		runTask2b(null, 0, -0.0, "def");
	}

	public void test2b_1_08() {
		runTask2b(null, Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_1_09() {
		runTask2b(null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"def");
	}

	public void test2b_1_10() {
		runTask2b(null, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_1_11() {
		runTask2b(null, Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_1_12() {
		runTask2b(null, Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_1_13() {
		runTask2b(null, Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_1_14() {
		runTask2b(null, Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_1_15() {
		runTask2b(null, Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_1_16() {
		runTask2b(null, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				"Infinity");
	}

	public void test2b_1_17() {
		runTask2b(null, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"def");
	}

	public void test2b_1_18() {
		runTask2b(null, Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_1_19() {
		runTask2b(null, Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_1_20() {
		runTask2b(null, Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_1_21() {
		runTask2b(null, Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_1_22() {
		runTask2b(null, Double.NaN, 0, "0.0");
	}

	public void test2b_1_23() {
		runTask2b(null, Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_1_24() {
		runTask2b(null, Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_1_25() {
		runTask2b(null, Double.NaN, Double.NaN, "def");
	}

	public void test2b_1_26() {
		runTask2b(null, Double.NaN, 1.0, "1.0");
	}

	public void test2b_1_27() {
		runTask2b(null, Double.NaN, +0.0, "0.0");
	}

	public void test2b_1_28() {
		runTask2b(null, Double.NaN, -0.0, "-0.0");
	}

	public void test2b_1_29() {
		runTask2b(null, 1.0, 0, "0.0");
	}

	public void test2b_1_30() {
		runTask2b(null, 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_1_31() {
		runTask2b(null, 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_1_32() {
		runTask2b(null, 1.0, Double.NaN, "NaN");
	}

	public void test2b_1_33() {
		runTask2b(null, 1.0, 1.0, "def");
	}

	public void test2b_1_34() {
		runTask2b(null, 1.0, +0.0, "0.0");
	}

	public void test2b_1_35() {
		runTask2b(null, 1.0, -0.0, "-0.0");
	}

	public void test2b_1_36() {
		runTask2b(null, +0.0, 0, "def");
	}

	public void test2b_1_37() {
		runTask2b(null, +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_1_38() {
		runTask2b(null, +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_1_39() {
		runTask2b(null, +0.0, Double.NaN, "NaN");
	}

	public void test2b_1_40() {
		runTask2b(null, +0.0, 1.0, "1.0");
	}

	public void test2b_1_41() {
		runTask2b(null, +0.0, +0.0, "def");
	}

	public void test2b_1_42() {
		runTask2b(null, +0.0, -0.0, "def");
	}

	public void test2b_2_01() {
		runTask2b("", 0, 0, "def");
	}

	public void test2b_2_02() {
		runTask2b("", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_2_03() {
		runTask2b("", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_2_04() {
		runTask2b("", 0, Double.NaN, "NaN");
	}

	public void test2b_2_05() {
		runTask2b("", 0, 1.0, "1.0");
	}

	public void test2b_2_06() {
		runTask2b("", 0, +0.0, "def");
	}

	public void test2b_2_07() {
		runTask2b("", 0, -0.0, "def");
	}

	public void test2b_2_08() {
		runTask2b("", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_2_09() {
		runTask2b("", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, "def");
	}

	public void test2b_2_10() {
		runTask2b("", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_2_11() {
		runTask2b("", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_2_12() {
		runTask2b("", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_2_13() {
		runTask2b("", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_2_14() {
		runTask2b("", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_2_15() {
		runTask2b("", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_2_16() {
		runTask2b("", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				"Infinity");
	}

	public void test2b_2_17() {
		runTask2b("", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, "def");
	}

	public void test2b_2_18() {
		runTask2b("", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_2_19() {
		runTask2b("", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_2_20() {
		runTask2b("", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_2_21() {
		runTask2b("", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_2_22() {
		runTask2b("", Double.NaN, 0, "0.0");
	}

	public void test2b_2_23() {
		runTask2b("", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_2_24() {
		runTask2b("", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_2_25() {
		runTask2b("", Double.NaN, Double.NaN, "def");
	}

	public void test2b_2_26() {
		runTask2b("", Double.NaN, 1.0, "1.0");
	}

	public void test2b_2_27() {
		runTask2b("", Double.NaN, +0.0, "0.0");
	}

	public void test2b_2_28() {
		runTask2b("", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_2_29() {
		runTask2b("", 1.0, 0, "0.0");
	}

	public void test2b_2_30() {
		runTask2b("", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_2_31() {
		runTask2b("", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_2_32() {
		runTask2b("", 1.0, Double.NaN, "NaN");
	}

	public void test2b_2_33() {
		runTask2b("", 1.0, 1.0, "def");
	}

	public void test2b_2_34() {
		runTask2b("", 1.0, +0.0, "0.0");
	}

	public void test2b_2_35() {
		runTask2b("", 1.0, -0.0, "-0.0");
	}

	public void test2b_2_36() {
		runTask2b("", +0.0, 0, "def");
	}

	public void test2b_2_37() {
		runTask2b("", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_2_38() {
		runTask2b("", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_2_39() {
		runTask2b("", +0.0, Double.NaN, "NaN");
	}

	public void test2b_2_40() {
		runTask2b("", +0.0, 1.0, "1.0");
	}

	public void test2b_2_41() {
		runTask2b("", +0.0, +0.0, "def");
	}

	public void test2b_2_42() {
		runTask2b("", +0.0, -0.0, "def");
	}

	public void test2b_3_01() {
		runTask2b("Infinity", 0, 0, "def");
	}

	public void test2b_3_02() {
		runTask2b("Infinity", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_3_03() {
		runTask2b("Infinity", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_3_04() {
		runTask2b("Infinity", 0, Double.NaN, "NaN");
	}

	public void test2b_3_05() {
		runTask2b("Infinity", 0, 1.0, "1.0");
	}

	public void test2b_3_06() {
		runTask2b("Infinity", 0, +0.0, "def");
	}

	public void test2b_3_07() {
		runTask2b("Infinity", 0, -0.0, "def");
	}

	public void test2b_3_08() {
		runTask2b("Infinity", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_3_09() {
		runTask2b("Infinity", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, "def");
	}

	public void test2b_3_10() {
		runTask2b("Infinity", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_3_11() {
		runTask2b("Infinity", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_3_12() {
		runTask2b("Infinity", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_3_13() {
		runTask2b("Infinity", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_3_14() {
		runTask2b("Infinity", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_3_15() {
		runTask2b("Infinity", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_3_16() {
		runTask2b("Infinity", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_3_17() {
		runTask2b("Infinity", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "def");
	}

	public void test2b_3_18() {
		runTask2b("Infinity", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_3_19() {
		runTask2b("Infinity", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_3_20() {
		runTask2b("Infinity", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_3_21() {
		runTask2b("Infinity", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_3_22() {
		runTask2b("Infinity", Double.NaN, 0, "0.0");
	}

	public void test2b_3_23() {
		runTask2b("Infinity", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_3_24() {
		runTask2b("Infinity", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_3_25() {
		runTask2b("Infinity", Double.NaN, Double.NaN, "def");
	}

	public void test2b_3_26() {
		runTask2b("Infinity", Double.NaN, 1.0, "1.0");
	}

	public void test2b_3_27() {
		runTask2b("Infinity", Double.NaN, +0.0, "0.0");
	}

	public void test2b_3_28() {
		runTask2b("Infinity", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_3_29() {
		runTask2b("Infinity", 1.0, 0, "0.0");
	}

	public void test2b_3_30() {
		runTask2b("Infinity", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_3_31() {
		runTask2b("Infinity", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_3_32() {
		runTask2b("Infinity", 1.0, Double.NaN, "NaN");
	}

	public void test2b_3_33() {
		runTask2b("Infinity", 1.0, 1.0, "def");
	}

	public void test2b_3_34() {
		runTask2b("Infinity", 1.0, +0.0, "0.0");
	}

	public void test2b_3_35() {
		runTask2b("Infinity", 1.0, -0.0, "-0.0");
	}

	public void test2b_3_36() {
		runTask2b("Infinity", +0.0, 0, "def");
	}

	public void test2b_3_37() {
		runTask2b("Infinity", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_3_38() {
		runTask2b("Infinity", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_3_39() {
		runTask2b("Infinity", +0.0, Double.NaN, "NaN");
	}

	public void test2b_3_40() {
		runTask2b("Infinity", +0.0, 1.0, "1.0");
	}

	public void test2b_3_41() {
		runTask2b("Infinity", +0.0, +0.0, "def");
	}

	public void test2b_3_42() {
		runTask2b("Infinity", +0.0, -0.0, "def");
	}

	public void test2b_4_01() {
		runTask2b("-Infinity", 0, 0, "def");
	}

	public void test2b_4_02() {
		runTask2b("-Infinity", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_4_03() {
		runTask2b("-Infinity", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_4_04() {
		runTask2b("-Infinity", 0, Double.NaN, "NaN");
	}

	public void test2b_4_05() {
		runTask2b("-Infinity", 0, 1.0, "1.0");
	}

	public void test2b_4_06() {
		runTask2b("-Infinity", 0, +0.0, "def");
	}

	public void test2b_4_07() {
		runTask2b("-Infinity", 0, -0.0, "def");
	}

	public void test2b_4_08() {
		runTask2b("-Infinity", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_4_09() {
		runTask2b("-Infinity", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, "def");
	}

	public void test2b_4_10() {
		runTask2b("-Infinity", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_4_11() {
		runTask2b("-Infinity", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_4_12() {
		runTask2b("-Infinity", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_4_13() {
		runTask2b("-Infinity", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_4_14() {
		runTask2b("-Infinity", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_4_15() {
		runTask2b("-Infinity", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_4_16() {
		runTask2b("-Infinity", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_4_17() {
		runTask2b("-Infinity", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "def");
	}

	public void test2b_4_18() {
		runTask2b("-Infinity", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_4_19() {
		runTask2b("-Infinity", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_4_20() {
		runTask2b("-Infinity", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_4_21() {
		runTask2b("-Infinity", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_4_22() {
		runTask2b("-Infinity", Double.NaN, 0, "0.0");
	}

	public void test2b_4_23() {
		runTask2b("-Infinity", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_4_24() {
		runTask2b("-Infinity", Double.NaN, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_4_25() {
		runTask2b("-Infinity", Double.NaN, Double.NaN, "def");
	}

	public void test2b_4_26() {
		runTask2b("-Infinity", Double.NaN, 1.0, "1.0");
	}

	public void test2b_4_27() {
		runTask2b("-Infinity", Double.NaN, +0.0, "0.0");
	}

	public void test2b_4_28() {
		runTask2b("-Infinity", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_4_29() {
		runTask2b("-Infinity", 1.0, 0, "0.0");
	}

	public void test2b_4_30() {
		runTask2b("-Infinity", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_4_31() {
		runTask2b("-Infinity", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_4_32() {
		runTask2b("-Infinity", 1.0, Double.NaN, "NaN");
	}

	public void test2b_4_33() {
		runTask2b("-Infinity", 1.0, 1.0, "def");
	}

	public void test2b_4_34() {
		runTask2b("-Infinity", 1.0, +0.0, "0.0");
	}

	public void test2b_4_35() {
		runTask2b("-Infinity", 1.0, -0.0, "-0.0");
	}

	public void test2b_4_36() {
		runTask2b("-Infinity", +0.0, 0, "def");
	}

	public void test2b_4_37() {
		runTask2b("-Infinity", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_4_38() {
		runTask2b("-Infinity", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_4_39() {
		runTask2b("-Infinity", +0.0, Double.NaN, "NaN");
	}

	public void test2b_4_40() {
		runTask2b("-Infinity", +0.0, 1.0, "1.0");
	}

	public void test2b_4_41() {
		runTask2b("-Infinity", +0.0, +0.0, "def");
	}

	public void test2b_4_42() {
		runTask2b("-Infinity", +0.0, -0.0, "def");
	}

	public void test2b_5_01() {
		runTask2b("NaN", 0, 0, "def");
	}

	public void test2b_5_02() {
		runTask2b("NaN", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_5_03() {
		runTask2b("NaN", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_5_04() {
		runTask2b("NaN", 0, Double.NaN, "NaN");
	}

	public void test2b_5_05() {
		runTask2b("NaN", 0, 1.0, "1.0");
	}

	public void test2b_5_06() {
		runTask2b("NaN", 0, +0.0, "def");
	}

	public void test2b_5_07() {
		runTask2b("NaN", 0, -0.0, "def");
	}

	public void test2b_5_08() {
		runTask2b("NaN", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_5_09() {
		runTask2b("NaN", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"def");
	}

	public void test2b_5_10() {
		runTask2b("NaN", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_5_11() {
		runTask2b("NaN", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_5_12() {
		runTask2b("NaN", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_5_13() {
		runTask2b("NaN", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_5_14() {
		runTask2b("NaN", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_5_15() {
		runTask2b("NaN", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_5_16() {
		runTask2b("NaN", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				"Infinity");
	}

	public void test2b_5_17() {
		runTask2b("NaN", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"def");
	}

	public void test2b_5_18() {
		runTask2b("NaN", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_5_19() {
		runTask2b("NaN", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_5_20() {
		runTask2b("NaN", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_5_21() {
		runTask2b("NaN", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_5_22() {
		runTask2b("NaN", Double.NaN, 0, "0.0");
	}

	public void test2b_5_23() {
		runTask2b("NaN", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_5_24() {
		runTask2b("NaN", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_5_25() {
		runTask2b("NaN", Double.NaN, Double.NaN, "def");
	}

	public void test2b_5_26() {
		runTask2b("NaN", Double.NaN, 1.0, "1.0");
	}

	public void test2b_5_27() {
		runTask2b("NaN", Double.NaN, +0.0, "0.0");
	}

	public void test2b_5_28() {
		runTask2b("NaN", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_5_29() {
		runTask2b("NaN", 1.0, 0, "0.0");
	}

	public void test2b_5_30() {
		runTask2b("NaN", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_5_31() {
		runTask2b("NaN", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_5_32() {
		runTask2b("NaN", 1.0, Double.NaN, "NaN");
	}

	public void test2b_5_33() {
		runTask2b("NaN", 1.0, 1.0, "def");
	}

	public void test2b_5_34() {
		runTask2b("NaN", 1.0, +0.0, "0.0");
	}

	public void test2b_5_35() {
		runTask2b("NaN", 1.0, -0.0, "-0.0");
	}

	public void test2b_5_36() {
		runTask2b("NaN", +0.0, 0, "def");
	}

	public void test2b_5_37() {
		runTask2b("NaN", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_5_38() {
		runTask2b("NaN", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_5_39() {
		runTask2b("NaN", +0.0, Double.NaN, "NaN");
	}

	public void test2b_5_40() {
		runTask2b("NaN", +0.0, 1.0, "1.0");
	}

	public void test2b_5_41() {
		runTask2b("NaN", +0.0, +0.0, "def");
	}

	public void test2b_5_42() {
		runTask2b("NaN", +0.0, -0.0, "def");
	}

	public void test2b_6_01() {
		runTask2b("0", 0, 0, "def");
	}

	public void test2b_6_02() {
		runTask2b("0", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_6_03() {
		runTask2b("0", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_6_04() {
		runTask2b("0", 0, Double.NaN, "NaN");
	}

	public void test2b_6_05() {
		runTask2b("0", 0, 1.0, "1.0");
	}

	public void test2b_6_06() {
		runTask2b("0", 0, +0.0, "def");
	}

	public void test2b_6_07() {
		runTask2b("0", 0, -0.0, "def");
	}

	public void test2b_6_08() {
		runTask2b("0", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_6_09() {
		runTask2b("0", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"def");
	}

	public void test2b_6_10() {
		runTask2b("0", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_6_11() {
		runTask2b("0", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_6_12() {
		runTask2b("0", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_6_13() {
		runTask2b("0", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_6_14() {
		runTask2b("0", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_6_15() {
		runTask2b("0", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_6_16() {
		runTask2b("0", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				"Infinity");
	}

	public void test2b_6_17() {
		runTask2b("0", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"def");
	}

	public void test2b_6_18() {
		runTask2b("0", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_6_19() {
		runTask2b("0", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_6_20() {
		runTask2b("0", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_6_21() {
		runTask2b("0", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_6_22() {
		runTask2b("0", Double.NaN, 0, "0.0");
	}

	public void test2b_6_23() {
		runTask2b("0", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_6_24() {
		runTask2b("0", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_6_25() {
		runTask2b("0", Double.NaN, Double.NaN, "def");
	}

	public void test2b_6_26() {
		runTask2b("0", Double.NaN, 1.0, "1.0");
	}

	public void test2b_6_27() {
		runTask2b("0", Double.NaN, +0.0, "0.0");
	}

	public void test2b_6_28() {
		runTask2b("0", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_6_29() {
		runTask2b("0", 1.0, 0, "0.0");
	}

	public void test2b_6_30() {
		runTask2b("0", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_6_31() {
		runTask2b("0", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_6_32() {
		runTask2b("0", 1.0, Double.NaN, "NaN");
	}

	public void test2b_6_33() {
		runTask2b("0", 1.0, 1.0, "def");
	}

	public void test2b_6_34() {
		runTask2b("0", 1.0, +0.0, "0.0");
	}

	public void test2b_6_35() {
		runTask2b("0", 1.0, -0.0, "-0.0");
	}

	public void test2b_6_36() {
		runTask2b("0", +0.0, 0, "def");
	}

	public void test2b_6_37() {
		runTask2b("0", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_6_38() {
		runTask2b("0", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_6_39() {
		runTask2b("0", +0.0, Double.NaN, "NaN");
	}

	public void test2b_6_40() {
		runTask2b("0", +0.0, 1.0, "1.0");
	}

	public void test2b_6_41() {
		runTask2b("0", +0.0, +0.0, "def");
	}

	public void test2b_6_42() {
		runTask2b("0", +0.0, -0.0, "def");
	}

	public void test2b_7_01() {
		runTask2b("1.1", 0, 0, "def");
	}

	public void test2b_7_02() {
		runTask2b("1.1", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_7_03() {
		runTask2b("1.1", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_7_04() {
		runTask2b("1.1", 0, Double.NaN, "NaN");
	}

	public void test2b_7_05() {
		runTask2b("1.1", 0, 1.0, "1.0");
	}

	public void test2b_7_06() {
		runTask2b("1.1", 0, +0.0, "def");
	}

	public void test2b_7_07() {
		runTask2b("1.1", 0, -0.0, "def");
	}

	public void test2b_7_08() {
		runTask2b("1.1", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_7_09() {
		runTask2b("1.1", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"def");
	}

	public void test2b_7_10() {
		runTask2b("1.1", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_7_11() {
		runTask2b("1.1", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_7_12() {
		runTask2b("1.1", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_7_13() {
		runTask2b("1.1", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_7_14() {
		runTask2b("1.1", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_7_15() {
		runTask2b("1.1", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_7_16() {
		runTask2b("1.1", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				"Infinity");
	}

	public void test2b_7_17() {
		runTask2b("1.1", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"def");
	}

	public void test2b_7_18() {
		runTask2b("1.1", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_7_19() {
		runTask2b("1.1", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_7_20() {
		runTask2b("1.1", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_7_21() {
		runTask2b("1.1", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_7_22() {
		runTask2b("1.1", Double.NaN, 0, "0.0");
	}

	public void test2b_7_23() {
		runTask2b("1.1", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_7_24() {
		runTask2b("1.1", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_7_25() {
		runTask2b("1.1", Double.NaN, Double.NaN, "def");
	}

	public void test2b_7_26() {
		runTask2b("1.1", Double.NaN, 1.0, "1.0");
	}

	public void test2b_7_27() {
		runTask2b("1.1", Double.NaN, +0.0, "0.0");
	}

	public void test2b_7_28() {
		runTask2b("1.1", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_7_29() {
		runTask2b("1.1", 1.0, 0, "0.0");
	}

	public void test2b_7_30() {
		runTask2b("1.1", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_7_31() {
		runTask2b("1.1", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_7_32() {
		runTask2b("1.1", 1.0, Double.NaN, "NaN");
	}

	public void test2b_7_33() {
		runTask2b("1.1", 1.0, 1.0, "def");
	}

	public void test2b_7_34() {
		runTask2b("1.1", 1.0, +0.0, "0.0");
	}

	public void test2b_7_35() {
		runTask2b("1.1", 1.0, -0.0, "-0.0");
	}

	public void test2b_7_36() {
		runTask2b("1.1", +0.0, 0, "def");
	}

	public void test2b_7_37() {
		runTask2b("1.1", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_7_38() {
		runTask2b("1.1", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_7_39() {
		runTask2b("1.1", +0.0, Double.NaN, "NaN");
	}

	public void test2b_7_40() {
		runTask2b("1.1", +0.0, 1.0, "1.0");
	}

	public void test2b_7_41() {
		runTask2b("1.1", +0.0, +0.0, "def");
	}

	public void test2b_7_42() {
		runTask2b("1.1", +0.0, -0.0, "def");
	}

	public void test2b_8_01() {
		runTask2b("4", 0, 0, "def");
	}

	public void test2b_8_02() {
		runTask2b("4", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_8_03() {
		runTask2b("4", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_8_04() {
		runTask2b("4", 0, Double.NaN, "NaN");
	}

	public void test2b_8_05() {
		runTask2b("4", 0, 1.0, "1.0");
	}

	public void test2b_8_06() {
		runTask2b("4", 0, +0.0, "def");
	}

	public void test2b_8_07() {
		runTask2b("4", 0, -0.0, "def");
	}

	public void test2b_8_08() {
		runTask2b("4", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_8_09() {
		runTask2b("4", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"def");
	}

	public void test2b_8_10() {
		runTask2b("4", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_8_11() {
		runTask2b("4", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_8_12() {
		runTask2b("4", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_8_13() {
		runTask2b("4", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_8_14() {
		runTask2b("4", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_8_15() {
		runTask2b("4", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_8_16() {
		runTask2b("4", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				"Infinity");
	}

	public void test2b_8_17() {
		runTask2b("4", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"def");
	}

	public void test2b_8_18() {
		runTask2b("4", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_8_19() {
		runTask2b("4", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_8_20() {
		runTask2b("4", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_8_21() {
		runTask2b("4", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_8_22() {
		runTask2b("4", Double.NaN, 0, "0.0");
	}

	public void test2b_8_23() {
		runTask2b("4", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_8_24() {
		runTask2b("4", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_8_25() {
		runTask2b("4", Double.NaN, Double.NaN, "def");
	}

	public void test2b_8_26() {
		runTask2b("4", Double.NaN, 1.0, "1.0");
	}

	public void test2b_8_27() {
		runTask2b("4", Double.NaN, +0.0, "0.0");
	}

	public void test2b_8_28() {
		runTask2b("4", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_8_29() {
		runTask2b("4", 1.0, 0, "0.0");
	}

	public void test2b_8_30() {
		runTask2b("4", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_8_31() {
		runTask2b("4", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_8_32() {
		runTask2b("4", 1.0, Double.NaN, "NaN");
	}

	public void test2b_8_33() {
		runTask2b("4", 1.0, 1.0, "def");
	}

	public void test2b_8_34() {
		runTask2b("4", 1.0, +0.0, "0.0");
	}

	public void test2b_8_35() {
		runTask2b("4", 1.0, -0.0, "-0.0");
	}

	public void test2b_8_36() {
		runTask2b("4", +0.0, 0, "def");
	}

	public void test2b_8_37() {
		runTask2b("4", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_8_38() {
		runTask2b("4", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_8_39() {
		runTask2b("4", +0.0, Double.NaN, "NaN");
	}

	public void test2b_8_40() {
		runTask2b("4", +0.0, 1.0, "1.0");
	}

	public void test2b_8_41() {
		runTask2b("4", +0.0, +0.0, "def");
	}

	public void test2b_8_42() {
		runTask2b("4", +0.0, -0.0, "def");
	}

	public void test2b_9_01() {
		runTask2b("12E-78", 0, 0, "def");
	}

	public void test2b_9_02() {
		runTask2b("12E-78", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_9_03() {
		runTask2b("12E-78", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_9_04() {
		runTask2b("12E-78", 0, Double.NaN, "NaN");
	}

	public void test2b_9_05() {
		runTask2b("12E-78", 0, 1.0, "1.0");
	}

	public void test2b_9_06() {
		runTask2b("12E-78", 0, +0.0, "def");
	}

	public void test2b_9_07() {
		runTask2b("12E-78", 0, -0.0, "def");
	}

	public void test2b_9_08() {
		runTask2b("12E-78", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_9_09() {
		runTask2b("12E-78", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"def");
	}

	public void test2b_9_10() {
		runTask2b("12E-78", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_9_11() {
		runTask2b("12E-78", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_9_12() {
		runTask2b("12E-78", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_9_13() {
		runTask2b("12E-78", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_9_14() {
		runTask2b("12E-78", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_9_15() {
		runTask2b("12E-78", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_9_16() {
		runTask2b("12E-78", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				"Infinity");
	}

	public void test2b_9_17() {
		runTask2b("12E-78", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"def");
	}

	public void test2b_9_18() {
		runTask2b("12E-78", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_9_19() {
		runTask2b("12E-78", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_9_20() {
		runTask2b("12E-78", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_9_21() {
		runTask2b("12E-78", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_9_22() {
		runTask2b("12E-78", Double.NaN, 0, "0.0");
	}

	public void test2b_9_23() {
		runTask2b("12E-78", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_9_24() {
		runTask2b("12E-78", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_9_25() {
		runTask2b("12E-78", Double.NaN, Double.NaN, "def");
	}

	public void test2b_9_26() {
		runTask2b("12E-78", Double.NaN, 1.0, "1.0");
	}

	public void test2b_9_27() {
		runTask2b("12E-78", Double.NaN, +0.0, "0.0");
	}

	public void test2b_9_28() {
		runTask2b("12E-78", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_9_29() {
		runTask2b("12E-78", 1.0, 0, "0.0");
	}

	public void test2b_9_30() {
		runTask2b("12E-78", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_9_31() {
		runTask2b("12E-78", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_9_32() {
		runTask2b("12E-78", 1.0, Double.NaN, "NaN");
	}

	public void test2b_9_33() {
		runTask2b("12E-78", 1.0, 1.0, "def");
	}

	public void test2b_9_34() {
		runTask2b("12E-78", 1.0, +0.0, "0.0");
	}

	public void test2b_9_35() {
		runTask2b("12E-78", 1.0, -0.0, "-0.0");
	}

	public void test2b_9_36() {
		runTask2b("12E-78", +0.0, 0, "def");
	}

	public void test2b_9_37() {
		runTask2b("12E-78", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_9_38() {
		runTask2b("12E-78", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_9_39() {
		runTask2b("12E-78", +0.0, Double.NaN, "NaN");
	}

	public void test2b_9_40() {
		runTask2b("12E-78", +0.0, 1.0, "1.0");
	}

	public void test2b_9_41() {
		runTask2b("12E-78", +0.0, +0.0, "def");
	}

	public void test2b_9_42() {
		runTask2b("12E-78", +0.0, -0.0, "def");
	}

	public void test2b_10_01() {
		runTask2b("12E-9000", 0, 0, "def");
	}

	public void test2b_10_02() {
		runTask2b("12E-9000", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_10_03() {
		runTask2b("12E-9000", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_10_04() {
		runTask2b("12E-9000", 0, Double.NaN, "NaN");
	}

	public void test2b_10_05() {
		runTask2b("12E-9000", 0, 1.0, "1.0");
	}

	public void test2b_10_06() {
		runTask2b("12E-9000", 0, +0.0, "def");
	}

	public void test2b_10_07() {
		runTask2b("12E-9000", 0, -0.0, "def");
	}

	public void test2b_10_08() {
		runTask2b("12E-9000", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_10_09() {
		runTask2b("12E-9000", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, "def");
	}

	public void test2b_10_10() {
		runTask2b("12E-9000", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_10_11() {
		runTask2b("12E-9000", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_10_12() {
		runTask2b("12E-9000", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_10_13() {
		runTask2b("12E-9000", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_10_14() {
		runTask2b("12E-9000", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_10_15() {
		runTask2b("12E-9000", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_10_16() {
		runTask2b("12E-9000", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_10_17() {
		runTask2b("12E-9000", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "def");
	}

	public void test2b_10_18() {
		runTask2b("12E-9000", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_10_19() {
		runTask2b("12E-9000", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_10_20() {
		runTask2b("12E-9000", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_10_21() {
		runTask2b("12E-9000", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_10_22() {
		runTask2b("12E-9000", Double.NaN, 0, "0.0");
	}

	public void test2b_10_23() {
		runTask2b("12E-9000", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_10_24() {
		runTask2b("12E-9000", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_10_25() {
		runTask2b("12E-9000", Double.NaN, Double.NaN, "def");
	}

	public void test2b_10_26() {
		runTask2b("12E-9000", Double.NaN, 1.0, "1.0");
	}

	public void test2b_10_27() {
		runTask2b("12E-9000", Double.NaN, +0.0, "0.0");
	}

	public void test2b_10_28() {
		runTask2b("12E-9000", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_10_29() {
		runTask2b("12E-9000", 1.0, 0, "0.0");
	}

	public void test2b_10_30() {
		runTask2b("12E-9000", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_10_31() {
		runTask2b("12E-9000", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_10_32() {
		runTask2b("12E-9000", 1.0, Double.NaN, "NaN");
	}

	public void test2b_10_33() {
		runTask2b("12E-9000", 1.0, 1.0, "def");
	}

	public void test2b_10_34() {
		runTask2b("12E-9000", 1.0, +0.0, "0.0");
	}

	public void test2b_10_35() {
		runTask2b("12E-9000", 1.0, -0.0, "-0.0");
	}

	public void test2b_10_36() {
		runTask2b("12E-9000", +0.0, 0, "def");
	}

	public void test2b_10_37() {
		runTask2b("12E-9000", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_10_38() {
		runTask2b("12E-9000", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_10_39() {
		runTask2b("12E-9000", +0.0, Double.NaN, "NaN");
	}

	public void test2b_10_40() {
		runTask2b("12E-9000", +0.0, 1.0, "1.0");
	}

	public void test2b_10_41() {
		runTask2b("12E-9000", +0.0, +0.0, "def");
	}

	public void test2b_10_42() {
		runTask2b("12E-9000", +0.0, -0.0, "def");
	}

	public void test2b_11_01() {
		runTask2b("12E9000", 0, 0, "def");
	}

	public void test2b_11_02() {
		runTask2b("12E9000", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_11_03() {
		runTask2b("12E9000", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_11_04() {
		runTask2b("12E9000", 0, Double.NaN, "NaN");
	}

	public void test2b_11_05() {
		runTask2b("12E9000", 0, 1.0, "1.0");
	}

	public void test2b_11_06() {
		runTask2b("12E9000", 0, +0.0, "def");
	}

	public void test2b_11_07() {
		runTask2b("12E9000", 0, -0.0, "def");
	}

	public void test2b_11_08() {
		runTask2b("12E9000", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_11_09() {
		runTask2b("12E9000", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, "def");
	}

	public void test2b_11_10() {
		runTask2b("12E9000", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_11_11() {
		runTask2b("12E9000", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_11_12() {
		runTask2b("12E9000", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_11_13() {
		runTask2b("12E9000", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_11_14() {
		runTask2b("12E9000", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_11_15() {
		runTask2b("12E9000", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_11_16() {
		runTask2b("12E9000", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_11_17() {
		runTask2b("12E9000", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "def");
	}

	public void test2b_11_18() {
		runTask2b("12E9000", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_11_19() {
		runTask2b("12E9000", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_11_20() {
		runTask2b("12E9000", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_11_21() {
		runTask2b("12E9000", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_11_22() {
		runTask2b("12E9000", Double.NaN, 0, "0.0");
	}

	public void test2b_11_23() {
		runTask2b("12E9000", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_11_24() {
		runTask2b("12E9000", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_11_25() {
		runTask2b("12E9000", Double.NaN, Double.NaN, "def");
	}

	public void test2b_11_26() {
		runTask2b("12E9000", Double.NaN, 1.0, "1.0");
	}

	public void test2b_11_27() {
		runTask2b("12E9000", Double.NaN, +0.0, "0.0");
	}

	public void test2b_11_28() {
		runTask2b("12E9000", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_11_29() {
		runTask2b("12E9000", 1.0, 0, "0.0");
	}

	public void test2b_11_30() {
		runTask2b("12E9000", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_11_31() {
		runTask2b("12E9000", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_11_32() {
		runTask2b("12E9000", 1.0, Double.NaN, "NaN");
	}

	public void test2b_11_33() {
		runTask2b("12E9000", 1.0, 1.0, "def");
	}

	public void test2b_11_34() {
		runTask2b("12E9000", 1.0, +0.0, "0.0");
	}

	public void test2b_11_35() {
		runTask2b("12E9000", 1.0, -0.0, "-0.0");
	}

	public void test2b_11_36() {
		runTask2b("12E9000", +0.0, 0, "def");
	}

	public void test2b_11_37() {
		runTask2b("12E9000", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_11_38() {
		runTask2b("12E9000", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_11_39() {
		runTask2b("12E9000", +0.0, Double.NaN, "NaN");
	}

	public void test2b_11_40() {
		runTask2b("12E9000", +0.0, 1.0, "1.0");
	}

	public void test2b_11_41() {
		runTask2b("12E9000", +0.0, +0.0, "def");
	}

	public void test2b_11_42() {
		runTask2b("12E9000", +0.0, -0.0, "def");
	}

	public void test2b_12_01() {
		runTask2b("-12E9000", 0, 0, "def");
	}

	public void test2b_12_02() {
		runTask2b("-12E9000", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_12_03() {
		runTask2b("-12E9000", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_12_04() {
		runTask2b("-12E9000", 0, Double.NaN, "NaN");
	}

	public void test2b_12_05() {
		runTask2b("-12E9000", 0, 1.0, "1.0");
	}

	public void test2b_12_06() {
		runTask2b("-12E9000", 0, +0.0, "def");
	}

	public void test2b_12_07() {
		runTask2b("-12E9000", 0, -0.0, "def");
	}

	public void test2b_12_08() {
		runTask2b("-12E9000", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_12_09() {
		runTask2b("-12E9000", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, "def");
	}

	public void test2b_12_10() {
		runTask2b("-12E9000", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_12_11() {
		runTask2b("-12E9000", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_12_12() {
		runTask2b("-12E9000", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_12_13() {
		runTask2b("-12E9000", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_12_14() {
		runTask2b("-12E9000", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_12_15() {
		runTask2b("-12E9000", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_12_16() {
		runTask2b("-12E9000", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_12_17() {
		runTask2b("-12E9000", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, "def");
	}

	public void test2b_12_18() {
		runTask2b("-12E9000", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_12_19() {
		runTask2b("-12E9000", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_12_20() {
		runTask2b("-12E9000", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_12_21() {
		runTask2b("-12E9000", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_12_22() {
		runTask2b("-12E9000", Double.NaN, 0, "0.0");
	}

	public void test2b_12_23() {
		runTask2b("-12E9000", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_12_24() {
		runTask2b("-12E9000", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_12_25() {
		runTask2b("-12E9000", Double.NaN, Double.NaN, "def");
	}

	public void test2b_12_26() {
		runTask2b("-12E9000", Double.NaN, 1.0, "1.0");
	}

	public void test2b_12_27() {
		runTask2b("-12E9000", Double.NaN, +0.0, "0.0");
	}

	public void test2b_12_28() {
		runTask2b("-12E9000", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_12_29() {
		runTask2b("-12E9000", 1.0, 0, "0.0");
	}

	public void test2b_12_30() {
		runTask2b("-12E9000", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_12_31() {
		runTask2b("-12E9000", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_12_32() {
		runTask2b("-12E9000", 1.0, Double.NaN, "NaN");
	}

	public void test2b_12_33() {
		runTask2b("-12E9000", 1.0, 1.0, "def");
	}

	public void test2b_12_34() {
		runTask2b("-12E9000", 1.0, +0.0, "0.0");
	}

	public void test2b_12_35() {
		runTask2b("-12E9000", 1.0, -0.0, "-0.0");
	}

	public void test2b_12_36() {
		runTask2b("-12E9000", +0.0, 0, "def");
	}

	public void test2b_12_37() {
		runTask2b("-12E9000", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_12_38() {
		runTask2b("-12E9000", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_12_39() {
		runTask2b("-12E9000", +0.0, Double.NaN, "NaN");
	}

	public void test2b_12_40() {
		runTask2b("-12E9000", +0.0, 1.0, "1.0");
	}

	public void test2b_12_41() {
		runTask2b("-12E9000", +0.0, +0.0, "def");
	}

	public void test2b_12_42() {
		runTask2b("-12E9000", +0.0, -0.0, "def");
	}

	public void test2b_133_01() {
		runTask2b("ерунда", 0, 0, "def");
	}

	public void test2b_13_02() {
		runTask2b("ерунда", 0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_13_03() {
		runTask2b("ерунда", 0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_13_04() {
		runTask2b("ерунда", 0, Double.NaN, "NaN");
	}

	public void test2b_13_05() {
		runTask2b("ерунда", 0, 1.0, "1.0");
	}

	public void test2b_13_06() {
		runTask2b("ерунда", 0, +0.0, "def");
	}

	public void test2b_13_07() {
		runTask2b("ерунда", 0, -0.0, "def");
	}

	public void test2b_13_08() {
		runTask2b("ерунда", Double.POSITIVE_INFINITY, 0, "0.0");
	}

	public void test2b_13_09() {
		runTask2b("ерунда", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				"def");
	}

	public void test2b_13_10() {
		runTask2b("ерунда", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"-Infinity");
	}

	public void test2b_13_11() {
		runTask2b("ерунда", Double.POSITIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_13_12() {
		runTask2b("ерунда", Double.POSITIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_13_13() {
		runTask2b("ерунда", Double.POSITIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_13_14() {
		runTask2b("ерунда", Double.POSITIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_13_15() {
		runTask2b("ерунда", Double.NEGATIVE_INFINITY, 0, "0.0");
	}

	public void test2b_13_16() {
		runTask2b("ерунда", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				"Infinity");
	}

	public void test2b_13_17() {
		runTask2b("ерунда", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				"def");
	}

	public void test2b_13_18() {
		runTask2b("ерунда", Double.NEGATIVE_INFINITY, Double.NaN, "NaN");
	}

	public void test2b_13_19() {
		runTask2b("ерунда", Double.NEGATIVE_INFINITY, 1.0, "1.0");
	}

	public void test2b_13_20() {
		runTask2b("ерунда", Double.NEGATIVE_INFINITY, +0.0, "0.0");
	}

	public void test2b_13_21() {
		runTask2b("ерунда", Double.NEGATIVE_INFINITY, -0.0, "-0.0");
	}

	public void test2b_13_22() {
		runTask2b("ерунда", Double.NaN, 0, "0.0");
	}

	public void test2b_13_23() {
		runTask2b("ерунда", Double.NaN, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_13_24() {
		runTask2b("ерунда", Double.NaN, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_13_25() {
		runTask2b("ерунда", Double.NaN, Double.NaN, "def");
	}

	public void test2b_13_26() {
		runTask2b("ерунда", Double.NaN, 1.0, "1.0");
	}

	public void test2b_13_27() {
		runTask2b("ерунда", Double.NaN, +0.0, "0.0");
	}

	public void test2b_13_28() {
		runTask2b("ерунда", Double.NaN, -0.0, "-0.0");
	}

	public void test2b_13_29() {
		runTask2b("ерунда", 1.0, 0, "0.0");
	}

	public void test2b_13_30() {
		runTask2b("ерунда", 1.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_13_31() {
		runTask2b("ерунда", 1.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_13_32() {
		runTask2b("ерунда", 1.0, Double.NaN, "NaN");
	}

	public void test2b_13_33() {
		runTask2b("ерунда", 1.0, 1.0, "def");
	}

	public void test2b_13_34() {
		runTask2b("ерунда", 1.0, +0.0, "0.0");
	}

	public void test2b_13_35() {
		runTask2b("ерунда", 1.0, -0.0, "-0.0");
	}

	public void test2b_13_36() {
		runTask2b("ерунда", +0.0, 0, "def");
	}

	public void test2b_13_37() {
		runTask2b("ерунда", +0.0, Double.POSITIVE_INFINITY, "Infinity");
	}

	public void test2b_13_38() {
		runTask2b("ерунда", +0.0, Double.NEGATIVE_INFINITY, "-Infinity");
	}

	public void test2b_13_39() {
		runTask2b("ерунда", +0.0, Double.NaN, "NaN");
	}

	public void test2b_13_40() {
		runTask2b("ерунда", +0.0, 1.0, "1.0");
	}

	public void test2b_13_41() {
		runTask2b("ерунда", +0.0, +0.0, "def");
	}

	public void test2b_13_42() {
		runTask2b("ерунда", +0.0, -0.0, "def");
	}
}
