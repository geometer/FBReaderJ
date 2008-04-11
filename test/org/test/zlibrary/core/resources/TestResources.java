package org.test.zlibrary.core.resources;

import java.util.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.xml.own.ZLOwnXMLProcessorFactory;
import org.geometerplus.zlibrary.ui.swing.library.ZLSwingLibrary;

import junit.framework.TestCase;

public class TestResources extends TestCase {

	public void setUp() {
		new ZLSwingLibrary();
		new ZLOwnXMLProcessorFactory();
		Locale.setDefault(Locale.ENGLISH);
		ZLResource.setApplicationDirectory("test/data/resources/application/");
		ZLResource.setZLibraryDirectory("test/data/resources/zlibrary/");
	}
	
	public void testMissingResource() {
		ZLResource res = ZLResource.resource("");
		assertFalse(res.hasValue());
	}
	
	public void testMissingResourceValue() {
		ZLResource res = ZLResource.resource("");
		assertEquals(res.getValue(), "????????");
	}
	
	public void testNoValueResource() {
		ZLResource res = ZLResource.resource("menu");
		assertFalse(res.hasValue());
	}
	
	public void testMissingValue() {
		ZLResource res = ZLResource.resource("menu");
		assertEquals(res.getValue(), "????????");
	}
	
	public void testNoValueResource2() {
		ZLResource res = ZLResource.resource("color");
		assertFalse(res.hasValue());
	}
	
	public void testMissingValue2() {
		ZLResource res = ZLResource.resource("color");
		assertEquals(res.getValue(), "????????");
	}
	
	public void testHasValue() {
		ZLResource res = ZLResource.resource("menu").getResource("search");
		assertTrue(res.hasValue());
	}
	
	public void testValue() {
		ZLResource res = ZLResource.resource("menu").getResource("search");
		assertEquals(res.getValue(), "Find");
	}
	
	public void testValue2() {
		ZLResource res = ZLResource.resource("menu").getResource("search").getResource("search");
		assertEquals(res.getValue(), "Find Text...");
	}
}
