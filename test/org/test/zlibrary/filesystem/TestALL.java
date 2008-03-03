package org.test.zlibrary.filesystem;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestALL {
	public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestZLDir.class);
        suite.addTestSuite(TestZLFile.class);
//      suite.addTestSuite(TestZLFSManager.class);
        return suite;
    }
}
