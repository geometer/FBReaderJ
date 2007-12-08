package org.test.zlibrary.options;

import org.zlibrary.core.options.ZLStringOption;
import org.zlibrary.core.options.config.ZLConfig;
import org.zlibrary.core.options.config.ZLConfigInstance;

import junit.framework.TestCase;

public class ZLStringOptionTests extends TestCase {
	
	private ZLConfig myConfig = ZLConfigInstance.getInstance();
	
	private void runTask1(String configValue, String defaultValue, 
			String expectedValue) {
		myConfig.setValue("string_group", "name", configValue, "category");
		ZLStringOption option = new ZLStringOption("category", 
				"string_group", "name", defaultValue);
		assertEquals(option.getValue(), expectedValue);
	}
	
	public void test1_01() {
		runTask1(null, "", "");
	}
	
	public void test1_02() {
		runTask1(null, "str", "str");
	}
	
	public void test1_03() {
		runTask1(null, null, "");
	}
	
	public void test1_04() {
		runTask1("", "", "");
	}
	
	public void test1_05() {
		runTask1("", "str", "");
	}
	
	public void test1_06() {
		runTask1("", null, "");
	}
	
	public void test1_07() {
		runTask1("str", "", "str");
	}
	
	public void test1_08() {
		runTask1("str", "str", "str");
	}
	
	public void test1_09() {
		runTask1("str", null, "str");
	}	
}
