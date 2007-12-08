package org.test.zlibrary.options;

import org.zlibrary.core.options.ZLBoolean3Option;
import org.zlibrary.core.options.config.ZLConfig;
import org.zlibrary.core.options.config.ZLConfigInstance;
import org.zlibrary.core.options.util.ZLBoolean3;

import junit.framework.TestCase;

public class ZLBoolean3OptionTests2 extends TestCase {
	
	private ZLConfig myConfig = ZLConfigInstance.getInstance();
	
	private void runTask2a(String configValue, ZLBoolean3 value2, 
			ZLBoolean3 value3, ZLBoolean3 expectedValue) {
		myConfig.setValue("boolean3_group_2", "name", configValue, "boolean3_category");
		ZLBoolean3Option option = new ZLBoolean3Option("boolean3_category", 
				"boolean3_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(option.getValue(), expectedValue);
	}
	
	private void runTask2b(String configValue, ZLBoolean3 value2, 
			ZLBoolean3 value3, String expectedValue) {
		myConfig.setValue("boolean3_group_2", "name", configValue, "boolean3_category");
		ZLBoolean3Option option = new ZLBoolean3Option("boolean3_category", 
				"boolean3_group_2", "name", value2);
		option.setValue(value3);
		assertEquals(myConfig.getValue("boolean3_group_2", "name", "def"), expectedValue);
	}
	
	public void tearDown() {
		myConfig.unsetValue("boolean3_group_2", "name");
	}
	
	public void test2a_1_01() {
		runTask2a(null, null, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_1_02() {
		runTask2a(null, null, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_1_03() {
		runTask2a(null, null, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_1_04() {
		runTask2a(null, ZLBoolean3.B3_UNDEFINED, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_1_05() {
		runTask2a(null, ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_1_06() {
		runTask2a(null, ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_1_07() {
		runTask2a(null, ZLBoolean3.B3_TRUE, 
				null, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_1_08() {
		runTask2a(null, ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_1_09() {
		runTask2a(null, ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_2_01() {
		runTask2a("", null, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_2_02() {
		runTask2a("", null, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_2_03() {
		runTask2a("", null, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_2_04() {
		runTask2a("", ZLBoolean3.B3_UNDEFINED, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_2_05() {
		runTask2a("", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_2_06() {
		runTask2a("", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_2_07() {
		runTask2a("", ZLBoolean3.B3_TRUE, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_2_08() {
		runTask2a("", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_2_09() {
		runTask2a("", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_3_01() {
		runTask2a("true", null, 
				null, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_3_02() {
		runTask2a("true", null, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_3_03() {
		runTask2a("true", null, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_3_04() {
		runTask2a("true", ZLBoolean3.B3_UNDEFINED, 
				null, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_3_05() {
		runTask2a("true", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_3_06() {
		runTask2a("true", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_3_07() {
		runTask2a("true", ZLBoolean3.B3_TRUE, 
				null, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_3_08() {
		runTask2a("true", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_3_09() {
		runTask2a("true", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_4_01() {
		runTask2a("undefined", null, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_4_02() {
		runTask2a("undefined", null, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_4_03() {
		runTask2a("undefined", null, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_4_04() {
		runTask2a("undefined", ZLBoolean3.B3_UNDEFINED, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_4_05() {
		runTask2a("undefined", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_4_06() {
		runTask2a("undefined", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_4_07() {
		runTask2a("undefined", ZLBoolean3.B3_TRUE, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_4_08() {
		runTask2a("undefined", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_4_09() {
		runTask2a("undefined", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_5_01() {
		runTask2a("ерунда", null, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_5_02() {
		runTask2a("ерунда", null, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_5_03() {
		runTask2a("ерунда", null, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_5_04() {
		runTask2a("ерунда", ZLBoolean3.B3_UNDEFINED, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_5_05() {
		runTask2a("ерунда", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_5_06() {
		runTask2a("ерунда", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2a_5_07() {
		runTask2a("ерунда", ZLBoolean3.B3_TRUE, 
				null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_5_08() {
		runTask2a("ерунда", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test2a_5_09() {
		runTask2a("ерунда", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test2b_1_01() {
		runTask2b(null, null, 
				null, "def");
	}
	
	public void test2b_1_02() {
		runTask2b(null, null, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_1_03() {
		runTask2b(null, null, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_1_04() {
		runTask2b(null, ZLBoolean3.B3_UNDEFINED, 
				null, "def");
	}
	
	public void test2b_1_05() {
		runTask2b(null, ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_1_06() {
		runTask2b(null, ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_1_07() {
		runTask2b(null, ZLBoolean3.B3_TRUE, 
				null, "def");
	}
	
	public void test2b_1_08() {
		runTask2b(null, ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, "undefined");
	}
	
	public void test2b_1_09() {
		runTask2b(null, ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, "def");
	}
	
	public void test2b_2_01() {
		runTask2b("", null, 
				null, "");
	}
	
	public void test2b_2_02() {
		runTask2b("", null, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_2_03() {
		runTask2b("", null, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_2_04() {
		runTask2b("", ZLBoolean3.B3_UNDEFINED, 
				null, "");
	}
	
	public void test2b_2_05() {
		runTask2b("", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_2_06() {
		runTask2b("", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_2_07() {
		runTask2b("", ZLBoolean3.B3_TRUE, 
				null, "");
	}
	
	public void test2b_2_08() {
		runTask2b("", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, "undefined");
	}
	
	public void test2b_2_09() {
		runTask2b("", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, "def");
	}
	
	public void test2b_3_01() {
		runTask2b("true", null, 
				null, "true");
	}
	
	public void test2b_3_02() {
		runTask2b("true", null, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_3_03() {
		runTask2b("true", null, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_3_04() {
		runTask2b("true", ZLBoolean3.B3_UNDEFINED, 
				null, "true");
	}
	
	public void test2b_3_05() {
		runTask2b("true", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_3_06() {
		runTask2b("true", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_3_07() {
		runTask2b("true", ZLBoolean3.B3_TRUE, 
				null, "true");
	}
	
	public void test2b_3_08() {
		runTask2b("true", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, "undefined");
	}
	
	public void test2b_3_09() {
		runTask2b("true", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, "def");
	}
	
	public void test2b_4_01() {
		runTask2b("undefined", null, 
				null, "undefined");
	}
	
	public void test2b_4_02() {
		runTask2b("undefined", null, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_4_03() {
		runTask2b("undefined", null, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_4_04() {
		runTask2b("undefined", ZLBoolean3.B3_UNDEFINED, 
				null, "undefined");
	}
	
	public void test2b_4_05() {
		runTask2b("undefined", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_4_06() {
		runTask2b("undefined", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_4_07() {
		runTask2b("undefined", ZLBoolean3.B3_TRUE, 
				null, "undefined");
	}
	
	public void test2b_4_08() {
		runTask2b("undefined", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, "undefined");
	}
	
	public void test2b_4_09() {
		runTask2b("undefined", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, "def");
	}
	
	public void test2b_5_01() {
		runTask2b("ерунда", null, 
				null, "ерунда");
	}
	
	public void test2b_5_02() {
		runTask2b("ерунда", null, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_5_03() {
		runTask2b("ерунда", null, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_5_04() {
		runTask2b("ерунда", ZLBoolean3.B3_UNDEFINED, 
				null, "ерунда");
	}
	
	public void test2b_5_05() {
		runTask2b("ерунда", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_UNDEFINED, "def");
	}
	
	public void test2b_5_06() {
		runTask2b("ерунда", ZLBoolean3.B3_UNDEFINED, 
				ZLBoolean3.B3_TRUE, "true");
	}
	
	public void test2b_5_07() {
		runTask2b("ерунда", ZLBoolean3.B3_TRUE, 
				null, "ерунда");
	}
	
	public void test2b_5_08() {
		runTask2b("ерунда", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_UNDEFINED, "undefined");
	}
	
	public void test2b_5_09() {
		runTask2b("ерунда", ZLBoolean3.B3_TRUE, 
				ZLBoolean3.B3_TRUE, "def");
	}
}	
