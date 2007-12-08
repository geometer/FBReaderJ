package org.test.zlibrary.options;

import org.zlibrary.core.options.ZLDoubleOption;
import org.zlibrary.core.options.config.ZLConfig;
import org.zlibrary.core.options.config.ZLConfigInstance;

import junit.framework.TestCase;

public class ZLDoubleOptionTests extends TestCase {
	
	private ZLConfig myConfig = ZLConfigInstance.getInstance();
	
	private void runTask1(String configValue, double defaultValue, 
			double expectedValue) {
		myConfig.setValue("double_group", "name", configValue, "category");
		ZLDoubleOption option = new ZLDoubleOption("category", 
				"double_group", "name", defaultValue);
		assertEquals(option.getValue(), expectedValue);
	}
	
	public void test1_01() {
		runTask1("", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public void test1_02() {
		runTask1("", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_03() {
		runTask1("", Double.NaN, Double.NaN);
	}
	
	public void test1_04() {
		runTask1("", 1.0, 1.0);
	}
	
	public void test1_05() {
		runTask1("", 0.0, 0.0);
	}
	
	public void test1_06() {
		runTask1(null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public void test1_07() {
		runTask1(null, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_08() {
		runTask1(null, Double.NaN, Double.NaN);
	}
	
	public void test1_09() {
		runTask1(null, 1.0, 1.0);
	}
	
	public void test1_10() {
		runTask1(null, 0.0, 0.0);
	}
	
	public void test1_11() {
		runTask1("Infinity", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public void test1_12() {
		runTask1("Infinity", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public void test1_13() {
		runTask1("Infinity", Double.NaN, Double.POSITIVE_INFINITY);
	}
	
	public void test1_14() {
		runTask1("Infinity", 1.0, Double.POSITIVE_INFINITY);
	}
	
	public void test1_15() {
		runTask1("Infinity", 0.0, Double.POSITIVE_INFINITY);
	}
	
	public void test1_16() {
		runTask1("-Infinity", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_17() {
		runTask1("-Infinity", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_18() {
		runTask1("-Infinity", Double.NaN, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_19() {
		runTask1("-Infinity", 1.0, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_20() {
		runTask1("-Infinity", 0.0, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_21() {
		runTask1("NaN", Double.POSITIVE_INFINITY, Double.NaN);
	}
	
	public void test1_22() {
		runTask1("NaN", Double.NEGATIVE_INFINITY, Double.NaN);
	}
	
	public void test1_23() {
		runTask1("NaN", Double.NaN, Double.NaN);
	}
	
	public void test1_24() {
		runTask1("NaN", 1.0, Double.NaN);
	}
	
	public void test1_25() {
		runTask1("NaN", 0.0, Double.NaN);
	}
	
	public void test1_26() {
		runTask1("0", Double.POSITIVE_INFINITY, 0);
	}
	
	public void test1_27() {
		runTask1("0", Double.NEGATIVE_INFINITY, 0);
	}
	
	public void test1_28() {
		runTask1("0", Double.NaN, 0);
	}
	
	public void test1_29() {
		runTask1("0", 1.0, 0);
	}
	
	public void test1_30() {
		runTask1("0", 0.0, 0);
	}
	
	public void test1_31() {
		runTask1("1.1", Double.POSITIVE_INFINITY, 1.1);
	}
	
	public void test1_32() {
		runTask1("1.1", Double.NEGATIVE_INFINITY, 1.1);
	}
	
	public void test1_33() {
		runTask1("1.1", Double.NaN, 1.1);
	}
	
	public void test1_34() {
		runTask1("1.1", 1.0, 1.1);
	}
	
	public void test1_35() {
		runTask1("1.1", 0.0, 1.1);
	}
	
	public void test1_36() {
		runTask1("4", Double.POSITIVE_INFINITY, 4);
	}
	
	public void test1_37() {
		runTask1("4", Double.NEGATIVE_INFINITY, 4);
	}
	
	public void test1_38() {
		runTask1("4", Double.NaN, 4);
	}
	
	public void test1_39() {
		runTask1("4", 1.0, 4);
	}
	
	public void test1_40() {
		runTask1("4", 0.0, 4);
	}
	
	public void test1_41() {
		runTask1("12E-78", Double.POSITIVE_INFINITY, 12E-78);
	}
	
	public void test1_42() {
		runTask1("12E-78", Double.NEGATIVE_INFINITY, 12E-78);
	}
	
	public void test1_43() {
		runTask1("12E-78", Double.NaN, 12E-78);
	}
	
	public void test1_44() {
		runTask1("12E-78", 1.0, 12E-78);
	}
	
	public void test1_45() {
		runTask1("12E-78", 0.0, 12E-78);
	}
	
	public void test1_46() {
		runTask1("12E-9000", Double.POSITIVE_INFINITY, 0);
	}
	
	public void test1_47() {
		runTask1("12E-9000", Double.NEGATIVE_INFINITY, 0);
	}
	
	public void test1_48() {
		runTask1("12E-9000", Double.NaN, 0);
	}
	
	public void test1_49() {
		runTask1("12E-9000", 1.0, 0);
	}
	
	public void test1_50() {
		runTask1("12E-9000", 0.0, 0);
	}
	
	public void test1_51() {
		runTask1("12E9000", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public void test1_52() {
		runTask1("12E9000", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public void test1_53() {
		runTask1("12E9000", Double.NaN, Double.POSITIVE_INFINITY);
	}
	
	public void test1_54() {
		runTask1("12E9000", 1.0, Double.POSITIVE_INFINITY);
	}
	
	public void test1_55() {
		runTask1("12E9000", 0.0, Double.POSITIVE_INFINITY);
	}
	
	public void test1_56() {
		runTask1("-12E9000", Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_57() {
		runTask1("-12E9000", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_58() {
		runTask1("-12E9000", Double.NaN, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_59() {
		runTask1("-12E9000", 1.0, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_60() {
		runTask1("-12E9000", 0.0, Double.NEGATIVE_INFINITY);
	}

	public void test1_61() {
		runTask1("ерунда", Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public void test1_62() {
		runTask1("ерунда", Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
	public void test1_63() {
		runTask1("ерунда", Double.NaN, Double.NaN);
	}
	
	public void test1_64() {
		runTask1("ерунда", 1.0, 1.0);
	}
	
	public void test1_65() {
		runTask1("ерунда", 0.0, 0.0);
	}
	
}
