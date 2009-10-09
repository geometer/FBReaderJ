package org.test.zlibrary.hyphenation;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestAll {
    public static Test suite() {
        TestSuite suite = new TestSuite();
	suite.addTestSuite(TestTextTeXHyphenationPattern.class);        
	suite.addTestSuite(TestTextTeXHyphenator.class);
	return suite;
    }
}

