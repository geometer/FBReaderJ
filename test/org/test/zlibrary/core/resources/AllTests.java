package org.test.zlibrary.core.resources;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestResources.class);    
        return suite;
    }
}
