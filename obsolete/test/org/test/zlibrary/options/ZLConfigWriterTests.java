package org.test.zlibrary.options;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.geometerplus.zlibrary.core.config.ZLConfigManager;

/**
 * тесты после 3-го - на запись дельты
 * 
 * @author Администратор
 * 
 */
public class ZLConfigWriterTests extends ZLOptionTests {
	final static String outputPath = "test/org/test/zlibrary/options/examples/output/";

	private ZLConfigWriterTests() {
		super(outputPath);
	}

	private void writeConfigAndCheck(String fileName, String expectedContent)
			throws FileNotFoundException {

		if (fileName.equals("delta")) {
			ZLConfigManager.getInstance().saveDelta();
		} else {
			ZLConfigManager.getInstance().saveAll();
		}
		try {
			FileReader fr = new FileReader(new File("test/org/test/zlibrary/options/examples/output/" + fileName + ".xml"));
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
			throw e;
		} catch (IOException e) {
			fail("IOException : " + e.getMessage());
		} catch (IndexOutOfBoundsException e) {
			fail("content has length that's not equal expected content length ");
		}
	}

	public void tearDown() {
		new File("test/org/test/zlibrary/options/examples/output/delta.xml").delete();
		new File("test/org/test/zlibrary/options/examples/output/category.xml").delete();
	}

	public void test01() {
		getConfig().setValue("group", "name", "VALUE", "category");
		try {
			writeConfigAndCheck("category", ""
					+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<config>\n" + "  <group name=\"group\">\n"
					+ "    <option name=\"name\" value=\"VALUE\"/>\n"
					+ "  </group>\n" + "</config>");
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		getConfig().unsetValue("group", "name");
	}

	public void test02() {
		getConfig().setValue("group", "name1", "VALUE1", "category");
		getConfig().setValue("group", "name2", "VALUE2", "category");
		try {
			writeConfigAndCheck("category", ""
					+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<config>\n" + "  <group name=\"group\">\n"
					+ "    <option name=\"name1\" value=\"VALUE1\"/>\n"
					+ "    <option name=\"name2\" value=\"VALUE2\"/>\n"
					+ "  </group>\n" + "</config>");
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		getConfig().unsetValue("group", "name1");
		getConfig().unsetValue("group", "name2");

	}

	public void test03() {
		getConfig().setValue("group1", "name1", "VALUE1", "category");
		getConfig().setValue("group1", "name2", "VALUE2", "category");
		getConfig().setValue("group2", "name3", "VALUE3", "category");
		try {
			writeConfigAndCheck("category", ""
					+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					+ "<config>\n" + "  <group name=\"group1\">\n"
					+ "    <option name=\"name1\" value=\"VALUE1\"/>\n"
					+ "    <option name=\"name2\" value=\"VALUE2\"/>\n"
					+ "  </group>\n" + "  <group name=\"group2\">\n"
					+ "    <option name=\"name3\" value=\"VALUE3\"/>\n"
					+ "  </group>\n" + "</config>");
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		getConfig().unsetValue("group1", "name1");
		getConfig().unsetValue("group1", "name2");
		getConfig().unsetValue("group2", "name3");
	}

	public void test04() {
		getConfig().setValue("group1", "name1", "VALUE1", "category");
		try {
			writeConfigAndCheck(
					"delta",
					""
							+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
							+ "<config>\n"
							+ "  <group name=\"group1\">\n"
							+ "    <option name=\"name1\"/>\n"
							+ "    <option name=\"name2\"/>\n"
							+ "  </group>\n"
							+ "  <group name=\"group2\">\n"
							+ "    <option name=\"name3\"/>\n"
							+ "  </group>\n"
							+ "  <group name=\"group1\">\n"
							+ "    <option name=\"name1\" value=\"VALUE1\" category=\"category\"/>\n"
							+ "  </group>\n" + "</config>");
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		getConfig().unsetValue("group1", "name1");
	}

	// тест на НЕзапись пустой категории
	public void test05() {
		getConfig().setValue("group", "name", "VALUE", "category");
		getConfig().unsetValue("group", "name");
		try {
			writeConfigAndCheck("category", "");
		} catch (FileNotFoundException e) {
			return;
		}
		fail("file category.xml wasn't deleted!");
	}

	// тест на НЕзапись не измененной категории
	public void test06() {
		getConfig().setValue("group1", "name1", "VALUE1", "category");
		getConfig().setValue("group1", "name2", "VALUE2", "category");
		getConfig().setValue("group2", "name3", "VALUE3", "category");

		ZLConfigManager.getInstance().saveAll();
		long modified = new File(outputPath + "category.xml").lastModified();

		getConfig().setValue("group", "name", "VALUE", "other_category");
		// getConfig().setValue("group", "name2", "VALUE", "category");

		ZLConfigManager.getInstance().saveAll();
		long thenModified = new File(outputPath + "category.xml")
				.lastModified();
		assertTrue(modified == thenModified);

		getConfig().unsetValue("group", "name");
		getConfig().unsetValue("group1", "name1");
		getConfig().unsetValue("group1", "name2");
		getConfig().unsetValue("group2", "name3");
		new File(outputPath + "other_category.xml").delete();
	}

	// тест на СТИРАНИЕ пустой категории
	public void test07() {
		getConfig().setValue("group1", "name1", "VALUE1", "category");
		ZLConfigManager.getInstance().saveAll();

		getConfig().unsetValue("group1", "name1");
		ZLConfigManager.getInstance().saveAll();

		try {
			writeConfigAndCheck("category", "");
		} catch (FileNotFoundException e) {
			return;
		}
		fail("file category.xml wasn't deleted!");
	}
}
