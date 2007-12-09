package org.test.zlibrary.options;

import org.zlibrary.core.options.ZLIntegerOption;
import org.zlibrary.core.options.config.ZLConfig;
import org.zlibrary.core.options.config.ZLConfigInstance;

import junit.framework.TestCase;

public class ZLIntegerOptionTests1 extends TestCase {

	private ZLConfig myConfig = ZLConfigInstance.getInstance();

	private void runTask1(String configValue, int defaultValue,
			int expectedValue) {
		myConfig.setValue("integer_group", "name", configValue, "category");
		ZLIntegerOption option = new ZLIntegerOption("category",
				"integer_group", "name", defaultValue);
		assertEquals(option.getValue(), expectedValue);
	}

	public void tearDown() {
		myConfig.unsetValue("integer_group", "name");
	}

	public void test1_01() {
		runTask1("1", 1, 1);
	}

	public void test1_02() {
		runTask1("1", 0, 1);
	}

	public void test1_03() {
		runTask1("", 1, 1);
	}

	public void test1_04() {
		runTask1("", 0, 0);
	}

	public void test1_05() {
		runTask1("ерунда", 1, 1);
	}

	public void test1_06() {
		runTask1("ерунда", 0, 0);
	}

	public void test1_07() {
		runTask1(null, 1, 1);
	}

	public void test1_08() {
		runTask1(null, 0, 0);
	}
}
