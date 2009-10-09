package org.test.zlibrary.options;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll {

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ConfigIOTests.class);
		suite.addTestSuite(UtilTests.class);
		suite.addTestSuite(ZLConfigWriterTests.class);

		suite.addTestSuite(ZLBoolean3OptionTests1.class);
		suite.addTestSuite(ZLBoolean3OptionTests2.class);
		suite.addTestSuite(ZLBooleanOptionTests1.class);
		suite.addTestSuite(ZLBooleanOptionTests2.class);
		suite.addTestSuite(ZLColorOptionTests1.class);
		suite.addTestSuite(ZLColorOptionTests2.class);
		suite.addTestSuite(ZLIntegerOptionTests1.class);
		suite.addTestSuite(ZLIntegerOptionTests2.class);
		suite.addTestSuite(ZLIntegerRangeOptionTests1.class);
		suite.addTestSuite(ZLIntegerRangeOptionTests2.class);
		suite.addTestSuite(ZLStringOptionTests1.class);
		suite.addTestSuite(ZLStringOptionTests2.class);
		return suite;
	}
}
