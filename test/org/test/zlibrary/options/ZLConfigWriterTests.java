package org.test.zlibrary.options;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.TestCase;

import org.zlibrary.core.options.config.ZLConfig;
import org.zlibrary.core.options.config.ZLConfigInstance;
import org.zlibrary.core.options.config.ZLConfigWriterFactory;

public class ZLConfigWriterTests extends TestCase {
	
	private ZLConfig myConfig = ZLConfigInstance.getInstance();
	
	public void test01() {
		myConfig.setValue("boolean_group_2", "name", "VALUE", "boolean_category");
		String outputPath = "test/org/test/" +
					"zlibrary/options/examples/output/";
		ZLConfigWriterFactory.createConfigWriter(outputPath).write();
		String expectedContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					 + "<config>\n"
					 + "  <group name=\"boolean_group_2\">\n"
					 + "    <option name=\"name\" value=\"VALUE\"/>\n"
					 + "  </group>\n"
					 + "</config>";
		//System.out.prBooleanln(expectedContent.length());
		try {
			FileReader fr = new FileReader(new File("test/org/test/" +
				"zlibrary/options/examples/output/boolean_category.xml"));
			try {
				int expectedContentSize = expectedContent.length();
				char[] buf = new char[expectedContentSize];
				fr.read(buf, 0, expectedContentSize);
				assertEquals(expectedContent, new String(buf));
			} finally {
				try {
					fr.close();
				} catch (IOException e) {
					fail("can't close reader");
				}
			}
		} catch (FileNotFoundException e) {
			fail("File Not Found : " + e.getMessage());
		} catch (IOException e) {
			fail("IOException : " + e.getMessage());
		} catch (IndexOutOfBoundsException e) {
			fail("content has length that's not equal expected content length ");
		}
	}
}
