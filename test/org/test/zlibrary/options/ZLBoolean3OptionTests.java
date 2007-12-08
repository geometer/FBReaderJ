package org.test.zlibrary.options;

import org.zlibrary.core.options.ZLBoolean3Option;
import org.zlibrary.core.options.util.ZLBoolean3;
import org.zlibrary.core.options.config.ZLConfig;
import org.zlibrary.core.options.config.ZLConfigInstance;

import junit.framework.TestCase;

public class ZLBoolean3OptionTests extends TestCase {
	
	private ZLConfig myConfig = ZLConfigInstance.getInstance();
	
	private void runTask1(String configValue, ZLBoolean3 defaultValue, 
			ZLBoolean3 expectedValue) {
		myConfig.setValue("boolean3_group", "name", configValue, "category");
		ZLBoolean3Option option = new ZLBoolean3Option("category", 
				"boolean3_group", "name", defaultValue);
		assertEquals(option.getValue(), expectedValue);
	}
	
	public void test1_01() {
		runTask1("", null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test1_02() {
		runTask1("", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
	
	public void test1_03() {
		runTask1(null, null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test1_04() {
		runTask1(null, ZLBoolean3.B3_FALSE, ZLBoolean3.B3_FALSE);
	}
	
	public void test1_05() {
		runTask1("false", null, ZLBoolean3.B3_FALSE);
	}
	
	public void test1_06() {
		runTask1("true", ZLBoolean3.B3_FALSE, ZLBoolean3.B3_TRUE);
	}
	
	public void test1_07() {
		runTask1("ерунда", null, ZLBoolean3.B3_UNDEFINED);
	}
	
	public void test1_08() {
		runTask1("ерунда", ZLBoolean3.B3_TRUE, ZLBoolean3.B3_TRUE);
	}
}
