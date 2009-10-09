package org.test.fbreader.formats.fb2;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestFB2Reader.class);    
        return suite;
    }
}
