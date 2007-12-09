package org.test.zlibrary.options;

import java.io.File;

import org.zlibrary.core.options.config.*;
import org.zlibrary.core.options.*;
import org.zlibrary.core.xml.sax.ZLSaxXMLProcessorFactory;
import org.zlibrary.ui.swing.library.ZLSwingLibrary;

import junit.framework.TestCase;

/**
 * содержит тесты на ZLConfigReader
 * 
 * @author Администратор
 */
public class ConfigIOTests extends TestCase {

	private ZLConfig myConfig = ZLConfigInstance.getInstance();

	public void setUp() {
		new ZLSwingLibrary();
		new ZLSaxXMLProcessorFactory();
		ZLConfigReaderFactory.createConfigReader(
				"test/org/test/" + "zlibrary/options/examples/").read();
	}

	public void tearDown() {
		new File("test/org/test/zlibrary/options/examples/output/new_category.xml").delete();
		new File("test/org/test/zlibrary/options/examples/output/options.xml").delete();
	}
	// тест на выдачу конфигом прочитанного значения
	public void test01() {
		assertEquals(myConfig.getValue("Options", "KeyDelay", "100"), 0 + "");
	}

	// ставим значение в опцию, а достаем из конфига
	public void test02() {
		ZLIntegerOption option = new ZLIntegerOption("options", "Options",
				"KeyDelay", 100);
		option.setValue(1223);
		assertEquals(myConfig.getValue("Options", "KeyDelay", "100"), 1223 + "");
	}

	// ставим в опцию значение, пишем конфиг в файл, читаем оттуда
	// снова и смотрим что получилось
	public void test03() {
		ZLIntegerOption option = new ZLIntegerOption("options", "Options",
				"KeyDelay", 100);
		option.setValue(123);
		ZLConfigWriterFactory.createConfigWriter(
				"test/org/test/zlibrary/options/examples/output/").write();

		// myConfig.unsetValue("Options", "KeyDelay");

		try {
			ZLConfigReaderFactory.createConfigReader(
					"test/org/test/" + "zlibrary/options/examples/output/")
					.read();
		} catch (IndexOutOfBoundsException e) {

		}
		assertEquals(myConfig.getValue("Options", "KeyDelay", "100"), 123 + "");
	}

	// просто тест на unset
	public void test04() {
		ZLConfigReaderFactory.createConfigReader(
				"test/org/test/" + "zlibrary/options/examples/output/").read();
		myConfig.unsetValue("Options", "KeyDelay");
		assertEquals(myConfig.getValue("Options", "KeyDelay", "meaningless"),
				"meaningless");
	}

	// тест на set несколько раз
	public void test05() {
		myConfig.setValue("Options", "KeyDelay", "VALUE1", "new_category");
		myConfig.setValue("Options", "KeyDelay", "VALUE2", "options");
		assertEquals(myConfig.getValue("Options", "KeyDelay", "meaningless"),
				"VALUE2");
	}

	// тест на появление новой категории
	public void test06() {
		myConfig.setValue("Options", "KeyDelay", "VALUE", "new_category");
		String outputPath = "test/org/test/"
				+ "zlibrary/options/examples/output/";
		ZLConfigWriterFactory.createConfigWriter(outputPath).write();
		String[] outDirFiles = new File(outputPath).list();
		for (int i = 0; i < outDirFiles.length; i++) {
			if (outDirFiles[i].equals("new_category.xml")) {
				return;
			}
		}
		fail("new_category.xml expected to exist");
	}
}
