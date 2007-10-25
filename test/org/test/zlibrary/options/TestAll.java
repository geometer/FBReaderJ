package org.test.zlibrary.options;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(ModelTests.class);       
        return suite;
    }
}