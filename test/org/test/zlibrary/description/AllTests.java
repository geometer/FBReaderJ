package org.test.zlibrary.description;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestDescriptionBook.class);
        return suite;
    }

}
