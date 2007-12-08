package org.test.zlibrary.options;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ConfigTests.class);
		suite.addTestSuite(ModelTests.class);
		suite.addTestSuite(UtilTests.class);
		suite.addTestSuite(ZLBoolean3OptionTests.class);
		suite.addTestSuite(ZLBooleanOptionTests.class);
		suite.addTestSuite(ZLColorOptionTests.class);
		suite.addTestSuite(ZLDoubleOptionTests.class);
		suite.addTestSuite(ZLIntegerOptionTests.class);
		suite.addTestSuite(ZLIntegerRangeOptionTests.class);
		suite.addTestSuite(ZLStringOptionTests.class);
		return suite;
	}
}