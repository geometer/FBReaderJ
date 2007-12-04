package org.test.zlibrary.model;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll {
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestTextControlEntry.class);
        suite.addTestSuite(TestTextEntry.class);
        suite.addTestSuite(TestTextParagraph.class);
        suite.addTestSuite(TestZLTextModel.class);       
        suite.addTestSuite(TestTreeParagraph.class); 
        suite.addTestSuite(TestTreeModel.class); 
        return suite;
    }
}
