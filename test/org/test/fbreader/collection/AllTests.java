package org.test.fbreader.collection;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
	public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestBookCollection.class);
        suite.addTestSuite(TestBookList.class);
        return suite;
    }

}
