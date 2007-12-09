package org.test.zlibrary.options;

import junit.framework.TestCase;

import org.zlibrary.core.options.ZLDoubleOption;
import org.zlibrary.core.options.config.ZLConfig;
import org.zlibrary.core.options.config.ZLConfigInstance;

public class ZLDoubleOptionTests2a extends TestCase {

	private ZLConfig myConfig = ZLConfigInstance.getInstance();

	private void runTask2a(String configValue, double value2, double value3,
			double expectedValue) {
		myConfig.setValue("double_group_2a", "name", configValue, "category");
		ZLDoubleOption option = new ZLDoubleOption("category",
				"double_group_2a", "name", value2);
		option.setValue(value3);
		assertEquals(option.getValue(), expectedValue);
	}

	public void tearDown() {
		myConfig.unsetValue("double_group_2a", "name");
	}

	public void test2a_1_01() {
		runTask2a(null, 0, 0, 0);
	}

	public void test2a_1_02() {
		runTask2a(null, 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_1_03() {
		runTask2a(null, 0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_1_04() {
		runTask2a(null, 0, Double.NaN, Double.NaN);
	}

	public void test2a_1_05() {
		runTask2a(null, 0, 1.0, 1.0);
	}

	public void test2a_1_06() {
		runTask2a(null, 0, +0.0, +0.0);
	}

	public void test2a_1_07() {
		runTask2a(null, 0, -0.0, -0.0);
	}

	public void test2a_1_08() {
		runTask2a(null, Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_1_09() {
		runTask2a(null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_1_10() {
		runTask2a(null, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_1_11() {
		runTask2a(null, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_1_12() {
		runTask2a(null, Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_1_13() {
		runTask2a(null, Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_1_14() {
		runTask2a(null, Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_1_15() {
		runTask2a(null, Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_1_16() {
		runTask2a(null, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_1_17() {
		runTask2a(null, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_1_18() {
		runTask2a(null, Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_1_19() {
		runTask2a(null, Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_1_20() {
		runTask2a(null, Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_1_21() {
		runTask2a(null, Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_1_22() {
		runTask2a(null, Double.NaN, 0, 0);
	}

	public void test2a_1_23() {
		runTask2a(null, Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_1_24() {
		runTask2a(null, Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_1_25() {
		runTask2a(null, Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_1_26() {
		runTask2a(null, Double.NaN, 1.0, 1.0);
	}

	public void test2a_1_27() {
		runTask2a(null, Double.NaN, +0.0, +0.0);
	}

	public void test2a_1_28() {
		runTask2a(null, Double.NaN, -0.0, -0.0);
	}

	public void test2a_1_29() {
		runTask2a(null, 1.0, 0, 0);
	}

	public void test2a_1_30() {
		runTask2a(null, 1.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_1_31() {
		runTask2a(null, 1.0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_1_32() {
		runTask2a(null, 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_1_33() {
		runTask2a(null, 1.0, 1.0, 1.0);
	}

	public void test2a_1_34() {
		runTask2a(null, 1.0, +0.0, +0.0);
	}

	public void test2a_1_35() {
		runTask2a(null, 1.0, -0.0, -0.0);
	}

	public void test2a_1_36() {
		runTask2a(null, +0.0, 0, 0);
	}

	public void test2a_1_37() {
		runTask2a(null, +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_1_38() {
		runTask2a(null, +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_1_39() {
		runTask2a(null, +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_1_40() {
		runTask2a(null, +0.0, 1.0, 1.0);
	}

	public void test2a_1_41() {
		runTask2a(null, +0.0, +0.0, +0.0);
	}

	public void test2a_1_42() {
		runTask2a(null, +0.0, -0.0, -0.0);
	}

	public void test2a_2_01() {
		runTask2a("", 0, 0, 0);
	}

	public void test2a_2_02() {
		runTask2a("", 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_2_03() {
		runTask2a("", 0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_2_04() {
		runTask2a("", 0, Double.NaN, Double.NaN);
	}

	public void test2a_2_05() {
		runTask2a("", 0, 1.0, 1.0);
	}

	public void test2a_2_06() {
		runTask2a("", 0, +0.0, +0.0);
	}

	public void test2a_2_07() {
		runTask2a("", 0, -0.0, -0.0);
	}

	public void test2a_2_08() {
		runTask2a("", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_2_09() {
		runTask2a("", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_2_10() {
		runTask2a("", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_2_11() {
		runTask2a("", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_2_12() {
		runTask2a("", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_2_13() {
		runTask2a("", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_2_14() {
		runTask2a("", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_2_15() {
		runTask2a("", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_2_16() {
		runTask2a("", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_2_17() {
		runTask2a("", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_2_18() {
		runTask2a("", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_2_19() {
		runTask2a("", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_2_20() {
		runTask2a("", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_2_21() {
		runTask2a("", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_2_22() {
		runTask2a("", Double.NaN, 0, 0);
	}

	public void test2a_2_23() {
		runTask2a("", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_2_24() {
		runTask2a("", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_2_25() {
		runTask2a("", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_2_26() {
		runTask2a("", Double.NaN, 1.0, 1.0);
	}

	public void test2a_2_27() {
		runTask2a("", Double.NaN, +0.0, +0.0);
	}

	public void test2a_2_28() {
		runTask2a("", Double.NaN, -0.0, -0.0);
	}

	public void test2a_2_29() {
		runTask2a("", 1.0, 0, 0);
	}

	public void test2a_2_30() {
		runTask2a("", 1.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_2_31() {
		runTask2a("", 1.0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_2_32() {
		runTask2a("", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_2_33() {
		runTask2a("", 1.0, 1.0, 1.0);
	}

	public void test2a_2_34() {
		runTask2a("", 1.0, +0.0, +0.0);
	}

	public void test2a_2_35() {
		runTask2a("", 1.0, -0.0, -0.0);
	}

	public void test2a_2_36() {
		runTask2a("", +0.0, 0, 0);
	}

	public void test2a_2_37() {
		runTask2a("", +0.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_2_38() {
		runTask2a("", +0.0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_2_39() {
		runTask2a("", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_2_40() {
		runTask2a("", +0.0, 1.0, 1.0);
	}

	public void test2a_2_41() {
		runTask2a("", +0.0, +0.0, +0.0);
	}

	public void test2a_2_42() {
		runTask2a("", +0.0, -0.0, -0.0);
	}

	public void test2a_3_01() {
		runTask2a("Infinity", 0, 0, 0);
	}

	public void test2a_3_02() {
		runTask2a("Infinity", 0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_3_03() {
		runTask2a("Infinity", 0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_3_04() {
		runTask2a("Infinity", 0, Double.NaN, Double.NaN);
	}

	public void test2a_3_05() {
		runTask2a("Infinity", 0, 1.0, 1.0);
	}

	public void test2a_3_06() {
		runTask2a("Infinity", 0, +0.0, +0.0);
	}

	public void test2a_3_07() {
		runTask2a("Infinity", 0, -0.0, -0.0);
	}

	public void test2a_3_08() {
		runTask2a("Infinity", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_3_09() {
		runTask2a("Infinity", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_3_10() {
		runTask2a("Infinity", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_3_11() {
		runTask2a("Infinity", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_3_12() {
		runTask2a("Infinity", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_3_13() {
		runTask2a("Infinity", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_3_14() {
		runTask2a("Infinity", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_3_15() {
		runTask2a("Infinity", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_3_16() {
		runTask2a("Infinity", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_3_17() {
		runTask2a("Infinity", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_3_18() {
		runTask2a("Infinity", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_3_19() {
		runTask2a("Infinity", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_3_20() {
		runTask2a("Infinity", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_3_21() {
		runTask2a("Infinity", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_3_22() {
		runTask2a("Infinity", Double.NaN, 0, 0);
	}

	public void test2a_3_23() {
		runTask2a("Infinity", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_3_24() {
		runTask2a("Infinity", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_3_25() {
		runTask2a("Infinity", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_3_26() {
		runTask2a("Infinity", Double.NaN, 1.0, 1.0);
	}

	public void test2a_3_27() {
		runTask2a("Infinity", Double.NaN, +0.0, +0.0);
	}

	public void test2a_3_28() {
		runTask2a("Infinity", Double.NaN, -0.0, -0.0);
	}

	public void test2a_3_29() {
		runTask2a("Infinity", 1.0, 0, 0);
	}

	public void test2a_3_30() {
		runTask2a("Infinity", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_3_31() {
		runTask2a("Infinity", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_3_32() {
		runTask2a("Infinity", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_3_33() {
		runTask2a("Infinity", 1.0, 1.0, 1.0);
	}

	public void test2a_3_34() {
		runTask2a("Infinity", 1.0, +0.0, +0.0);
	}

	public void test2a_3_35() {
		runTask2a("Infinity", 1.0, -0.0, -0.0);
	}

	public void test2a_3_36() {
		runTask2a("Infinity", +0.0, 0, 0);
	}

	public void test2a_3_37() {
		runTask2a("Infinity", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_3_38() {
		runTask2a("Infinity", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_3_39() {
		runTask2a("Infinity", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_3_40() {
		runTask2a("Infinity", +0.0, 1.0, 1.0);
	}

	public void test2a_3_41() {
		runTask2a("Infinity", +0.0, +0.0, +0.0);
	}

	public void test2a_3_42() {
		runTask2a("Infinity", +0.0, -0.0, -0.0);
	}

	public void test2a_4_01() {
		runTask2a("-Infinity", 0, 0, 0);
	}

	public void test2a_4_02() {
		runTask2a("-Infinity", 0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_4_03() {
		runTask2a("-Infinity", 0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_4_04() {
		runTask2a("-Infinity", 0, Double.NaN, Double.NaN);
	}

	public void test2a_4_05() {
		runTask2a("-Infinity", 0, 1.0, 1.0);
	}

	public void test2a_4_06() {
		runTask2a("-Infinity", 0, +0.0, +0.0);
	}

	public void test2a_4_07() {
		runTask2a("-Infinity", 0, -0.0, -0.0);
	}

	public void test2a_4_08() {
		runTask2a("-Infinity", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_4_09() {
		runTask2a("-Infinity", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_4_10() {
		runTask2a("-Infinity", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_4_11() {
		runTask2a("-Infinity", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_4_12() {
		runTask2a("-Infinity", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_4_13() {
		runTask2a("-Infinity", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_4_14() {
		runTask2a("-Infinity", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_4_15() {
		runTask2a("-Infinity", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_4_16() {
		runTask2a("-Infinity", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_4_17() {
		runTask2a("-Infinity", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_4_18() {
		runTask2a("-Infinity", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_4_19() {
		runTask2a("-Infinity", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_4_20() {
		runTask2a("-Infinity", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_4_21() {
		runTask2a("-Infinity", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_4_22() {
		runTask2a("-Infinity", Double.NaN, 0, 0);
	}

	public void test2a_4_23() {
		runTask2a("-Infinity", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_4_24() {
		runTask2a("-Infinity", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_4_25() {
		runTask2a("-Infinity", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_4_26() {
		runTask2a("-Infinity", Double.NaN, 1.0, 1.0);
	}

	public void test2a_4_27() {
		runTask2a("-Infinity", Double.NaN, +0.0, +0.0);
	}

	public void test2a_4_28() {
		runTask2a("-Infinity", Double.NaN, -0.0, -0.0);
	}

	public void test2a_4_29() {
		runTask2a("-Infinity", 1.0, 0, 0);
	}

	public void test2a_4_30() {
		runTask2a("-Infinity", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_4_31() {
		runTask2a("-Infinity", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_4_32() {
		runTask2a("-Infinity", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_4_33() {
		runTask2a("-Infinity", 1.0, 1.0, 1.0);
	}

	public void test2a_4_34() {
		runTask2a("-Infinity", 1.0, +0.0, +0.0);
	}

	public void test2a_4_35() {
		runTask2a("-Infinity", 1.0, -0.0, -0.0);
	}

	public void test2a_4_36() {
		runTask2a("-Infinity", +0.0, 0, 0);
	}

	public void test2a_4_37() {
		runTask2a("-Infinity", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_4_38() {
		runTask2a("-Infinity", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_4_39() {
		runTask2a("-Infinity", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_4_40() {
		runTask2a("-Infinity", +0.0, 1.0, 1.0);
	}

	public void test2a_4_41() {
		runTask2a("-Infinity", +0.0, +0.0, +0.0);
	}

	public void test2a_4_42() {
		runTask2a("-Infinity", +0.0, -0.0, -0.0);
	}

	public void test2a_5_01() {
		runTask2a("NaN", 0, 0, 0);
	}

	public void test2a_5_02() {
		runTask2a("NaN", 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_5_03() {
		runTask2a("NaN", 0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_5_04() {
		runTask2a("NaN", 0, Double.NaN, Double.NaN);
	}

	public void test2a_5_05() {
		runTask2a("NaN", 0, 1.0, 1.0);
	}

	public void test2a_5_06() {
		runTask2a("NaN", 0, +0.0, +0.0);
	}

	public void test2a_5_07() {
		runTask2a("NaN", 0, -0.0, -0.0);
	}

	public void test2a_5_08() {
		runTask2a("NaN", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_5_09() {
		runTask2a("NaN", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_5_10() {
		runTask2a("NaN", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_5_11() {
		runTask2a("NaN", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_5_12() {
		runTask2a("NaN", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_5_13() {
		runTask2a("NaN", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_5_14() {
		runTask2a("NaN", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_5_15() {
		runTask2a("NaN", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_5_16() {
		runTask2a("NaN", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_5_17() {
		runTask2a("NaN", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_5_18() {
		runTask2a("NaN", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_5_19() {
		runTask2a("NaN", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_5_20() {
		runTask2a("NaN", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_5_21() {
		runTask2a("NaN", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_5_22() {
		runTask2a("NaN", Double.NaN, 0, 0);
	}

	public void test2a_5_23() {
		runTask2a("NaN", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_5_24() {
		runTask2a("NaN", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_5_25() {
		runTask2a("NaN", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_5_26() {
		runTask2a("NaN", Double.NaN, 1.0, 1.0);
	}

	public void test2a_5_27() {
		runTask2a("NaN", Double.NaN, +0.0, +0.0);
	}

	public void test2a_5_28() {
		runTask2a("NaN", Double.NaN, -0.0, -0.0);
	}

	public void test2a_5_29() {
		runTask2a("NaN", 1.0, 0, 0);
	}

	public void test2a_5_30() {
		runTask2a("NaN", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_5_31() {
		runTask2a("NaN", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_5_32() {
		runTask2a("NaN", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_5_33() {
		runTask2a("NaN", 1.0, 1.0, 1.0);
	}

	public void test2a_5_34() {
		runTask2a("NaN", 1.0, +0.0, +0.0);
	}

	public void test2a_5_35() {
		runTask2a("NaN", 1.0, -0.0, -0.0);
	}

	public void test2a_5_36() {
		runTask2a("NaN", +0.0, 0, 0);
	}

	public void test2a_5_37() {
		runTask2a("NaN", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_5_38() {
		runTask2a("NaN", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_5_39() {
		runTask2a("NaN", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_5_40() {
		runTask2a("NaN", +0.0, 1.0, 1.0);
	}

	public void test2a_5_41() {
		runTask2a("NaN", +0.0, +0.0, +0.0);
	}

	public void test2a_5_42() {
		runTask2a("NaN", +0.0, -0.0, -0.0);
	}

	public void test2a_6_01() {
		runTask2a("0", 0, 0, 0);
	}

	public void test2a_6_02() {
		runTask2a("0", 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_6_03() {
		runTask2a("0", 0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_6_04() {
		runTask2a("0", 0, Double.NaN, Double.NaN);
	}

	public void test2a_6_05() {
		runTask2a("0", 0, 1.0, 1.0);
	}

	public void test2a_6_06() {
		runTask2a("0", 0, +0.0, +0.0);
	}

	public void test2a_6_07() {
		runTask2a("0", 0, -0.0, -0.0);
	}

	public void test2a_6_08() {
		runTask2a("0", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_6_09() {
		runTask2a("0", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_6_10() {
		runTask2a("0", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_6_11() {
		runTask2a("0", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_6_12() {
		runTask2a("0", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_6_13() {
		runTask2a("0", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_6_14() {
		runTask2a("0", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_6_15() {
		runTask2a("0", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_6_16() {
		runTask2a("0", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_6_17() {
		runTask2a("0", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_6_18() {
		runTask2a("0", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_6_19() {
		runTask2a("0", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_6_20() {
		runTask2a("0", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_6_21() {
		runTask2a("0", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_6_22() {
		runTask2a("0", Double.NaN, 0, 0);
	}

	public void test2a_6_23() {
		runTask2a("0", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_6_24() {
		runTask2a("0", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_6_25() {
		runTask2a("0", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_6_26() {
		runTask2a("0", Double.NaN, 1.0, 1.0);
	}

	public void test2a_6_27() {
		runTask2a("0", Double.NaN, +0.0, +0.0);
	}

	public void test2a_6_28() {
		runTask2a("0", Double.NaN, -0.0, -0.0);
	}

	public void test2a_6_29() {
		runTask2a("0", 1.0, 0, 0);
	}

	public void test2a_6_30() {
		runTask2a("0", 1.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_6_31() {
		runTask2a("0", 1.0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_6_32() {
		runTask2a("0", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_6_33() {
		runTask2a("0", 1.0, 1.0, 1.0);
	}

	public void test2a_6_34() {
		runTask2a("0", 1.0, +0.0, +0.0);
	}

	public void test2a_6_35() {
		runTask2a("0", 1.0, -0.0, -0.0);
	}

	public void test2a_6_36() {
		runTask2a("0", +0.0, 0, 0);
	}

	public void test2a_6_37() {
		runTask2a("0", +0.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_6_38() {
		runTask2a("0", +0.0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_6_39() {
		runTask2a("0", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_6_40() {
		runTask2a("0", +0.0, 1.0, 1.0);
	}

	public void test2a_6_41() {
		runTask2a("0", +0.0, +0.0, +0.0);
	}

	public void test2a_6_42() {
		runTask2a("0", +0.0, -0.0, -0.0);
	}

	public void test2a_7_01() {
		runTask2a("1.1", 0, 0, 0);
	}

	public void test2a_7_02() {
		runTask2a("1.1", 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_7_03() {
		runTask2a("1.1", 0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_7_04() {
		runTask2a("1.1", 0, Double.NaN, Double.NaN);
	}

	public void test2a_7_05() {
		runTask2a("1.1", 0, 1.0, 1.0);
	}

	public void test2a_7_06() {
		runTask2a("1.1", 0, +0.0, +0.0);
	}

	public void test2a_7_07() {
		runTask2a("1.1", 0, -0.0, -0.0);
	}

	public void test2a_7_08() {
		runTask2a("1.1", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_7_09() {
		runTask2a("1.1", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_7_10() {
		runTask2a("1.1", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_7_11() {
		runTask2a("1.1", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_7_12() {
		runTask2a("1.1", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_7_13() {
		runTask2a("1.1", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_7_14() {
		runTask2a("1.1", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_7_15() {
		runTask2a("1.1", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_7_16() {
		runTask2a("1.1", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_7_17() {
		runTask2a("1.1", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_7_18() {
		runTask2a("1.1", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_7_19() {
		runTask2a("1.1", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_7_20() {
		runTask2a("1.1", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_7_21() {
		runTask2a("1.1", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_7_22() {
		runTask2a("1.1", Double.NaN, 0, 0);
	}

	public void test2a_7_23() {
		runTask2a("1.1", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_7_24() {
		runTask2a("1.1", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_7_25() {
		runTask2a("1.1", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_7_26() {
		runTask2a("1.1", Double.NaN, 1.0, 1.0);
	}

	public void test2a_7_27() {
		runTask2a("1.1", Double.NaN, +0.0, +0.0);
	}

	public void test2a_7_28() {
		runTask2a("1.1", Double.NaN, -0.0, -0.0);
	}

	public void test2a_7_29() {
		runTask2a("1.1", 1.0, 0, 0);
	}

	public void test2a_7_30() {
		runTask2a("1.1", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_7_31() {
		runTask2a("1.1", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_7_32() {
		runTask2a("1.1", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_7_33() {
		runTask2a("1.1", 1.0, 1.0, 1.0);
	}

	public void test2a_7_34() {
		runTask2a("1.1", 1.0, +0.0, +0.0);
	}

	public void test2a_7_35() {
		runTask2a("1.1", 1.0, -0.0, -0.0);
	}

	public void test2a_7_36() {
		runTask2a("1.1", +0.0, 0, 0);
	}

	public void test2a_7_37() {
		runTask2a("1.1", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_7_38() {
		runTask2a("1.1", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_7_39() {
		runTask2a("1.1", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_7_40() {
		runTask2a("1.1", +0.0, 1.0, 1.0);
	}

	public void test2a_7_41() {
		runTask2a("1.1", +0.0, +0.0, +0.0);
	}

	public void test2a_7_42() {
		runTask2a("1.1", +0.0, -0.0, -0.0);
	}

	public void test2a_8_01() {
		runTask2a("4", 0, 0, 0);
	}

	public void test2a_8_02() {
		runTask2a("4", 0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_8_03() {
		runTask2a("4", 0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_8_04() {
		runTask2a("4", 0, Double.NaN, Double.NaN);
	}

	public void test2a_8_05() {
		runTask2a("4", 0, 1.0, 1.0);
	}

	public void test2a_8_06() {
		runTask2a("4", 0, +0.0, +0.0);
	}

	public void test2a_8_07() {
		runTask2a("4", 0, -0.0, -0.0);
	}

	public void test2a_8_08() {
		runTask2a("4", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_8_09() {
		runTask2a("4", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_8_10() {
		runTask2a("4", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_8_11() {
		runTask2a("4", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_8_12() {
		runTask2a("4", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_8_13() {
		runTask2a("4", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_8_14() {
		runTask2a("4", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_8_15() {
		runTask2a("4", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_8_16() {
		runTask2a("4", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_8_17() {
		runTask2a("4", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_8_18() {
		runTask2a("4", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_8_19() {
		runTask2a("4", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_8_20() {
		runTask2a("4", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_8_21() {
		runTask2a("4", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_8_22() {
		runTask2a("4", Double.NaN, 0, 0);
	}

	public void test2a_8_23() {
		runTask2a("4", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_8_24() {
		runTask2a("4", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_8_25() {
		runTask2a("4", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_8_26() {
		runTask2a("4", Double.NaN, 1.0, 1.0);
	}

	public void test2a_8_27() {
		runTask2a("4", Double.NaN, +0.0, +0.0);
	}

	public void test2a_8_28() {
		runTask2a("4", Double.NaN, -0.0, -0.0);
	}

	public void test2a_8_29() {
		runTask2a("4", 1.0, 0, 0);
	}

	public void test2a_8_30() {
		runTask2a("4", 1.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_8_31() {
		runTask2a("4", 1.0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_8_32() {
		runTask2a("4", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_8_33() {
		runTask2a("4", 1.0, 1.0, 1.0);
	}

	public void test2a_8_34() {
		runTask2a("4", 1.0, +0.0, +0.0);
	}

	public void test2a_8_35() {
		runTask2a("4", 1.0, -0.0, -0.0);
	}

	public void test2a_8_36() {
		runTask2a("4", +0.0, 0, 0);
	}

	public void test2a_8_37() {
		runTask2a("4", +0.0, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_8_38() {
		runTask2a("4", +0.0, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_8_39() {
		runTask2a("4", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_8_40() {
		runTask2a("4", +0.0, 1.0, 1.0);
	}

	public void test2a_8_41() {
		runTask2a("4", +0.0, +0.0, +0.0);
	}

	public void test2a_8_42() {
		runTask2a("4", +0.0, -0.0, -0.0);
	}

	public void test2a_9_01() {
		runTask2a("12E-78", 0, 0, 0);
	}

	public void test2a_9_02() {
		runTask2a("12E-78", 0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_9_03() {
		runTask2a("12E-78", 0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_9_04() {
		runTask2a("12E-78", 0, Double.NaN, Double.NaN);
	}

	public void test2a_9_05() {
		runTask2a("12E-78", 0, 1.0, 1.0);
	}

	public void test2a_9_06() {
		runTask2a("12E-78", 0, +0.0, +0.0);
	}

	public void test2a_9_07() {
		runTask2a("12E-78", 0, -0.0, -0.0);
	}

	public void test2a_9_08() {
		runTask2a("12E-78", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_9_09() {
		runTask2a("12E-78", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_9_10() {
		runTask2a("12E-78", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_9_11() {
		runTask2a("12E-78", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_9_12() {
		runTask2a("12E-78", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_9_13() {
		runTask2a("12E-78", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_9_14() {
		runTask2a("12E-78", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_9_15() {
		runTask2a("12E-78", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_9_16() {
		runTask2a("12E-78", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_9_17() {
		runTask2a("12E-78", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_9_18() {
		runTask2a("12E-78", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_9_19() {
		runTask2a("12E-78", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_9_20() {
		runTask2a("12E-78", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_9_21() {
		runTask2a("12E-78", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_9_22() {
		runTask2a("12E-78", Double.NaN, 0, 0);
	}

	public void test2a_9_23() {
		runTask2a("12E-78", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_9_24() {
		runTask2a("12E-78", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_9_25() {
		runTask2a("12E-78", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_9_26() {
		runTask2a("12E-78", Double.NaN, 1.0, 1.0);
	}

	public void test2a_9_27() {
		runTask2a("12E-78", Double.NaN, +0.0, +0.0);
	}

	public void test2a_9_28() {
		runTask2a("12E-78", Double.NaN, -0.0, -0.0);
	}

	public void test2a_9_29() {
		runTask2a("12E-78", 1.0, 0, 0);
	}

	public void test2a_9_30() {
		runTask2a("12E-78", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_9_31() {
		runTask2a("12E-78", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_9_32() {
		runTask2a("12E-78", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_9_33() {
		runTask2a("12E-78", 1.0, 1.0, 1.0);
	}

	public void test2a_9_34() {
		runTask2a("12E-78", 1.0, +0.0, +0.0);
	}

	public void test2a_9_35() {
		runTask2a("12E-78", 1.0, -0.0, -0.0);
	}

	public void test2a_9_36() {
		runTask2a("12E-78", +0.0, 0, 0);
	}

	public void test2a_9_37() {
		runTask2a("12E-78", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_9_38() {
		runTask2a("12E-78", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_9_39() {
		runTask2a("12E-78", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_9_40() {
		runTask2a("12E-78", +0.0, 1.0, 1.0);
	}

	public void test2a_9_41() {
		runTask2a("12E-78", +0.0, +0.0, +0.0);
	}

	public void test2a_9_42() {
		runTask2a("12E-78", +0.0, -0.0, -0.0);
	}

	public void test2a_10_01() {
		runTask2a("12E-9000", 0, 0, 0);
	}

	public void test2a_10_02() {
		runTask2a("12E-9000", 0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_10_03() {
		runTask2a("12E-9000", 0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_10_04() {
		runTask2a("12E-9000", 0, Double.NaN, Double.NaN);
	}

	public void test2a_10_05() {
		runTask2a("12E-9000", 0, 1.0, 1.0);
	}

	public void test2a_10_06() {
		runTask2a("12E-9000", 0, +0.0, +0.0);
	}

	public void test2a_10_07() {
		runTask2a("12E-9000", 0, -0.0, -0.0);
	}

	public void test2a_10_08() {
		runTask2a("12E-9000", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_10_09() {
		runTask2a("12E-9000", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_10_10() {
		runTask2a("12E-9000", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_10_11() {
		runTask2a("12E-9000", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_10_12() {
		runTask2a("12E-9000", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_10_13() {
		runTask2a("12E-9000", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_10_14() {
		runTask2a("12E-9000", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_10_15() {
		runTask2a("12E-9000", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_10_16() {
		runTask2a("12E-9000", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_10_17() {
		runTask2a("12E-9000", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_10_18() {
		runTask2a("12E-9000", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_10_19() {
		runTask2a("12E-9000", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_10_20() {
		runTask2a("12E-9000", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_10_21() {
		runTask2a("12E-9000", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_10_22() {
		runTask2a("12E-9000", Double.NaN, 0, 0);
	}

	public void test2a_10_23() {
		runTask2a("12E-9000", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_10_24() {
		runTask2a("12E-9000", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_10_25() {
		runTask2a("12E-9000", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_10_26() {
		runTask2a("12E-9000", Double.NaN, 1.0, 1.0);
	}

	public void test2a_10_27() {
		runTask2a("12E-9000", Double.NaN, +0.0, +0.0);
	}

	public void test2a_10_28() {
		runTask2a("12E-9000", Double.NaN, -0.0, -0.0);
	}

	public void test2a_10_29() {
		runTask2a("12E-9000", 1.0, 0, 0);
	}

	public void test2a_10_30() {
		runTask2a("12E-9000", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_10_31() {
		runTask2a("12E-9000", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_10_32() {
		runTask2a("12E-9000", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_10_33() {
		runTask2a("12E-9000", 1.0, 1.0, 1.0);
	}

	public void test2a_10_34() {
		runTask2a("12E-9000", 1.0, +0.0, +0.0);
	}

	public void test2a_10_35() {
		runTask2a("12E-9000", 1.0, -0.0, -0.0);
	}

	public void test2a_10_36() {
		runTask2a("12E-9000", +0.0, 0, 0);
	}

	public void test2a_10_37() {
		runTask2a("12E-9000", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_10_38() {
		runTask2a("12E-9000", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_10_39() {
		runTask2a("12E-9000", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_10_40() {
		runTask2a("12E-9000", +0.0, 1.0, 1.0);
	}

	public void test2a_10_41() {
		runTask2a("12E-9000", +0.0, +0.0, +0.0);
	}

	public void test2a_10_42() {
		runTask2a("12E-9000", +0.0, -0.0, -0.0);
	}

	public void test2a_11_01() {
		runTask2a("12E9000", 0, 0, 0);
	}

	public void test2a_11_02() {
		runTask2a("12E9000", 0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_11_03() {
		runTask2a("12E9000", 0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_11_04() {
		runTask2a("12E9000", 0, Double.NaN, Double.NaN);
	}

	public void test2a_11_05() {
		runTask2a("12E9000", 0, 1.0, 1.0);
	}

	public void test2a_11_06() {
		runTask2a("12E9000", 0, +0.0, +0.0);
	}

	public void test2a_11_07() {
		runTask2a("12E9000", 0, -0.0, -0.0);
	}

	public void test2a_11_08() {
		runTask2a("12E9000", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_11_09() {
		runTask2a("12E9000", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_11_10() {
		runTask2a("12E9000", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_11_11() {
		runTask2a("12E9000", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_11_12() {
		runTask2a("12E9000", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_11_13() {
		runTask2a("12E9000", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_11_14() {
		runTask2a("12E9000", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_11_15() {
		runTask2a("12E9000", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_11_16() {
		runTask2a("12E9000", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_11_17() {
		runTask2a("12E9000", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_11_18() {
		runTask2a("12E9000", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_11_19() {
		runTask2a("12E9000", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_11_20() {
		runTask2a("12E9000", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_11_21() {
		runTask2a("12E9000", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_11_22() {
		runTask2a("12E9000", Double.NaN, 0, 0);
	}

	public void test2a_11_23() {
		runTask2a("12E9000", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_11_24() {
		runTask2a("12E9000", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_11_25() {
		runTask2a("12E9000", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_11_26() {
		runTask2a("12E9000", Double.NaN, 1.0, 1.0);
	}

	public void test2a_11_27() {
		runTask2a("12E9000", Double.NaN, +0.0, +0.0);
	}

	public void test2a_11_28() {
		runTask2a("12E9000", Double.NaN, -0.0, -0.0);
	}

	public void test2a_11_29() {
		runTask2a("12E9000", 1.0, 0, 0);
	}

	public void test2a_11_30() {
		runTask2a("12E9000", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_11_31() {
		runTask2a("12E9000", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_11_32() {
		runTask2a("12E9000", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_11_33() {
		runTask2a("12E9000", 1.0, 1.0, 1.0);
	}

	public void test2a_11_34() {
		runTask2a("12E9000", 1.0, +0.0, +0.0);
	}

	public void test2a_11_35() {
		runTask2a("12E9000", 1.0, -0.0, -0.0);
	}

	public void test2a_11_36() {
		runTask2a("12E9000", +0.0, 0, 0);
	}

	public void test2a_11_37() {
		runTask2a("12E9000", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_11_38() {
		runTask2a("12E9000", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_11_39() {
		runTask2a("12E9000", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_11_40() {
		runTask2a("12E9000", +0.0, 1.0, 1.0);
	}

	public void test2a_11_41() {
		runTask2a("12E9000", +0.0, +0.0, +0.0);
	}

	public void test2a_11_42() {
		runTask2a("12E9000", +0.0, -0.0, -0.0);
	}

	public void test2a_12_01() {
		runTask2a("-12E9000", 0, 0, 0);
	}

	public void test2a_12_02() {
		runTask2a("-12E9000", 0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_12_03() {
		runTask2a("-12E9000", 0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_12_04() {
		runTask2a("-12E9000", 0, Double.NaN, Double.NaN);
	}

	public void test2a_12_05() {
		runTask2a("-12E9000", 0, 1.0, 1.0);
	}

	public void test2a_12_06() {
		runTask2a("-12E9000", 0, +0.0, +0.0);
	}

	public void test2a_12_07() {
		runTask2a("-12E9000", 0, -0.0, -0.0);
	}

	public void test2a_12_08() {
		runTask2a("-12E9000", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_12_09() {
		runTask2a("-12E9000", Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_12_10() {
		runTask2a("-12E9000", Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_12_11() {
		runTask2a("-12E9000", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_12_12() {
		runTask2a("-12E9000", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_12_13() {
		runTask2a("-12E9000", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_12_14() {
		runTask2a("-12E9000", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_12_15() {
		runTask2a("-12E9000", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_12_16() {
		runTask2a("-12E9000", Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	public void test2a_12_17() {
		runTask2a("-12E9000", Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	public void test2a_12_18() {
		runTask2a("-12E9000", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_12_19() {
		runTask2a("-12E9000", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_12_20() {
		runTask2a("-12E9000", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_12_21() {
		runTask2a("-12E9000", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_12_22() {
		runTask2a("-12E9000", Double.NaN, 0, 0);
	}

	public void test2a_12_23() {
		runTask2a("-12E9000", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_12_24() {
		runTask2a("-12E9000", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_12_25() {
		runTask2a("-12E9000", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_12_26() {
		runTask2a("-12E9000", Double.NaN, 1.0, 1.0);
	}

	public void test2a_12_27() {
		runTask2a("-12E9000", Double.NaN, +0.0, +0.0);
	}

	public void test2a_12_28() {
		runTask2a("-12E9000", Double.NaN, -0.0, -0.0);
	}

	public void test2a_12_29() {
		runTask2a("-12E9000", 1.0, 0, 0);
	}

	public void test2a_12_30() {
		runTask2a("-12E9000", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_12_31() {
		runTask2a("-12E9000", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_12_32() {
		runTask2a("-12E9000", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_12_33() {
		runTask2a("-12E9000", 1.0, 1.0, 1.0);
	}

	public void test2a_12_34() {
		runTask2a("-12E9000", 1.0, +0.0, +0.0);
	}

	public void test2a_12_35() {
		runTask2a("-12E9000", 1.0, -0.0, -0.0);
	}

	public void test2a_12_36() {
		runTask2a("-12E9000", +0.0, 0, 0);
	}

	public void test2a_12_37() {
		runTask2a("-12E9000", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_12_38() {
		runTask2a("-12E9000", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_12_39() {
		runTask2a("-12E9000", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_12_40() {
		runTask2a("-12E9000", +0.0, 1.0, 1.0);
	}

	public void test2a_12_41() {
		runTask2a("-12E9000", +0.0, +0.0, +0.0);
	}

	public void test2a_12_42() {
		runTask2a("-12E9000", +0.0, -0.0, -0.0);
	}

	public void test2a_13_01() {
		runTask2a("ерунда", 0, 0, 0);
	}

	public void test2a_13_02() {
		runTask2a("ерунда", 0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_13_03() {
		runTask2a("ерунда", 0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_13_04() {
		runTask2a("ерунда", 0, Double.NaN, Double.NaN);
	}

	public void test2a_13_05() {
		runTask2a("ерунда", 0, 1.0, 1.0);
	}

	public void test2a_13_06() {
		runTask2a("ерунда", 0, +0.0, +0.0);
	}

	public void test2a_13_07() {
		runTask2a("ерунда", 0, -0.0, -0.0);
	}

	public void test2a_13_08() {
		runTask2a("ерунда", Double.POSITIVE_INFINITY, 0, 0);
	}

	public void test2a_13_09() {
		runTask2a("ерунда", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_13_10() {
		runTask2a("ерунда", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_13_11() {
		runTask2a("ерунда", Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_13_12() {
		runTask2a("ерунда", Double.POSITIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_13_13() {
		runTask2a("ерунда", Double.POSITIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_13_14() {
		runTask2a("ерунда", Double.POSITIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_13_15() {
		runTask2a("ерунда", Double.NEGATIVE_INFINITY, 0, 0);
	}

	public void test2a_13_16() {
		runTask2a("ерунда", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_13_17() {
		runTask2a("ерунда", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_13_18() {
		runTask2a("ерунда", Double.NEGATIVE_INFINITY, Double.NaN, Double.NaN);
	}

	public void test2a_13_19() {
		runTask2a("ерунда", Double.NEGATIVE_INFINITY, 1.0, 1.0);
	}

	public void test2a_13_20() {
		runTask2a("ерунда", Double.NEGATIVE_INFINITY, +0.0, +0.0);
	}

	public void test2a_13_21() {
		runTask2a("ерунда", Double.NEGATIVE_INFINITY, -0.0, -0.0);
	}

	public void test2a_13_22() {
		runTask2a("ерунда", Double.NaN, 0, 0);
	}

	public void test2a_13_23() {
		runTask2a("ерунда", Double.NaN, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_13_24() {
		runTask2a("ерунда", Double.NaN, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_13_25() {
		runTask2a("ерунда", Double.NaN, Double.NaN, Double.NaN);
	}

	public void test2a_13_26() {
		runTask2a("ерунда", Double.NaN, 1.0, 1.0);
	}

	public void test2a_13_27() {
		runTask2a("ерунда", Double.NaN, +0.0, +0.0);
	}

	public void test2a_13_28() {
		runTask2a("ерунда", Double.NaN, -0.0, -0.0);
	}

	public void test2a_13_29() {
		runTask2a("ерунда", 1.0, 0, 0);
	}

	public void test2a_13_30() {
		runTask2a("ерунда", 1.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_13_31() {
		runTask2a("ерунда", 1.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_13_32() {
		runTask2a("ерунда", 1.0, Double.NaN, Double.NaN);
	}

	public void test2a_13_33() {
		runTask2a("ерунда", 1.0, 1.0, 1.0);
	}

	public void test2a_13_34() {
		runTask2a("ерунда", 1.0, +0.0, +0.0);
	}

	public void test2a_13_35() {
		runTask2a("ерунда", 1.0, -0.0, -0.0);
	}

	public void test2a_13_36() {
		runTask2a("ерунда", +0.0, 0, 0);
	}

	public void test2a_13_37() {
		runTask2a("ерунда", +0.0, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY);
	}

	public void test2a_13_38() {
		runTask2a("ерунда", +0.0, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY);
	}

	public void test2a_13_39() {
		runTask2a("ерунда", +0.0, Double.NaN, Double.NaN);
	}

	public void test2a_13_40() {
		runTask2a("ерунда", +0.0, 1.0, 1.0);
	}

	public void test2a_13_41() {
		runTask2a("ерунда", +0.0, +0.0, +0.0);
	}

	public void test2a_13_42() {
		runTask2a("ерунда", +0.0, -0.0, -0.0);
	}
}
